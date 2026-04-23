import asyncio
import logging
import os
from contextlib import asynccontextmanager
from pathlib import Path
from typing import Any

import httpx
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from pyrogram import Client, filters

ROOT_ENV_FILE = Path(__file__).resolve().parent.parent / ".env"
load_dotenv(ROOT_ENV_FILE if ROOT_ENV_FILE.exists() else None)

logging.basicConfig(
    level=os.getenv("LOG_LEVEL", "INFO"),
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)
log = logging.getLogger("userbot_server")


def require_env(name: str) -> str:
    value = os.getenv(name, "").strip()
    if not value:
        raise RuntimeError(f"{name} must be configured")
    return value


def require_int_env(name: str) -> int:
    raw_value = require_env(name)
    try:
        return int(raw_value)
    except ValueError as exc:
        raise RuntimeError(f"{name} must be an integer, got: {raw_value}") from exc


api_id = require_int_env("TELEGRAM_API_ID")
api_hash = require_env("TELEGRAM_API_HASH")
source_group_id = require_int_env("SOURCE_GROUP_ID")
spring_detect_url = os.getenv("SPRING_DETECT_URL", "http://localhost:8080/api/detect").strip()
port = int(os.getenv("PORT", "8081"))
session_dir = Path(__file__).resolve().parent / "session"
session_basename = os.getenv("PYROGRAM_SESSION", "freight_userbot").strip() or "freight_userbot"
session_name = str(session_dir / session_basename.replace(".session", ""))

session_dir.mkdir(parents=True, exist_ok=True)

app_client = Client(
    name=session_name,
    api_id=api_id,
    api_hash=api_hash,
)


class ForwardRequest(BaseModel):
    from_chat_id: int
    message_id: int
    to_chat_id: int


@asynccontextmanager
async def lifespan(_: FastAPI):
    log.info(
        "Starting userbot with source_group_id=%s detect_url=%s session=%s",
        source_group_id,
        spring_detect_url,
        session_name,
    )
    try:
        await app_client.start()
        me = await app_client.get_me()
        log.info("Pyrogram userbot connected as id=%s username=%s", me.id, me.username)
    except Exception:
        log.error("Failed to start Pyrogram userbot client", exc_info=True)
        raise

    try:
        yield
    finally:
        log.info("Stopping Pyrogram userbot client")
        try:
            await app_client.stop()
            log.info("Pyrogram userbot stopped")
        except Exception:
            log.error("Failed to stop Pyrogram userbot client", exc_info=True)


app = FastAPI(title="Freight Userbot", lifespan=lifespan)


async def detect_target_groups(text: str) -> list[int]:
    last_error: Exception | None = None
    for attempt in range(1, 4):
        try:
            async with httpx.AsyncClient(timeout=20.0) as http_client:
                response = await http_client.post(spring_detect_url, json={"text": text})
                response.raise_for_status()
                return normalize_group_ids(response.json())
        except Exception as exc:
            last_error = exc
            log.warning("Detect API attempt %s failed", attempt, exc_info=True)
            if attempt < 3:
                await asyncio.sleep(1.5 * attempt)

    raise RuntimeError("Detect API failed after 3 attempts") from last_error


def normalize_group_ids(payload: Any) -> list[int]:
    if not isinstance(payload, list):
        raise ValueError("Detect API response must be a list of group ids")

    normalized_group_ids: list[int] = []
    for group_id in payload:
        if isinstance(group_id, int):
            normalized_group_ids.append(group_id)
            continue
        if isinstance(group_id, str) and group_id.strip():
            normalized_group_ids.append(int(group_id))
            continue
        raise ValueError(f"Invalid group id in detect response: {group_id}")

    # Preserve order and remove duplicates.
    return list(dict.fromkeys(normalized_group_ids))


async def forward_to_group(from_chat_id: int, message_id: int, to_chat_id: int) -> None:
    await app_client.forward_messages(
        chat_id=to_chat_id,
        from_chat_id=from_chat_id,
        message_ids=message_id,
    )
    log.info(
        "Message forwarded successfully: from_chat_id=%s message_id=%s to_chat_id=%s",
        from_chat_id,
        message_id,
        to_chat_id,
    )


@app.post("/forward")
async def forward_message(request: ForwardRequest) -> dict[str, str]:
    log.info(
        "Manual forward request received: from_chat_id=%s message_id=%s to_chat_id=%s",
        request.from_chat_id,
        request.message_id,
        request.to_chat_id,
    )
    try:
        await forward_to_group(request.from_chat_id, request.message_id, request.to_chat_id)
    except Exception as exc:
        log.error("Manual forward failed", exc_info=True)
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return {"status": "ok"}


@app.get("/health")
async def health() -> dict[str, Any]:
    connected = bool(getattr(app_client, "is_connected", False))
    return {
        "status": "ok",
        "connected": connected,
        "source_group_id": source_group_id,
        "detect_url": spring_detect_url,
        "session_name": session_basename,
    }


@app_client.on_message()
async def log_any_message(_: Client, message) -> None:
    text = (message.text or message.caption or "").strip()
    preview = text[:80] if text else "<no text>"
    log.info(
        "Observed message: chat_id=%s message_id=%s outgoing=%s from_user_id=%s text=%s",
        getattr(message.chat, "id", None),
        message.id,
        message.outgoing,
        getattr(getattr(message, "from_user", None), "id", None),
        preview,
    )


@app_client.on_message(filters.chat(source_group_id))
async def handle_source_message(client: Client, message) -> None:
    del client
    text = message.text or message.caption
    if not text or not text.strip():
        log.info("Skipping message without text or caption: message_id=%s", message.id)
        return

    log.info(
        "New source message received: chat_id=%s message_id=%s",
        message.chat.id,
        message.id,
    )

    try:
        target_group_ids = await detect_target_groups(text)
        log.info("Detected %s target groups for message_id=%s", len(target_group_ids), message.id)
    except Exception:
        log.error("Failed to detect target groups for message_id=%s", message.id, exc_info=True)
        return

    if not target_group_ids:
        log.info("No target groups detected for message_id=%s", message.id)
        return

    for target_group_id in target_group_ids:
        try:
            await forward_to_group(message.chat.id, message.id, target_group_id)
            await asyncio.sleep(0.7)
        except Exception:
            log.error(
                "Failed to forward detected message_id=%s to target_group_id=%s",
                message.id,
                target_group_id,
                exc_info=True,
            )


if __name__ == "__main__":
    log.info("Starting FastAPI server on port %s", port)
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=port)

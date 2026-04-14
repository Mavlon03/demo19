import logging
import os
from contextlib import asynccontextmanager

from dotenv import load_dotenv
from fastapi import FastAPI
from pydantic import BaseModel
from pyrogram import Client

load_dotenv()

logging.basicConfig(
    level=os.getenv("LOG_LEVEL", "INFO"),
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)
log = logging.getLogger("userbot_server")

api_id = int(os.getenv("TELEGRAM_API_ID", "0"))
api_hash = os.getenv("TELEGRAM_API_HASH", "")
session_dir = os.path.join(os.path.dirname(__file__), "session")
session_name = os.path.join(session_dir, "freight_userbot")

os.makedirs(session_dir, exist_ok=True)

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
    log.info("Starting Pyrogram userbot client")
    try:
        await app_client.start()
        me = await app_client.get_me()
        log.info("Pyrogram userbot connected as id=%s", me.id)
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


app = FastAPI(lifespan=lifespan)


@app.post("/forward")
async def forward_message(request: ForwardRequest) -> dict:
    log.info(
        "Forward request received: from_chat_id=%s, message_id=%s, to_chat_id=%s",
        request.from_chat_id,
        request.message_id,
        request.to_chat_id,
    )
    try:
        await app_client.forward_messages(
            chat_id=request.to_chat_id,
            from_chat_id=request.from_chat_id,
            message_ids=request.message_id,
        )
        log.info(
            "Message forwarded successfully: message_id=%s to_chat_id=%s",
            request.message_id,
            request.to_chat_id,
        )
        return {"status": "ok"}
    except Exception as exc:
        log.error(
            "Failed to forward message_id=%s to_chat_id=%s",
            request.message_id,
            request.to_chat_id,
            exc_info=True,
        )
        return {"status": "error", "detail": str(exc)}


@app.get("/health")
async def health() -> dict:
    connected = bool(getattr(app_client, "is_connected", False))
    log.info("Health check requested: connected=%s", connected)
    return {"status": "ok", "connected": connected}


if __name__ == "__main__":
    import uvicorn

    port = int(os.getenv("PORT", "8081"))
    log.info("Starting FastAPI server on port %s", port)
    uvicorn.run(app, host="0.0.0.0", port=port)

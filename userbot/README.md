# FreightBot Userbot Service

This Python microservice listens to a source Telegram group with your user account and forwards matching messages to target groups via Spring Boot.

## Setup

1. Copy `.env.example` from the project root to `.env`.
2. Fill `TELEGRAM_API_ID`, `TELEGRAM_API_HASH`, `SOURCE_GROUP_ID`, and `SPRING_DETECT_URL`.
3. Install dependencies:

```bash
cd userbot
python -m pip install -r requirements.txt
```

## Run

```bash
cd userbot
uvicorn userbot_server:app --reload --host 0.0.0.0 --port 8000
```

## Notes

- The first run will request your phone number and login code.
- The session file will be stored as `freight_userbot.session`.
- The Python service forwards messages only after Spring Boot returns target group IDs.
- `BOT_ENABLE_LEGACY_BOT=false` keeps the Java app logic-only and avoids running the legacy Telegram bot.

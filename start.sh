#!/bin/bash
set -e

echo "=== Python userbot ishga tushmoqda ==="
cd userbot
pip install -r requirements.txt -q
python userbot_server.py &
PYTHON_PID=$!
cd ..

echo "=== Python server tayyor bo'lguncha kutilmoqda ==="
for i in $(seq 1 15); do
  sleep 2
  if curl -sf http://localhost:8081/health > /dev/null 2>&1; then
    echo "Python server tayyor! (${i}x2s kutildi)"
    break
  fi
  echo "Kutilmoqda... ($i/15)"
done

echo "=== Spring Boot ishga tushmoqda ==="
./mvnw spring-boot:run

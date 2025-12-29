#!/usr/bin/env bash
set -euo pipefail

trap 'echo -e "❌ Local CI failed"' ERR

echo "=== kafka-avro-genai-streaming-poc ==="
./gradlew --no-daemon :kafka-avro-genai-streaming-poc:clean :kafka-avro-genai-streaming-poc:build

echo "=== agentic-notifier-service ==="
./gradlew --no-daemon :agentic-notifier-service:clean :agentic-notifier-service:build

echo "✅ Local CI passed"

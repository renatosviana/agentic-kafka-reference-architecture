#!/usr/bin/env bash
set -euo pipefail

trap 'echo -e "❌ Local CI failed"' ERR

echo "=== kafka-avro-genai-streaming-poc ==="
cd kafka-avro-genai-streaming-poc
./gradlew --no-daemon clean build

echo "=== agentic-notifier-service ==="
cd ../agentic-notifier-service
./gradlew --no-daemon clean build

echo "✅ Local CI passed"

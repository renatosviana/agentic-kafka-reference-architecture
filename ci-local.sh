#!/usr/bin/env bash
set -e

echo "=== kafka-avro-genai-streaming-poc ==="
cd kafka-avro-genai-streaming-poc
./gradlew --no-daemon clean build

echo "=== agentic-notifier-service ==="
cd ../agentic-notifier-service/AgenticNotifierService
./gradlew --no-daemon clean build

echo "✅ Local CI passed"

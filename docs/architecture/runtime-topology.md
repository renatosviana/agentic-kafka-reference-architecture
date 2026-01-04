# Local development topology
Microservices communicate over long-lived TCP connections. Kafka bridges host and container networks using dual listeners. Control Center is a side-car observer and not part of the data path. TLS is intentionally disabled for local development and **must be enabled in production**.

```
┌────────────────────────────── HOST (Laptop) ──────────────────────────────┐
│                                                                           │
│  Postman / IntelliJ            Browser                                    │
│  ──────────││──────            ─────────                                  │
│  HTTP → localhost:18082        Control Center UI                          │
│                                http://localhost:59021                     │
│                                                                           │
│  Kafka client                                                             │
│  ────────────                                                             │
│  localhost:29092                                                          │
│        │                                                                  │
│        │ Docker port publish                                              │
│        ▼                                                                  │
│      29092                                                                │
└───────────────────────────────────────────────────────────────────────────┘
│
▼
──────────────────────────── DOCKER NETWORK ────────────────────────────────
│
▼
┌────────────────────────────── KAFKA BROKER ───────────────────────────────┐
│                                                                           │
│  Listeners:                                                               │
│   - 9092  (containers)                                                    │
│   - 29092 (host)                                                          │
│                                                                           │
│  Advertised:                                                              │
│   - kafka:9092      → containers                                          │
│   - localhost:29092 → host                                                │
│                                                                           │
└───────────────┬───────────────────────────────┬──────────────────────────┘
                │                               │    
                ▼                               ▼
┌──────────────────────────┐        ┌──────────────────────────┐
│          GENAI           │        │        CONTROL CENTER    │
│  HTTP :8082              │        │  Kafka Streams + Admin   │
│  Kafka Producer          │        │  Observability only      │
│  TCP → kafka:9092        │        │  TCP → kafka:9092        │
│                          │        │                          │
│  HTTP → embeddings:8000  │        │                          │
└───────────────┬──────────┘        └──────────────────────────┘
                │
                ▼
┌──────────────────────────┐
│         AGENTIC          │
│  Kafka Consumer/Producer │
│  TCP → kafka:9092        │
│                          │
│  SMTP → mailhog:1025     │
└───────────────┬──────────┘
                ▼
┌──────────────────────────┐
│         MAILHOG          │
│  SMTP :1025              │
│  UI   :8025              │
└──────────────────────────┘
```
Supporting services:
- PostgreSQL (pgvector) :5432
- Schema Registry       :8081 (host 18081)
- Zookeeper             :2181 (host 12181)

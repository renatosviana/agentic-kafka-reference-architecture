[![CI](https://github.com/renatosviana/agentic-kafka-reference-architecture/actions/workflows/ci.yml/badge.svg)](https://github.com/renatosviana/agentic-kafka-reference-architecture/actions/workflows/ci.yml)
👋 If this architecture helped you, please ⭐ the repo or open a Discussion with questions or feedback.

# Governed, Event-Driven AI for Deterministic Enterprise Systems

## What this is
A reference architecture that explores **where AI should live inside event-driven enterprise systems**.

Instead of treating GenAI as an autonomous application layer, this project demonstrates how intelligence can operate **within deterministic, replayable, and schema-governed workflows**, using Kafka as the system of record.

A production-style reference architecture that demonstrates how to combine:
- Event-driven systems as the source of truth (Kafka)
- GenAI as a constrained reasoning layer
- Policy-driven agentic workflows with explicit audit trails
- Java + Spring Boot as a representative enterprise runtime

## Why this exists
Most GenAI examples optimize for demos, not operations.

They ignore:
- Determinism and replayability
- Schema evolution and data contracts
- Throughput and idempotency
- Auditability and governance boundaries
- Streaming consistency (ordering, idempotency, and replay-safe processing)

These gaps make many AI systems unusable in regulated or high-scale environments.

## Who this is for

- **Platform / Streaming engineers** building Kafka-based pipelines with strong delivery guarantees
- **Architects** designing AI inside regulated or high-scale systems (auditability, replay, governance)
- **Backend engineers** wanting production patterns (Spring Boot, Kafka Streams, Schema Registry)
- **Teams exploring semantic memory** (pgvector) without jumping straight to a dedicated vector DB

## Start here
- 🧭 High-Level Architecture & System Overview: [docs/architecture/overview.md](docs/architecture/overview.md)
- 🧠 Agentic memory (pgvector): [docs/concepts/memory-pgvector.md](docs/concepts/memory-pgvector.md)
- 🔁 Avro vs JSON: [docs/concepts/avro-vs-json.md](docs/concepts/avro-vs-json.md)
- 📦 Schema evolution: [docs/concepts/schema-evolution.md](docs/concepts/schema-evolution.md)
- ☸️ Production notes: [docs/deployment/production.md](docs/deployment/production.md)
- 📮 Postman collections: [docs/postman](docs/postman)
- 🌐 Full local runtime and transport topology: [docs/architecture/runtime-topology.md](docs/architecture/runtime-topology.md).

## What problem this solves
This architecture explores how to **govern AI behavior using event logs, schemas, and explicit decision policies**.

It shows how:
- Kafka remains the source of truth
- GenAI enriches events without mutating facts
- Agentic decision-making is constrained, auditable, and replayable
- Actions are emitted as events, not side effects
- Vector memory is a derived index that can be rebuilt from Kafka events.

The result is an AI-enabled system that can be reasoned about, replayed, and evolved safely over time.

## Quick Start (Local Docker Compose)

**Prerequisites**

- Docker Desktop (Docker Compose v2)
- Git
- An OpenAI API key (stored locally in .env, not committed) -- see [OpenAI / GenAI Setup](https://github.com/renatosviana/agentic-kafka-reference-architecture/tree/main/kafka-avro-genai-streaming-poc#openai--genai-setup)

### 1. Clone the repository

```bash
git clone https://github.com/renatosviana/agentic-kafka-reference-architecture.git
cd agentic-kafka-reference-architecture
```
### 2. Create local environment file

```bash
OPENAI_API_KEY=sk-xxxx
```

- ⚠️ .env is gitignored and must never be committed.
- ⚠️ Demo-only security posture: docker-compose and `application.yml` use open, non-production defaults (plain HTTP, dev credentials, single-node Kafka/Postgres); harden these before any real deployment.
### 3. Start the full stack

```bash
docker compose up -d
```
This starts:
- Zookeeper
- Kafka (dual listeners)
- Schema Registry
- Control Center
- PostgreSQL
- GenAI service
- Agentic Notifier service
- MailHog

### 4. Verify running containers

```bash
docker compose ps
```
All services should be Up.

### 5. Service Endpoints & Ports

### GenAI API
- **URL:** ``` http://localhost:18082 ```
- **Purpose:** Produces account events and enriched messages

Example:
```bash
POST http://localhost:18082/accounts/ACC123/debit?amount=45
```
### 6. Agentic Notifier Service

- **URL:** ``` http://localhost:18082 ```
- **Purpose:** Consumes enriched events, makes decisions, triggers actions

| Kafka                        | Boostrap Server  |
|------------------------------|------------------|
| Containers                   | Kafka:9092       | 
| Host (local tools)           | localhost:2902   |

### 7. Confluent Control Center

- **URL:** [Confluent Control Center](http://localhost:59021)
- **Purpose:** View topics, messages, consumer groups

#### Trigger events (Postman / curl)
Example (credit):
```bash
curl -X POST "http://localhost:18082/accounts/ACC123/credit?amount=48"
```
Run it in Postman (use [Postman collection](https://github.com/renatosviana/agentic-kafka-reference-architecture/tree/main/docs/postman)):
- In Postman Import -> folders -> Upload
  <img width="1196" height="832" alt="image" src="https://github.com/user-attachments/assets/c225d909-4126-4bcd-8848-10f69d898f55" />

<img width="1377" height="811" alt="image" src="https://github.com/user-attachments/assets/9f3649ae-104a-4869-8798-0c863789132d" />


(Adjust host/port if your app uses a different server port.)

#### Observe in Confluent Control Center
Confirm messages appear in:

- account-events
- account.enriched.v1
- agent.decision.v1
- agent.action_result.v1

**Example:**
- Observe in Control Center:
  <img width="1237" height="577" alt="image" src="https://github.com/user-attachments/assets/eb56fc35-ec16-414f-8888-4faa93205d41" />
  <img width="1181" height="639" alt="image" src="https://github.com/user-attachments/assets/e6a2ab34-6a36-4b82-8473-2ac40ac56149" />

### 8. MailHog (Email Sink)

- **Web UI:** [MailHog UI](http://localhost:8025)
- **SMTP:** mailhog:1025 (inside Docker)

Used for local email notifications from the Agentic service.

### 9. Verify the Full Flow

1. Trigger a debit event via GenAI
2. Verify message in account.enriched.v1
3. Agentic service consumes the event
4. Decision published to agent.decision.v1
5. Action result published to agent.action_result.v1
6. Email appears in MailHog UI

### 10. Stop & Clean Up
```bash
docker compose down -v
```

### 11. Run Local CI (MANDATORY before pushing)

From the repo root:
```bash
./ci-local.sh
```
**Required:** Run ./ci-local.sh before every push/PR to ensure both Gradle builds pass locally (matches GitHub Actions).


## Kafka Topics

Input / domain:
- `account-events` — raw credit/debit events

State:
- `account-balance-store-changelog` — Kafka Streams changelog
- `account-balance-store` — KTable state

GenAI enrichment:
- `account.enriched.v1` — enriched signal {eventId, accountId, riskScore, summary, timestamp}

Agentic audit:
- `agent.decision.v1` — agent decisions (what/why)
- `agent.action_result.v1` — action results (success/failure)

## Key Concepts
- Kafka Streams (KTable for state)
- LLM-based classification & reasoning
- Agent orchestration (decision → action)
- Avro schemas & evolution
- Failure handling & retries

## Tech Stack
- Java/Spring Boot · Kafka/Kafka Streams · Avro/Schema Registry · PostgreSQL/pgvector · Python embeddings · Docker Compose · MailHog · GitHub Actions

## Local Development

This project uses Docker Compose for local development and experimentation.

## Status
Active development – January 2026

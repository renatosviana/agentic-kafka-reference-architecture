[![CI](https://github.com/renatosviana/agentic-kafka-reference-architecture/actions/workflows/ci.yml/badge.svg)](https://github.com/renatosviana/agentic-kafka-reference-architecture/actions/workflows/ci.yml)

# Agentic Kafka Reference Architecture

## What this is
A production-style reference architecture combining:
- Event-driven microservices (Kafka / Redpanda)
- GenAI (LLM-based reasoning)
- Agentic workflows
- Java + Spring Boot

## Why this exists
Most GenAI examples ignore:
- Throughput
- Idempotency
- Schema evolution
- Streaming consistency

This project shows how to do GenAI *at scale*.

## What problem this solves
This reference architecture shows how to combine Kafka event streaming with GenAI enrichment and an agentic decision layer, producing auditable decisions and observable actions (email notifications) using Spring Boot, Kafka Streams, and local-first tooling.

## Architecture
### Architecture Diagram

```mermaid
flowchart LR

  subgraph User
    A1[POST /accounts/:id/credit]
    A2[POST /accounts/:id/debit]
    A3[UI Load Summaries]
  end

  subgraph SpringBoot["Spring Boot (GenAI + APIs)"]
    C1[AccountController]
    C2[AccountEventProducer]
    C3[AccountProcessingService + GenAIClient]
    C4[AccountSummaryController]
    C5[EnrichedEventPublisher]
  end

  subgraph Kafka
    K1[(account-events)]
    K2[(account-balance-store-changelog)]
    K3[KTable: account-balance-store]
    K4[(account.enriched.v1)]
    K5[(agent.decision.v1)]
    K6[(agent.action_result.v1)]
  end

  subgraph StreamApp
    S1[Kafka Streams Processor + Balance Aggregation]
  end

  subgraph Postgres
    DB[(account_summaries)]
  end

  subgraph Agentic["Agentic Notifier Service"]
    N1[EnrichedEventListener]
    N2[DecisionEngine]
    N3[ActionExecutor]
    N4[EmailNotifier]
  end

  subgraph MailHog["Local Email Sink (MailHog)"]
    M1[SMTP :1025]
    M2[Web UI :8025]
  end

  A1 --> C1 --> C2 --> K1
  A2 --> C1 --> C2 --> K1

  K1 --> S1 --> K3 --> C3
  C3 --> DB
  C3 --> C5 --> K4

  A3 --> C4 --> DB --> A3

  K4 --> N1 --> N2 --> K5
  K5 --> N3 --> K6
  N4 --> M1 --> M2

```
## GenAI vs Agentic AI (why two stages)

- GenAI (enrichment) turns raw account activity into a structured signal: riskScore, summary, timestamp and publishes to account.enriched.v1.

- Agentic AI (decision + action) consumes enriched events, makes deterministic decisions (auditable), and executes actions (ex: email) while emitting an audit trail to Kafka (agent.decision.v1, agent.action_result.v1).

## Why this architecture uses both Avro and JSON

This project intentionally uses Avro and JSON in different stages, based on what each stage optimizes for.

### Avro: high-throughput, schema-governed event streams

Avro is used for core domain and stateful streaming topics, where correctness and evolution matter most:

- account-events

- account-balance-store-changelog

- internal Kafka Streams state

### Why Avro here:

- Strong schemas with compatibility guarantees

- Compact binary encoding (high throughput)

- Safe schema evolution over time

- First-class support with Kafka Streams and Schema Registry

These topics represent facts and state, not interpretations.

### JSON: semantic, AI-friendly decision and action messages

JSON is used for GenAI outputs and agentic workflows, such as:

- account.enriched.v1

- agent.decision.v1

- agent.action_result.v1

### Why JSON here:

LLMs reason better with explicit, readable structures

- Easier prompt composition and inspection

- Flexible payloads for rationale, explanations, and context

- Human-readable for debugging, audits, and ops teams

These messages represent interpretations, decisions, and actions, not raw facts.

### Design principle: facts vs reasoning

This split reflects a deliberate architectural principle:

#### Data formats by architectural layer

| Layer                         | Format | Reason                                      |
|------------------------------|--------|---------------------------------------------|
| Domain events & state        | Avro   | Correctness, performance, schema evolution  |
| GenAI enrichment             | JSON   | Semantic clarity, explainability             |
| Agentic decisions & actions  | JSON   | Auditability, observability                  |

This avoids forcing AI-centric concerns into low-level event streams, while keeping GenAI and agentic logic flexible and inspectable.

#### What is schema evolution (in simple terms)

Schema evolution means you can change the structure of your data over time without breaking running systems.

In real systems, data formats never stay the same:

- new fields are added

- old fields become optional

- some fields are deprecated

Schema evolution defines rules that allow producers and consumers to keep working even when data changes.

#### Example 

Imagine you start with this event:
```json
{
  "accountId": "ACC123",
  "amount": 50
}
```

Later, the business needs a new field:
```json
{
  "accountId": "ACC123",
  "amount": 50,
  "currency": "CAD"
}

```
**Corresponding Avro field added to the existing record (breaking change):**
```json
{
  "name": "currency",
  "type": "string"
}
 ```
#### Runtime failure caused by breaking schema evolution

When sending a request via Postman, the application fails while converting the request payload into an Avro record because the new field `currency` was added without a default value.
<img width="1426" height="523" alt="image" src="https://github.com/user-attachments/assets/e9c86792-5705-493f-8f5f-99de0a35f0cb" />

**This simulates a real production deployment where a new service version is deployed while existing producers, consumers, or request payloads do not yet include the new field.**

The failure occurs during Avro record construction (before the message is sent to Kafka):

#### Request-to-Kafka Avro Serialization Flow
```
Postman JSON
   ↓
Spring Controller
   ↓
Avro Builder (generated class)
   ↓
Kafka Avro Serializer
   ↓
Schema Registry
```
When adding a new field (e.g., `currency`) **without a default value**, producing an event using the generated Avro `Builder` fails because Avro cannot supply a default for the missing field.

```
Path in schema: --> currency
        at org.apache.avro.generic.GenericData.getDefaultValue(GenericData.java:1286)
        at org.apache.avro.data.RecordBuilderBase.defaultValue(RecordBuilderBase.java:138)
        at com.viana.avro.AccountEvent$Builder.build(AccountEvent.java:607)
        at com.viana.poc.controller.AccountController.credit(AccountController.java:36)
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
```
**Key failure point:**  
`AccountEvent$Builder.build(AccountEvent.java:607)`

#### Fix: make the new field backward compatible

To safely evolve the schema, the new field was made nullable and a default value was provided:

**Corresponding Avro field (compatible change):**
```json
{
  "name": "currency",
  "type": ["null", "string"],
  "default": null
}
```

#### Why this works:

- Older messages do not contain the currency field.

- Avro uses the default value (null) when reading older data.

- The Avro Builder no longer throws an exception when the field is not set.

- The schema remains backward compatible and is accepted by Schema Registry.

#### Result:

- POST requests sent via Postman succeed.
- Events are serialized and published to Kafka.
<img width="1032" height="560" alt="image" src="https://github.com/user-attachments/assets/3999d6db-77cc-4957-8f78-bbfc9cc03064" />

**Without schema evolution:**

- older consumers may crash

- newer producers may break older apps

- teams are forced to upgrade everything at once

**With schema evolution:**

- old consumers safely ignore currency

- new consumers can use it

- systems evolve independently

#### Why Avro + Schema Registry matters here

When using Avro with a Schema Registry:

- every message follows a registered schema

- compatibility rules are enforced automatically

- breaking changes are rejected before they reach production

This guarantees:

- Correctness: consumers always know what fields exist and their types

- Performance: compact binary format, efficient at scale

- Safe evolution: fields can be added/removed in controlled ways

#### Why this is critical for domain events

Domain events (like account credits, debits, balances):

- represent facts

- are consumed by many systems

- must not change unpredictably

Schema evolution allows these facts to evolve without outages.

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

## Quickstart: Verify the full flow

### 1) Start infrastructure (Docker Compose)

From the GenAI path:

```bash
cd kafka-avro-genai-streaming-poc
docker compose up -d
docker compose ps
```
(Optional) follow infrastructure logs:
```bash
docker compose logs -f
```

Useful UIs:

- [Confluent Control Center](http://localhost:9021)

- [MailHog UI](http://localhost:8025)

### 2) Run Local CI (MANDATORY before pushing)

From the repo root:
```bash
./ci-local.sh
```
**Required:** Run ./ci-local.sh before every push/PR to ensure both Gradle builds pass locally (matches GitHub Actions).

### 3) Start the services (two terminals)
**Terminal 1 — GenAI + Streaming app**
```bash
cd kafka-avro-genai-streaming-poc
./gradlew --no-daemon clean bootRun
```
**Terminal 2 — Agentic Notifier service**
```bash
cd agentic-notifier-service
./gradlew --no-daemon clean bootRun
```
### 4) Trigger events (Postman / curl)
Example (credit):
```bash
curl -X POST "http://localhost:8080/accounts/ACC123/credit?amount=48"
```
- Run it in Postman (use [Postman collection](https://github.com/renatosviana/agentic-kafka-reference-architecture/tree/main/kafka-avro-genai-streaming-poc/doc)):
<img width="1407" height="492" alt="image" src="https://github.com/user-attachments/assets/1d1e5d9b-96c8-41a8-861e-0cffd71844c8" />

(Adjust host/port if your app uses a different server port.)

### 5) Observe in Confluent Control Center
Confirm messages appear in:

- account-events
- account.enriched.v1
- agent.decision.v1
- agent.action_result.v1

**Example:**
- Observe in Control Center:
<img width="1657" height="771" alt="image" src="https://github.com/user-attachments/assets/9a6e7806-6b7e-47ee-818a-6a7032d1abf1">
<img width="1657" height="771" alt="image" src="https://github.com/user-attachments/assets/65c5e983-ea8e-4b42-84c1-6b9baf1a6211">

### 6) Verify email

Open MailHog UI:
- [MailHog UI](http://localhost:8025)

## Key Concepts
- Kafka Streams (KTable for state)
- LLM-based classification & reasoning
- Agent orchestration (decision → action)
- Avro schemas & evolution
- Failure handling & retries

## Tech Stack
Java, Spring Boot, Kafka, Avro

## Status
Active development – December 2025

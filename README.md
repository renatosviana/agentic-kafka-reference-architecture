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
- 
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

1) Start infrastructure (Kafka + Control Center + Postgres + MailHog).
2) Start services:
   - `kafka-avro-genai-streaming-poc`
   - `agentic-notifier-service`

3) Trigger events (Postman):
   - `POST /accounts/ACC123/credit?amount=48`

4) Observe in Confluent Control Center:
   - `account-events` receives events
   - `account.enriched.v1` receives enriched events
   - `agent.decision.v1` receives decisions
   - `agent.action_result.v1` receives action results

5) Verify email:
   - Open MailHog UI: `http://localhost:8025`


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

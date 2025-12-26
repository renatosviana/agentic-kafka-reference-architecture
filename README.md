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

## Architecture
### Architecture Diagram

```mermaid
flowchart LR

  subgraph User
    A1[POST /accounts/:id/credit]
    A2[POST /accounts/:id/debit]
    A3[UI Load Summaries]
  end

  subgraph SpringBoot
    C1[AccountController]
    C2[AccountEventProducer]
    C3[AccountProcessingService + GenAIClient]
    C4[AccountSummaryController]
  end

  subgraph Kafka
    K1[(account-events)]
    K2[(account-balance-store-changelog)]
    K3[KTable: account-balance-store]
  end

  subgraph StreamApp
    S1[Kafka Streams Processor + Balance Aggregation]
  end

  subgraph Postgres
    DB[(account_summaries)]
  end

  A1 --> C1 --> C2 --> K1
  A2 --> C1 --> C2 --> K1

  K1 --> S1 --> K3 --> C3
  C3 --> DB

  A3 --> C4 --> DB --> A3
```


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

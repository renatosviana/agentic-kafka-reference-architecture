# High-Level Architecture & System Overview

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

  K1 --> AccountEventConsumer --> C3
  C3 --> DB
  C3 --> C5 --> K4

  A3 --> C4 --> DB 

  K4 --> N1 --> N2
  N2 --> K5
  N3 --> K6
  N3 --> N4 --> M1 --> M2

```
### Implementation note

I intentionally keep a simple consumer-driven balance calc for clarity in the reference implementation. The Kafka Streams KTable is present to show the scalable pattern, and I will align GenAI enrichment to the KTable/changelog in a future iteration.

## How it works (end-to-end flow)

1. **User triggers commands**  
   `POST /accounts/:id/credit` or `POST /accounts/:id/debit` hits `AccountController`.

2. **API produces events (write path)**  
   `AccountEventProducer` publishes account events to Kafka: `account-events`.

3. **Streaming aggregation (state from events)**  
   Kafka Streams aggregates account events into a `KTable (account-balance-store)`, with all state changes durably persisted to the changelog topic `(account-balance-store-changelog)` to enable fault-tolerant recovery and scaling.

5. **GenAI enrichment + materialized read model**  
   `AccountProcessingService + GenAIClient` consumes the aggregated balance view, writes summaries to Postgres
   (`account_summaries`), and publishes enriched events to `account.enriched.v1`.

6. **Fast reads (read path)**  
   The UI loads summaries via `AccountSummaryController` from Postgres (materialized view pattern).

7. **Agentic decision loop**  
   `Agentic Notifier Service` consumes `account.enriched.v1`, makes decisions (`agent.decision.v1`), executes actions,
   and emits results (`agent.action_result.v1`).

8. **Notifications (local dev)**  
   `EmailNotifier` sends emails to MailHog (SMTP :1025, UI :8025) for verification.

## Agentic Memory Flow
```mermaid
flowchart LR
  subgraph Agentic["Agentic Notifier Service"]
    D1[Decision Engine]
    D2[Embedding Client]
    D3["Memory Store<br/>(pgvector)"]
  end

  subgraph Embeddings["Embedding Service"]
    E1[Sentence Transformer]
  end

  D1 --> D2 --> E1
  E1 --> D3
  D3 --> D1
```
## Agentic Memory Flow (embedding-based memory loop)

The Decision Engine can store and retrieve “memory” to improve future decisions:

1. Decision Engine → Embedding Client → Embedding Service (Sentence Transformer)
2. Embeddings stored in Postgres **pgvector**
3. Memory retrieved back into Decision Engine during decision making

**Trade-off**
- Dedicated vector DB might be needed at large scale, but Postgres+pgvector is “good enough” for a reference architecture

## Key ideas
- **Event-driven core:** account changes are immutable events; state is derived via stream processing.
- **CQRS-style split:** writes go to Kafka; reads come from a Postgres materialized view for speed.
- **Replayability:** Kafka topics + changelogged state enable deterministic reprocessing.
- **Agentic automation:** enriched events drive autonomous decisions + actions, with auditable topics.

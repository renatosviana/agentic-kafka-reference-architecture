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
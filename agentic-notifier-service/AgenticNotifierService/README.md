# Agentic Kafka Reference Architecture

Production-oriented reference architecture for combining Kafka, GenAI enrichment, and agentic decision-making with deterministic actions.

## 1. Purpose

This repository demonstrates how to design an event-driven, agentic architecture where:

- Kafka streams carry high-volume domain events
- GenAI is used for reasoning, summarization, and classification
- An agent layer makes controlled decisions
- Actions (email notifications, case creation, escalation) are executed deterministically and auditable

The goal is to show how GenAI and Agentic AI can be safely integrated into enterprise systems, not just how to call an LLM.

## 2. Architectural Principles

- Separation of concerns
  - GenAI enrichment ≠ agentic execution
- Event-driven contracts
  - Kafka topics define system boundaries
- Guard-railed agentic behavior
  - LLMs propose actions, systems execute them
- Auditability & safety
  - Every decision and action is traceable
- Scalability
  - Designed for high-throughput Kafka workloads

## 3. High-Level Architecture
```
┌──────────────┐
│ Domain Event │
└──────┬───────┘
│ Kafka
▼
┌────────────────────────────┐
│ GenAI Enrichment Service   │
│ (Summarize / Classify)     │
└──────┬─────────────────────┘
│ account.enriched.v1
▼
┌────────────────────────────┐
│ Agentic Orchestrator       │
│ - Decision Planning        │
│ - Guardrails               │
│ - Tool Execution           │
└──────┬─────────────────────┘
│
├── agent.decision.v1
└── agent.action_result.v1
``` 

## 4. Agentic Notifier Service

### Purpose:
Make agentic decisions and execute deterministic actions.

Responsibilities:

- Consume enriched events
- Produce an AgentDecision (JSON / Avro)
- Execute approved actions
- Emit audit events

Supported Actions:

- NO_ACTION
- NOTIFY_EMAIL
- CREATE_CASE
- FLAG_ACCOUNT
- REQUEST_MORE_CONTEXT
- ESCALATE_HUMAN_REVIEW

Output Topics:

agent.decision.v1
agent.action_result.v1

# 5. Agentic Design Model

This architecture follows a safe agentic pattern:

1. Observe – Consume enriched Kafka events
2. Reason – Use LLMs or rules to propose actions
3. Decide – Validate against guardrails
4. Act – Execute deterministic tools
5. Audit – Publish results to Kafka

# 6. Guardrails & Safety

- Tool whitelisting
- Schema validation
- Idempotent execution
- Human-in-the-loop escalation
- Full Kafka audit trail

# 7. License

MIT
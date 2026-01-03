## GenAI vs Agentic AI (why two stages)

- GenAI (enrichment) turns raw account activity into a structured signal: riskScore, summary, timestamp and publishes to account.enriched.v1.

- Agentic AI (decision + action) consumes enriched events, makes deterministic decisions (auditable), and executes actions (ex: email) while emitting an audit trail to Kafka (agent.decision.v1, agent.action_result.v1).
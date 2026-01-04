## Agentic Memory & Vector Recall (pgvector)

Beyond event processing and decision-making, this architecture demonstrates **long-term semantic memory** for agentic systems using vector embeddings.

### What problem this solves

Traditional event-driven systems are **stateless across decisions**.
Agentic systems, however, benefit from remembering past context, such as:

- previous risk signals
- past decisions
- historical summaries
- similar situations encountered before

This project introduces a **vector-based memory layer** that allows the agent to:

- store enriched events as embeddings
- recall semantically similar past events at decision time
- reason with historical context, not just the current event

### How agentic memory works

1. **Enriched events** are embedded using a local embedding service.
2. Embeddings are stored in PostgreSQL using **pgvector**.
3. At decision time, the agent:
    - embeds the current query/context
    - performs a similarity search using cosine distance
    - retrieves the most relevant past memories
4. Retrieved memories influence the agent’s decision logic.

### Why PostgreSQL + pgvector

This design intentionally avoids external vector databases to show that:

- vector search can live **inside existing relational infrastructure**
- transactional data and embeddings can coexist
- operational complexity stays low for many real-world use cases

This mirrors how many teams **incrementally adopt AI** without introducing new infrastructure prematurely.

Run it in Postman (use [Postman collection](https://github.com/renatosviana/agentic-kafka-reference-architecture/tree/main/docs/postman)):
Store a memory:
<img width="846" height="522" alt="image" src="https://github.com/user-attachments/assets/c3c594f2-3d0f-42f9-a0de-f2fb9974f13d" />

Recall similar memories:
<img width="857" height="780" alt="image" src="https://github.com/user-attachments/assets/42aa71a9-f9fe-40ed-b6c5-817f53d7ae07" />

### Design principles demonstrated

- Deterministic SQL-based vector recall
- Explicit type handling for JDBC + PostgreSQL
- Clear separation between:
    - facts (Kafka events)
    - interpretations (GenAI output)
    - memory (semantic embeddings)
- Fully observable and debuggable behavior

\c agentic;

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS account_memory (
  id BIGSERIAL PRIMARY KEY,
  account_id TEXT NOT NULL,
  event_id TEXT,
  content TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  embedding vector(384) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_account_memory_account_id ON account_memory(account_id);

CREATE INDEX IF NOT EXISTS idx_account_memory_embedding
  ON account_memory USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

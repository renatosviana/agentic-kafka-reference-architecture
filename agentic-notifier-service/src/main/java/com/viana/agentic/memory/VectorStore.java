package com.viana.agentic.memory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;

@Repository
public class VectorStore {

    private final JdbcTemplate jdbc;

    public VectorStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertMemory(String accountId, String eventId, String content, List<Double> embedding) {
        String vectorLiteral = toPgVectorLiteral(embedding); // "[0.1,0.2,...]"
        jdbc.update("""
            INSERT INTO account_memory(account_id, event_id, content, embedding)
            VALUES (?, ?, ?, ?::vector)
        """, accountId, eventId, content, vectorLiteral);
    }

    public List<MemoryHit> searchSimilar(String accountId, String excludeEventId, List<Double> queryEmbedding, int topK) {
        String vectorLiteral = toPgVectorLiteral(queryEmbedding);

        return jdbc.query("""
            SELECT id, account_id, event_id, content, created_at,
                   (embedding <=> ?::vector) AS distance
            FROM account_memory
            WHERE account_id = ?
              AND (? IS NULL OR event_id IS DISTINCT FROM ?)
            ORDER BY embedding <=> ?::vector
            LIMIT ?
        """, (ResultSet rs, int rowNum) -> new MemoryHit(
                rs.getLong("id"),
                rs.getString("account_id"),
                rs.getString("event_id"),
                rs.getString("content"),
                rs.getObject("created_at", java.time.OffsetDateTime.class).toInstant(),
                rs.getDouble("distance")
        ), vectorLiteral, accountId, excludeEventId, excludeEventId, vectorLiteral, topK);
    }

    private static String toPgVectorLiteral(List<Double> v) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < v.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(v.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}

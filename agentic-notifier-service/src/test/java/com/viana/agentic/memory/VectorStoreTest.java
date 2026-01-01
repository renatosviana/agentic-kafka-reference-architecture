package com.viana.agentic.memory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class VectorStoreTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private VectorStore vectorStore;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @Test
    void insertMemory_shouldPassVectorLiteralToJdbc() {
        vectorStore.insertMemory("ACC1", "EVT1", "hello world", List.of(0.1, 0.2));

        Mockito.verify(jdbcTemplate).update(sqlCaptor.capture(), eq("ACC1"), eq("EVT1"), eq("hello world"), eq("[0.1,0.2]"));
        assertThat(sqlCaptor.getValue()).contains("INSERT INTO account_memory");
    }

    @Test
    void searchSimilar_shouldQueryWithVectorLiteralAndArguments() {
        String expectedSql = """
            SELECT id, account_id, event_id, content, created_at,
                   (embedding <=> ?::vector) AS distance
            FROM account_memory
            WHERE account_id = ?
              AND (?::text IS NULL OR event_id IS DISTINCT FROM ?::text)
            ORDER BY embedding <=> ?::vector
            LIMIT ?
        """;
        var expectedHit = new MemoryHit(1L, "ACC1", "EVT2", "content", Instant.parse("2024-01-01T00:00:00Z"), 0.12);
        Mockito.when(jdbcTemplate.query(eq(expectedSql), any(RowMapper.class),
                        any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(expectedHit));

        var results = vectorStore.searchSimilar("ACC1", "EVT1", List.of(0.5, 0.75), 3);

        assertThat(results).containsExactly(expectedHit);
        Mockito.verify(jdbcTemplate).query(eq(expectedSql), any(RowMapper.class),
                eq("[0.5,0.75]"), eq("ACC1"), eq("EVT1"), eq("EVT1"), eq("[0.5,0.75]"), eq(3));
    }
}
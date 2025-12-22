package com.viana.agentic.decision;

import com.viana.agentic.model.AgentDecision;
import com.viana.agentic.model.EnrichedAccountEvent;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionEngineTest {

    private final DecisionEngine engine = new DecisionEngine(Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void shouldDecideNotifyEmail_whenRiskHigh() {
        EnrichedAccountEvent e = EnrichedAccountEvent.builder()
                .eventId("E1")
                .accountId("ACC123")
                .riskScore(9)
                .summary("Suspicious pattern")
                .build();

        AgentDecision d = engine.decide(e);

        assertNotNull(d.actions(), "actions should not be null");

        boolean hasNotifyEmail = d.actions().stream()
                .anyMatch(a -> "NOTIFY_EMAIL".equals(a.type()));

        assertTrue(hasNotifyEmail, "Expected NOTIFY_EMAIL but got: " + d.actions());
    }}


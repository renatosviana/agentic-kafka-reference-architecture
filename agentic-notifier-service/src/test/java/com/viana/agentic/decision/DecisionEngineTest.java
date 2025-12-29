package com.viana.agentic.decision;

import com.viana.agentic.model.AgentAction;
import com.viana.agentic.model.AgentDecision;
import com.viana.common.events.EnrichedAccountEvent;
import com.viana.common.events.EventType;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DecisionEngineTest {

    private final DecisionEngine engine =
            new DecisionEngine(Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC));

    private static EnrichedAccountEvent event(
            Integer riskScore,
            String summary,
            EventType eventType,
            Double amount
    ) {
        return EnrichedAccountEvent.builder()
                .eventId("E1")
                .accountId("ACC123")
                .riskScore(riskScore)
                .summary(summary)
                .eventType(eventType)
                .amount(amount)
                .currency("CAD")
                .timestamp(Instant.parse("2025-01-01T00:00:00Z"))
                .build();
    }

    private static boolean hasAction(AgentDecision d, String type) {
        return d.actions() != null && d.actions().stream().anyMatch(a -> type.equals(a.type()));
    }

    private static Map<String, Object> notifyArgs(AgentDecision d) {
        AgentAction action = d.actions().stream()
                .filter(a -> "NOTIFY_EMAIL".equals(a.type()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("NOTIFY_EMAIL action not found"));
        return action.args();
    }

    @Test
    void shouldNotifyHigh_whenRiskScoreAtOrAboveHighThreshold() {
        var e = event(
                80,
                "This appears normal.",
                EventType.CREDIT,
                48.0
        );

        var d = engine.decide(e);

        assertTrue(hasAction(d, "NOTIFY_EMAIL"), "Expected NOTIFY_EMAIL");
        var args = notifyArgs(d);

        assertEquals("HIGH", args.get("severity"));
        assertEquals("Suspicious account activity", args.get("subject"));
        assertTrue(d.rationale().toLowerCase().contains("high"), "Rationale should reflect HIGH");
    }

    @Test
    void shouldNotifyHigh_whenNegativeCreditAmount() {
        var e = event(
                0,
                "Credit transaction of -9999.00 happened.",
                EventType.CREDIT,
                -9999.0
        );

        var d = engine.decide(e);

        assertTrue(hasAction(d, "NOTIFY_EMAIL"), "Expected NOTIFY_EMAIL");
        var args = notifyArgs(d);

        assertNotNull(args.get("severity"));
        assertNotNull(args.get("subject"));
    }
}

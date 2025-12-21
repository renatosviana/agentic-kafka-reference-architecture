package com.viana.agentic.decision;

import com.viana.agentic.model.AgentAction;
import com.viana.agentic.model.AgentDecision;
import com.viana.agentic.model.EnrichedAccountEvent;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class DecisionEngine {

    private static final String ACTION_NO_ACTION = "NO_ACTION";
    private static final String ACTION_NOTIFY_EMAIL = "NOTIFY_EMAIL";

    private final Clock clock;

    public DecisionEngine() {
        this(Clock.systemUTC());
    }

    public DecisionEngine(Clock clock) {
        this.clock = clock;
    }

    public AgentDecision decide(EnrichedAccountEvent e) {
        String decisionId = UUID.randomUUID().toString();
        Instant now = Instant.now(clock);

        if (!isSuspicious(e)) {
            return new AgentDecision(
                    decisionId,
                    e.eventId(),
                    e.accountId(),
                    now,
                    List.of(new AgentAction(ACTION_NO_ACTION, Map.of())),
                    "No risk indicators detected."
            );
        }

        return new AgentDecision(
                decisionId,
                e.eventId(),
                e.accountId(),
                now,
                List.of(new AgentAction(ACTION_NOTIFY_EMAIL, Map.of(
                        "subject", "Suspicious account activity",
                        "severity", "MEDIUM"
                ))),
                "Risk indicators detected (riskScore>0 or 'unusual' in summary)."
        );
    }

    private boolean isSuspicious(EnrichedAccountEvent e) {
        boolean riskScorePositive = e.riskScore() != null && e.riskScore() > 0;
        boolean mentionsUnusual = e.summary() != null && e.summary().toLowerCase().contains("unusual");
        return riskScorePositive || mentionsUnusual;
    }
}

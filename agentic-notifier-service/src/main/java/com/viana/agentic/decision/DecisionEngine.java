package com.viana.agentic.decision;

import com.viana.agentic.model.AgentAction;
import com.viana.agentic.model.AgentDecision;
import com.viana.common.events.EnrichedAccountEvent;
import com.viana.common.events.EventType;
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

    private static final int MEDIUM_THRESHOLD = 50;
    private static final int HIGH_THRESHOLD = 80;

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

        Severity severity = classifySeverity(e);

        if (severity == Severity.NONE) {
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
                        "subject", buildSubject(severity),
                        "severity",severity.name()
                ))),
                buildRationale(e, severity)
        );
    }

    private String buildSubject(Severity severity) {
        return severity == Severity.HIGH
                ? "Suspicious account activity"
                : "Account activity notification";
    }

    private String buildRationale(EnrichedAccountEvent e, Severity severity) {
        int score = e.riskScore() == null ? 0 : e.riskScore();
        String summary = e.summary() == null ? "" : e.summary().toLowerCase();

        boolean keywordFlag =
                summary.contains("suspicious") ||
                        summary.contains("fraud") ||
                        summary.contains("anomal") ||
                        summary.contains("unusual");

        String trigger = (score >= HIGH_THRESHOLD) ? "score>=80"
                : (score >= MEDIUM_THRESHOLD) ? "score>=50"
                : keywordFlag ? "keyword(unusual/suspicious/...)"
                : "none";

        return "Risk signal: severity=" + severity + ", riskScore=" + score + ", trigger=" + trigger + ".";
    }

    private Severity classifySeverity(EnrichedAccountEvent e) {
        Severity amountSeverity = classifyAmountAnomaly(e);
        if (amountSeverity != Severity.NONE) {
            return amountSeverity;
        }

        int score = (e.riskScore() == null) ? 0 : e.riskScore();
        String summary = (e.summary() == null) ? "" : e.summary().toLowerCase();

        boolean explicitlyNormal =
                summary.contains("appears to be normal") ||
                        summary.contains("normal transaction") ||
                        summary.contains("no unusual");

        boolean suspiciousKeywords =
                summary.contains("suspicious") ||
                        summary.contains("fraud") ||
                        summary.contains("anomal") ||
                        summary.contains("unusual");

        if (score >= HIGH_THRESHOLD) return Severity.HIGH;
        if (score >= MEDIUM_THRESHOLD && !explicitlyNormal) return Severity.MEDIUM;
        if (suspiciousKeywords && !explicitlyNormal) return Severity.MEDIUM;

        return Severity.NONE;
    }

    private Severity classifyAmountAnomaly(EnrichedAccountEvent e) {
        if (e.amount() == null || e.eventType() == null) {
            return Severity.NONE;
        }

        if (e.eventType() == EventType.CREDIT && e.amount() < 0) {
            return Severity.HIGH;
        }

        if (e.eventType() == EventType.DEBIT && e.amount() < 0) {
            return Severity.MEDIUM;
        }

        return Severity.NONE;
    }


    enum Severity { NONE, MEDIUM, HIGH }
}

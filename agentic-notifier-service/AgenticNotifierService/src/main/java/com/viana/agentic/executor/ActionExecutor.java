package com.viana.agentic.executor;

import com.viana.agentic.model.AgentAction;
import com.viana.agentic.model.AgentDecision;
import com.viana.agentic.model.EnrichedAccountEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ActionExecutor {

    private final EmailNotifier emailNotifier;

    public ActionExecutor(EmailNotifier emailNotifier) {
        this.emailNotifier = emailNotifier;
    }

    public void execute(AgentDecision decision, EnrichedAccountEvent event, AgentAction action) {
        switch (action.type()) {
            case "NO_ACTION" -> { /* nothing */ }
            case "NOTIFY_EMAIL" -> {
                Map<String, Object> args = action.args();
                String subject = String.valueOf(args.getOrDefault("subject", "Agentic notification"));
                String body =
                        "Account: " + event.accountId() + "\n" +
                        "EventId: " + event.eventId() + "\n" +
                        "Summary: " + event.summary() + "\n" +
                        "Rationale: " + decision.rationale() + "\n";
                emailNotifier.send(subject, body);
            }
            default -> throw new IllegalArgumentException("Action not supported: " + action.type());
        }
    }
}

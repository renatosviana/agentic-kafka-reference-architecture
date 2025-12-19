package com.viana.agentic.kafka;

import com.viana.agentic.config.KafkaTopics;
import com.viana.agentic.decision.DecisionEngine;
import com.viana.agentic.executor.ActionExecutor;
import com.viana.agentic.model.ActionResult;
import com.viana.agentic.model.AgentAction;
import com.viana.agentic.model.AgentDecision;
import com.viana.agentic.model.EnrichedAccountEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.viana.agentic.config.KafkaTopics.INPUT_ENRICHED;

@Component
public class EnrichedEventListener {

    private final DecisionEngine decisionEngine = new DecisionEngine(); // keep PR1 simple
    private final ActionExecutor actionExecutor;
    private final AuditPublisher auditPublisher;

    public EnrichedEventListener(ActionExecutor actionExecutor, AuditPublisher auditPublisher) {
        this.actionExecutor = actionExecutor;
        this.auditPublisher = auditPublisher;
    }

//    @KafkaListener(topics = INPUT_ENRICHED)
    @KafkaListener(topics = KafkaTopics.INPUT_ENRICHED, groupId = "agentic-notifier-service")
    public void onMessage(EnrichedAccountEvent event) {
        AgentDecision decision = decisionEngine.decide(event);

        auditPublisher.publishDecision(decision);

        for (AgentAction action : decision.actions()) {
            try {
                actionExecutor.execute(decision, event, action);
                auditPublisher.publishResult(new ActionResult(
                        decision.decisionId(),
                        event.eventId(),
                        action.type(),
                        Instant.now(),
                        true,
                        "Executed successfully"
                ));
            } catch (Exception ex) {
                auditPublisher.publishResult(new ActionResult(
                        decision.decisionId(),
                        event.eventId(),
                        action.type(),
                        Instant.now(),
                        false,
                        ex.getMessage()
                ));
            }
        }
    }
}

package com.viana.agentic.kafka;

import com.viana.agentic.KafkaTopics;
import com.viana.agentic.model.ActionResult;
import com.viana.agentic.model.AgentDecision;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AuditPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDecision(AgentDecision decision) {
        kafkaTemplate.send(KafkaTopics.OUT_DECISION, decision.accountId(), decision);
    }

    public void publishResult(ActionResult result) {
        kafkaTemplate.send(KafkaTopics.OUT_RESULT, result.eventId(), result);
    }
}

package com.viana.agentic.kafka;

import com.viana.agentic.KafkaTopics;
import com.viana.agentic.executor.ActionExecutor;
import com.viana.agentic.model.ActionResult;
import com.viana.agentic.model.AgentDecision;
import com.viana.agentic.model.EnrichedAccountEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditPublisher {

    private static final Logger log = LoggerFactory.getLogger(ActionExecutor.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AuditPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDecision(EnrichedAccountEvent event, AgentDecision decision) {
        log.info("Publishing decision to topic {}", KafkaTopics.OUT_DECISION);

        kafkaTemplate.send(KafkaTopics.OUT_DECISION, decision.accountId(), decision);
    }

    public void publishResult(ActionResult result) {
        log.info("Publishing result to topic {}", KafkaTopics.OUT_RESULT);

        kafkaTemplate.send(KafkaTopics.OUT_RESULT, result.eventId(), result);
    }
}

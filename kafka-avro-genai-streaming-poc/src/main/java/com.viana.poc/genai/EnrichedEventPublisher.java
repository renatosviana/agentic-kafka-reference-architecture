package com.viana.poc.genai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viana.poc.events.EnrichedAccountEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EnrichedEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(EnrichedEventPublisher.class);
    private static final String TOPIC = "account.enriched.v1";

    private final KafkaTemplate<String, EnrichedAccountEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EnrichedEventPublisher(KafkaTemplate<String, EnrichedAccountEvent> kafkaTemplate,
                                 ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(EnrichedAccountEvent event) {
        kafkaTemplate.send(TOPIC, event.accountId(), event);
        log.info("Published enriched event to topic {} for account {}", TOPIC, event.accountId());
    }

}

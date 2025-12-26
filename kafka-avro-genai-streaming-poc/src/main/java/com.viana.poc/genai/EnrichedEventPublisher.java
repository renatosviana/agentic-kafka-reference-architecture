package com.viana.poc.genai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viana.poc.events.EnrichedAccountEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EnrichedEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(EnrichedEventPublisher.class);
    private static final String TOPIC = "account.enriched.v1";
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public EnrichedEventPublisher(KafkaTemplate<String, String> kafkaTemplate, 
                                 ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    public void publish(EnrichedAccountEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, event.accountId(), json);
            log.info("Published enriched event to topic {} for account {}", TOPIC, event.accountId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize enriched event", e);
            throw new RuntimeException("Failed to serialize enriched event", e);
        }
    }
}

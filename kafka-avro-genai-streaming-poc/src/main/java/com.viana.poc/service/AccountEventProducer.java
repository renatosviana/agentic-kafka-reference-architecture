package com.viana.poc.service;

import com.viana.avro.AccountEvent;
import com.viana.poc.genai.GenAiResponse;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.viana.poc.constants.Constants.ACCOUNT_EVENTS_TOPIC;

@Service
public class AccountEventProducer {

    private final KafkaTemplate<String, AccountEvent> kafkaTemplate;

    public AccountEventProducer(KafkaTemplate<String, AccountEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAccountEvent(AccountEvent event) {
        kafkaTemplate.send(ACCOUNT_EVENTS_TOPIC, event.getAccountId(), event);
    }
}
// src/main/java/com/viana/poc/service/AccountSummaryProducer.java
package com.viana.poc.service;

import com.viana.avro.AccountEvent;
import com.viana.avro.AccountEventSummary;
import com.viana.poc.genai.GenAiResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AccountSummaryProducer {

    private final KafkaTemplate<String, AccountEventSummary> kafkaTemplate;

    public AccountSummaryProducer(
            KafkaTemplate<String, AccountEventSummary> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendSummary(AccountEvent sourceEvent, GenAiResult result) {

        var record = AccountEventSummary.newBuilder()
                .setAccountId(sourceEvent.getAccountId())
                .setSummary(result.getSummary())
                .setClassification(result.getClassification())
                .setRiskScore(result.getRiskScore())
                .setCreatedAt(Instant.now().toString())
                .build();

        kafkaTemplate.send("account-event-summaries-avro", sourceEvent.getAccountId(), record);
    }
}

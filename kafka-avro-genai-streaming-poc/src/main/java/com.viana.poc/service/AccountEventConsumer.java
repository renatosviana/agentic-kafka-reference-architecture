package com.viana.poc.service;

import com.viana.avro.AccountEvent;
import com.viana.poc.genai.GenAiClient;
import com.viana.poc.genai.GenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AccountEventConsumer.class);

    private final Map<String, Double> balances = new ConcurrentHashMap<>();
    private final AccountProcessingService processingService;

    public AccountEventConsumer(AccountProcessingService processingService) {
        this.processingService = processingService;
    }

    @KafkaListener(topics = "account-events", groupId = "account-processor")
    public void handle(AccountEvent event) {
        String accountId = event.getAccountId();

        double previousBalance = balances.getOrDefault(accountId, 0.0);
        double updated = switch (event.getEventType()) {
            case CREDIT -> previousBalance + event.getAmount();
            case DEBIT  -> previousBalance - event.getAmount();
        };
        balances.put(accountId, updated);

        processingService.process(event, updated);

        System.out.printf(
                "Event %s for account %s → new balance = %.2f%n",
                event.getEventType(), accountId, updated
        );
    }
}
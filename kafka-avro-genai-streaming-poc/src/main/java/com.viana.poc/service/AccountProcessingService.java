package com.viana.poc.service;

import com.viana.avro.AccountEvent;
import com.viana.poc.entity.AccountSummaryEntity;
import com.viana.poc.genai.GenAiClient;
import com.viana.poc.genai.GenAiRequest;
import com.viana.poc.genai.GenAiResponse;
import com.viana.poc.repository.AccountSummaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AccountProcessingService {

    private static final Logger log = LoggerFactory.getLogger(AccountProcessingService.class);

    private final GenAiClient genAiClient;
    private final AccountSummaryRepository summaryRepository;

    public AccountProcessingService(GenAiClient genAiClient,
                                    AccountSummaryRepository summaryRepository) {
        this.genAiClient = genAiClient;
        this.summaryRepository = summaryRepository;
    }

    public GenAiResponse process(AccountEvent event, double newBalance) {

        // 1) Call GenAI
        GenAiRequest request = new GenAiRequest(
                event.getAccountId(),
                event.getEventType().toString(),
                event.getAmount(),
                newBalance
        );

        String summary = genAiClient.summarizeEvent(request).getSummary();
        GenAiResponse response = new GenAiResponse(summary);

        AccountSummaryEntity entity = new AccountSummaryEntity();
        entity.setAccountId(event.getAccountId());
        entity.setSummary(summary);
        entity.setClassification("NORMAL");
        entity.setRiskScore(50);
        entity.setCreatedAt(Instant.now());

        summaryRepository.save(entity);

        log.info("GenAI summary for account {}: {}", event.getAccountId(), summary);

        return response;
    }
}
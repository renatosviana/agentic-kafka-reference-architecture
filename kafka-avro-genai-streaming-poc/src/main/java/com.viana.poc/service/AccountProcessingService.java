package com.viana.poc.service;

import com.viana.avro.AccountEvent;
import com.viana.common.events.EnrichedAccountEvent;
import com.viana.common.events.EventType;
import com.viana.poc.entity.AccountSummaryEntity;
import com.viana.poc.genai.EnrichedEventPublisher;
import com.viana.poc.genai.GenAiClient;
import com.viana.poc.genai.GenAiRequest;
import com.viana.poc.genai.GenAiResponse;
import com.viana.poc.repository.AccountSummaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AccountProcessingService {

    private static final Logger log = LoggerFactory.getLogger(AccountProcessingService.class);

    private final GenAiClient genAiClient;
    private final AccountSummaryRepository summaryRepository;
    private final EnrichedEventPublisher enrichedEventPublisher;

    public AccountProcessingService(GenAiClient genAiClient,
                                    AccountSummaryRepository summaryRepository,
                                    EnrichedEventPublisher enrichedEventPublisher) {
        this.genAiClient = genAiClient;
        this.summaryRepository = summaryRepository;
        this.enrichedEventPublisher = enrichedEventPublisher;
    }

    public GenAiResponse process(AccountEvent event, double newBalance) {

        Instant now = Instant.now();

        GenAiRequest request = new GenAiRequest(
                event.getAccountId(),
                event.getEventType().toString(),
                event.getAmount(),
                newBalance
        );

        GenAiResponse response = genAiClient.summarizeEvent(request);

        EnrichedAccountEvent enriched = EnrichedAccountEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .accountId(event.getAccountId())
                .riskScore(response.getRiskScore())
                .summary(response.getSummary())
                .eventType(EventType.valueOf(event.getEventType().toString()))
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .timestamp(now)

                .build();

        enrichedEventPublisher.publish(enriched);

        log.info("Published enriched event to Kafka for accountId={}", event.getAccountId());

        AccountSummaryEntity entity = new AccountSummaryEntity();
        entity.setAccountId(event.getAccountId());
        entity.setSummary(response.getSummary());
        entity.setClassification(response.getClassification());
        entity.setRiskScore(response.getRiskScore());
        entity.setCreatedAt(now);

        summaryRepository.save(entity);

        log.info("GenAI summary for account {}: {}", event.getAccountId(), response);

        return response;
    }
}
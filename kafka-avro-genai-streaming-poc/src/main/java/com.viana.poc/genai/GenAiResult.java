package com.viana.poc.genai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenAiResult {

    private String summary;
    private String classification;
    private int riskScore;
    private boolean fromFallback;
    private String reason;
    private Instant generatedAt;

    public static GenAiResult success(String summary,
                                      String classification,
                                      int riskScore) {
        return GenAiResult.builder()
                .summary(summary)
                .classification(classification)
                .riskScore(riskScore)
                .fromFallback(false)
                .reason(null)
                .generatedAt(Instant.now())
                .build();
    }

    public static GenAiResult fallback(String summary) {
        return GenAiResult.builder()
                .summary(summary)
                .classification("UNKNOWN")
                .riskScore(0)
                .fromFallback(true)
                .reason("fallback")
                .generatedAt(Instant.now())
                .build();
    }
}

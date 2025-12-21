package com.viana.poc.genai;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class GenAiResponse {

    @Column(length = 4000)
    private String summary;
    private String classification;
    private int riskScore;

}

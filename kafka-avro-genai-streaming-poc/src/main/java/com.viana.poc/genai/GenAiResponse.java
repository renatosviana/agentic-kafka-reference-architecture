package com.viana.poc.genai;

public class GenAiResponse {
    private String summary;

    public GenAiResponse() {}

    public GenAiResponse(String summary) {
        this.summary = summary;
    }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}

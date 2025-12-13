package com.viana.poc.genai;

public class GenAiRequest {
    private String accountId;
    private String eventType;
    private double amount;
    private double newBalance;

    public GenAiRequest() {}

    public GenAiRequest(String accountId, String eventType, double amount, double newBalance) {
        this.accountId = accountId;
        this.eventType = eventType;
        this.amount = amount;
        this.newBalance = newBalance;
    }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public double getNewBalance() { return newBalance; }
    public void setNewBalance(double newBalance) { this.newBalance = newBalance; }
}

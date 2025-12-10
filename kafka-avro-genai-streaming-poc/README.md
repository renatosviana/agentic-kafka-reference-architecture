# GenAI + Kafka + Agentic Streaming POC

Real-time Account Event Processing with GenAI Summaries, Kafka Streams KTable, and React UI

This project is a fully functional end-to-end streaming architecture that ingests account events, computes running balances via Kafka Streams, generates intelligent GenAI summaries, classifies risk behavior, and stores normalized summaries in Postgres.

# A lightweight React UI fetches account summaries to display a human-readable history.

# Technologies Used
## Backend


| Component        | Tech                                                   |
|------------------|--------------------------------------------------------|
| Runtime          | **Java 21, Spring Boot 3**                             |
| Messaging        | **Kafka 7.x** (Confluent images)                       |
| Schema           | **Avro + Schema Registry**                             |
| Stream Processing| **Kafka Streams (KTable)**                             |
| Data Store       | **PostgreSQL 15**                                      |
| AI / LLM         | **Custom GenAIClient using OpenAI API (or local)**     |
| Build Tool       | **Gradle**                                             |


## Frontend

| Component        | Tech                                                   |
|------------------|--------------------------------------------------------|
| UI Framework     | **React + Vite**                                       |
| HTTP             | **axios**                                              |

	
	
### Infrastructure

Docker Compose (Kafka stack + Postgres)

## Folder Structure
```
kafka-avro-genai-streaming-poc/
│
├── account-service/              # Spring Boot app
│   ├── src/main/java/com/viana/poc
│   │   ├── controller/           # REST endpoints
│   │   ├── entity/               # JPA entities
│   │   ├── repository/           # JPA repositories
│   │   ├── streams/              # Kafka Stream processor (KTable)
│   │   ├── service/              # GenAI + Kafka logic
│   │   ├── genai/                # GenAI client, request/response
│   │   └── constants/
│   └── resources/
│       ├── application.yml
│       └── avro schemas
│
├── docker/
│   └── docker-compose.yml        # Kafka, Zookeeper, Schema Registry, Postgres
│
├── account-ui/
│   └── src/App.jsx               # React UI
│
└── README.md
```
# System Architecture (High-Level)
End-to-end Flow

User sends a credit or debit event via REST.

Event is serialized as Avro and published to Kafka topic account-events.

Kafka Streams KTable maintains a running balance per account.

When balance updates, a downstream consumer:

Calls GenAI to interpret the event.

Produces an agentic summary, classification, and risk level.

Stores the result in Postgres.

The React UI fetches /summaries/{accountId} and displays results.

# Mermaid Architecture Diagram
```
sequenceDiagram
    actor U as User
    participant UI as React UI (Vite)
    participant API as Spring Boot API<br/>AccountController
    participant Prod as AccountEventProducer
    participant K as Kafka<br/>account-events
    participant KS as Kafka Streams<br/>Balance KTable
    participant Cons as AccountEventConsumer
    participant GA as GenAiClient<br/>(OpenAI API)
    participant SumProd as AccountSummaryProducer
    participant K2 as Kafka<br/>account-event-summaries-avro
    participant SumCons as SummaryConsumer<br/>JPA Service
    participant DB as Postgres<br/>account_summaries
    participant SumAPI as AccountSummaryController

    %% ---- Produce event ----
    U->>UI: Click "Credit/Debit" (ACC123, amount)
    UI->>API: POST /accounts/{id}/credit?amount=...
    API->>Prod: build AccountEvent (Avro)
    Prod->>K: send(AccountEvent) to topic account-events

    %% ---- Streaming & balance ----
    K-->>KS: AccountEvent consumed by Streams
    KS-->>KS: Update KTable balance for ACC123
    KS-->>Cons: Emit event + newBalance

    %% ---- GenAI & summary ----
    Cons->>GA: summarize(accountId, type, amount, newBalance)
    GA-->>Cons: GenAiResponse (summary, classification, riskScore)
    Cons->>SumProd: sendSummary(sourceEvent, GenAiResponse)
    SumProd->>K2: send(AccountEventSummary Avro)

    %% ---- Persist in Postgres ----
    K2-->>SumCons: consume AccountEventSummary
    SumCons->>DB: INSERT INTO account_summaries (...)

    %% ---- UI reads summaries ----
    U->>UI: Click "Load summaries"
    UI->>SumAPI: GET /summaries/ACC123
    SumAPI->>DB: SELECT * FROM account_summaries WHERE account_id=ACC123 ORDER BY created_at DESC
    DB-->>SumAPI: List<AccountSummaryEntity>
    SumAPI-->>UI: JSON summaries
    UI-->>U: Render timeline of GenAI summaries

```
# Components Explained
1. Event Producer (AccountEventProducer)

Publishes Avro-encoded event to Kafka topic account-events.

Calls GenAI to generate human-readable summaries.

2. Kafka Streams State Store (KTable)

Maintains real-time account balances:

"groupByKey().aggregate(...)" → state store → changelog topic


State is recovered on restart.

3. Agentic GenAI Processing

AccountProcessingService:

Receives the event + computed balance

Sends a structured request to GenAI

The AI:

Interprets the event

Generates a natural-language summary

Classifies behavior (NORMAL / SUSPICIOUS)

Assigns a risk score

Saves the result in Postgres

4. UI (React)

Calls backend: GET http://localhost:8080/summaries/ACC123

Displays all summaries for the account

# End-to-End Testing
## 1. Start the environment
cd docker
docker compose up -d


You should have:

Kafka on port 29092

Schema Registry on 8081

Postgres on 5432

## 2. Start Spring Boot app
cd account-service
./gradlew bootRun


Runs on http://localhost:8080

## 3. Start UI
$ cd account-ui
$ npm install
$ npm run dev


Open browser:

http://localhost:5173

# Testing via REST (Postman or curl)
Credit event
curl
curl -X POST "http://localhost:8080/accounts/ACC123/credit?amount=50"

Postman

POST → http://localhost:8080/accounts/ACC123/credit?amount=50

Debit event
curl -X POST "http://localhost:8080/accounts/ACC123/debit?amount=20"

Check summaries in Postgres

Inside container:

$ docker exec -it genai_kafka_postgres psql -U postgres -d genai_kafka

genai_kafka=# select * from account_summaries order by id desc;

# Testing the UI

Open:
http://localhost:5173

Input: ACC123

Click Load summaries

You should see records like:

Created: 2025-12-09
Classification: NORMAL (risk: 50)
Summary: A credit of $50 was made...

# Agentic Behavior Testing

To verify GenAI decisions:

Normal event
POST /accounts/ACC123/credit?amount=50

Suspicious event (negative amount)
POST /accounts/ACC123/credit?amount=-10


## Expected behavior:

GenAI flags unusual behavior

Risk score increases

Summary explains anomaly

Stored in Postgres

Visible in UI
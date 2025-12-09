# GenAI + Kafka + Agentic Streaming POC

Real-time Account Event Processing with GenAI Summaries, Kafka Streams KTable, and React UI

This project is a fully functional end-to-end streaming architecture that ingests account events, computes running balances via Kafka Streams, generates intelligent GenAI summaries, classifies risk behavior, and stores normalized summaries in Postgres.

# A lightweight React UI fetches account summaries to display a human-readable history.

# Technologies Used
Backend
Component	Tech
Runtime	Java 21, Spring Boot 3
Messaging	Kafka 7.x (Confluent images)
Schema	Avro + Schema Registry
Stream Processing	Kafka Streams (KTable)
Data Store	PostgreSQL 15
AI / LLM	Custom GenAIClient using OpenAI API (or local)
Build tool	Gradle
Frontend
Component	Tech
UI Framework	React + Vite
HTTP	axios
Infrastructure

Docker Compose (Kafka stack + Postgres)

# Folder Structure
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
flowchart LR

subgraph User
A1[POST /accounts/{id}/credit]
A2[POST /accounts/{id}/debit]
A3[UI Load Summaries]
end

subgraph SpringBoot
C1[AccountController]
C2[AccountEventProducer]
C3[AccountProcessingService<br/>GenAIClient]
C4[AccountSummaryController]
end

subgraph Kafka
K1[(account-events)]
K2[(account-balance-store-changelog)]
K3>KTable: account-balance-store]
end

subgraph StreamApp
S1[Kafka Streams Processor<br/>Balance Aggregation]
end

subgraph Postgres
DB[(account_summaries table)]
end

A1 --> C1 --> C2 --> K1
A2 --> C1 --> C2 --> K1

K1 --> S1 --> K3 --> C3
C3 --> DB

A3 --> C4 --> DB --> A3
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
cd account-ui
npm install
npm run dev


Open browser:

http://localhost:5174

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

docker exec -it postgres psql -U postgres genai_kafka

select * from account_summaries order by id desc;

# Testing the UI

Open:
http://localhost:5174

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
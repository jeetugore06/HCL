# Z Bank :: Credit Card Application — Microservices

Three Spring Boot services + Kafka.

## Services

| Service | Port | Database | Owns | Endpoints |
|---|---|---|---|---|
| **customer-service** | 8081 | `creditcards_customer_db` | `customer` | `POST /api/credit-cards/apply` |
| **scoring-service** | 8082 | `creditcards_scoring_db` | `credit_score` | `GET /api/credit-cards/customers/{customerReference}/credit-score` |
| **card-service** | 8083 | `creditcards_card_db` | `credit_card_application`, `credit_card`, `card_audit_log` | `POST /api/credit-cards/first-login`, `POST /api/credit-cards/change-pin` |

> Each service has its **own DB schema** (database-per-service rule). They reference each other only by `customer_reference` (UUID) and `application_reference` (UUID) — never by surrogate ID, never via SQL JOINs.

## Kafka Topic Map

| Topic | Producer | Consumers |
|---|---|---|
| `customer.registered` | customer-service | scoring-service |
| `customer.scored` | scoring-service | card-service |
| `card.activated` | card-service | card-service (email notifier) |
| `application.pending-docs` | card-service | card-service (email notifier) |
| `application.rejected` | card-service | card-service (email notifier) |
| `card.pin-changed` | card-service | card-service (email notifier) |

> The dummy email notifier lives **inside** card-service for hackathon scope. It listens to its own outbound topics and appends rendered emails to `email-outbox.log`. A standalone `notification-service` is a Phase-2 split.

## One-time Setup

1. **MySQL** running on `localhost:3307` with `root / root`. (Match what the `payments` service already uses.)
2. **Apply schemas** — run each service's `schema.sql`:
   ```cmd
   mysql -h localhost -P 3307 -u root -proot < customer-service\schema.sql
   mysql -h localhost -P 3307 -u root -proot < scoring-service\schema.sql
   mysql -h localhost -P 3307 -u root -proot < card-service\schema.sql
   ```
3. **Kafka** running on `localhost:9092` (you already have it on `c:\kafka`).
4. **Create topics** (Spring will auto-create on first publish, but explicit is cleaner):
   ```cmd
   cd c:\kafka
   for %%t in (customer.registered customer.scored card.activated application.pending-docs application.rejected card.pin-changed) do .\bin\windows\kafka-topics.bat --create --topic %%t --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 --if-not-exists
   ```

## Running Locally

Open three Command Prompts, one per service:

```cmd
cd D:\interview-poc\hackathon\HCL\customer-service
mvn spring-boot:run
```
```cmd
cd D:\interview-poc\hackathon\HCL\scoring-service
mvn spring-boot:run
```
```cmd
cd D:\interview-poc\hackathon\HCL\card-service
mvn spring-boot:run
```

## Folder Structure

```
HCL/
├── README.md                  ← this file
├── customer-service/
│   ├── pom.xml
│   ├── schema.sql
│   └── src/...
├── scoring-service/
│   ├── pom.xml
│   ├── schema.sql
│   └── src/...
└── card-service/
    ├── pom.xml
    ├── schema.sql
    └── src/...
```

## Coding Standards

All three services follow `..\coding-standards.md` — Java 17, Lombok, no records/sealed, constructor injection, custom exceptions + `@RestControllerAdvice`, `BigDecimal` for money, parameterized SLF4J logging, BCrypt for PIN hashes.

## Reference Documentation

- `..\creditcards-architecture.md` — full architecture overview
- `..\creditcards-flows.html` — visual flow diagrams (open in browser)
- `..\creditcards-rest-api.md` — REST API spec with payloads
- `..\creditcards-schema.sql` — original monolithic schema (superseded by per-service schemas above)

## Status

Scaffolds only — main class + `application.properties` + `pom.xml` + `schema.sql` per service. Entities, repos, DTOs, services, controllers, exception handlers, Kafka publishers/listeners, and tests come next per service in the sequence we've agreed.

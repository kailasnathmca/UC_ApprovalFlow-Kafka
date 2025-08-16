# UC_ApprovalFlow-Kafka
Context: UC  want a spring boot project for investment proposal management. with rest apis for approval workflow with kafka. wanted in detail. with all class and config files. and how to run kafka etc..


All set! I generated a complete Spring Boot project (Java 17, Spring Boot 3.3) for an Investment Proposal Management system with a multi-step approval workflow and Kafka events—plus Docker Compose to run Kafka locally.

[Download the project ZIP](sandbox:/mnt/data/ipm-service.zip)

### What’s inside (high level)

* REST APIs:

  * `POST /api/proposals` create (starts as DRAFT)
  * `POST /api/proposals/{id}/submit` move to UNDER\_REVIEW (uses default chain `PEER_REVIEW → MANAGER_APPROVAL → COMPLIANCE` unless you pass a custom chain body `["TEAM_LEAD","RISK","CFO"]`)
  * `POST /api/proposals/{id}/approve` approve current step
  * `POST /api/proposals/{id}/reject` reject current step
  * `GET /api/proposals/{id}` fetch one
  * `GET /api/proposals?status=&page=&size=` list/paginate
* Kafka:

  * Topics auto-created: `proposal-events`, `proposal-events.DLT`, `audit-logs`
  * Producer sends `PROPOSAL_SUBMITTED`, `STEP_APPROVED`, `PROPOSAL_APPROVED`, `PROPOSAL_REJECTED` to `proposal-events`, plus human-readable lines to `audit-logs`
  * Consumer logs events and sends failures to DLT with retry/backoff
* DB: H2 (in-memory) by default. Oracle config snippet included in README if you want to switch.
* Extras: Swagger/OpenAPI UI, global exception handler, sample data, Postman collection.

### Run Kafka & the app (quick steps)

1. Start Kafka stack:

```bash
cd ipm-service
docker compose up -d
# Kafka UI: http://localhost:8080
```

2. Run the service:

```bash
mvn spring-boot:run
# Swagger: http://localhost:8081/swagger-ui/index.html
```

### Quick API demo (curl)

```bash
# Create
curl -s -X POST http://localhost:8081/api/proposals \
 -H "Content-Type: application/json" \
 -d '{"title":"IPO Fund","applicantName":"Riya","amount":150000,"description":"New fund"}'

# Submit (uses default chain)
curl -s -X POST http://localhost:8081/api/proposals/1/submit

# Approve current step
curl -s -X POST http://localhost:8081/api/proposals/1/approve \
 -H "Content-Type: application/json" \
 -d '{"approver":"john.doe","comments":"Looks good"}'

# Reject (instead of approve)
curl -s -X POST http://localhost:8081/api/proposals/1/reject \
 -H "Content-Type: application/json" \
 -d '{"approver":"john.doe","comments":"Insufficient docs"}'
```

If you want me to switch DB to Oracle and wire up Flyway scripts, add JWT security, or split this into separate producer/consumer microservices, say the word and I’ll ship that version too.


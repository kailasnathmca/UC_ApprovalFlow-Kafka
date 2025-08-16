Got you. Here’s a complete, copy-paste friendly test plan covering **every REST API** in the workflow, in the exact order you’d use them. It includes sample requests, what you should see back, and how many times to call each step.

---

# Prereqs (once)

```bash
# 1) Start Kafka + Kafka UI
docker compose up -d    # from the project root with docker-compose.yml

# 2) Start each service (separate terminals)
mvn -pl proposal-service spring-boot:run
mvn -pl audit-service spring-boot:run
mvn -pl notification-service spring-boot:run
```

Swagger:

* Proposal:  `http://localhost:8081/swagger-ui/index.html`
* Audit:     `http://localhost:8082/swagger-ui/index.html`
* Notif:     `http://localhost:8083/swagger-ui/index.html`
  Kafka UI:    `http://localhost:8080`

> Tip: If you kept the sample `data.sql`, either remove the explicit `id=1` or bump the identity (see earlier fix). Otherwise you may hit PK conflicts on your first create.

---

# API cheat sheet

## Proposal Service ([http://localhost:8081](http://localhost:8081))

* **POST** `/api/proposals` – create DRAFT
* **GET** `/api/proposals` – list (paged, optional `status=`)
* **GET** `/api/proposals/{id}` – fetch one
* **POST** `/api/proposals/{id}/submit` – DRAFT → UNDER\_REVIEW (creates steps; optional custom chain)
* **POST** `/api/proposals/{id}/approve` – approve current step
* **POST** `/api/proposals/{id}/reject` – reject current step (finalizes REJECTED)

## Audit Service ([http://localhost:8082](http://localhost:8082))

* **GET** `/api/audit` – list all audit rows (paged)
* **GET** `/api/audit?proposalId={id}` – audit rows for one proposal

## Notification Service ([http://localhost:8083](http://localhost:8083))

* **GET** `/api/health` – simple health (for smoke test)

---

# End-to-end Test Flow (Happy Path: submit → approve → approve → approve)

### 1) Create a proposal (DRAFT)

```bash
curl -X POST http://localhost:8081/api/proposals \
  -H "Content-Type: application/json" \
  -d '{
        "title":"IPO Fund",
        "applicantName":"Riya",
        "amount":150000,
        "description":"Seed round investment"
      }'
```

**Expect (trimmed):**

```json
{
  "id": 1,
  "status": "DRAFT",
  "currentStepIndex": 0,
  "steps": []
}
```

### 2) Submit for review (creates steps)

* Default chain (from config): `PEER_REVIEW → MANAGER_APPROVAL → COMPLIANCE`

```bash
curl -X POST http://localhost:8081/api/proposals/1/submit
```

**Expect:** status `UNDER_REVIEW`, `steps` filled with 3 entries (all `PENDING`), `currentStepIndex: 0`.

> Optional: **Custom chain** (override default)

```bash
curl -X POST http://localhost:8081/api/proposals/1/submit \
  -H "Content-Type: application/json" \
  -d '["TEAM_LEAD","RISK","CFO"]'
```

### 3) Approve current step (do this once per step)

```bash
curl -X POST http://localhost:8081/api/proposals/1/approve \
  -H "Content-Type: application/json" \
  -d '{"approver":"john.doe","comments":"Looks good"}'
```

**Expect:** first step becomes `APPROVED`, `currentStepIndex` moves to next.
Repeat the **approve** call **until the last step**. On the **final approve**, overall proposal `status` becomes `APPROVED`.

> Check the current state anytime:

```bash
curl -s http://localhost:8081/api/proposals/1 | jq
```

### 4) Verify audit trail

```bash
# all audit entries (paged)
curl -s "http://localhost:8082/api/audit?page=0&size=20" | jq

# audit entries for proposal 1
curl -s "http://localhost:8082/api/audit?proposalId=1" | jq
```

You should see rows for `PROPOSAL_SUBMITTED`, each `STEP_APPROVED`, and `PROPOSAL_APPROVED`.

### 5) (Optional) Check notification logs

Look at the **notification-service** console: it logs a line per event received (simulating email/SMS).

---

# Reject Path (submit → reject)

### A) Create a second proposal

```bash
curl -X POST http://localhost:8081/api/proposals \
  -H "Content-Type: application/json" \
  -d '{
        "title":"Bond Ladder",
        "applicantName":"Anil",
        "amount":80000,
        "description":"Fixed income strategy"
      }'
```

Suppose this returned `id: 2`.

### B) Submit it

```bash
curl -X POST http://localhost:8081/api/proposals/2/submit
```

### C) Reject the current step

```bash
curl -X POST http://localhost:8081/api/proposals/2/reject \
  -H "Content-Type: application/json" \
  -d '{"approver":"qa.user","comments":"Insufficient documentation"}'
```

**Expect:** proposal `status` becomes `REJECTED`.

### D) Verify audit trail for #2

```bash
curl -s "http://localhost:8082/api/audit?proposalId=2" | jq
```

Includes `PROPOSAL_SUBMITTED` and `PROPOSAL_REJECTED`.

---

# Listing & Filtering

### List all proposals (paged)

```bash
curl -s "http://localhost:8081/api/proposals?page=0&size=10&sort=id,desc" | jq
```

### List by status

```bash
curl -s "http://localhost:8081/api/proposals?status=UNDER_REVIEW&page=0&size=10" | jq
```

Valid statuses: `DRAFT`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`.

---

# Sanity + Troubleshooting

* **Swagger** not loading? Ensure dependency `springdoc-openapi-starter-webmvc-ui:2.6.0` is present and the app is up on the expected port.
* **“Table not found” on startup**: set

  ```yaml
  spring:
    jpa:
      hibernate:
        ddl-auto: update
      defer-datasource-initialization: true
    sql:
      init:
        mode: always
  ```

  or rename `data.sql` → `import.sql`.
* **Primary key duplicate** after seeding: remove `id` from seed insert **or** `ALTER TABLE proposals ALTER COLUMN id RESTART WITH 2;`.
* **No audit rows**: make sure **audit-service** is running and connected to the same Kafka (`localhost:9092`), and check Kafka UI → `proposal-events`.
* **DLT filling up**: a consumer is failing persistently. Fix the bug/data, then replay (optional DLT replayer).

---

# One-liner “full happy path” (for quick smoke)

```bash
# create
curl -s -X POST http://localhost:8081/api/proposals -H "Content-Type: application/json" \
 -d '{"title":"IPO Fund","applicantName":"Riya","amount":150000,"description":"New fund"}' | jq

# submit
curl -s -X POST http://localhost:8081/api/proposals/1/submit | jq

# approve step 1
curl -s -X POST http://localhost:8081/api/proposals/1/approve -H "Content-Type: application/json" \
 -d '{"approver":"john.doe","comments":"OK"}' | jq

# approve step 2
curl -s -X POST http://localhost:8081/api/proposals/1/approve -H "Content-Type: application/json" \
 -d '{"approver":"jane.manager","comments":"Proceed"}' | jq

# approve step 3 (final)
curl -s -X POST http://localhost:8081/api/proposals/1/approve -H "Content-Type: application/json" \
 -d '{"approver":"ravi.compliance","comments":"Compliant"}' | jq

# audit for proposal 1
curl -s "http://localhost:8082/api/audit?proposalId=1" | jq
```

If you want, I can generate a tiny **Postman collection** or a **bash script** that runs the whole scenario end-to-end with pauses and prints out the key fields.

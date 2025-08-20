Awesome—let’s turn **Uniredit (UC): Investment Proposal Management** into your study vehicle for **Java 21 + Spring Boot + Kafka**. Here’s a tight, do-by-do plan, with code tweaks and practice prompts that map directly to your app.

---

# 7-day plan (hands-on first)

## Day 1 — Java 21 essentials in your codebase

**Targets**

* Virtual threads, pattern-matching `switch`, records, Sequenced Collections.

**Do**

1. **Enable virtual threads** in all services:

```yaml
# application.yml
spring.threads.virtual.enabled: true
```

2. **Use records** for immutable DTOs/events:

```java
// ipm-common
public record ProposalEvent(
  String id,
  ProposalEventType type,
  Long proposalId,
  Map<String,Object> payload,
  java.time.OffsetDateTime at
) {}
```

3. **Pattern matching switch** in consumers:

```java
switch (event.type()) {
  case PROPOSAL_SUBMITTED -> handleSubmit(event);
  case STEP_APPROVED, PROPOSAL_APPROVED -> handleApprove(event);
  case PROPOSAL_REJECTED -> handleReject(event);
}
```

4. **Sequenced Collections** (Java 21): if you keep recent audits in memory anywhere:

```java
SequencedMap<Long, Proposal> cache = new java.util.LinkedHashMap<>();
cache.putFirst(p.getId(), p);  // newest first
```

## Day 2 — Spring Boot 3.x mastery (with your endpoints)

**Targets**

* `@ConfigurationProperties`, validation, error handling, profiles.

**Do**

* Convert hardcoded defaults (approval chain, topic names) into **typed config**:

```java
@ConfigurationProperties(prefix="uc.workflow")
public record WorkflowProps(List<String> defaultChain) {}
// application.yml
uc:
  workflow:
    defaultChain: [PEER_REVIEW, MANAGER_APPROVAL, COMPLIANCE]
```

* Review your `GlobalExceptionHandler` returns (400/404) and add Bean Validation on DTOs.
* Add **`actuator`** with health + metrics:

```yaml
management.endpoints.web.exposure.include: health,info,metrics
```

## Day 3 — Kafka fundamentals applied to UC

**Targets**

* Keys/ordering, producer acks/idempotence, consumers, DLT, headers.

**Do**

1. **Producer hardening** (proposal-service):

```yaml
spring.kafka.producer.properties:
  acks: all
  enable.idempotence: true
  compression.type: zstd
  linger.ms: 10
  batch.size: 65536
```

2. **Key by proposalId** (ordering per proposal) and add correlation header:

```java
var msg = org.springframework.messaging.support.MessageBuilder.withPayload(event)
  .setHeader(org.springframework.kafka.support.KafkaHeaders.TOPIC, "proposal-events")
  .setHeader(org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY, String.valueOf(event.proposalId()))
  .setHeader("x-correlation-id", java.util.UUID.randomUUID().toString())
  .build();
kafkaTemplate.send(msg).whenComplete((m, ex) -> { /* inspect partition/offset in debug */ });
```

3. **DLT** in consumers (audit/notification):

```java
var recoverer = new DeadLetterPublishingRecoverer(dltTemplate,
  (rec, ex) -> new org.apache.kafka.common.TopicPartition(rec.topic()+".DLT", rec.partition()));
factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L)));
```

4. **Idempotent consumers** (effectively-once):

* `audit-service`: unique index on `eventId` (or whole event `id`) and ignore duplicates.
* `notification-service`: “sent” marker table by `eventId`.

## Day 4 — Testing (unit, slice, integration)

**Targets**

* Spring test slices, Testcontainers Kafka, EmbeddedKafka.

**Do**

1. **Controller slice**:

```java
@WebMvcTest(ProposalController.class)
class ProposalControllerTest { /* MockMvc + validation paths */ }
```

2. **JPA slice**:

```java
@DataJpaTest
class ProposalRepositoryTest { /* derived queries, constraints */ }
```

3. **Kafka integration** (pick one):

* **Testcontainers (recommended)**:

```java
@Testcontainers
class KafkaFlowIT {
  @Container static org.testcontainers.containers.KafkaContainer kafka =
      new org.testcontainers.containers.KafkaContainer("confluentinc/cp-kafka:7.6.1");
  // bootstrap Spring with kafka.getBootstrapServers(), verify produce→consume
}
```

* **@EmbeddedKafka** for fast local.

4. **DLT test**: make consumer throw for `PROPOSAL_SUBMITTED`, assert record appears in `.DLT`.

## Day 5 — Data & migrations; reliability patterns

**Targets**

* Flyway/Liquibase, Outbox vs Transactions, graceful error patterns.

**Do**

* Add **Flyway** and move schema/seed from `data.sql` to versioned migrations.
* Sketch **Transactional Outbox** (simple table + scheduled publisher). You can keep it disabled but know how to explain it.

## Day 6 — Observability & performance

**Targets**

* Lag, metrics, logs, thread model.

**Do**

* Expose Prometheus metrics; look at `kafka.consumer.records-consumed-total`, process timings.
* Log headers in consumers (correlation id) and verify per-proposal partition stickiness.
* Try **concurrency=1** vs `=2` while watching Kafka UI lag.

## Day 7 — Interview drill (whiteboard + live)

* Dry-run the **happy path** and **reject path** with debugger breakpoints in:

    * `ApprovalWorkflowService#submit/approve/reject`
    * `EventPublisher#publish`
    * Consumers’ `onEvent`
* Practice a 3-minute “system overview” + 3 design trade-offs (below).

---

# Interview-ready talking points (with UC context)

**System overview (60–90 sec)**

* REST writes proposal to DB, emits **domain events** (`proposal-events`) keyed by **proposalId** for **per-proposal ordering**.
* **Audit** and **Notification** consume in separate **groups** (pub/sub fan-out).
* **Retries + DLT** protect flow; consumers are **idempotent**.
* Observability via **Kafka UI + Actuator metrics**.

**Why key by proposalId?**
Ordering within a single proposal’s lifecycle (submit → step approvals → final state) must be strict; different proposals can process in parallel.

**Exactly-once vs effectively-once**
We run at-least-once + **idempotent consumers** (+ idempotent producer) → effectively-once. For strict EOS, consider Kafka transactions, but **Transactional Outbox** is simpler for CRUD microservices.

**Handling failures**
`DefaultErrorHandler` retries with backoff; after N attempts, **DLT** gets the record. We inspect & **replay** post-fix. Idempotency keeps replays safe.

**Scaling**
Increase **partitions**; scale consumer instances; keep **concurrency ≤ assigned partitions**. Watch **lag** and GC.

**Schema evolution**
Start JSON with a **version** field; move to **Avro + Schema Registry (BACKWARD)** as contracts stabilize.

**Java 21**
Virtual threads reduce thread-per-request overhead (great for IO-bound REST). Pattern-matching `switch` cleans event branching; records make DTOs/audit payloads concise.

---

# Quick labs you can demo (15–20 min each)

1. **See ordering:** create two proposals; interleave actions; show each proposal’s events stay ordered (same partition in Kafka UI).
2. **Trigger DLT:** make `audit-service` throw on `PROPOSAL_SUBMITTED`; watch retries → DLT; fix & replay.
3. **Virtual threads on/off:** toggle `spring.threads.virtual.enabled`, hit `/api/proposals` with 200 concurrent requests (JMeter or `hey`), show thread counts and throughput.

---

# Rapid command sheet

```bash
# run services (from IDE or CLI)
mvn -pl proposal-service spring-boot:run
mvn -pl audit-service spring-boot:run
mvn -pl notification-service spring-boot:run

# happy path
curl -X POST :8081/api/proposals -H "Content-Type: application/json" \
 -d '{"title":"IPO Fund","applicantName":"Riya","amount":150000,"description":"New fund"}'
curl -X POST :8081/api/proposals/1/submit
curl -X POST :8081/api/proposals/1/approve -H "Content-Type: application/json" \
 -d '{"approver":"john.doe","comments":"OK"}'

# audit
curl :8082/api/audit?proposalId=1 | jq

# Kafka tail (from docker)
docker compose exec kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 --topic proposal-events \
  --from-beginning --property print.key=true --property key.separator=" : "
```

---

## Want me to turn this into a **mock interview pack**?

I can bundle: (1) a one-page architecture slide, (2) 20 targeted Q\&A, (3) 3 whiteboard prompts with expected answers, and (4) 2 integration tests (Testcontainers Kafka + DLT).

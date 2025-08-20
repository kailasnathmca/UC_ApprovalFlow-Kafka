Here’s a **comprehensive set of Kafka interview questions with detailed answers**, all framed in the **Investment Proposal** system we built (create → submit → multi-step approve/reject), where `proposal-service` publishes `ProposalEvent`s and `audit-service` / `notification-service` consume them.

---

# A. Fundamentals & Design

### 1) What role does Kafka play in the Investment Proposal system?

**Answer:** Kafka is the **event backbone**. `proposal-service` publishes domain events (e.g., `PROPOSAL_SUBMITTED`, `STEP_APPROVED`, `PROPOSAL_REJECTED`) to the `proposal-events` topic. Multiple services subscribe in **their own consumer groups**—`audit-service` persists an immutable audit trail; `notification-service` triggers “email/SMS” side effects. Kafka decouples services, preserves **ordered history per proposal** via keys, and lets us **scale** consumers independently.

---

### 2) Why event-driven instead of synchronous RPC between services?

**Answer:**

* **Decoupling:** New downstream services (e.g., analytics) subscribe without changing `proposal-service`.
* **Reliability:** Kafka buffers spikes; consumers process at their pace.
* **Auditability:** Events are immutable facts we can replay.
* **Ordering per proposal:** Using `proposalId` as message key guarantees per-proposal ordering.

---

# B. Topics, Partitions, Keys & Ordering

### 3) How do you ensure events for a single proposal are processed in order?

**Answer:** Use **`proposalId` as the Kafka key** when producing, so all its events go to the **same partition**. Kafka guarantees **ordering within a partition**.

```java
template.send("proposal-events", String.valueOf(ev.getProposalId()), ev);
```

---

### 4) How many partitions should `proposal-events` have?

**Answer:** At least the **max parallelism required per consumer group**. For dev: 3; for prod: size based on expected throughput and peak concurrency (e.g., number of active consumer instances × concurrency). Beware **hot keys** (one proposal with huge event volume) when sizing.

---

### 5) What happens if you change the number of partitions?

**Answer:**

* **Increasing partitions** changes partitioner results for **new** messages (if using hash of key), so the distribution across partitions changes going forward (past ordering within partitions is preserved in the log).
* **Decreasing partitions** is not supported.
* If stateful processing depends on partitioning, plan migration carefully.

---

# C. Serialization & Schema Evolution

### 6) Why JSON serialization here, and when would you choose Avro/Protobuf?

**Answer:** JSON is simple and human-readable—great for quick start. Avro/Proto + Schema Registry is preferred in prod for **strong contracts**, **compatibility rules** (backward/forward), and **smaller payloads**. In UC, start with JSON, add a **`version`** field, then move to Avro when events stabilize.

---

### 7) How do you evolve the `ProposalEvent` safely?

**Answer:**

* **Additive changes** only (new optional fields).
* Don’t rename or remove existing fields.
* Use a **`version`** and default behaviors in consumers.
* With Schema Registry, set compatibility to **BACKWARD**.

---

# D. Producers: Acks, Idempotence, Batching

### 8) What producer settings ensure durability?

**Answer:**

* `acks=all` (+ proper broker `min.insync.replicas`)
* **Idempotence**: `enable.idempotence=true` to avoid duplicates on retries
* **Compression & batching** for throughput: `compression.type=zstd`, `linger.ms≈5–20ms`, `batch.size≈32–128KB`

```yaml
spring.kafka.producer.properties:
  acks: all
  enable.idempotence: true
  compression.type: zstd
  linger.ms: 10
  batch.size: 65536
```

---

### 9) How do you choose the message key?

**Answer:** By **business affinity to ordering** and **parallelism**. In UC, the natural key is `proposalId`, because business logic requires ordered transitions per proposal.

---

# E. Consumers: Groups, Offsets, Concurrency, Backpressure

### 10) Why separate consumer groups for audit and notification?

**Answer:** Each **group receives every message once**. Separate groups (`audit-service`, `notification-service`) implement **fan-out**—each service processes **all** events independently.

---

### 11) How do you scale consumers safely?

**Answer:**

* Match **listener concurrency** to **assigned partitions** (effective upper bound).
* Run multiple instances; Kafka **rebalances** partitions among instances.
* Keep handlers **idempotent**, fast, and non-blocking (delegate heavy I/O asynchronously if needed).

---

### 12) How do you handle spikes (backpressure) on consumers?

**Answer:**

* Tune `max.poll.records`, `fetch.min.bytes`, `fetch.max.wait.ms`.
* Enable **batch listeners** if appropriate.
* Temporarily **pause** containers via `KafkaListenerEndpointRegistry` and **resume** when healthy.

---

# F. Error Handling, Retries & Dead Letter Topics (DLT)

### 13) How are consumer failures handled?

**Answer:** With Spring Kafka’s `DefaultErrorHandler` (or `CommonErrorHandler`): **retry** with backoff, then publish the failed record to a **DLT** (`proposal-events.DLT`) via `DeadLetterPublishingRecoverer`. Partition is preserved.

```java
var recoverer = new DeadLetterPublishingRecoverer(dltTemplate,
  (rec, ex) -> new TopicPartition(rec.topic()+".DLT", rec.partition()));
factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L)));
```

---

### 14) How do you reprocess DLT messages?

**Answer:** Add a **controlled replayer** that reads `proposal-events.DLT`, optionally fixes payloads, and republishes to `proposal-events`. Use allowlists/feature flags to avoid loops.

---

# G. Delivery Semantics & Idempotency

### 15) Does the system guarantee exactly-once processing?

**Answer:** By default, **at-least-once**. To achieve **effectively-once**:

* **Idempotent consumers** (e.g., `audit-service` uses unique `eventId` constraint and no-op on duplicates; `notification-service` keeps a “sent” marker per `eventId`).
* **Idempotent producer** (`enable.idempotence=true`).
  **Exactly-once** is possible with Kafka transactions/EOS v2 but is heavier operationally.

---

### 16) How do you implement idempotency in `audit-service`?

**Answer:** Unique index on `eventId`; dedup by catching duplicate key violation:

```java
@Entity @Table(uniqueConstraints = @UniqueConstraint(columnNames = "eventId"))
class AuditEntry { /* eventId, proposalId, ... */ }

try { repo.save(entity); }
catch (DataIntegrityViolationException ignored) { /* duplicate -> no-op */ }
```

---

# H. DB + Kafka Atomicity: Transactions vs Outbox

### 17) How do you avoid “DB updated but event not published” (dual-write) problems?

**Answer:** Use **Transactional Outbox**: write both the DB change and an **outbox row** in the same DB transaction. A background publisher publishes outbox rows to Kafka and marks them as SENT. It’s simpler to run in prod than Kafka/JPA distributed transactions.

---

### 18) When would you use Kafka Transactions instead?

**Answer:** If you need to atomically publish to **multiple Kafka topics/partitions** and commit **consumer offsets** as part of a single boundary (e.g., stream processing). In CRUD + outbox patterns, outbox is usually easier.

---

# I. Retention, Compaction & Replays

### 19) What retention policies do you set?

**Answer:**

* `proposal-events`: **time-based retention** (e.g., 14–30 days) for replay/debug.
* `.DLT`: longer retention to investigate.
* `audit-logs`: moderate/long; it’s small.
  **Compaction** is **not** used for immutable event logs but is suitable for a “latest state” topic (e.g., `proposal-state`).

---

### 20) How do you replay a corrupted consumer?

**Answer:** Reset the consumer group offsets (e.g., to **earliest**) after fixing the bug, or use a **new groupId** for a fresh read. For targeted recovery, use a **DLT replayer** that resubmits only the failing records.

---

# J. Kafka Streams (Stateful Processing)

### 21) How would you compute proposal approval SLA in real time?

**Answer:** Use Kafka Streams to join `PROPOSAL_SUBMITTED` with `PROPOSAL_APPROVED` per key, compute duration, and write to `proposal-insights`. Enable EOS v2 (`processing.guarantee=exactly_once_v2`) if you’re updating state stores.

---

# K. Kafka Connect (Ingest & Sink)

### 22) How do you ingest legacy proposal changes and export audits?

**Answer:**

* **Ingest:** Debezium CDC connector reads `proposals` table changes and writes to a Kafka topic (e.g., `cdc.uc.proposals`).
* **Sink:** S3 Sink pushes `audit-logs` or `proposal-events` to S3 (data lake) for compliance/analytics.

---

# L. Security & Multi-Tenancy

### 23) How do you secure the cluster?

**Answer:** Use **SASL\_SSL** for auth/TLS and **ACLs** per service principal:

* `proposal-service`: Producer to `proposal-events`, `audit-logs`
* `audit-service`, `notification-service`: Consumer of `proposal-events`
  Rotate keys in a secrets store; disable auto topic creation; manage topics/ACLs via IaC.

---

### 24) How do you isolate data for multiple BUs/tenants?

**Answer:**

* Add a **tenant header** (e.g., `x-tenant: UC-INVESTMENTS`) and enforce it in consumers, **or**
* Use **per-tenant topics** (easier for ACLs and quotas), e.g., `proposal-events.uc`, `proposal-events.asset-mgmt`.

---

# M. Observability & Operations

### 25) How do you monitor consumer lag and health?

**Answer:**

* **Kafka UI** and `kafka-consumer-groups --describe` for lag.
* **Micrometer/Prometheus** in each service: message rates, processing latency, error counts, DLT counts.
* Alert on **sustained lag**, **DLT growth**, and **rebalance thrashing**.

---

### 26) How do you test Kafka flows?

**Answer:**

* **Testcontainers** to spin real Kafka in integration tests.
* **@EmbeddedKafka** for fast Spring tests.
* Contract tests for **event schemas** (JSON/Avro).
* Load tests for throughput (vary producer batching + consumer concurrency).

---

# N. Rebalances, Seeks, Batch, Pause/Resume

### 27) What happens during a rebalance and how to make it safe?

**Answer:** Kafka reassigns partitions when instances join/leave. Ensure listeners are **idempotent**, keep **processing fast**, and avoid long blocking calls. Optionally implement `ConsumerAwareRebalanceListener` for logging and coordinated shutdown.

---

### 28) How do you batch-process events?

**Answer:** Enable batch mode on the listener container and consume `List<ProposalEvent>`. Use when per-record overhead (DB roundtrips) dominates.

---

### 29) How do you pause/resume consumption under pressure?

**Answer:** Use `KafkaListenerEndpointRegistry` to **pause** listener containers (e.g., when DB is degraded) and **resume** once healthy; prevents DLT storms.

---

# O. Practical Troubleshooting

### 30) Why would events land in DLT immediately?

**Answer:** A consistent exception inside the listener (e.g., JSON schema mismatch, DB constraint, NPE). Fix code or data, then **replay DLT**. Validate trusted packages for JSON deserializer and ensure your event class path matches.

---

### 31) You see duplicates in `audit-service`. Why?

**Answer:** At-least-once delivery + consumer restarts or redelivery on failure. Fix by **idempotent writes** (unique `eventId`). Ensure producer idempotence is on to reduce duplicates originating from producer retries.

---

### 32) Consumers are slow; lag grows. What do you change?

**Answer:**

* Increase **partitions** and/or **consumer instances**.
* Tune **concurrency** and **max.poll.records**.
* Optimize DB writes (batching, connection pool).
* Offload heavy I/O; consider **batch listener**.
* Check GC, CPU, and network.

---

# P. Code Snippets (UC-specific)

**Producer with headers (correlation & tenant)**

```java
Message<ProposalEvent> msg = MessageBuilder.withPayload(ev)
  .setHeader(KafkaHeaders.TOPIC, "proposal-events")
  .setHeader(KafkaHeaders.MESSAGE_KEY, String.valueOf(ev.getProposalId()))
  .setHeader("x-correlation-id", corrId)
  .setHeader("x-tenant", "UC")
  .build();
kafkaTemplate.send(msg);
```

**Idempotent audit consumer**

```java
@KafkaListener(topics="proposal-events", groupId="audit-service")
@Transactional
public void onEvent(ProposalEvent ev) {
  try { repo.save(map(ev)); } // eventId unique column
  catch (DataIntegrityViolationException ignored) { /* duplicate */ }
}
```

**Error handler → DLT**

```java
var recoverer = new DeadLetterPublishingRecoverer(dltTemplate,
  (r, ex) -> new TopicPartition(r.topic()+".DLT", r.partition()));
factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L)));
```

**DLT replayer**

```java
@KafkaListener(topics="proposal-events.DLT", groupId="audit-replayer")
public void reprocess(ProposalEvent ev, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
  kafkaTemplate.send("proposal-events", key, ev);
}
```

**Streams app (SLA)**

```java
// join SUBMITTED with APPROVED per proposalId, emit durations to proposal-insights
```

---

# Q. Rapid-fire “Why” answers (frequent interview checks)

* **Why key by proposalId?** Ordering per proposal; parallelism across proposals.
* **Why DLT?** Retain failing records for diagnosis and safe replay.
* **Why idempotent consumers?** At-least-once delivery; avoid duplicate side effects.
* **Why Outbox?** Atomic DB+Kafka change without distributed transactions.
* **Why multiple consumer groups?** Pub/sub fan-out; each service processes all events.
* **Why compression + linger?** Better throughput and network efficiency.
* **Why Schema Registry (later)?** Enforce contracts and compat rules at scale.
* **Why monitor lag?** Early signal of throughput issues or consumer health problems.

---

If you want, I can turn this into a **printable cheat-sheet** or a **mock interview deck** with sample whiteboard questions + expected code you’d write live (producer config, idempotent consumer, DLT handler, and an Outbox entity).

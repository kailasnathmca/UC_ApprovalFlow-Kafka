Absolutely—here’s a focused, step-by-step way to **debug the Kafka flow in IntelliJ** so you can see events leaving `proposal-service` and being handled by `audit-service` / `notification-service`.

---
# When you POST to /api/proposals in proposal-service, go to Kafka UI → proposal-events topic → you’ll see the JSON event appear. Then check audit-logs to see how audit-service consumed it.

# 0) Prep

1. **Start Kafka stack**

```bash
docker compose up -d
```

2. **Use host bootstrap from IDE**
   Ensure all three services have:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

(Use `kafka:29092` only from inside containers.)

3. **Easier debugging knobs (dev only)**

```yaml
spring:
  kafka:
    listener:
      concurrency: 1        # one thread per service = predictable stepping
logging:
  level:
    org.springframework.kafka: DEBUG
    org.apache.kafka.clients: INFO
```

---

# 1) IntelliJ run/debug setups

### Option A — Run directly from IDE (simplest)

* In each service, right-click `Application.java` → **Debug ‘Application’**.
* Create **three** Application configs (proposal, audit, notification).
* Optional: a **Compound** config to start all three together in Debug.

### Option B — If you must run via Maven

* Start with remote debug agent:

```bash
mvn -pl proposal-service spring-boot:run \
  -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

* In IntelliJ: **Run → Edit Configurations → + → Remote JVM Debug**, host `localhost`, port `5005`, then **Debug**.

---

# 2) Where to put breakpoints (exact files)

### Producer path (proposal-service)

* `controller/ProposalController#create` (POST /api/proposals)
* `controller/ApprovalController#submit/#approve/#reject`
* `service/ApprovalWorkflowService#submit/#approve/#reject`  ← state machine
* `kafka/EventPublisher#publish`  ← **critical**: message sent here

> **Pro tip:** Wrap send with a callback so you can break **when the broker acks**:

```java
kafkaTemplate.send("proposal-events", String.valueOf(ev.getProposalId()), ev)
  .whenComplete((meta, ex) -> {
    // put a breakpoint here
    // inspect meta.topic(), meta.partition(), meta.offset()
  });
```

### Consumer path

* `audit-service/kafka/ProposalEventListener#onEvent`  ← persists audit row
* `notification-service/kafka/NotificationListener#onEvent`  ← logs “would notify”
* (If you added DLT) your error handler or DLT replayer.

### Error/edge points (optional)

* `config/KafkaConfig` where you build the `DefaultErrorHandler` → set a BP to see retries
* `repository` save methods to verify DB writes

---

# 3) Use **conditional breakpoints** (makes life easier)

In IntelliJ, Alt+Click the breakpoint to add conditions, e.g.:

* Only break for one proposal:

```java
ev.getProposalId() == 1L
```

* Only break on final approval:

```java
ev.getType().name().contains("APPROVED")
```

* Only break on submit path in service:

```java
proposal.getStatus().name().equals("UNDER_REVIEW")
```

---

# 4) Drive the flow while paused

With all three services in **Debug**, run these in a terminal:

```bash
# Create proposal (hits ProposalController#create; step into service/repo)
curl -X POST http://localhost:8081/api/proposals \
  -H "Content-Type: application/json" \
  -d '{"title":"IPO Fund","applicantName":"Riya","amount":150000,"description":"New fund"}'

# Submit (hits ApprovalWorkflowService#submit, then EventPublisher#publish)
curl -X POST http://localhost:8081/api/proposals/1/submit

# Approve step 1..N (each triggers publish → consumers)
curl -X POST http://localhost:8081/api/proposals/1/approve \
  -H "Content-Type: application/json" \
  -d '{"approver":"john.doe","comments":"OK"}'
```

**What you should see, step-by-step**

1. Break in `ApprovalWorkflowService#submit/approve` → inspect entity changes.
2. Step to `EventPublisher#publish` → **Inspect**: key (`proposalId`), payload, headers.
3. If you added `.whenComplete(...)` callback: break there → check `partition`/`offset`.
4. Switch to **audit-service** debug tab → break in `ProposalEventListener#onEvent` → inspect the same `proposalId`, `type`, contents.
5. (Optional) Do the same in **notification-service**.

---

# 5) Inspect headers & keys (handy during debug)

**Producer (proposal-service):**

```java
Message<ProposalEvent> msg = MessageBuilder.withPayload(ev)
  .setHeader(org.springframework.kafka.support.KafkaHeaders.TOPIC, "proposal-events")
  .setHeader(org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY, String.valueOf(ev.getProposalId()))
  .setHeader("x-correlation-id", java.util.UUID.randomUUID().toString())
  .build();
kafkaTemplate.send(msg);
```

**Consumer (audit-service):**

```java
@KafkaListener(topics = "proposal-events", groupId = "audit-service")
public void onEvent(ProposalEvent ev,
   @Header("x-correlation-id") Optional<String> corrId,
   @Header(org.springframework.kafka.support.KafkaHeaders.RECEIVED_PARTITION_ID) int part,
   @Header(org.springframework.kafka.support.KafkaHeaders.OFFSET) long offset) {
  // put a breakpoint: inspect corrId/part/offset
}
```

---

# 6) Control the firehose while debugging

* **Single-thread consumption:** `spring.kafka.listener.concurrency: 1`
* **Pause/resume** a listener container at runtime:

```java
@Autowired org.springframework.kafka.config.KafkaListenerEndpointRegistry registry;

// In a debug-only endpoint or bean:
registry.getListenerContainers().forEach(c -> c.pause());
// ... resume later:
registry.getListenerContainers().forEach(c -> c.resume());
```

* **Trigger a DLT message** (to see error path): temporarily throw in a consumer:

```java
if (ev.getType().name().equals("PROPOSAL_SUBMITTED"))
  throw new RuntimeException("simulate failure");
```

Then watch it land in `proposal-events.DLT` (and step through your error handler).

---

# 7) Common “why am I not hitting breakpoints?” fixes

* **Wrong bootstrap** (host vs container). From IDE, it must be `localhost:9092`.
* **Topic missing** → create `proposal-events`, `proposal-events.DLT`, `audit-logs` (partitions 3/3/1).
* **Listener not assigned** (all partitions taken by another instance) → stop duplicate instances or use different groupId.
* **Concurrency > partitions** → some threads idle; keep `concurrency: 1` while debugging.
* **Fast consumer finishes before you switch tabs** → add a tiny `Thread.sleep(200)` temporarily (dev only) or rely on breakpoints earlier in the handler.

---

# 8) Lightweight visibility without stepping

* **Kafka UI** (from docker compose) → watch `proposal-events` and `.DLT` in real time.
* **CLI tail**:

```bash
docker compose exec kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 \
  --topic proposal-events --from-beginning \
  --property print.key=true --property key.separator=" : "
```

---

## Quick checklist

* [ ] Three IDE debug configs (proposal, audit, notification) running.
* [ ] Breakpoints: service methods + `EventPublisher#publish` + consumers.
* [ ] Conditional BPs for specific `proposalId` or event types.
* [ ] Concurrency set to 1; topics exist; using `localhost:9092` from IDE.
* [ ] Drive with curl; step through producer → broker ack → consumers.

If you paste a screenshot of your IntelliJ run configs or tell me which breakpoints aren’t firing, I’ll point out the exact knob to flip.

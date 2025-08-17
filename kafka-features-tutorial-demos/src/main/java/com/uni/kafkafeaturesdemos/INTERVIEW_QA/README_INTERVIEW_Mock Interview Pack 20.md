Awesome — here’s your **Mock Interview Pack** for **Uniredit (UC): Investment Proposal Management** covering Kafka (and touching Spring Boot + Java 21 where helpful).

---

# 1) One-page architecture “slide” (text)

```
UC — Investment Proposal Management (Event-driven)

[ Client / UI ]
      │  (HTTP create / submit / approve / reject)
      ▼
[ Proposal Service ] — REST + JPA
      ├─ writes DB state
      └─ PUBLISHES domain events (ProposalEvent, key = proposalId)
                │
=================  KAFKA (event backbone)  =================
  Topics:
   • proposal-events (3 partitions) — JSON domain events
   • proposal-events.DLT (3)        — dead letters (failures)
   • audit-logs (1)                 — human-readable audit lines
============================================================
     │             (pub/sub fan-out: 1 msg → many services)
     ├───────────────┐
     ▼               ▼
[ Audit Service ]   [ Notification Service ]
  - consumes proposal-events      - consumes proposal-events
  - persists AuditEntry (idempotent)     - prints/sends notifications
  - query via /api/audit
            │
            └─ Optional analytics / Streams (SLA, stuck proposals)
```

**Key ideas you can say out loud (30s):**
Kafka is the **event backbone**. We **key by `proposalId`** so each proposal’s lifecycle is strictly ordered on one partition, while multiple proposals run in parallel. Each downstream service has its **own consumer group** (fan-out). Failures go to **`.DLT`** for inspection/replay. Consumers are **idempotent** to get effectively-once processing.

---

# 2) 20 interview Q\&A (UC-context, concise + precise)

1. **What role does Kafka play here?**
   Event backbone decoupling services: Proposal emits domain events; Audit/Notif consume in separate groups; ordering per proposal via key.

2. **Why key by `proposalId`?**
   Guarantees **per-proposal ordering** (partition stickiness) while allowing cross-proposal parallelism.

3. **How many partitions for `proposal-events`?**
   ≥ target parallelism per consumer group (dev 3; prod based on throughput). Beware hot keys; monitor skew.

4. **Producer durability settings?**
   `acks=all`, `enable.idempotence=true`, reasonable batching (`linger.ms`, `batch.size`), compression (`zstd`).

5. **What’s your delivery guarantee?**
   At-least-once + idempotent consumers (dedupe on `eventId`) → **effectively-once**. EOSv2 possible but heavier.

6. **How do you make consumers idempotent?**
   DB unique on `eventId`; on duplicate key → no-op. Notifications store “sent” marker per `(eventId, channel)`.

7. **What is DLT and why use it?**
   Dead Letter Topic for messages that fail after retries; isolates poison records; supports diagnosis + replay.

8. **How do you replay from DLT safely?**
   Dedicated replayer (separate group) with filters/feature flags; republish to `proposal-events`; idempotent handlers keep it safe.

9. **JSON vs Avro/Proto?**
   Start JSON (fast, human-readable), include `version`. For scale, **Schema Registry** with Avro/Proto (compat = BACKWARD).

10. **How do you evolve `ProposalEvent`?**
    Additive changes only; don’t break existing fields; use defaults in consumers; enforce compat in registry.

11. **Consumer scaling strategy?**
    Increase instances and concurrency (≤ partitions). Watch **lag**; ensure handlers are fast/non-blocking.

12. **Backpressure controls?**
    Tune `max.poll.records`, `fetch.min.bytes`, batch listeners, temporary `pause()`/`resume()` on listener containers.

13. **Rebalance effects?**
    Partitions re-assigned; maintain idempotency; short processing time; optional `ConsumerAwareRebalanceListener` for logs.

14. **Transactional Outbox vs Kafka Transactions?**
    Outbox = simpler operationally for CRUD microservices (DB change + outbox row in one DB tx, async publisher). Kafka tx = strong guarantees but more moving parts.

15. **Retention policy choices?**
    `proposal-events`: time retention (e.g., 14–30 days). `.DLT`: longer. `audit-logs`: moderate/long. Compaction only for “latest state” topics, not immutable events.

16. **Observability?**
    Kafka UI + `kafka-consumer-groups` for lag; Micrometer/Prometheus for rates/latencies/errors; alert on lag & DLT growth.

17. **Headers & tracing?**
    Add `x-correlation-id`, tenant headers; propagate/ log in consumers; helps stitch request→event→consumer.

18. **Hot key scenario?**
    One proposal spamming events skews one partition. Mitigate by throttling, batching side-effects, or splitting event types (rarely needed).

19. **Security in prod?**
    SASL\_SSL, TLS, ACLs per service; disable auto-create; manage topics/ACLs via IaC; secret rotation.

20. **Kafka Streams use-case here?**
    Compute approval SLA: join SUBMITTED and APPROVED by key; emit durations to `proposal-insights` for dashboards.

---

# 3) Whiteboard prompts (with high-level answers)

### Prompt A — Design event model & topics

* **Ask:** Define your event(s), keying, topics, and consumer groups.
* **Answer:** `ProposalEvent{id, type, proposalId, payload, at, version}`; key=`proposalId`; topics: `proposal-events`, `proposal-events.DLT`, `audit-logs`; groups: `audit-service`, `notification-service`; partitions=3; ordering per proposal; DLT for failures.

### Prompt B — Guarantee ordering & scale

* **Ask:** How to keep per-proposal ordering while handling thousands of proposals?
* **Answer:** Hash partitioning by `proposalId`; enough partitions for target parallelism; consumers scaled to ≤ partitions; idempotent handlers; monitor partition skew.

### Prompt C — Failure strategy & replay

* **Ask:** A consumer keeps failing on certain records — design a recovery path.
* **Answer:** Retries with backoff → DLT; investigate schema/data; deploy fix; **controlled replayer** (filter by time/key/type) republish to `proposal-events`; idempotency ensures safe reprocessing.

---

# 4) Two integration tests (ready-to-drop examples)

> Add these test dependencies (root or service `pom.xml`):

```xml
<dependency>
  <groupId>org.testcontainers</groupId><artifactId>kafka</artifactId><version>1.20.1</version><scope>test</scope>
</dependency>
<dependency>
  <groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><version>5.10.2</version><scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework.kafka</groupId><artifactId>spring-kafka-test</artifactId><scope>test</scope>
</dependency>
```

### Test 1 — **Publish→Consume flow** with Testcontainers (no Spring context)

```java
// src/test/java/com/uc/kafka/KafkaFlowIT.java
package com.uc.kafka;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class KafkaFlowIT {

  @Container
  static KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.6.1");

  @Test
  void publishThenConsumeProposalEvent() throws Exception {
    String bootstrap = kafka.getBootstrapServers();

    // 1) Create topic
    try (AdminClient admin = AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap))) {
      admin.createTopics(List.of(new NewTopic("proposal-events", 3, (short)1))).all().get();
    }

    // 2) Producer (key=proposalId, value=JSON string)
    try (Producer<String, String> producer = new KafkaProducer<>(
        Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
               ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
               ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class))) {

      String json = """
        {"id":"ev-1","type":"PROPOSAL_SUBMITTED","proposalId":42,"payload":{"chain":"PEER_REVIEW"},"at":"2025-08-17T00:00:00Z","version":1}
        """;
      producer.send(new ProducerRecord<>("proposal-events", "42", json)).get();
    }

    // 3) Consumer (same topic)
    try (Consumer<String, String> consumer = new KafkaConsumer<>(
        Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
               ConsumerConfig.GROUP_ID_CONFIG, "test-group",
               ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
               ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
               ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"))) {

      consumer.subscribe(List.of("proposal-events"));
      var records = consumer.poll(Duration.ofSeconds(10));
      assertThat(records.count()).isGreaterThan(0);

      var record = records.iterator().next();
      assertThat(record.key()).isEqualTo("42");
      assertThat(record.value()).contains("\"type\":\"PROPOSAL_SUBMITTED\"");
    }
  }
}
```

### Test 2 — **DLT routing** with Embedded Kafka + Spring error handler

```java
// src/test/java/com/uc/kafka/DltRoutingIT.java
package com.uc.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DltRoutingIT.Config.class)
@EmbeddedKafka(partitions = 3, topics = {"proposal-events", "proposal-events.DLT"})
class DltRoutingIT {

  @Autowired KafkaTemplate<String, Map<String, Object>> template;
  @Autowired EmbeddedKafkaBroker broker;
  @Autowired TestProbe probe;

  @Test
  void failingConsumerIsRoutedToDlt() {
    // send a record that will make listener throw
    Map<String,Object> ev = Map.of("id","ev-2","type","PROPOSAL_SUBMITTED","proposalId",99,"payload",Map.of());
    template.send("proposal-events", "99", ev);

    // wait until our probe sees DLT
    var rec = probe.awaitDlt();
    assertThat(rec.topic()).isEqualTo("proposal-events.DLT");
    assertThat(rec.key()).isEqualTo("99");
  }

  @TestConfiguration
  static class Config {
    @Bean
    ProducerFactory<String, Map<String,Object>> pf(EmbeddedKafkaBroker broker) {
      var props = KafkaTestUtils.producerProps(broker);
      return new DefaultKafkaProducerFactory<>(props, new org.apache.kafka.common.serialization.StringSerializer(), new JsonSerializer<>());
    }
    @Bean KafkaTemplate<String, Map<String,Object>> kt(ProducerFactory<String, Map<String,Object>> pf) { return new KafkaTemplate<>(pf); }

    @Bean
    ConsumerFactory<String, Map<String,Object>> cf(EmbeddedKafkaBroker broker) {
      var props = KafkaTestUtils.consumerProps("test-consumer", "false", broker);
      var jd = new JsonDeserializer<Map<String,Object>>(); jd.addTrustedPackages("*");
      return new DefaultKafkaConsumerFactory<>(props, new org.apache.kafka.common.serialization.StringDeserializer(), jd);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, Map<String,Object>> factory(
        ConsumerFactory<String, Map<String,Object>> cf, KafkaTemplate<String, Map<String,Object>> kt) {
      var f = new ConcurrentKafkaListenerContainerFactory<String, Map<String,Object>>();
      f.setConsumerFactory(cf);

      var recoverer = new org.springframework.kafka.listener.DeadLetterPublishingRecoverer(kt,
          (rec, ex) -> new TopicPartition(rec.topic()+".DLT", rec.partition()));
      var handler = new org.springframework.kafka.listener.DefaultErrorHandler(recoverer, new org.springframework.util.backoff.FixedBackOff(200L, 2L));
      f.setCommonErrorHandler(handler);
      return f;
    }

    // Listener that intentionally fails for PROPOSAL_SUBMITTED to trigger DLT
    @Bean TestProbe probe() { return new TestProbe(); }

    @KafkaListener(topics = "proposal-events", groupId = "failing")
    public void failingListener(Map<String,Object> ev) {
      if ("PROPOSAL_SUBMITTED".equals(ev.get("type"))) throw new RuntimeException("boom");
    }

    @KafkaListener(topics = "proposal-events.DLT", groupId = "probe")
    public void dltListener(ConsumerRecord<String, Map<String,Object>> rec) { probe().onDlt(rec); }
  }

  static class TestProbe {
    private volatile ConsumerRecord<String, Map<String,Object>> seen;
    void onDlt(ConsumerRecord<String, Map<String,Object>> r) { this.seen = r; }
    ConsumerRecord<String, Map<String,Object>> awaitDlt() {
      for (int i=0;i<50;i++) { if (seen!=null) return seen; try { Thread.sleep(100);} catch (InterruptedException ignored) {} }
      throw new AssertionError("No DLT message observed");
    }
  }
}
```

> These tests prove: (1) keying + publish/consume works; (2) **DefaultErrorHandler → DLT** routing works.

---

## How to use this pack

* Rehearse the **architecture slide** out loud (≤60s).
* Drill the **20 Q\&A** (pick 8–10 to lead the conversation).
* Practice **one whiteboard prompt/day**.
* Run the **tests** locally once—you’ll be fluent describing DLT and partitioning.

If you’d like, I can also generate a **single PDF handout** from this content or create **IntelliJ run configs** for a one-click debug demo.

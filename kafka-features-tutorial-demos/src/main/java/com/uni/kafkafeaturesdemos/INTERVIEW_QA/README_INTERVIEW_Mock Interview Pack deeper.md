awesome — here are **35 more Kafka interview questions (+ crisp, UC-context answers)** to deepen your prep. They extend the earlier set and stay grounded in the Uniredit (UC) Investment Proposal system.

---

## A) Architecture & Design

1. **How do you detect and handle “hot keys” (one proposal dominating a partition)?**
   **Answer:** Monitor partition traffic (Kafka UI, broker metrics). If a proposal floods events (e.g., automated workflows), throttle producer, coalesce events, or split event types into separate topics. Keep key = `proposalId` for correctness; solve throughput outside partitioning first.

2. **When would you split `proposal-events` into multiple topics?**
   **Answer:** Different retention/consumers/evolution speed. Example: `proposal-lifecycle` (immutable domain events, 14–30d retention) and `proposal-ops` (ops/alerts, shorter retention). Keep clear ownership and contracts.

3. **What’s your fallback if Kafka is temporarily unavailable when approving a step?**
   **Answer:** Transactional **Outbox**: persist DB change + outbox row in same DB tx; async publisher retries until Kafka is back. App remains available and consistent.

4. **How do you support multiple business units (tenants)?**
   **Answer:** Easiest: per-tenant topics (`proposal-events.uc`, `proposal-events.am`). Or single topic with `x-tenant` header + ACLs + consumer-side filtering. For strict isolation or quotas, prefer per-tenant topics.

---

## B) Producer Deep Dive

5. **How do `acks=all` and `min.insync.replicas` interact?**
   **Answer:** With RF=3 and `min.insync.replicas=2`, an `acks=all` send succeeds when leader + ≥1 replica ack. If ISR drops below 2, produces fail fast—safer than silent data loss.

6. **What does idempotent producer actually guarantee?**
   **Answer:** De-dupes on broker using producerId+sequenceNumber; avoids duplicates from retries. Use with `max.in.flight.requests.per.connection<=5` (Spring defaults are safe).

7. **How do you add correlation between HTTP request and Kafka record?**
   **Answer:** Generate `x-correlation-id` per request; include as Kafka header; log in consumers and in HTTP response for traceability.

8. **How do you choose compression?**
   **Answer:** `zstd` (or `lz4`) for best size/CPU trade-off. Validate with benchmark; measure end-to-end latency and broker/network utilization.

---

## C) Consumer Deep Dive

9. **Record vs batch listener—when and why?**
   **Answer:** Record listener for low latency and simple logic. Batch when DB roundtrips dominate—process `List<ProposalEvent>` and commit once per batch.

10. **Manual vs auto offset commits in Spring Kafka?**
    **Answer:** Default (sync after listener returns) is fine. For exactly controlling commit after DB write, use `AckMode.MANUAL_IMMEDIATE` and call `ack.acknowledge()` post-persist (idempotent anyway).

11. **How do `max.poll.interval.ms` and `max.poll.records` affect stability?**
    **Answer:** If handler is slow and exceeds `max.poll.interval.ms`, group triggers rebalance. Keep work per poll bounded (batch size) and push long I/O async or use pause/resume.

12. **How do you pause consumption during DB outage?**
    **Answer:** `KafkaListenerEndpointRegistry#pause()` all containers; back off producers or let Kafka buffer; resume once DB recovers.

13. **How do you ensure consumers remain idempotent under retries?**
    **Answer:** Unique key (`eventId`) in DB (or `(eventId, channel)`), catch duplicate key violation → no-op. For external effects, store “sent markers”.

---

## D) Ordering, Partitions, Rebalances

14. **How do you prove per-proposal ordering to an interviewer?**
    **Answer:** Set key = `proposalId`; show message metadata `partition` stable for a given id; display increasing offsets for that partition in Kafka UI or logs.

15. **What’s the effect of increasing partitions on existing ordering?**
    **Answer:** Past records remain as-is; future messages may map to different partitions (key hashing spread). Per-key ordering still holds (deterministic hash).

16. **How do you make rebalances observable?**
    **Answer:** Implement `ConsumerAwareRebalanceListener`; log assignments and revocations with topic/partition; add metrics for assignment duration.

---

## E) Error Handling & DLT

17. **What belongs in DLT headers for investigation?**
    **Answer:** Original topic/partition/offset, exception class/message, stack trace (truncated), timestamp, consumer group. Spring adds many automatically.

18. **When *not* to DLT?**
    **Answer:** For transient infra errors (DB connection blips), rely on retries + backoff; DLT is for non-transient/poison messages (schema, validation).

19. **How do you prevent DLT replay loops?**
    **Answer:** Replayer uses different groupId, filters by time range/type, and only runs after a fix is deployed. Optionally annotate replayed messages with a header and block if seen again.

---

## F) Schema & Serialization

20. **Biggest JSON pitfalls?**
    **Answer:** Silent field drops/renames, number coercion, and deserialization security. Set trusted packages and validation on event fields; keep a `version`.

21. **Moving to Avro—what compatibility mode?**
    **Answer:** **BACKWARD** (consumers can read new producer versions). Use logical types for timestamps/decimals; document defaults.

22. **How do you roll out a breaking change?**
    **Answer:** Blue/green topic strategy: produce **both** versions temporarily (or dual write), migrate consumers, then retire the old topic/schema.

---

## G) Streaming & Analytics

23. **How would you build a “stuck proposals” dashboard?**
    **Answer:** Kafka Streams: join `SUBMITTED` with periodic “heartbeat” or current step events; compute time since last progress; emit to `proposal-insights` topic → materialize in a store; expose via REST or sink to OLAP.

24. **Windowing choice for SLA?**
    **Answer:** Session or time window keyed by `proposalId`. For SLA (submit→approved), you can maintain two KTables (submittedAt, approvedAt) and join; no window if you keep latest timestamps by key.

---

## H) Connect, DR, Multi-Region

25. **Where does Kafka Connect fit for UC?**
    **Answer:** CDC from legacy DB (`cdc.uc.proposals`) to enrich analytics; S3 sink of `audit-logs` for compliance; Elastic sink for searchable audits.

26. **How to do DR/geo—MirrorMaker 2?**
    **Answer:** Mirror `proposal-events` to DR cluster; replicate consumer group offsets; choose active-active (resolve duplicates with idempotent consumers) or active-standby (simpler, longer RTO).

---

## I) Security & Governance

27. **How do you secure producers/consumers?**
    **Answer:** SASL\_SSL with per-service principals; ACLs per topic (produce/consume). Rotate creds via Secret Manager; disable auto‐create; IaC for topics/ACLs.

28. **PII in events—what’s your approach?**
    **Answer:** Keep events **minimally necessary**; mask or tokenize sensitive fields; prefer storing PII in DB and reference by ID in event payloads.

---

## J) Observability & SLOs

29. **Which metrics signal trouble first?**
    **Answer:** Consumer **lag**, DLT message rate, poll duration, handler exceptions, producer retries. Alert on sustained lag/DLT growth.

30. **How do you trace a user request across services?**
    **Answer:** `x-correlation-id` propagated: HTTP request → Kafka header → consumer logs + DB; log the id and include in API responses.

---

## K) Performance & Tuning

31. **What’s your tuning strategy before scaling hardware?**
    **Answer:** Producer compression and small `linger.ms`, right batch size; consumer batch size & concurrency; DB connection pool size; ensure GC and thread pools aren’t bottlenecks; profile handler logic.

32. **Bigger messages (>1MB)?**
    **Answer:** Avoid. If needed, raise `max.request.size`/`message.max.bytes` and **offload payloads** to object storage, keep pointers in events.

33. **How do you validate partition skew?**
    **Answer:** Broker metrics or Kafka UI partition size/message counts; for UC, keys are evenly distributed unless a specific proposal spikes.

---

## L) Spring Kafka specifics

34. **How do you test listeners without a real broker?**
    **Answer:** `@EmbeddedKafka` for fast functional tests; `Testcontainers Kafka` for realistic integration. Use `KafkaTestUtils` to wire producers/consumers.

35. **How do you ensure listener starts only after topics exist?**
    **Answer:** Spring Kafka **Admin** with `fail-fast` and `NewTopic` beans; or pre-create via IaC. In CI, start EmbeddedKafka/Testcontainers before app.

36. **How do you switch between host and container bootstrap addresses?**
    **Answer:** Profiles or env vars: `localhost:9092` from IDE; `kafka:29092` from inside Docker. Centralize in `application-*.yml`.

---

## M) Edge Cases & Gotchas

37. **Producer warns `LEADER_NOT_AVAILABLE`—why?**
    **Answer:** Topic doesn’t exist or replication factor > brokers; or wrong bootstrap (host vs container). Create topic with RF=1 on single-broker dev and verify `bootstrap-servers`.

38. **Why would consumers see the same message after a success?**
    **Answer:** Rebalance right after processing but before commit (in manual modes), or container shutdown. Keep default commit mode or ensure ack after durable write.

39. **What if two services must process *mutually exclusively* (only one handles a given event)?**
    **Answer:** Put them in the **same consumer group** so partitions are shared; or introduce a **router** that sends different event types to different topics.

40. **How do you backfill audits for old proposals?**
    **Answer:** Load historical DB rows and emit **synthetic events** with `at` timestamps in the past (tagged via header), or write directly to `audit-service` DB with a “backfilled” flag (clearly documented).

41. **How do you cap topic growth in dev?**
    **Answer:** Set small `retention.ms` and/or `retention.bytes` in docker compose; regularly purge topics; keep DLT retention longer than main events for debugging.

---

### Want even more?

I can turn these into **flash cards** or a **practice quiz** (randomized), or craft a **mock panel** with 10 scenario questions where you whiteboard trade-offs (Outbox vs Transactions, compaction vs retention, DR topologies, etc.).

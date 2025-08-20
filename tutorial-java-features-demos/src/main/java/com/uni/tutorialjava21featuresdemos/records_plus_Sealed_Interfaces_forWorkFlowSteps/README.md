Awesome ask. Here are two clean, interview-ready patterns using **Sealed Interfaces + Records** in your **Investment Proposal (UC)** system:

---

# 1) Command side — approvals as a sealed hierarchy

### `ApprovalCommand.java`

```java
package com.uc.proposalservice.domain;

/**
 * Sealed interface = compile-time closed hierarchy.
 * Only the permitted types below can implement it.
 * This lets our switch be exhaustive and safe.
 */
public sealed interface ApprovalCommand
  permits Approve, Reject { }
```

### `Approve.java` / `Reject.java`

```java
package com.uc.proposalservice.domain;

/** Records = immutable, concise data carriers for commands. */
public record Approve(String approver, String comments) implements ApprovalCommand { }

public record Reject(String approver, String reason) implements ApprovalCommand { }
```

### `ApprovalHandler.java`

```java
package com.uc.proposalservice.service;

import com.uc.proposalservice.domain.*;
import org.springframework.stereotype.Service;

/**
 * Exhaustive switch: compiler forces us to handle all command types.
 * If we add a new command later, the switch won’t compile until updated.
 */
@Service
public class ApprovalHandler {

  public void handle(long proposalId, ApprovalCommand cmd) {
    switch (cmd) {
      case Approve(var approver, var comments) -> approve(proposalId, approver, comments);
      case Reject (var approver, var reason)   -> reject (proposalId, approver, reason);
    }
  }

  private void approve(long id, String by, String comments) {
    // 1) mutate domain state & persist
    // 2) emit ProposalApproved event
  }

  private void reject(long id, String by, String reason) {
    // 1) mutate domain state & persist
    // 2) emit ProposalRejected event
  }
}
```

**Why it’s good:**

* Clear domain API (only valid commands exist).
* Safer refactors (compiler guides you).
* Records give you immutability and zero boilerplate.

**Mini diagram**

```
Approve ┐
Reject  ┘ implements → ApprovalCommand (sealed)
                 │
                 ▼
          ApprovalHandler.switch(cmd)  → approve()/reject()
```

---

# 2) Event side — domain events as sealed + record variants

Use a sealed event “base type” so producers/consumers can pattern-match precisely and evolve safely.

### `ProposalDomainEvent.java`

```java
package com.uc.common;

import java.time.OffsetDateTime;

/**
 * Base event type (sealed). All concrete events are records.
 * Great for Kafka JSON payloads and exhaustive switches in consumers.
 */
public sealed interface ProposalDomainEvent
  permits ProposalSubmitted, StepApproved, ProposalApproved, ProposalRejected {

  String id();              // event id (UUID string)
  long proposalId();        // aggregate id
  OffsetDateTime at();      // occurrence time
  int version();            // schema version for evolution
}
```

### Concrete events (records)

```java
package com.uc.common;

import java.time.OffsetDateTime;
import java.util.Map;

/** Emitted when a proposal enters review. */
public record ProposalSubmitted(
    String id,
    long proposalId,
    OffsetDateTime at,
    int version,
    Map<String, Object> payload // e.g., { "chain": ["PEER","MANAGER","COMP"] }
) implements ProposalDomainEvent { }

/** One workflow step was approved. */
public record StepApproved(
    String id,
    long proposalId,
    OffsetDateTime at,
    int version,
    String step,                // e.g., "MANAGER"
    String approver,
    String comments
) implements ProposalDomainEvent { }

/** Terminal outcomes. */
public record ProposalApproved(
    String id,
    long proposalId,
    OffsetDateTime at,
    int version
) implements ProposalDomainEvent { }

public record ProposalRejected(
    String id,
    long proposalId,
    OffsetDateTime at,
    int version,
    String rejectedBy,
    String reason
) implements ProposalDomainEvent { }
```

### Producer usage (Proposal Service)

```java
// Build and send a specific record variant (immutability, clear shape)
var ev = new ProposalSubmitted(
  UUID.randomUUID().toString(),
  proposalId,
  OffsetDateTime.now(),
  1,
  Map.of("chain", List.of("PEER_REVIEW", "MANAGER_APPROVAL", "COMPLIANCE"))
);
kafkaTemplate.send("proposal-events", String.valueOf(ev.proposalId()), ev);
```

### Consumer routing (Audit Service)

```java
package com.uc.auditservice.kafka;

import com.uc.common.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {

  @KafkaListener(topics = "proposal-events", groupId = "audit-service")
  public void onEvent(ProposalDomainEvent ev) {
    // Pattern-matching switch (Java 21) over the SEALED hierarchy:
    switch (ev) {
      case ProposalSubmitted s -> addRow(s.proposalId(), "SUBMITTED", s.payload());
      case StepApproved     s  -> addRow(s.proposalId(), "STEP_APPROVED:"+s.step(), Map.of("by", s.approver()));
      case ProposalApproved  p -> addRow(p.proposalId(), "APPROVED", null);
      case ProposalRejected  r -> addRow(r.proposalId(), "REJECTED:"+r.reason(), Map.of("by", r.rejectedBy()));
    }
  }

  private void addRow(long pid, String action, Object meta) {
    // persist audit entry
  }
}
```

**Why it’s good:**

* Each event has a **precise, self-documenting shape**.
* Consumers can **pattern-match by event type** and the compiler ensures exhaustiveness.
* Evolution path: add a new event record, update switches; version field helps schema changes.

**Mini diagram**

```
ProposalDomainEvent (sealed)
 ├─ ProposalSubmitted (record)
 ├─ StepApproved      (record)
 ├─ ProposalApproved  (record)
 └─ ProposalRejected  (record)
                 │
                 ▼
       Consumers switch(ev) exhaustively
```

---

## Serialization tips (Spring Boot 3 / Jackson)

* Records work out of the box (no extra module needed in Boot 3).
* If you send the sealed base type over Kafka (`ProposalDomainEvent`), include a **type hint** so Jackson knows which concrete record to create on the consumer side. Easiest:

### Add Jackson polymorphic annotations to the base interface

```java
import com.fasterxml.jackson.annotation.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ProposalSubmitted.class,  name = "PROPOSAL_SUBMITTED"),
  @JsonSubTypes.Type(value = StepApproved.class,       name = "STEP_APPROVED"),
  @JsonSubTypes.Type(value = ProposalApproved.class,   name = "PROPOSAL_APPROVED"),
  @JsonSubTypes.Type(value = ProposalRejected.class,   name = "PROPOSAL_REJECTED")
})
public sealed interface ProposalDomainEvent permits ProposalSubmitted, StepApproved, ProposalApproved, ProposalRejected {
  // same as above
}
```

Then the produced JSON will have `"eventType":"PROPOSAL_SUBMITTED"` etc., and consumers will deserialize to the correct record automatically.

---

## Where to use which pattern?

* **Commands (Approve/Reject)** → *sealed interface + records* in the **write path** (controller/service).
* **Events (Submitted/Approved/Rejected/StepApproved)** → *sealed interface + record variants* across **Kafka** to downstream services.

These two patterns together give you **compile-time safety**, **immutability**, and **clean, evolvable contracts**—great talking points for your interview.

Perfect—let’s wire **modern Java (21)** right into your **Uniredit (UC) – Investment Proposal Management** app. Below are the **latest Java features you can use today**, where they fit in UC, and **drop-in class files** with **step-by-step comments**. I’ve split them into:

* **Core (stable in Java 21)** → safe by default
* **Preview (opt-in in Java 21)** → powerful, needs `--enable-preview`

Each feature also has a **tiny text diagram** so you can picture it quickly.

---

# 0) Build setup for Java 21 (and previews if you want them)

**Parent `pom.xml` essentials**

```xml
<properties>
  <java.version>21</java.version>
</properties>

<build>
  <plugins>
    <!-- Java 21 compile -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.11.0</version>
      <configuration>
        <release>21</release>
        <!-- Enable this ONLY if you use preview features (String Templates, Scoped Values, Structured Concurrency) -->
        <!-- <compilerArgs><arg>--enable-preview</arg></compilerArgs> -->
      </configuration>
    </plugin>

    <!-- JUnit / integration tests with preview (optional) -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>3.2.5</version>
      <configuration>
        <!-- needed only when previews are used -->
        <!-- <argLine>--enable-preview</argLine> -->
      </configuration>
    </plugin>
  </plugins>
</build>
```

---

# A) CORE (stable) Java 21 features

## 1) Records for DTOs & Events

**Why here?** Clean, immutable data for REST DTOs and Kafka events; Jackson in Spring Boot 3.x supports records out of the box.

**Class files**

`ipm-common/src/main/java/com/uc/common/ProposalEventType.java`

```java
package com.uc.common;
public enum ProposalEventType {
  PROPOSAL_SUBMITTED, STEP_APPROVED, PROPOSAL_APPROVED, PROPOSAL_REJECTED
}
```

`ipm-common/src/main/java/com/uc/common/ProposalEvent.java`

```java
package com.uc.common;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 1) Records are immutable data carriers; perfect for events.
 * 2) Jackson can serialize/deserialize them without setters.
 * 3) All fields are final; canonical constructor auto-generated.
 */
public record ProposalEvent(
    String id,
    ProposalEventType type,
    Long proposalId,
    Map<String, Object> payload,
    OffsetDateTime at,
    int version // helps schema evolution
) {}
```

`proposal-service/src/main/java/com/uc/proposalservice/dto/CreateProposalRequest.java`

```java
package com.uc.proposalservice.dto;

import jakarta.validation.constraints.*;
/** 1) Request bodies as records keep controller code concise. */
public record CreateProposalRequest(
    @NotBlank String title,
    @NotBlank String applicantName,
    @Positive double amount,
    String description
) {}
```

**Mini diagram**

```
[HTTP JSON] → [Record DTO] → [Service] → [Record Event] → Kafka
```

---

## 2) Pattern Matching for `switch` (JEP 441)

**Why here?** Cleaner branching on event types in consumers.

`audit-service/src/main/java/com/uc/auditservice/service/EventRouter.java`

```java
package com.uc.auditservice.service;

import com.uc.common.*;
import org.springframework.stereotype.Service;

/**
 * 1) Use switch expression on enum (Java 21 finalized).
 * 2) No boilerplate; add cases as event types grow.
 */
@Service
public class EventRouter {
  public String route(ProposalEvent ev) {
    return switch (ev.type()) {
      case PROPOSAL_SUBMITTED -> "create-audit-row";
      case STEP_APPROVED      -> "append-step-audit";
      case PROPOSAL_APPROVED  -> "close-with-success";
      case PROPOSAL_REJECTED  -> "close-with-rejection";
    };
  }
}
```

**Mini diagram**

```
[ProposalEvent] → switch(type) → [Handler chosen]
```

---

## 3) Record Patterns (JEP 440)

**Why here?** Deconstruct nested records directly in `switch` cases.

`audit-service/src/main/java/com/uc/auditservice/kafka/ProposalEventListener.java`

```java
package com.uc.auditservice.kafka;

import com.uc.common.ProposalEvent;
import com.uc.common.ProposalEventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 1) Pattern-match record + guard on payload content.
 * 2) Extract fields inline; no manual getters/mapping noise.
 */
@Component
public class ProposalEventListener {

  @KafkaListener(topics = "proposal-events", groupId = "audit-service")
  public void onEvent(ProposalEvent ev) {
    switch (ev) {
      case ProposalEvent(String id, ProposalEventType.PROPOSAL_SUBMITTED, Long proposalId,
                         Map<String, Object> payload, var at, var version)
           when payload != null && payload.containsKey("chain") -> {
        // 3) Step-wise: we already have id, proposalId, and payload extracted.
        // 4) Persist audit row using values straight from pattern variables.
        persist(id, proposalId, "SUBMITTED with chain="+payload.get("chain"), at);
      }
      case ProposalEvent(String id, ProposalEventType.PROPOSAL_APPROVED, Long proposalId, var p, var at, var v) -> {
        persist(id, proposalId, "APPROVED", at);
      }
      default -> {
        persist(ev.id(), ev.proposalId(), ev.type().name(), ev.at());
      }
    }
  }

  private void persist(String id, Long pid, String note, java.time.OffsetDateTime at) { /* save... */ }
}
```

**Mini diagram**

```
switch(ev) { case ProposalEvent(var id, PROPOSAL_SUBMITTED, var pid, var payload, ...) -> ... }
```

---

## 4) Virtual Threads (JEP 444)

**Why here?** Massive concurrency for IO-bound work (HTTP, DB, Kafka), simpler @Async tasks.

**Enable in Spring Boot**
`application.yml`

```yaml
spring:
  threads:
    virtual:
      enabled: true   # 1) Spring Boot will run @Async and web requests on virtual threads
```

**Parallel enrichment with virtual threads**
`proposal-service/src/main/java/com/uc/proposalservice/service/EnrichmentService.java`

```java
package com.uc.proposalservice.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.*;

/**
 * 1) Use a lightweight virtual-thread-per-task Executor.
 * 2) Fetch risk & compliance in parallel without heavy platform threads.
 */
@Service
public class EnrichmentService {

  private static final Executor vt = Executors.newVirtualThreadPerTaskExecutor();

  public record Enrichment(double riskScore, boolean complianceOk) {}

  public Enrichment fetchAll(long proposalId) {
    try (var scope = vt) { // 3) Auto-close after tasks complete
      // 4) Submit independent IO calls
      Future<Double> risk = ((ExecutorService) vt).submit(() -> callRiskService(proposalId));
      Future<Boolean> comp = ((ExecutorService) vt).submit(() -> callComplianceService(proposalId));

      // 5) Wait and combine
      return new Enrichment(risk.get(), comp.get());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private double callRiskService(long id) throws InterruptedException { Thread.sleep(50); return 0.12; }
  private boolean callComplianceService(long id) throws InterruptedException { Thread.sleep(50); return true; }
}
```

**Mini diagram**

```
[submit()] ─▶ (virtual thread) risk call
[submit()] ─▶ (virtual thread) compliance call
           ◀── join ─── combine → Enrichment
```

---

## 5) Sequenced Collections (JEP 431)

**Why here?** Your approval chain is an ordered list; Java 21 adds first/last operations.

`proposal-service/src/main/java/com/uc/proposalservice/workflow/ApprovalChain.java`

```java
package com.uc.proposalservice.workflow;

import java.util.*;

/**
 * 1) LinkedHashSet/Map now implement Sequenced* interfaces.
 * 2) Use putFirst/putLast/removeLast to manipulate step order.
 */
public class ApprovalChain {
  private final SequencedSet<String> steps = new LinkedHashSet<>();

  public ApprovalChain(Collection<String> defaults) {
    steps.addAll(defaults);            // e.g., [PEER_REVIEW, MANAGER_APPROVAL, COMPLIANCE]
  }

  public void prioritize(String role) { steps.addFirst(role); } // 3) Java 21
  public void deprioritize(String role) { steps.remove(role); steps.addLast(role); } // 4)
  public List<String> asList() { return List.copyOf(steps); }
}
```

**Mini diagram**

```
HEAD → [TEAM_LEAD] → [PEER_REVIEW] → [MANAGER] → [COMPLIANCE] → TAIL
        ↑ addFirst()                           addLast() ↓
```

---

# B) PREVIEW features (optional, enable with `--enable-preview`)

> Enable preview in the Maven compiler & surefire sections (shown earlier as commented lines).

## 6) String Templates (JEP 430 – preview)

**Why here?** Safer, readable string building (great for audit lines/logs).

`proposal-service/src/main/java/com/uc/proposalservice/audit/AuditLineFactory.java`

```java
package com.uc.proposalservice.audit;

import static java.lang.StringTemplate.STR; // 1) Import the template processor (preview)

import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;

/**
 * 2) Build audit text with embedded expressions; no brittle concatenation.
 */
@Component
public class AuditLineFactory {
  public String approved(long pid, String approver) {
    var ts = OffsetDateTime.now();
    // 3) Placeholders are checked; expressions are real Java
    return STR."[\{ts}] Proposal \{pid} APPROVED by \{approver}";
  }
}
```

**Mini diagram**

```
"[\{ts}] Proposal \{pid} APPROVED by \{approver}"  →  "[2025-08-17] Proposal 12 APPROVED by john.doe"
```

---

## 7) Scoped Values (JEP 446 – preview)

**Why here?** Pass a correlation ID through nested/async calls (incl. virtual threads) *without* ThreadLocals.

`proposal-service/src/main/java/com/uc/proposalservice/infra/Correlation.java`

```java
package com.uc.proposalservice.infra;

import jdk.incubator.concurrent.ScopedValue; // 1) Preview API

/** 2) Define a well-known scoped value to carry the correlation id. */
public final class Correlation {
  public static final ScopedValue<String> CORR_ID = ScopedValue.newInstance();
  private Correlation() {}
}
```

`proposal-service/src/main/java/com/uc/proposalservice/controller/ProposalController.java`

```java
package com.uc.proposalservice.controller;

import com.uc.proposalservice.dto.CreateProposalRequest;
import com.uc.proposalservice.service.ProposalService;
import com.uc.proposalservice.infra.Correlation;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/proposals")
public class ProposalController {
  private final ProposalService service;
  public ProposalController(ProposalService service){ this.service = service; }

  @PostMapping
  public Object create(@RequestBody CreateProposalRequest req) {
    String corr = UUID.randomUUID().toString(); // 1) generate correlation id
    // 2) Bind corr id to the current (virtual) thread and all children
    return ScopedValue.where(Correlation.CORR_ID, corr).call(() -> service.create(req));
  }
}
```

`proposal-service/src/main/java/com/uc/proposalservice/kafka/EventPublisher.java`

```java
package com.uc.proposalservice.kafka;

import com.uc.common.ProposalEvent;
import com.uc.proposalservice.infra.Correlation;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 3) Read the correlation id from the scoped context and send as header.
 */
@Component
public class EventPublisher {
  private final KafkaTemplate<String, ProposalEvent> template;
  public EventPublisher(KafkaTemplate<String, ProposalEvent> template){ this.template = template; }

  public void publish(ProposalEvent ev) {
    String corr = Correlation.CORR_ID.orElse("missing"); // safe read
    var msg = MessageBuilder.withPayload(ev)
      .setHeader(KafkaHeaders.TOPIC, "proposal-events")
      .setHeader(KafkaHeaders.MESSAGE_KEY, String.valueOf(ev.proposalId()))
      .setHeader("x-correlation-id", corr)
      .build();
    template.send(msg);
  }
}
```

**Mini diagram**

```
[Controller] --bind--> (CORR_ID in scoped context)
         \→ [Service] → [Kafka Publisher] (reads CORR_ID) → Kafka header
```

---

## 8) Structured Concurrency (JEP 453 – preview)

**Why here?** Run related subtasks (risk, KYC, AML) *as a unit* and fail/timeout them together.

`proposal-service/src/main/java/com/uc/proposalservice/service/SubmissionChecks.java`

```java
package com.uc.proposalservice.service;

import jdk.incubator.concurrent.StructuredTaskScope; // 1) Preview API
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 2) Parent scope supervises child tasks.
 * 3) On error/timeout, cancels siblings and propagates a coherent failure.
 */
@Service
public class SubmissionChecks {

  public record Result(boolean ok, double risk, boolean aml) {}

  public Result runAll(long proposalId) {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var risk  = scope.fork(() -> fetchRiskScore(proposalId));
      var aml   = scope.fork(() -> performAmlCheck(proposalId));
      // 4) Wait with timeout and throw if any fails
      scope.joinUntil(java.time.Instant.now().plus(Duration.ofSeconds(2)));
      scope.throwIfFailed();
      return new Result(true, risk.get(), aml.get());
    } catch (Exception e) {
      return new Result(false, 1.0, false); // 5) map failure coherently
    }
  }

  private double fetchRiskScore(long id) throws InterruptedException { Thread.sleep(80); return 0.18; }
  private boolean performAmlCheck(long id) throws InterruptedException { Thread.sleep(40); return true; }
}
```

**Mini diagram**

```
[Parent scope]
   ├─ task A: risk
   └─ task B: AML
   ── joinUntil(2s) → if any fails → cancel siblings → throw
```

---

## 9) Sealed Interfaces + Records (since 17; still modern)

**Why here?** Model approval commands and make the `switch` exhaustive by type.

`proposal-service/src/main/java/com/uc/proposalservice/workflow/ApprovalCommand.java`

```java
package com.uc.proposalservice.workflow;

/**
 * 1) Limit who can implement (compile-time sealed).
 * 2) Records are perfect concrete implementations.
 */
public sealed interface ApprovalCommand permits Approve, Reject {}

public record Approve(String approver, String comments) implements ApprovalCommand {}
public record Reject (String approver, String reason  ) implements ApprovalCommand {}
```

`proposal-service/src/main/java/com/uc/proposalservice/service/ApprovalHandler.java`

```java
package com.uc.proposalservice.service;

import com.uc.proposalservice.workflow.*;
import org.springframework.stereotype.Service;

/** 3) The switch is exhaustive because the interface is sealed. */
@Service
public class ApprovalHandler {
  public void handle(ApprovalCommand cmd) {
    switch (cmd) {
      case Approve(var approver, var comments) -> approve(approver, comments); // record pattern
      case Reject (var approver, var reason)   -> reject(approver, reason);
    }
  }
  private void approve(String by, String c) { /* persist + emit event */ }
  private void reject (String by, String r) { /* persist + emit event */ }
}
```

**Mini diagram**

```
[ApprovalCommand] = sealed { Approve, Reject } → switch exhaustively handles both
```

---

# C) How they all fit together (one flow)

**Create → Submit (with checks) → Publish event with correlation → Consume with pattern matching**

```
Client
  → Controller (bind CORR_ID via ScopedValue)              [preview]
     → EnrichmentService (virtual threads)                 [core]
     → SubmissionChecks (StructuredTaskScope)              [preview]
     → Build ProposalEvent (record)                        [core]
     → EventPublisher (adds x-correlation-id header)       [preview/core]
        → Kafka
           → Audit consumer switch/record patterns         [core]
```

---

# D) Bonus: enable/disable preview quickly

If you prefer **no preview**, use only: **Records, Pattern matching for switch/records, Virtual Threads, Sequenced Collections, Sealed types**.
When ready to show extra polish in interviews, enable preview and add **String Templates, Scoped Values, Structured Concurrency**.

---

## Copy checklist (what to paste where)

* **Common**: `ProposalEventType`, `ProposalEvent` (record)
* **Proposal Service**:

    * DTO record `CreateProposalRequest`
    * `EnrichmentService` (virtual threads)
    * `SubmissionChecks` (structured concurrency, preview)
    * `EventPublisher` (uses correlation header; ScopedValue if preview)
    * `ApprovalCommand` (sealed + records) & `ApprovalHandler`
* **Audit Service**:

    * `EventRouter` (switch on enum)
    * `ProposalEventListener` (record patterns in switch)

---

If you want, I can **inline these into your existing package paths** (exact filenames) or generate a **“diff pack”** that shows what to add/modify per module (proposal/audit/notification) with zero guesswork.

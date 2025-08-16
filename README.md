# UC_ApprovalFlow-Kafka
Context: UC  want a spring boot project for investment proposal management. with rest apis for approval workflow with kafka. wanted in detail. with all class and config files. and how to run kafka etc..


All set! I generated a complete Spring Boot project (Java 17, Spring Boot 3.3) for an Investment Proposal Management system with a multi-step approval workflow and Kafka events—plus Docker Compose to run Kafka locally.
here’s a clean tree of the **3-service Spring Boot + Kafka** project you can copy:

```text
ipm-microservices/
├─ docker-compose.yml
│
├─ proposal-service/
│  ├─ pom.xml
│  └─ src/
│     ├─ main/
│     │  ├─ java/
│     │  │  └─ com/uc/
│     │  │     ├─ common/                       # shared event model (duplicated per service)
│     │  │     │  ├─ ProposalEvent.java
│     │  │     │  └─ ProposalEventType.java
│     │  │     └─ proposalservice/
│     │  │        ├─ Application.java
│     │  │        ├─ config/
│     │  │        │  ├─ KafkaConfig.java
│     │  │        │  └─ SwaggerConfig.java
│     │  │        ├─ enums/
│     │  │        │  ├─ Decision.java
│     │  │        │  └─ ProposalStatus.java
│     │  │        ├─ entity/
│     │  │        │  ├─ ApprovalStep.java
│     │  │        │  └─ Proposal.java
│     │  │        ├─ dto/
│     │  │        │  ├─ ApproveRequest.java
│     │  │        │  ├─ CreateProposalRequest.java
│     │  │        │  ├─ ProposalResponse.java
│     │  │        │  └─ RejectRequest.java
│     │  │        ├─ exception/
│     │  │        │  ├─ BadRequestException.java
│     │  │        │  ├─ GlobalExceptionHandler.java
│     │  │        │  └─ NotFoundException.java
│     │  │        ├─ kafka/
│     │  │        │  ├─ EventPublisher.java
│     │  │        │  └─ ProposalEventConsumer.java
│     │  │        ├─ repository/
│     │  │        │  ├─ ApprovalStepRepository.java
│     │  │        │  └─ ProposalRepository.java
│     │  │        ├─ service/
│     │  │        │  ├─ ApprovalWorkflowService.java
│     │  │        │  └─ ProposalService.java
│     │  │        └─ controller/
│     │  │           ├─ ApprovalController.java
│     │  │           └─ ProposalController.java
│     │  └─ resources/
│     │     ├─ application.yml
│     │     └─ data.sql              # optional seed
│     └─ test/                       # (optional)
│
├─ audit-service/
│  ├─ pom.xml
│  └─ src/
│     ├─ main/
│     │  ├─ java/
│     │  │  └─ com/uc/
│     │  │     ├─ common/
│     │  │     │  ├─ ProposalEvent.java
│     │  │     │  └─ ProposalEventType.java
│     │  │     └─ auditservice/
│     │  │        ├─ Application.java
│     │  │        ├─ config/
│     │  │        │  ├─ KafkaConfig.java
│     │  │        │  └─ SwaggerConfig.java
│     │  │        ├─ controller/
│     │  │        │  └─ AuditController.java
│     │  │        ├─ entity/
│     │  │        │  └─ AuditEntry.java
│     │  │        ├─ kafka/
│     │  │        │  └─ ProposalEventListener.java
│     │  │        ├─ repository/
│     │  │        │  └─ AuditRepository.java
│     │  │        └─ service/
│     │  │           └─ AuditService.java
│     │  └─ resources/
│     │     └─ application.yml
│     └─ test/                       # (optional)
│
└─ notification-service/
   ├─ pom.xml
   └─ src/
      ├─ main/
      │  ├─ java/
      │  │  └─ com/uc/
      │  │     ├─ common/
      │  │     │  ├─ ProposalEvent.java
      │  │     │  └─ ProposalEventType.java
      │  │     └─ notificationservice/
      │  │        ├─ Application.java
      │  │        ├─ config/
      │  │        │  ├─ KafkaConfig.java
      │  │        │  └─ SwaggerConfig.java
      │  │        ├─ controller/
      │  │        │  └─ HealthController.java
      │  │        └─ kafka/
      │  │           └─ NotificationListener.java
      │  └─ resources/
      │     └─ application.yml
      └─ test/                       # (optional)
```

need anything added (Dockerfiles for each service, a parent Maven aggregator, or a shared `common` module instead of duplicating those two classes)?

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

_________________________________________________________________________________________________________________________________________________________________________________


Got it — here’s the **full multi-service project code (3 Spring Boot services + Kafka)** printed inline, with clear comments. Copy each file into your project with the same paths.

---

# Root (Kafka stack)

## `docker-compose.yml`

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.1
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.6.1
    depends_on: [zookeeper]
    ports:
      - "9092:9092"     # external
      - "29092:29092"   # internal (used by kafka-ui)
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://kafka:29092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0

  kafka-ui:
    image: provectuslabs/kafka-ui:v0.7.2
    depends_on: [kafka]
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181
```

---

# Shared (copy into each service under `src/main/java/com/uc/common`)

> These 2 classes are duplicated in each service to avoid a parent/pom complexity. They define the **event type** and **event payload** used on Kafka.

## `src/main/java/com/uc/common/ProposalEventType.java`

```java
package com.uc.common;

/** Domain event types emitted by proposal-service and consumed by others. */
public enum ProposalEventType {
    PROPOSAL_SUBMITTED,
    STEP_APPROVED,
    STEP_REJECTED,
    PROPOSAL_APPROVED,
    PROPOSAL_REJECTED
}
```

## `src/main/java/com/uc/common/ProposalEvent.java`

```java
package com.uc.common;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The JSON payload we publish/consume on Kafka. Spring-Kafka's JsonSerializer/JsonDeserializer
 * will serialize/deserialize this class automatically.
 */
public class ProposalEvent {
    private String id;                   // unique event id (UUID string)
    private ProposalEventType type;      // event type
    private Long proposalId;             // which proposal this event is about
    private Map<String, Object> payload; // small key-value details (role, approver, etc.)
    private OffsetDateTime at = OffsetDateTime.now(); // when the event occurred

    public ProposalEvent() { }

    public ProposalEvent(String id, ProposalEventType type, Long proposalId, Map<String, Object> payload) {
        this.id = id;
        this.type = type;
        this.proposalId = proposalId;
        this.payload = payload;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public ProposalEventType getType() { return type; }
    public void setType(ProposalEventType type) { this.type = type; }
    public Long getProposalId() { return proposalId; }
    public void setProposalId(Long proposalId) { this.proposalId = proposalId; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public OffsetDateTime getAt() { return at; }
    public void setAt(OffsetDateTime at) { this.at = at; }
}
```

---

# Service 1: `proposal-service` (port 8081)

> REST + JPA/H2. Manages proposals & approval steps. **Publishes Kafka events** and also a simple string line to an `audit-logs` topic for convenience.

### `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.uc</groupId>
  <artifactId>proposal-service</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <name>proposal-service</name>
  <description>Proposal Service for IPM</description>

  <properties>
    <java.version>17</java.version>
    <spring-boot.version>3.3.2</spring-boot.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <!-- Web + Validation -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- JPA + H2 for local dev -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <scope>runtime</scope>
    </dependency>

    <!-- Kafka -->
    <dependency>
      <groupId>org.springframework.kafka</groupId>
      <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- Swagger / OpenAPI -->
    <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.6.0</version>
    </dependency>

    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration>
          <source>${java.version}</source>
          <target>${java.version}</target>
          <compilerArgs><arg>-parameters</arg></compilerArgs>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

### `src/main/resources/application.yml`

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:h2:mem:proposalservice;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false
    username: sa
    password: ""
    driverClassName: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: proposal-service
      properties:
        spring:
          json:
            trusted:
              packages: "*"
    producer:
      properties:
        spring:
          json:
            add:
              type:
                headers: false

# Default approval chain if client doesn't provide one
ipm:
  default-approval-chain: "PEER_REVIEW,MANAGER_APPROVAL,COMPLIANCE"

logging:
  level:
    root: INFO
    org.apache.kafka: WARN
    com.uc: DEBUG
```

### (Optional seed) `src/main/resources/data.sql`

```sql
INSERT INTO proposals (id, title, applicant_name, amount, description, status, current_step_index, created_at, updated_at)
VALUES (1, 'First Investment', 'Alice', 100000.00, 'High priority proposal', 'DRAFT', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
```

### `src/main/java/com/uc/proposalservice/Application.java`

```java
package com.uc.proposalservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Bootstraps the Proposal Service. */
@SpringBootApplication
public class Application {
    public static void main(String[] args) { SpringApplication.run(Application.class, args); }
}
```

### `src/main/java/com/uc/proposalservice/config/KafkaConfig.java`

```java
package com.uc.proposalservice.config;

import com.uc.common.ProposalEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures Kafka producers/consumers and auto-creates topics for local dev.
 * - proposal-events: JSON events
 * - proposal-events.DLT: dead letter topic
 * - audit-logs: plain string audit lines
 */
@EnableKafka
@Configuration
public class KafkaConfig {
    public static final String EVENTS_TOPIC = "proposal-events";
    public static final String AUDIT_TOPIC = "audit-logs";
    public static final String DLT_SUFFIX = ".DLT";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ---- Producers ----
    @Bean
    public ProducerFactory<String, ProposalEvent> eventProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }
    @Bean public KafkaTemplate<String, ProposalEvent> eventKafkaTemplate() {
        return new KafkaTemplate<>(eventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }
    @Bean public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    // ---- Consumer factory with simple retry then DLT ----
    @Bean
    public ConsumerFactory<String, ProposalEvent> eventConsumerFactory() {
        JsonDeserializer<ProposalEvent> json = new JsonDeserializer<>(ProposalEvent.class);
        json.addTrustedPackages("*");
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, "proposal-service");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), json);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProposalEvent> kafkaListenerContainerFactory(
            KafkaTemplate<String, ProposalEvent> dltTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, ProposalEvent> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(eventConsumerFactory());
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dltTemplate,
                (record, ex) -> new org.apache.kafka.common.TopicPartition(record.topic() + DLT_SUFFIX, record.partition())
        );
        f.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L)));
        f.setConcurrency(2);
        return f;
    }

    // ---- Auto-create topics (works when broker allows) ----
    @Bean public NewTopic proposalEventsTopic() { return new NewTopic(EVENTS_TOPIC, 3, (short) 1); }
    @Bean public NewTopic proposalEventsDltTopic() { return new NewTopic(EVENTS_TOPIC + DLT_SUFFIX, 3, (short) 1); }
    @Bean public NewTopic auditTopic() { return new NewTopic(AUDIT_TOPIC, 1, (short) 1); }
}
```

### `src/main/java/com/uc/proposalservice/config/SwaggerConfig.java`

```java
package com.uc.proposalservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Minimal OpenAPI so you can use Swagger UI at /swagger-ui. */
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(new Info().title("proposal-service API").version("v1"));
    }
}
```

### `src/main/java/com/uc/proposalservice/enums/ProposalStatus.java`

```java
package com.uc.proposalservice.enums;

/** Lifecycle of a proposal. */
public enum ProposalStatus {
    DRAFT,        // newly created, editable by user
    UNDER_REVIEW, // submitted to approval chain
    APPROVED,     // fully approved at final step
    REJECTED      // rejected at some step
}
```

### `src/main/java/com/uc/proposalservice/enums/Decision.java`

```java
package com.uc.proposalservice.enums;

/** Decision taken on an individual step in the chain. */
public enum Decision { PENDING, APPROVED, REJECTED }
```

### `src/main/java/com/uc/proposalservice/entity/Proposal.java`

```java
package com.uc.proposalservice.entity;

import com.uc.proposalservice.enums.ProposalStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root for a proposal. Contains the ordered approval steps.
 * For simplicity, steps are eagerly loaded for API display.
 */
@Entity @Table(name = "proposals")
public class Proposal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String applicantName;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status = ProposalStatus.DRAFT;

    /** Index into steps list indicating "current" step to act. */
    private Integer currentStepIndex = 0;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();
    private OffsetDateTime submittedAt;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("stepOrder ASC")
    private List<ApprovalStep> steps = new ArrayList<>();

    // Getters/Setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getApplicantName() { return applicantName; } public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public ProposalStatus getStatus() { return status; } public void setStatus(ProposalStatus status) { this.status = status; }
    public Integer getCurrentStepIndex() { return currentStepIndex; } public void setCurrentStepIndex(Integer currentStepIndex) { this.currentStepIndex = currentStepIndex; }
    public OffsetDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; } public void setSubmittedAt(OffsetDateTime submittedAt) { this.submittedAt = submittedAt; }
    public List<ApprovalStep> getSteps() { return steps; } public void setSteps(List<ApprovalStep> steps) { this.steps = steps; }
}
```

### `src/main/java/com/uc/proposalservice/entity/ApprovalStep.java`

```java
package com.uc.proposalservice.entity;

import com.uc.proposalservice.enums.Decision;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

/** Single stage in the approval flow (e.g., PEER_REVIEW → MANAGER → COMPLIANCE). */
@Entity @Table(name = "approval_steps")
public class ApprovalStep {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "proposal_id")
    private Proposal proposal;

    private Integer stepOrder; // 0-based order
    private String role;       // e.g., "MANAGER_APPROVAL"
    private String approver;   // who took the decision

    @Enumerated(EnumType.STRING)
    private Decision decision = Decision.PENDING;

    private String comments;
    private OffsetDateTime decidedAt;

    // Getters/Setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Proposal getProposal() { return proposal; } public void setProposal(Proposal proposal) { this.proposal = proposal; }
    public Integer getStepOrder() { return stepOrder; } public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
    public String getRole() { return role; } public void setRole(String role) { this.role = role; }
    public String getApprover() { return approver; } public void setApprover(String approver) { this.approver = approver; }
    public Decision getDecision() { return decision; } public void setDecision(Decision decision) { this.decision = decision; }
    public String getComments() { return comments; } public void setComments(String comments) { this.comments = comments; }
    public OffsetDateTime getDecidedAt() { return decidedAt; } public void setDecidedAt(OffsetDateTime decidedAt) { this.decidedAt = decidedAt; }
}
```

### DTOs

`src/main/java/com/uc/proposalservice/dto/CreateProposalRequest.java`

```java
package com.uc.proposalservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/** Request body to create a proposal (starts in DRAFT). */
public class CreateProposalRequest {
    @NotBlank private String title;
    @NotBlank private String applicantName;
    @NotNull @DecimalMin("0.00") private BigDecimal amount;
    @Size(max = 2000) private String description;

    /** Optional override for chain: ["TEAM_LEAD","RISK","CFO"]. If null/empty, default chain is used on submit. */
    private List<String> approvalChain;

    // Getters/Setters
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getApplicantName() { return applicantName; } public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public List<String> getApprovalChain() { return approvalChain; } public void setApprovalChain(List<String> approvalChain) { this.approvalChain = approvalChain; }
}
```

`src/main/java/com/uc/proposalservice/dto/ApproveRequest.java`

```java
package com.uc.proposalservice.dto;

import jakarta.validation.constraints.NotBlank;

/** Payload for approving the current step. */
public class ApproveRequest {
    @NotBlank private String approver;
    private String comments;

    public String getApprover() { return approver; } public void setApprover(String approver) { this.approver = approver; }
    public String getComments() { return comments; } public void setComments(String comments) { this.comments = comments; }
}
```

`src/main/java/com/uc/proposalservice/dto/RejectRequest.java`

```java
package com.uc.proposalservice.dto;

import jakarta.validation.constraints.NotBlank;

/** Payload for rejecting the current step. */
public class RejectRequest {
    @NotBlank private String approver;
    private String comments;

    public String getApprover() { return approver; } public void setApprover(String approver) { this.approver = approver; }
    public String getComments() { return comments; } public void setComments(String comments) { this.comments = comments; }
}
```

`src/main/java/com/uc/proposalservice/dto/ProposalResponse.java`

```java
package com.uc.proposalservice.dto;

import com.uc.proposalservice.entity.ApprovalStep;
import com.uc.proposalservice.enums.ProposalStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** Response used by GETs to expose state + steps. */
public class ProposalResponse {
    private Long id;
    private String title;
    private String applicantName;
    private BigDecimal amount;
    private String description;
    private ProposalStatus status;
    private Integer currentStepIndex;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime submittedAt;
    private List<ApprovalStep> steps;

    // Getters/Setters
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getApplicantName(){return applicantName;} public void setApplicantName(String v){applicantName=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public ProposalStatus getStatus(){return status;} public void setStatus(ProposalStatus v){status=v;}
    public Integer getCurrentStepIndex(){return currentStepIndex;} public void setCurrentStepIndex(Integer v){currentStepIndex=v;}
    public OffsetDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(OffsetDateTime v){createdAt=v;}
    public OffsetDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(OffsetDateTime v){updatedAt=v;}
    public OffsetDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(OffsetDateTime v){submittedAt=v;}
    public List<ApprovalStep> getSteps(){return steps;} public void setSteps(List<ApprovalStep> v){steps=v;}
}
```

### Repositories

`src/main/java/com/uc/proposalservice/repository/ProposalRepository.java`

```java
package com.uc.proposalservice.repository;

import com.uc.proposalservice.entity.Proposal;
import com.uc.proposalservice.enums.ProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Basic JPA repo + finder by status for listing. */
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    Page<Proposal> findByStatus(ProposalStatus status, Pageable pageable);
}
```

`src/main/java/com/uc/proposalservice/repository/ApprovalStepRepository.java`

```java
package com.uc.proposalservice.repository;

import com.uc.proposalservice.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;

/** Separate repo (not used heavily but kept for clarity). */
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> { }
```

### Exceptions

`src/main/java/com/uc/proposalservice/exception/NotFoundException.java`

```java
package com.uc.proposalservice.exception;
public class NotFoundException extends RuntimeException { public NotFoundException(String m){ super(m); } }
```

`src/main/java/com/uc/proposalservice/exception/BadRequestException.java`

```java
package com.uc.proposalservice.exception;
public class BadRequestException extends RuntimeException { public BadRequestException(String m){ super(m); } }
```

`src/main/java/com/uc/proposalservice/exception/GlobalExceptionHandler.java`

```java
package com.uc.proposalservice.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** Converts exceptions into standard JSON error responses. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> notFound(NotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> bad(BadRequestException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> val(MethodArgumentNotValidException ex){
        Map<String,String> m=new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e->m.put(e.getField(),e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(m);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> other(Exception ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }
}
```

### Kafka publisher (and optional consumer to log self-events)

`src/main/java/com/uc/proposalservice/kafka/EventPublisher.java`

```java
package com.uc.proposalservice.kafka;

import com.uc.common.ProposalEvent;
import com.uc.proposalservice.config.KafkaConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Publishes domain events to 'proposal-events' and a simple string line to 'audit-logs'.
 * The audit line is handy to see something immediately in Kafka UI.
 */
@Component
public class EventPublisher {
    private final KafkaTemplate<String, ProposalEvent> eventTemplate;
    private final KafkaTemplate<String, String> stringTemplate;

    public EventPublisher(KafkaTemplate<String, ProposalEvent> eventTemplate,
                          KafkaTemplate<String, String> stringTemplate) {
        this.eventTemplate = eventTemplate;
        this.stringTemplate = stringTemplate;
    }

    public void publish(ProposalEvent ev) {
        if (ev.getId() == null) ev.setId(UUID.randomUUID().toString());
        eventTemplate.send(KafkaConfig.EVENTS_TOPIC, String.valueOf(ev.getProposalId()), ev);
        stringTemplate.send(KafkaConfig.AUDIT_TOPIC,
                "[" + ev.getAt() + "] " + ev.getType() + " proposalId=" + ev.getProposalId() + " payload=" + ev.getPayload());
    }
}
```

`src/main/java/com/uc/proposalservice/kafka/ProposalEventConsumer.java`

```java
package com.uc.proposalservice.kafka;

import com.uc.common.ProposalEvent;
import com.uc.proposalservice.config.KafkaConfig;
import org.apache.commons.logging.Log; import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** Optional: see our own events flowing; safe to remove if not needed. */
@Component
public class ProposalEventConsumer {
    private static final Log log = LogFactory.getLog(ProposalEventConsumer.class);

    @KafkaListener(topics = KafkaConfig.EVENTS_TOPIC, groupId = "proposal-service")
    public void onEvent(@Payload ProposalEvent event) {
        log.info("proposal-service saw event: " + event.getType() + " pid=" + event.getProposalId());
    }
}
```

### Services

`src/main/java/com/uc/proposalservice/service/ProposalService.java`

```java
package com.uc.proposalservice.service;

import com.uc.proposalservice.dto.CreateProposalRequest;
import com.uc.proposalservice.dto.ProposalResponse;
import com.uc.proposalservice.entity.Proposal;
import com.uc.proposalservice.enums.ProposalStatus;
import com.uc.proposalservice.exception.NotFoundException;
import com.uc.proposalservice.repository.ProposalRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** CRUD-ish operations not involving approval logic. */
@Service
public class ProposalService {
    private final ProposalRepository repo;
    public ProposalService(ProposalRepository repo){ this.repo = repo; }

    /** Create DRAFT proposal. */
    public ProposalResponse create(CreateProposalRequest req){
        Proposal p = new Proposal();
        p.setTitle(req.getTitle());
        p.setApplicantName(req.getApplicantName());
        p.setAmount(req.getAmount());
        p.setDescription(req.getDescription());
        p.setStatus(ProposalStatus.DRAFT);
        Proposal saved = repo.save(p);
        return toResponse(saved);
    }

    public Proposal getEntity(Long id){
        return repo.findById(id).orElseThrow(()->new NotFoundException("Proposal not found: "+id));
    }

    public ProposalResponse get(Long id){ return toResponse(getEntity(id)); }

    /** List by status if provided, else all (paged). */
    public Page<Proposal> list(ProposalStatus st, Pageable pageable){
        return st != null ? repo.findByStatus(st, pageable) : repo.findAll(pageable);
    }

    public ProposalResponse toResponse(Proposal p){
        ProposalResponse r = new ProposalResponse();
        BeanUtils.copyProperties(p, r);
        r.setSteps(p.getSteps());
        return r;
    }
}
```

`src/main/java/com/uc/proposalservice/service/ApprovalWorkflowService.java`

```java
package com.uc.proposalservice.service;

import com.uc.common.ProposalEvent;
import com.uc.common.ProposalEventType;
import com.uc.proposalservice.dto.ApproveRequest;
import com.uc.proposalservice.dto.RejectRequest;
import com.uc.proposalservice.entity.ApprovalStep;
import com.uc.proposalservice.entity.Proposal;
import com.uc.proposalservice.enums.Decision;
import com.uc.proposalservice.enums.ProposalStatus;
import com.uc.proposalservice.exception.BadRequestException;
import com.uc.proposalservice.kafka.EventPublisher;
import com.uc.proposalservice.repository.ApprovalStepRepository;
import com.uc.proposalservice.repository.ProposalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Encapsulates state transitions:
 * DRAFT -> submit() -> UNDER_REVIEW
 * approve()/reject() step by step -> APPROVED/REJECTED
 * Emits Kafka events on each transition.
 */
@Service
public class ApprovalWorkflowService {
    private final ProposalRepository proposals;
    private final ApprovalStepRepository steps;
    private final EventPublisher publisher;

    @Value("${ipm.default-approval-chain:PEER_REVIEW,MANAGER_APPROVAL,COMPLIANCE}")
    private String defaultChain;

    public ApprovalWorkflowService(ProposalRepository proposals,
                                   ApprovalStepRepository steps,
                                   EventPublisher publisher) {
        this.proposals = proposals;
        this.steps = steps;
        this.publisher = publisher;
    }

    /** Move from DRAFT to UNDER_REVIEW with configured chain. */
    @Transactional
    public Proposal submit(Long id, List<String> customChain) {
        Proposal p = proposals.findById(id)
                .orElseThrow(() -> new BadRequestException("Proposal not found: " + id));

        if (p.getStatus() != ProposalStatus.DRAFT)
            throw new BadRequestException("Only DRAFT proposals can be submitted.");

        List<String> chain = (customChain == null || customChain.isEmpty())
                ? Arrays.asList(defaultChain.split(","))
                : customChain;

        // Rebuild steps for this submission
        p.getSteps().clear();
        for (int i = 0; i < chain.size(); i++) {
            ApprovalStep s = new ApprovalStep();
            s.setProposal(p);
            s.setStepOrder(i);
            s.setRole(chain.get(i).trim());
            s.setDecision(Decision.PENDING);
            p.getSteps().add(s);
        }
        p.setStatus(ProposalStatus.UNDER_REVIEW);
        p.setSubmittedAt(OffsetDateTime.now());
        p.setCurrentStepIndex(0);

        Proposal saved = proposals.save(p);

        publisher.publish(new ProposalEvent(UUID.randomUUID().toString(),
                ProposalEventType.PROPOSAL_SUBMITTED, saved.getId(), Map.of("chain", chain)));

        return saved;
    }

    /** Approve current step; advance pointer or finalize APPROVED. */
    @Transactional
    public Proposal approve(Long id, ApproveRequest req) {
        Proposal p = loadActive(id);
        ApprovalStep current = getCurrentStep(p);

        current.setApprover(req.getApprover());
        current.setComments(req.getComments());
        current.setDecision(Decision.APPROVED);
        current.setDecidedAt(OffsetDateTime.now());

        boolean lastStep = p.getCurrentStepIndex() >= p.getSteps().size() - 1;
        if (lastStep) {
            p.setStatus(ProposalStatus.APPROVED);
            publisher.publish(new ProposalEvent(UUID.randomUUID().toString(),
                    ProposalEventType.PROPOSAL_APPROVED, p.getId(),
                    Map.of("role", current.getRole(), "approver", req.getApprover())));
        } else {
            p.setCurrentStepIndex(p.getCurrentStepIndex() + 1);
            publisher.publish(new ProposalEvent(UUID.randomUUID().toString(),
                    ProposalEventType.STEP_APPROVED, p.getId(),
                    Map.of("role", current.getRole(), "approver", req.getApprover(), "nextStep", p.getCurrentStepIndex())));
        }
        return proposals.save(p);
    }

    /** Reject current step; finalize REJECTED. */
    @Transactional
    public Proposal reject(Long id, RejectRequest req) {
        Proposal p = loadActive(id);
        ApprovalStep current = getCurrentStep(p);

        current.setApprover(req.getApprover());
        current.setComments(req.getComments());
        current.setDecision(Decision.REJECTED);
        current.setDecidedAt(OffsetDateTime.now());

        p.setStatus(ProposalStatus.REJECTED);

        publisher.publish(new ProposalEvent(UUID.randomUUID().toString(),
                ProposalEventType.PROPOSAL_REJECTED, p.getId(),
                Map.of("role", current.getRole(), "approver", req.getApprover(), "reason", req.getComments())));

        return proposals.save(p);
    }

    // ---- helpers ----
    private Proposal loadActive(Long id) {
        Proposal p = proposals.findById(id)
                .orElseThrow(() -> new BadRequestException("Proposal not found: " + id));
        if (p.getStatus() != ProposalStatus.UNDER_REVIEW)
            throw new BadRequestException("Proposal is not UNDER_REVIEW; status=" + p.getStatus());
        if (p.getSteps().isEmpty())
            throw new BadRequestException("No approval steps configured.");
        return p;
    }

    private ApprovalStep getCurrentStep(Proposal p) {
        int idx = p.getCurrentStepIndex();
        if (idx < 0 || idx >= p.getSteps().size())
            throw new BadRequestException("Invalid current step index=" + idx);
        ApprovalStep s = p.getSteps().get(idx);
        if (s.getDecision() != Decision.PENDING)
            throw new BadRequestException("Current step already decided.");
        return s;
    }
}
```

### Controllers

`src/main/java/com/uc/proposalservice/controller/ProposalController.java`

```java
package com.uc.proposalservice.controller;

import com.uc.proposalservice.dto.CreateProposalRequest;
import com.uc.proposalservice.dto.ProposalResponse;
import com.uc.proposalservice.entity.Proposal;
import com.uc.proposalservice.enums.ProposalStatus;
import com.uc.proposalservice.service.ProposalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/** CRUD-ish endpoints (no approval transitions here). */
@RestController
@RequestMapping("/api/proposals")
public class ProposalController {
    private final ProposalService svc;
    public ProposalController(ProposalService svc){ this.svc = svc; }

    @PostMapping
    public ProposalResponse create(@Valid @RequestBody CreateProposalRequest req){ return svc.create(req); }

    @GetMapping("/{id}")
    public ProposalResponse get(@PathVariable Long id){ return svc.get(id); }

    @GetMapping
    public Page<Proposal> list(@RequestParam(required=false) ProposalStatus status, Pageable pageable){
        return svc.list(status, pageable);
    }
}
```

`src/main/java/com/uc/proposalservice/controller/ApprovalController.java`

```java
package com.uc.proposalservice.controller;

import com.uc.proposalservice.dto.ApproveRequest;
import com.uc.proposalservice.dto.RejectRequest;
import com.uc.proposalservice.entity.Proposal;
import com.uc.proposalservice.service.ApprovalWorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Approval actions: submit, approve, reject. */
@RestController
@RequestMapping("/api/proposals")
public class ApprovalController {
    private final ApprovalWorkflowService wf;
    public ApprovalController(ApprovalWorkflowService wf){ this.wf = wf; }

    /** Submit for review. Optional custom chain body ["TEAM_LEAD","RISK","CFO"]. */
    @PostMapping("/{id}/submit")
    public Proposal submit(@PathVariable Long id, @RequestBody(required=false) List<String> customChain){
        return wf.submit(id, customChain);
    }

    @PostMapping("/{id}/approve")
    public Proposal approve(@PathVariable Long id, @Valid @RequestBody ApproveRequest req){
        return wf.approve(id, req);
    }

    @PostMapping("/{id}/reject")
    public Proposal reject(@PathVariable Long id, @Valid @RequestBody RejectRequest req){
        return wf.reject(id, req);
    }
}
```

---

# Service 2: `audit-service` (port 8082)

> Consumes `proposal-events`, persists an **audit trail** in H2, and exposes `/api/audit` to query.

### `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.uc</groupId>
  <artifactId>audit-service</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <name>audit-service</name>
  <description>Audit Service for IPM</description>

  <properties>
    <java.version>17</java.version>
    <spring-boot.version>3.3.2</spring-boot.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <!-- Web + Validation -->
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>

    <!-- JPA + H2 -->
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
    <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>runtime</scope></dependency>

    <!-- Kafka -->
    <dependency><groupId>org.springframework.kafka</groupId><artifactId>spring-kafka</artifactId></dependency>

    <!-- Swagger / OpenAPI -->
    <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>2.6.0</version></dependency>

    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId><version>3.11.0</version>
        <configuration><source>${java.version}</source><target>${java.version}</target><compilerArgs><arg>-parameters</arg></compilerArgs></configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

### `src/main/resources/application.yml`

```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:h2:mem:auditservice;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false
    username: sa
    password: ""
    driverClassName: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: audit-service
      properties:
        spring:
          json:
            trusted:
              packages: "*"

logging:
  level:
    root: INFO
    com.uc: DEBUG
    org.apache.kafka: WARN
```

### `src/main/java/com/uc/auditservice/Application.java`

```java
package com.uc.auditservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Bootstraps the Audit Service. */
@SpringBootApplication
public class Application {
    public static void main(String[] args) { SpringApplication.run(Application.class, args); }
}
```

### `src/main/java/com/uc/auditservice/config/KafkaConfig.java`

```java
package com.uc.auditservice.config;

import com.uc.common.ProposalEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

/** Minimal consumer config for audit-service. */
@EnableKafka
@Configuration
public class KafkaConfig {
    public static final String EVENTS_TOPIC = "proposal-events";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, ProposalEvent> eventConsumerFactory() {
        JsonDeserializer<ProposalEvent> json = new JsonDeserializer<>(ProposalEvent.class);
        json.addTrustedPackages("*");
        var props = new java.util.HashMap<String,Object>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, "audit-service");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), json);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProposalEvent> kafkaListenerContainerFactory() {
        var f = new ConcurrentKafkaListenerContainerFactory<String, ProposalEvent>();
        f.setConsumerFactory(eventConsumerFactory());
        f.setConcurrency(2);
        return f;
    }
}
```

### `src/main/java/com/uc/auditservice/config/SwaggerConfig.java`

```java
package com.uc.auditservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI at /swagger-ui */
@Configuration
public class SwaggerConfig {
    @Bean public OpenAPI api(){ return new OpenAPI().info(new Info().title("audit-service API").version("v1")); }
}
```

### Entity / Repository / Service / Listener / Controller

`src/main/java/com/uc/auditservice/entity/AuditEntry.java`

```java
package com.uc.auditservice.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/** Each consumed event becomes one persisted audit row. */
@Entity @Table(name="audit_entries")
public class AuditEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;
    private String eventType;
    private Long proposalId;

    @Column(length = 4000)
    private String payloadJson;

    private OffsetDateTime at;

    // Getters/Setters
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getEventId(){return eventId;} public void setEventId(String v){eventId=v;}
    public String getEventType(){return eventType;} public void setEventType(String v){eventType=v;}
    public Long getProposalId(){return proposalId;} public void setProposalId(Long v){proposalId=v;}
    public String getPayloadJson(){return payloadJson;} public void setPayloadJson(String v){payloadJson=v;}
    public OffsetDateTime getAt(){return at;} public void setAt(OffsetDateTime v){at=v;}
}
```

`src/main/java/com/uc/auditservice/repository/AuditRepository.java`

```java
package com.uc.auditservice.repository;

import com.uc.auditservice.entity.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Paging repo, allows filtering by proposalId. */
public interface AuditRepository extends JpaRepository<AuditEntry, Long> {
    Page<AuditEntry> findByProposalId(Long proposalId, Pageable pageable);
}
```

`src/main/java/com/uc/auditservice/service/AuditService.java`

```java
package com.uc.auditservice.service;

import com.uc.auditservice.entity.AuditEntry;
import com.uc.auditservice.repository.AuditRepository;
import com.uc.common.ProposalEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

/** Converts consumed event -> AuditEntry row. */
@Service
public class AuditService {
    private final AuditRepository repo;
    private final ObjectMapper om = new ObjectMapper();
    public AuditService(AuditRepository repo){ this.repo = repo; }

    public void save(ProposalEvent ev){
        try {
            AuditEntry e = new AuditEntry();
            e.setEventId(ev.getId());
            e.setEventType(ev.getType().name());
            e.setProposalId(ev.getProposalId());
            e.setPayloadJson(om.writeValueAsString(ev.getPayload()==null? Map.of(): ev.getPayload()));
            e.setAt(ev.getAt());
            repo.save(e);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Page<AuditEntry> list(Long proposalId, Pageable pageable){
        return proposalId == null ? repo.findAll(pageable) : repo.findByProposalId(proposalId, pageable);
    }
}
```

`src/main/java/com/uc/auditservice/kafka/ProposalEventListener.java`

```java
package com.uc.auditservice.kafka;

import com.uc.auditservice.service.AuditService;
import com.uc.common.ProposalEvent;
import com.uc.auditservice.config.KafkaConfig;
import org.apache.commons.logging.Log; import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** Consumes 'proposal-events' and persists them. */
@Component
public class ProposalEventListener {
    private static final Log log = LogFactory.getLog(ProposalEventListener.class);
    private final AuditService audit;
    public ProposalEventListener(AuditService audit){ this.audit = audit; }

    @KafkaListener(topics = KafkaConfig.EVENTS_TOPIC, groupId = "audit-service")
    public void onEvent(@Payload ProposalEvent ev){
        log.info("audit-service storing event " + ev.getType() + " for proposal " + ev.getProposalId());
        audit.save(ev);
    }
}
```

`src/main/java/com/uc/auditservice/controller/AuditController.java`

```java
package com.uc.auditservice.controller;

import com.uc.auditservice.entity.AuditEntry;
import com.uc.auditservice.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/** Simple paging API to inspect audit rows. */
@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditService svc;
    public AuditController(AuditService svc){ this.svc = svc; }

    @GetMapping
    public Page<AuditEntry> list(@RequestParam(required=false) Long proposalId, Pageable pageable){
        return svc.list(proposalId, pageable);
    }
}
```

---

# Service 3: `notification-service` (port 8083)

> Consumes `proposal-events` and **simulates notifications** (logs that an email/SMS would be sent).

### `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.uc</groupId>
  <artifactId>notification-service</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <name>notification-service</name>
  <description>Notification Service for IPM</description>

  <properties>
    <java.version>17</java.version>
    <spring-boot.version>3.3.2</spring-boot.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.springframework.kafka</groupId><artifactId>spring-kafka</artifactId></dependency>
    <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>2.6.0</version></dependency>
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId><version>3.11.0</version>
        <configuration><source>${java.version}</source><target>${java.version}</target><compilerArgs><arg>-parameters</arg></compilerArgs></configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

### `src/main/resources/application.yml`

```yaml
server:
  port: 8083

spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: notification-service
      properties:
        spring:
          json:
            trusted:
              packages: "*"

logging:
  level:
    root: INFO
    com.uc: DEBUG
    org.apache.kafka: WARN
```

### `src/main/java/com/uc/notificationservice/Application.java`

```java
package com.uc.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Bootstraps the Notification Service. */
@SpringBootApplication
public class Application {
    public static void main(String[] args) { SpringApplication.run(Application.class, args); }
}
```

### `src/main/java/com/uc/notificationservice/config/KafkaConfig.java`

```java
package com.uc.notificationservice.config;

import com.uc.common.ProposalEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

/** Minimal consumer config for notification-service. */
@EnableKafka
@Configuration
public class KafkaConfig {
    public static final String EVENTS_TOPIC = "proposal-events";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, ProposalEvent> eventConsumerFactory() {
        JsonDeserializer<ProposalEvent> json = new JsonDeserializer<>(ProposalEvent.class);
        json.addTrustedPackages("*");
        var props = new java.util.HashMap<String,Object>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, "notification-service");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), json);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProposalEvent> kafkaListenerContainerFactory() {
        var f = new ConcurrentKafkaListenerContainerFactory<String, ProposalEvent>();
        f.setConsumerFactory(eventConsumerFactory());
        f.setConcurrency(2);
        return f;
    }
}
```

### `src/main/java/com/uc/notificationservice/config/SwaggerConfig.java`

```java
package com.uc.notificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI at /swagger-ui */
@Configuration
public class SwaggerConfig {
    @Bean public OpenAPI api(){ return new OpenAPI().info(new Info().title("notification-service API").version("v1")); }
}
```

### Kafka listener & trivial health endpoint

`src/main/java/com/uc/notificationservice/kafka/NotificationListener.java`

```java
package com.uc.notificationservice.kafka;

import com.uc.common.ProposalEvent;
import com.uc.notificationservice.config.KafkaConfig;
import org.apache.commons.logging.Log; import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Simulates sending notifications. In real life, inject an Email/SMS gateway
 * and branch behavior by event type or payload content.
 */
@Component
public class NotificationListener {
    private static final Log log = LogFactory.getLog(NotificationListener.class);

    @KafkaListener(topics = KafkaConfig.EVENTS_TOPIC, groupId = "notification-service")
    public void onEvent(@Payload ProposalEvent ev){
        log.info("notification-service would notify: " + ev.getType() + " for proposal " + ev.getProposalId());
    }
}
```

`src/main/java/com/uc/notificationservice/controller/HealthController.java`

```java
package com.uc.notificationservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/** Simple health endpoint for smoke checks. */
@RestController
public class HealthController {
    @GetMapping("/api/health")
    public Map<String, String> health(){ return Map.of("status","ok"); }
}
```

---

## How to run (recap)

1. Start Kafka:

```bash
docker compose up -d
# Kafka UI: http://localhost:8080
```

2. Run services (three terminals):

```bash
cd proposal-service && mvn spring-boot:run
cd audit-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

3. Try the flow:

```bash
# Create
curl -s -X POST http://localhost:8081/api/proposals \
 -H "Content-Type: application/json" \
 -d '{"title":"IPO Fund","applicantName":"Riya","amount":150000,"description":"New fund"}'

# Submit (default chain)
curl -s -X POST http://localhost:8081/api/proposals/1/submit

# Approve current step
curl -s -X POST http://localhost:8081/api/proposals/1/approve \
 -H "Content-Type: application/json" \
 -d '{"approver":"john.doe","comments":"Looks good"}'

# Inspect audit
curl -s "http://localhost:8082/api/audit?proposalId=1"
```

---

If you want, I can also:

* add **JWT security** across services,
* switch **Proposal DB to Oracle** with **Flyway**,
* add **Dockerfiles** for each service and a **docker-compose-services.yml** to run all 3 services in containers.

_____________________________________________________________________________________________________________________


Here’s how to open Swagger for each service (once they’re running):

* Proposal Service → `http://localhost:8081/swagger-ui/index.html`
  OpenAPI JSON: `http://localhost:8081/v3/api-docs`

* Audit Service → `http://localhost:8082/swagger-ui/index.html`
  OpenAPI JSON: `http://localhost:8082/v3/api-docs`

* Notification Service → `http://localhost:8083/swagger-ui/index.html`
  OpenAPI JSON: `http://localhost:8083/v3/api-docs`

### Quick steps

1. Start Kafka stack:

```bash
docker compose up -d
```

2. Start each service in its own terminal:

```bash
mvn -pl proposal-service spring-boot:run
mvn -pl audit-service spring-boot:run
mvn -pl notification-service spring-boot:run
```

3. Visit the Swagger URLs above in your browser.

### If Swagger doesn’t load (404)

* Make sure each service includes the dependency:
  `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0`
* Ensure the app actually started on the expected port (8081/8082/8083).
* We also added a `SwaggerConfig` bean in each service—keep it as-is.
* If you want a custom path (e.g., `/docs`), add this to the service’s `application.yml`:

  ```yaml
  springdoc:
    swagger-ui:
      path: /docs
  ```

  Then open `http://localhost:<port>/docs`.

(Heads-up: Kafka UI from Docker Compose is at `http://localhost:8080`, unrelated to Swagger.)

______________________________________________________________________________________________________________________

#################################################################################################################

Here’s how to open Swagger for each service (once they’re running):

* Proposal Service → `http://localhost:8081/swagger-ui/index.html`
  OpenAPI JSON: `http://localhost:8081/v3/api-docs`

* Audit Service → `http://localhost:8082/swagger-ui/index.html`
  OpenAPI JSON: `http://localhost:8082/v3/api-docs`

* Notification Service → `http://localhost:8083/swagger-ui/index.html`
  OpenAPI JSON: `http://localhost:8083/v3/api-docs`

### Quick steps

1. Start Kafka stack:

```bash
docker compose up -d
```

2. Start each service in its own terminal:

```bash
mvn -pl proposal-service spring-boot:run
mvn -pl audit-service spring-boot:run
mvn -pl notification-service spring-boot:run
```

3. Visit the Swagger URLs above in your browser.

### If Swagger doesn’t load (404)

* Make sure each service includes the dependency:
  `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0`
* Ensure the app actually started on the expected port (8081/8082/8083).
* We also added a `SwaggerConfig` bean in each service—keep it as-is.
* If you want a custom path (e.g., `/docs`), add this to the service’s `application.yml`:

  ```yaml
  springdoc:
    swagger-ui:
      path: /docs
  ```

  Then open `http://localhost:<port>/docs`.

(Heads-up: Kafka UI from Docker Compose is at `http://localhost:8080`, unrelated to Swagger.)
##########################################################################################################
how the kafka flow works in this project

Here’s the Kafka flow in this project—short, practical, and mapped to the code you have.

---

# What gets created

**Topics (from `proposal-service` → `KafkaConfig`):**

* `proposal-events` (3 partitions) — JSON **domain events**
* `proposal-events.DLT` (3 partitions) — **dead-letter** for failed records
* `audit-logs` (1 partition) — plain-text **audit lines**

**Event payload type (shared POJO):**

```java
class ProposalEvent {
  String id;                 // UUID
  ProposalEventType type;    // SUBMITTED, STEP_APPROVED, ...
  Long proposalId;
  Map<String,Object> payload;// small details (role, approver, nextStep...)
  OffsetDateTime at;         // event time
}
```

---

# Who produces

**`proposal-service`**

* File: `proposal-service/kafka/EventPublisher.java`
* Sends **JSON** to `proposal-events` and a **string** line to `audit-logs`
* **Key** = `String.valueOf(proposalId)` → guarantees **per-proposal ordering** (all events for one proposal land in the same partition)

Emitted when:

* `POST /api/proposals/{id}/submit` → `PROPOSAL_SUBMITTED`
* `POST /api/proposals/{id}/approve` → `STEP_APPROVED` (or `PROPOSAL_APPROVED` at final step)
* `POST /api/proposals/{id}/reject` → `PROPOSAL_REJECTED`

---

# Who consumes (fan-out)

Each service has its **own consumer group**, so **all** see **every** event:

* **`audit-service`** (`groupId = audit-service`)
  `kafka/ProposalEventListener.java` → persists to H2 (`AuditEntry`) → query via `GET /api/audit`

* **`notification-service`** (`groupId = notification-service`)
  `kafka/NotificationListener.java` → logs that it would send email/SMS

* **`proposal-service`** (optional self-listener)
  `kafka/ProposalEventConsumer.java` → just logs events for debugging

---

# Reliability: retries & DLT

Configured in each service’s `KafkaConfig`:

* **Retry**: `FixedBackOff(1000ms, 3)` on listener errors
* **After retries fail**: message is published to **`proposal-events.DLT`** by `DeadLetterPublishingRecoverer`
* Partition is preserved when sent to DLT (helps with ordering analysis)
* Delivery semantics are **at-least-once**; make consumers idempotent if they cause side-effects

---

# End-to-end example (approve path)

```
Client
  POST /api/proposals/1/approve  { "approver":"john.doe", "comments":"OK" }

proposal-service
  ApprovalWorkflowService.approve(...)  // updates JPA state
  ↓
  EventPublisher.publish(
     ProposalEvent{ type=STEP_APPROVED, proposalId=1, payload={role, approver, nextStep} }
  )  → topic: proposal-events, key: "1"
  also → "audit-logs" (plain text line)

Kafka
  proposal-events (3 partitions) -- key "1" puts this record on one partition deterministically

Consumers (different groups → all receive it)
  audit-service@partX        → save AuditEntry in H2
  notification-service@partX → log "would notify STEP_APPROVED for 1"
  (optional) proposal-service logs the event too
```

---

# Where to look in the code

* **Producer & topics**:
  `proposal-service/config/KafkaConfig.java`
  `proposal-service/kafka/EventPublisher.java`

* **When events fire**:
  `proposal-service/service/ApprovalWorkflowService.java`
  (methods `submit`, `approve`, `reject`)

* **Consumers**:
  `audit-service/kafka/ProposalEventListener.java`
  `notification-service/kafka/NotificationListener.java`
  `proposal-service/kafka/ProposalEventConsumer.java` (optional)

---

# See it live

1. Start Kafka:

```bash
docker compose up -d
```

2. Run services (separate terminals):

```bash
mvn -pl proposal-service spring-boot:run
mvn -pl audit-service spring-boot:run
mvn -pl notification-service spring-boot:run
```

3. Hit APIs and watch messages:

```bash
# create → submit → approve
curl -s -X POST http://localhost:8081/api/proposals \
  -H "Content-Type: application/json" \
  -d '{"title":"IPO Fund","applicantName":"Riya","amount":150000,"description":"New fund"}'
curl -s -X POST http://localhost:8081/api/proposals/1/submit
curl -s -X POST http://localhost:8081/api/proposals/1/approve \
  -H "Content-Type: application/json" -d '{"approver":"john.doe","comments":"OK"}'
```

4. Inspect in **Kafka UI**: `http://localhost:8080`

  * `proposal-events` for JSON events
  * `audit-logs` for readable lines
  * `proposal-events.DLT` for any dead letters

5. Swagger UIs:
   notification-service API
   http://localhost:8083/swagger-ui/index.html
   audit-service API
   http://localhost:8082/swagger-ui/index.html
   proposal-service API
   http://localhost:8081/swagger-ui/index.html
6. 
(Optional CLI watchers via Docker):

```bash
docker compose exec kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 --topic proposal-events --from-beginning \
  --property print.key=true --property key.separator=" : "

docker compose exec kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 --topic proposal-events.DLT --from-beginning
```

---

# Notes on scaling & ordering

* `proposal-events` has **3 partitions** → max parallelism per consumer group = 3
* Listeners use **concurrency 2** (extra threads but capped by assigned partitions)
* **Ordering is guaranteed per key** (we use `proposalId`), so each proposal’s history is strictly ordered

---

If you want, I can add: a tiny **DLT replayer**, **email provider integration**, or **transactional outbox** to make DB+Kafka writes atomic.
# topics: proposal-events 

Services:

proposal-service → producer (publishes JSON ProposalEvents + text audit lines)

audit-service → consumer (stores audit trail)

notification-service → consumer (logs “email/SMS” notifications)

Kafka UI: http://localhost:8080 (from docker compose)

# DLT = Dead Letter Topic** in Kafka.

### What it is (in plain words)

A **DLT** is a special Kafka topic where messages are sent **after a consumer keeps failing** to process them (even after configured retries). It lets you **quarantine bad records** so your stream keeps moving, while you **inspect, fix, and optionally replay** those messages later.

### How it works in your Investment Proposal system

* Main topic: `proposal-events`
* DLT: **`proposal-events.DLT`**
* If a consumer (e.g., `audit-service`) throws errors repeatedly (DB constraint, deserialization, NPE), Spring Kafka’s **`DefaultErrorHandler`** hands the record to a **`DeadLetterPublishingRecoverer`**, which publishes it to `proposal-events.DLT` (same partition as the original).
* The DLT record keeps:

  * the **original key/value** (so replays preserve ordering per `proposalId`)
  * **metadata headers** (original topic/partition/offset and exception info) to aid debugging.

### Why it’s useful

* **Prevents pipeline stalls**: one “poison” message doesn’t block the whole partition.
* **Observability & recovery**: you can see what failed, fix data/code, then **replay**.

### Where to see it

* **Kafka UI** (from your docker-compose): open `proposal-events.DLT`
* CLI:

  ```bash
  docker compose exec kafka kafka-console-consumer \
    --bootstrap-server kafka:29092 \
    --topic proposal-events.DLT --from-beginning \
    --property print.key=true --property key.separator=" : "
  ```

### The config you already have (essentials)

```java
// in KafkaConfig (each service)
DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(dltKafkaTemplate,
  (rec, ex) -> new TopicPartition(rec.topic() + ".DLT", rec.partition()));

DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
// 3 retries, 1s apart; then publish to proposal-events.DLT
factory.setCommonErrorHandler(handler);
```

### Replaying from the DLT (safe trickle replay)

```java
// Example: re-publish DLT records back to the main topic after you fixed the bug/data
@Component
public class DltReplayer {
  private final KafkaTemplate<String, ProposalEvent> template;
  public DltReplayer(KafkaTemplate<String, ProposalEvent> template){ this.template = template; }

  @KafkaListener(topics = "proposal-events.DLT", groupId = "replayer")
  public void reprocess(ProposalEvent ev,
      @org.springframework.messaging.handler.annotation.Header(
        org.springframework.kafka.support.KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {

    // add guardrails as needed (filters, time window, manual trigger)
    template.send("proposal-events", key, ev);
  }
}
```

### Tips

* Set **retention** on DLT long enough for investigation.
* Make consumers **idempotent** (e.g., `audit-service` dedup by `eventId`) so replays are safe.
* Treat DLT volume spikes as alerts: they often indicate schema mismatches or downstream outages.

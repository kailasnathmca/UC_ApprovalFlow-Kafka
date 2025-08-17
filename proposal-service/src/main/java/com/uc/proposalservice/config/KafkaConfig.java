package com.uc.proposalservice.config;

import com.uc.common.ProposalEvent;
// Domain object that represents a business event in the proposal workflow (submitted, approved, etc.).

import org.apache.kafka.clients.admin.NewTopic;
// Kafka Admin client API used here to auto-create topics during local development.

import org.apache.kafka.clients.producer.ProducerConfig;
// Provides configuration keys for Kafka producers.

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
// Default Kafka string serializer/deserializer classes.

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
// Enables detection of Kafka-related annotations like @KafkaListener.

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
// Core Spring Kafka classes for producer/consumer factories and KafkaTemplate.

import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
// Classes to handle errors: messages that can’t be processed go to a Dead Letter Topic (DLT).

import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
// Used to (de)serialize ProposalEvent as JSON when sending/receiving Kafka messages.

import org.springframework.util.backoff.FixedBackOff;
// Provides retry strategy (retry delay and max attempts).

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaConfig
 *
 * This configuration class sets up:
 * - Kafka producers (ProposalEvent producer + String producer for audit logs)
 * - Kafka consumers (with retry + Dead Letter Topic handling)
 * - Auto-creation of topics for local development
 *
 * Topics in UC context:
 *  - proposal-events: carries ProposalEvent messages
 *  - proposal-events.DLT: Dead Letter Topic for failed messages
 *  - audit-logs: simple string messages for compliance/audit tracking
 */
@EnableKafka
@Configuration
public class KafkaConfig {
    public static final String EVENTS_TOPIC = "proposal-events";
    public static final String AUDIT_TOPIC = "audit-logs";
    public static final String DLT_SUFFIX = ".DLT";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers; // Injected from application.yml/properties

    // ---- PRODUCERS ----

    /**
     * ProducerFactory for ProposalEvent messages.
     * Serializes key as String, value as JSON (ProposalEvent).
     */
    @Bean
    public ProducerFactory<String, ProposalEvent> eventProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * KafkaTemplate for sending ProposalEvent messages.
     * This is injected into services (e.g., ProposalService) to publish domain events.
     */
    @Bean
    public KafkaTemplate<String, ProposalEvent> eventKafkaTemplate() {
        return new KafkaTemplate<>(eventProducerFactory());
    }

    /**
     * ProducerFactory for simple String messages (used for audit logs).
     */
    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * KafkaTemplate for sending audit log messages as plain Strings.
     */
    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    // ---- CONSUMERS ----

    /**
     * ConsumerFactory for ProposalEvent messages.
     * Configures deserialization from JSON and basic consumer properties.
     */
    @Bean
    public ConsumerFactory<String, ProposalEvent> eventConsumerFactory() {
        JsonDeserializer<ProposalEvent> json = new JsonDeserializer<>(ProposalEvent.class);
        json.addTrustedPackages("*"); // Trust all packages for deserialization
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, "proposal-service");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), json);
    }

    /**
     * ConcurrentKafkaListenerContainerFactory for ProposalEvent.
     * - Sets up retry policy with FixedBackOff (1 second delay, 3 retries).
     * - If still failing → publishes to Dead Letter Topic (.DLT).
     * - Concurrency = 2 → runs 2 consumer threads in parallel.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProposalEvent> kafkaListenerContainerFactory(
            KafkaTemplate<String, ProposalEvent> dltTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, ProposalEvent> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(eventConsumerFactory());

        // DeadLetterPublishingRecoverer: sends failed messages to <originalTopic>.DLT
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dltTemplate,
                (record, ex) -> new org.apache.kafka.common.TopicPartition(record.topic() + DLT_SUFFIX, record.partition())
        );

        // DefaultErrorHandler with retry policy
        f.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L)));

        f.setConcurrency(2);
        return f;
    }

    // ---- TOPIC CREATION ----

    /**
     * Auto-creates proposal-events topic with 3 partitions, replication factor 1.
     */
    @Bean
    public NewTopic proposalEventsTopic() {
        return new NewTopic(EVENTS_TOPIC, 3, (short) 1);
    }

    /**
     * Auto-creates Dead Letter Topic for proposal-events.
     */
    @Bean
    public NewTopic proposalEventsDltTopic() {
        return new NewTopic(EVENTS_TOPIC + DLT_SUFFIX, 3, (short) 1);
    }

    /**
     * Auto-creates audit-logs topic with 1 partition.
     */
    @Bean
    public NewTopic auditTopic() {
        return new NewTopic(AUDIT_TOPIC, 1, (short) 1);
    }
}

package com.uc.notificationservice.config;

import com.uc.common.ProposalEvent;
// Domain event class shared via ipm-common module.
// This represents events like SUBMITTED, APPROVED, REJECTED etc. from Proposal Service.

import org.apache.kafka.common.serialization.StringDeserializer;
// Deserializer for String keys coming from Kafka messages.

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
// Spring Kafka utilities for configuring consumers, factories, and deserializers.

/**
 * Kafka consumer configuration for Notification Service.
 * Purpose: Allow this service to consume ProposalEvents published
 * by Proposal Service via Kafka topics.
 */
@EnableKafka // Enables detection of @KafkaListener annotated methods in this Spring Boot app.
@Configuration // Marks this class as a configuration class that defines Spring beans.
public class KafkaConfig {

    // Topic name constant used across this service.
    public static final String EVENTS_TOPIC = "proposal-events";

    // Injects Kafka broker addresses from application.yml/properties
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Bean: ConsumerFactory
     * - Creates Kafka consumers for messages with key type String and value type ProposalEvent.
     * - Configures connection to brokers, group id, and deserialization rules.
     */
    @Bean
    public ConsumerFactory<String, ProposalEvent> eventConsumerFactory() {
        // JSON deserializer to convert Kafka message values into ProposalEvent objects.
        JsonDeserializer<ProposalEvent> json = new JsonDeserializer<>(ProposalEvent.class);

        // Trust all packages (simplifies deserialization, but in prod you’d whitelist packages).
        json.addTrustedPackages("*");

        // Consumer configuration properties.
        var props = new java.util.HashMap<String,Object>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Kafka broker address(es).

        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, "notification-service");
        // Consumer group id ensures all notification-service instances belong to same group,
        // so Kafka load balances events among them (only one instance processes a given event).

        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // If no offset is stored for this group, start reading from earliest messages.

        // Create factory with String key deserializer and ProposalEvent JSON deserializer.
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), json);
    }

    /**
     * Bean: Kafka Listener Container Factory
     * - Creates listener containers that handle incoming Kafka messages.
     * - Used by methods annotated with @KafkaListener in this service.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProposalEvent> kafkaListenerContainerFactory() {
        var f = new ConcurrentKafkaListenerContainerFactory<String, ProposalEvent>();

        // Attach the consumer factory defined above.
        f.setConsumerFactory(eventConsumerFactory());

        // Configure concurrency = 2 → two threads can process messages in parallel.
        // This helps in scaling the consumption if topic has multiple partitions.
        f.setConcurrency(2);

        return f;
    }
}

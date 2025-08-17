package com.uc.notificationservice.kafka;

import com.uc.common.ProposalEvent;
// Domain event class (shared via ipm-common).
// This represents an event in the proposal workflow: SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, etc.

import com.uc.notificationservice.config.KafkaConfig;
// Imports KafkaConfig to reuse constants (e.g., topic name).

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
// Apache Commons Logging API used for logging messages.
// In Spring Boot, this typically delegates to Logback or Log4j2 under the hood.

import org.springframework.kafka.annotation.KafkaListener;
// Annotation to mark a method as a Kafka message listener (consumer).
// Behind the scenes, Spring Kafka creates a background thread(s) that will call this method
// whenever new messages are available on the specified topic.

import org.springframework.messaging.handler.annotation.Payload;
// Indicates that the method parameter should be bound to the message payload (the actual Kafka message body).

import org.springframework.stereotype.Component;
// Marks this class as a Spring-managed bean (discovered via component scanning).
// This way Spring can create and inject this listener where needed.

/**
 * NotificationListener
 *
 * This class is responsible for consuming messages (ProposalEvent) from Kafka
 * and simulating sending notifications.
 * In a real-world UC system:
 *   - It would integrate with external notification channels such as Email, SMS, or Push Notifications.
 *   - Logic could branch based on event type (SUBMITTED, APPROVED, etc.).
 */
@Component
public class NotificationListener {

    // Logger instance for writing logs.
    // Good practice for tracking consumed messages and debugging.
    private static final Log log = LogFactory.getLog(NotificationListener.class);

    /**
     * Method: onEvent
     *
     * - Annotated with @KafkaListener → Spring Kafka subscribes this method to a Kafka topic.
     * - "topics" = KafkaConfig.EVENTS_TOPIC → listens to "proposal-events".
     * - "groupId" = "notification-service" → ensures this service forms a consumer group
     *   for load balancing (only one instance processes each message).
     *
     * @param ev → The ProposalEvent object, deserialized automatically from JSON.
     */
    @KafkaListener(topics = KafkaConfig.EVENTS_TOPIC, groupId = "notification-service")
    public void onEvent(@Payload ProposalEvent ev) {
        // Log that the event was received.
        // In a real system, this would trigger sending an email/SMS/notification.
        log.info("notification-service would notify: "
                + ev.getType()           // e.g., SUBMITTED, APPROVED, REJECTED
                + " for proposal "
                + ev.getProposalId());   // The ID of the proposal that triggered this event.
    }
}

package com.uc.common;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * ProposalEvent (using Java 21 record)
 * <p>
 * This record is used as the payload for Kafka messages.
 * - Immutable → safer for concurrent systems
 * - Automatically generates equals(), hashCode(), toString()
 * - Works perfectly with Spring Kafka's JsonSerializer/JsonDeserializer
 */
public record ProposalEvent_J21_record(
        String id,                   // unique event id (UUID string)
        ProposalEventType type,      // event type (enum: CREATED, APPROVED, etc.)
        Long proposalId,             // which proposal this event is about
        Map<String, Object> payload, // extra details (role, approver, etc.)
        OffsetDateTime at            // when the event occurred
) {
    // Compact constructor: sets default timestamp if not provided
    public ProposalEvent_J21_record {
        if (at == null) {
            at = OffsetDateTime.now();
        }
    }

    // Convenience constructor (without timestamp → defaults to now)
    public ProposalEvent_J21_record(String id, ProposalEventType type, Long proposalId, Map<String, Object> payload) {
        this(id, type, proposalId, payload, OffsetDateTime.now());
    }
}


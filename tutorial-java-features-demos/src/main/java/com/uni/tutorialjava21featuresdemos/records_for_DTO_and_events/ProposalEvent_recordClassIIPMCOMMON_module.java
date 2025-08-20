package com.uni.tutorialjava21featuresdemos.records_for_DTO_and_events;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 1) Records are immutable data carriers; perfect for events.
 * 2) All fields are final; canonical constructor auto-generated.
 * 3) Jackson can serialize/deserialize them without setters.
 */
public record ProposalEvent_recordClassIIPMCOMMON_module(
        String id,
        Object type,
        Long proposalId,
        Map<String, Object> payload,
        OffsetDateTime at,
        int version // helps schema evolution
) {}
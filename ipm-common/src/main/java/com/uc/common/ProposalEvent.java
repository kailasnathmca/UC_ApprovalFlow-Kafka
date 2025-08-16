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

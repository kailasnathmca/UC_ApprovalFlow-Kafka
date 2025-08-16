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

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

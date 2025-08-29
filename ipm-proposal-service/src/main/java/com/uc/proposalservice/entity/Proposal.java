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

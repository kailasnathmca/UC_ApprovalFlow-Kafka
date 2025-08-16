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

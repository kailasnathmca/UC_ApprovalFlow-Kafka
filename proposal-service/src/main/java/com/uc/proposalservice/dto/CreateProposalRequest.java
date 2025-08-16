package com.uc.proposalservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/** Request body to create a proposal (starts in DRAFT). */
public class CreateProposalRequest {
    @NotBlank private String title;
    @NotBlank private String applicantName;
    @NotNull @DecimalMin("0.00") private BigDecimal amount;
    @Size(max = 2000) private String description;

    /** Optional override for chain: ["TEAM_LEAD","RISK","CFO"]. If null/empty, default chain is used on submit. */
    private List<String> approvalChain;

    // Getters/Setters
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getApplicantName() { return applicantName; } public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public List<String> getApprovalChain() { return approvalChain; } public void setApprovalChain(List<String> approvalChain) { this.approvalChain = approvalChain; }
}

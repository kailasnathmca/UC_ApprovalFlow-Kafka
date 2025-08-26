package com.uc.ipm.s2s.dto;

import jakarta.validation.constraints.NotNull;

public class RiskScoreRequest {
    @NotNull
    private Long proposalId;

    public RiskScoreRequest() {}
    public RiskScoreRequest(Long proposalId) { this.proposalId = proposalId; }
    public Long getProposalId() { return proposalId; }
    public void setProposalId(Long proposalId) { this.proposalId = proposalId; }
}

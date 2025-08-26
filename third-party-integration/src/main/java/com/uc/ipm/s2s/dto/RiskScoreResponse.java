package com.uc.ipm.s2s.dto;

public class RiskScoreResponse {
    private Long proposalId;
    private double score;
    private String riskLevel;

    public RiskScoreResponse() {}

    public RiskScoreResponse(Long proposalId, double score, String riskLevel) {
        this.proposalId = proposalId;
        this.score = score;
        this.riskLevel = riskLevel;
    }

    public Long getProposalId() { return proposalId; }
    public void setProposalId(Long proposalId) { this.proposalId = proposalId; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}

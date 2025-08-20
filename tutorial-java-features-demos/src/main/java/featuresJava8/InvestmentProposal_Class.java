package main.java.featuresJava8;

import java.time.LocalDateTime;

/**
 * Investment Proposal class representing a client's investment request
 * Models real-world investment data with all necessary attributes
 */
class InvestmentProposal_Class {
    private String id;                    // Unique identifier for tracking proposals
    private String clientName;           // Client identification for relationship management
    private double proposedAmount;       // Investment amount for portfolio allocation
    private double expectedReturn;       // Expected ROI for performance analysis
    private String riskLevel;            // Risk assessment for portfolio diversification
    private LocalDateTime proposalDate;  // Timestamp for compliance and audit trails
    private boolean approved;            // Approval status for operational workflows
    private String investmentType;       // Asset class for portfolio categorization
    private String advisorName;          // Advisor assignment for accountability

    /**
     * Constructor for creating new investment proposals
     * @param id Unique proposal identifier
     * @param clientName Client name for relationship management
     * @param proposedAmount Investment amount for allocation planning
     * @param expectedReturn Expected return for performance forecasting
     * @param riskLevel Risk level for portfolio optimization
     * @param investmentType Asset class for diversification strategy
     * @param advisorName Assigned advisor for service delivery
     */
    public InvestmentProposal_Class(String id, String clientName, double proposedAmount,
                                    double expectedReturn, String riskLevel, String investmentType,
                                    String advisorName) {
        this.id = id;
        this.clientName = clientName;
        this.proposedAmount = proposedAmount;
        this.expectedReturn = expectedReturn;
        this.riskLevel = riskLevel;
        this.investmentType = investmentType;
        this.advisorName = advisorName;
        this.proposalDate = LocalDateTime.now(); // Automatic timestamping for compliance
        this.approved = false; // Default unapproved state for risk management
    }

    // Getters and setters for data encapsulation and access control
    public String getId() { return id; }
    public String getClientName() { return clientName; }
    public double getProposedAmount() { return proposedAmount; }
    public double getExpectedReturn() { return expectedReturn; }
    public String getRiskLevel() { return riskLevel; }
    public LocalDateTime getProposalDate() { return proposalDate; }
    public boolean isApproved() { return approved; }
    public String getInvestmentType() { return investmentType; }
    public String getAdvisorName() { return advisorName; }
    public void setApproved(boolean approved) { this.approved = approved; }

    /**
     * String representation for logging and reporting purposes
     * Enables clear audit trails and status tracking
     */
    @Override
    public String toString() {
        return String.format("Proposal{id='%s', client='%s', amount=$%.2f, return=%.2f%%, risk='%s', approved=%s}",
                id, clientName, proposedAmount, expectedReturn, riskLevel, approved);
    }
}

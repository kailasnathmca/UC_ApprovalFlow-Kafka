package main.java.featuresJava8;

/**
 * Standard Investment Manager for routine investment approvals
 * Handles lower-tier investments with automated processing capabilities
 * Reduces operational overhead for standard client requests
 */
final class StandardAbstractClassInvestmentManager extends AbstractClass_InvestmentManager {
    @Override
    public void processProposal(InvestmentProposal_Class proposal) {
        System.out.println("Standard manager processing: " + proposal.getId());
        // Automated approval for standard investment limits
        if (proposal.getProposedAmount() <= 100000) {
            proposal.setApproved(true);
            System.out.println("Proposal approved by Standard Manager - automated workflow");
        } else {
            System.out.println("Proposal requires Premium Manager escalation - risk control");
        }
    }

    @Override
    public String getManagerType() {
        return "Standard";
    }
}

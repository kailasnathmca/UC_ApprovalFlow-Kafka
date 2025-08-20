package main.java.featuresJava8;

/**
 * Premium Investment Manager for high-value investment approvals
 * Handles complex, high-value investments requiring specialized review
 * Ensures proper oversight for significant client investments
 */
final class PremiumAbstractClassInvestmentManager extends AbstractClass_InvestmentManager {
    @Override
    public void processProposal(InvestmentProposal_Class proposal) {
        System.out.println("Premium manager processing: " + proposal.getId());
        // Enhanced review for premium investment limits
        if (proposal.getProposedAmount() <= 1000000) {
            proposal.setApproved(true);
            System.out.println("Proposal approved by Premium Manager - specialized review");
        } else {
            System.out.println("Proposal exceeds maximum limit - escalation required");
        }
    }

    @Override
    public String getManagerType() {
        return "Premium";
    }
}

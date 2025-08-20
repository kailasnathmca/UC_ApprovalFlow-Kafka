package main.java.featuresJava8;

/**
 * Abstract base class for investment managers
 * Provides common functionality while allowing specialized implementations
 * Helps in creating a flexible approval hierarchy based on investment tiers
 *
 */
abstract class AbstractClass_InvestmentManager {
    public abstract void processProposal(InvestmentProposal_Class proposal);
    public abstract String getManagerType();
}

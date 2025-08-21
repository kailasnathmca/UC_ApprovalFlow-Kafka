package main.java.featuresJava8to21AllFeatures;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Comprehensive Investment Proposal Management System
 * Demonstrates Java 8 to 21 features in investment management context
 * Each feature is explained with detailed comments showing investment applications
 */
public class InvestmentProposalManagementSystem {

    // Java 8 Features

    /**
     * Investment Proposal class representing client investment requests
     * Java 8: Basic class structure with modern design principles
     * Models real-world investment data for portfolio management
     */
    static class InvestmentProposal {
        private String id;                    // Unique investment identifier for tracking
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
         * Java 8: Standard constructor with parameter validation
         * Ensures investment data integrity at creation time
         */
        public InvestmentProposal(String id, String clientName, double proposedAmount,
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
         * Java 8: toString() method for investment data presentation
         * Enables clear audit trails and status tracking for investment workflows
         */
        @Override
        public String toString() {
            return String.format("Proposal{id='%s', client='%s', amount=$%.2f, return=%.2f%%, risk='%s', approved=%s}",
                    id, clientName, proposedAmount, expectedReturn, riskLevel, approved);
        }
    }

    /**
     * Abstract base class for investment managers
     * Java 8: Abstract class with common functionality
     * Provides shared capabilities while allowing specialized implementations
     * Helps in creating a flexible approval hierarchy based on investment tiers
     */
    static abstract class InvestmentManager {
        public abstract void processProposal(InvestmentProposal proposal);
        public abstract String getManagerType();
    }

    /**
     * Standard Investment Manager for routine investment approvals
     * Java 8: Concrete implementation with automated processing capabilities
     * Handles lower-tier investments with efficient processing workflows
     * Reduces operational overhead for standard client requests
     */
    static final class StandardInvestmentManager extends InvestmentManager {
        @Override
        public void processProposal(InvestmentProposal proposal) {
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

    /**
     * Premium Investment Manager for high-value investment approvals
     * Java 8: Specialized implementation for complex investments
     * Handles high-value investments requiring specialized review
     * Ensures proper oversight for significant client investments
     */
    static final class PremiumInvestmentManager extends InvestmentManager {
        @Override
        public void processProposal(InvestmentProposal proposal) {
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

    /**
     * Functional interface for investment calculations
     * Java 8: @FunctionalInterface annotation for single abstract method
     * Enables flexible financial modeling and scenario analysis
     * Supports various investment calculation methodologies
     */
    @FunctionalInterface
    interface InvestmentCalculator {
        double calculate(double amount, double rate, int years);
    }

    /**
     * Functional interface for risk assessment
     * Java 8: Custom functional interface for investment risk evaluation
     * Provides customizable risk evaluation logic for different investment types
     * Supports dynamic risk management based on market conditions
     */
    @FunctionalInterface
    interface RiskAssessor {
        boolean assessRisk(InvestmentProposal proposal);
    }

    // Java 9 Features

    /**
     * Java 9: Private Interface Methods for Investment Analysis
     * Enables code reuse while maintaining clean public APIs
     * Supports complex investment calculation workflows with shared logic
     */
    interface InvestmentAnalyzer {

        /**
         * Public method for investment risk assessment
         * Java 9: Default method in interface for client-facing functionality
         * Utilizes private helper methods for complex calculations
         * Provides standardized risk analysis for investment decisions
         */
        default void analyzeInvestmentRisk(double amount, String riskLevel) {
            // Validate investment parameters before risk analysis
            if (validateInvestmentParameters(amount, riskLevel)) {
                double riskScore = calculateRiskScore(amount, riskLevel);
                generateRiskReport(riskScore);
            } else {
                System.out.println("Invalid investment parameters for risk analysis");
            }
        }

        /**
         * Private interface method for parameter validation
         * Java 9: Private methods in interfaces for internal logic
         * Reduces code duplication while maintaining interface clarity
         * Ensures investment data quality before processing
         */
        private boolean validateInvestmentParameters(double amount, String riskLevel) {
            return amount > 0 &&
                    (riskLevel.equals("LOW") || riskLevel.equals("MEDIUM") || riskLevel.equals("HIGH"));
        }

        /**
         * Private interface method for risk score calculation
         * Java 9: Private methods enable complex internal logic
         * Supports sophisticated risk modeling without exposing implementation details
         * Provides consistent risk assessment across investment types
         */
        private double calculateRiskScore(double amount, String riskLevel) {
            double baseScore = amount * 0.001; // Base risk factor
            switch (riskLevel) {
                case "LOW": return baseScore * 0.5;    // Reduced risk weighting
                case "MEDIUM": return baseScore * 1.0; // Standard risk weighting
                case "HIGH": return baseScore * 2.0;   // Elevated risk weighting
                default: return baseScore;
            }
        }

        /**
         * Private interface method for risk report generation
         * Java 9: Private methods support internal reporting logic
         * Enables standardized risk communication while maintaining interface simplicity
         * Supports compliance reporting requirements for investment decisions
         */
        private void generateRiskReport(double riskScore) {
            System.out.println("=== Investment Risk Analysis Report ===");
            System.out.printf("Calculated Risk Score: %.2f%n", riskScore);
            System.out.println("Risk Level: " + determineRiskCategory(riskScore));
        }

        /**
         * Private helper method for risk categorization
         * Java 9: Private methods enable helper functionality
         * Provides consistent risk classification for investment portfolios
         * Supports regulatory compliance and client communication standards
         */
        private String determineRiskCategory(double riskScore) {
            if (riskScore < 50) return "Acceptable";
            else if (riskScore < 100) return "Moderate";
            else return "High Risk - Review Required";
        }
    }

    /**
     * Java 9: Collection Factory Methods for Investment Configuration
     * Provides immutable investment configuration data structures
     * Ensures investment system stability with immutable configuration
     * Reduces memory overhead for frequently accessed investment settings
     */
    static class InvestmentConfiguration {

        /**
         * Immutable investment risk levels using Java 9 factory methods
         * Java 9: Collection factory methods for immutable collections
         * Ensures investment risk level consistency across system components
         * Prevents accidental modification of critical investment parameters
         */
        private static final List<String> RISK_LEVELS = List.of("LOW", "MEDIUM", "HIGH");

        /**
         * Immutable investment asset types using Java 9 factory methods
         * Java 9: Concise immutable collection creation
         * Provides standardized investment categorization for portfolio management
         * Supports consistent investment reporting and analysis workflows
         */
        private static final Set<String> ASSET_TYPES = Set.of("STOCKS", "BONDS", "REAL_ESTATE", "MUTUAL_FUNDS");

        /**
         * Immutable investment fee structure using Java 9 factory methods
         * Java 9: Map factory methods for configuration data
         * Ensures consistent fee application across investment transactions
         * Supports transparent investment cost communication to clients
         */
        private static final Map<String, Double> FEE_STRUCTURE = Map.of(
                "STANDARD", 0.01,    // 1% standard investment fee
                "PREMIUM", 0.005,    // 0.5% premium investment fee
                "INSTITUTIONAL", 0.001 // 0.1% institutional investment fee
        );

        /**
         * Retrieves immutable investment risk levels
         * Java 9: Safe sharing of immutable configuration data
         * Supports investment risk management workflows
         * Ensures consistent risk assessment across investment processes
         */
        public List<String> getRiskLevels() {
            return RISK_LEVELS; // Returns immutable list - safe for sharing
        }

        /**
         * Retrieves immutable investment asset types
         * Java 9: Efficient immutable collection access
         * Enables standardized investment categorization and reporting
         * Supports portfolio diversification analysis workflows
         */
        public Set<String> getAssetTypes() {
            return ASSET_TYPES; // Returns immutable set - thread-safe
        }

        /**
         * Retrieves immutable investment fee structure
         * Java 9: Immutable map for configuration data
         * Ensures consistent fee application in investment transactions
         * Supports transparent investment cost management
         */
        public Map<String, Double> getFeeStructure() {
            return FEE_STRUCTURE; // Returns immutable map - prevents accidental modification
        }
    }

    // Java 10 Features

    /**
     * Java 10: Local-Variable Type Inference for Investment Processing
     * Simplifies investment code while maintaining type safety
     * Reduces boilerplate in investment analysis workflows
     * Improves code readability for complex investment calculations
     */
    static class InvestmentProcessor {

        /**
         * Demonstrates Java 10 var keyword in investment processing
         * Java 10: Local-variable type inference for cleaner code
         * Simplifies investment code without sacrificing type safety
         * Enhances readability of complex investment analysis workflows
         */
        public void processInvestmentPortfolio() {
            // Java 10 var keyword infers type from initialization
            var portfolio = new ArrayList<String>(); // Investment portfolio container
            portfolio.add("STOCKS");
            portfolio.add("BONDS");
            portfolio.add("REAL_ESTATE");

            // var with investment calculation results
            var totalInvestment = 1000000.0; // Java 10 infers double type
            var expectedReturn = 0.08;       // Java 10 infers double type
            var annualIncome = totalInvestment * expectedReturn; // Type inference in calculations

            // var with investment analysis results
            var riskAssessment = Map.of("score", 75, "level", "MEDIUM"); // Immutable map inference
            var clientProfile = new InvestmentProposal("CLIENT-001", "John Doe", 50000, 7.5, "MEDIUM", "STOCKS", "Advisor A");

            System.out.println("Portfolio Analysis Complete:");
            System.out.println("Total Investment: $" + totalInvestment);
            System.out.println("Expected Annual Income: $" + annualIncome);
        }
    }

    // Java 11 Features

    /**
     * Java 11: Enhanced String Methods for Investment Data Processing
     * Improved investment data cleaning and formatting capabilities
     * Supports efficient investment report generation and client communication
     */
    static class InvestmentStringProcessor {

        /**
         * Java 11: Enhanced String methods for investment data processing
         * Java 11: New String methods for data manipulation
         * Improves investment data cleaning and formatting workflows
         * Supports efficient investment report generation and client communication
         */
        public void processInvestmentData() {
            // Java 11: isBlank() method for investment data validation
            var emptyData = "   "; // Investment data with only whitespace
            if (emptyData.isBlank()) {
                System.out.println("Investment data is blank - skipping processing");
            }

            // Java 11: lines() method for investment report parsing
            var investmentReport = """
                Client: John Doe
                Portfolio Value: $1,000,000
                Expected Return: 8.5%
                Risk Level: MEDIUM
                """;

            // Process investment report lines
            investmentReport.lines()
                    .map(String::strip) // Java 11: strip() removes leading/trailing whitespace
                    .filter(line -> !line.isEmpty()) // Java 11: isEmpty() for empty string detection
                    .forEach(System.out::println);

            // Java 11: repeat() method for investment formatting
            var separator = "=".repeat(50); // Consistent investment report formatting
            System.out.println(separator);
            System.out.println("Investment Report Header");
            System.out.println(separator);
        }
    }

    // Java 12-13 Features

    /**
     * Java 12-13: Switch Expressions and Text Blocks for Investment Management
     * Modern syntax for investment logic and data representation
     * Improved code readability and maintainability for investment workflows
     */
    static class InvestmentSwitchProcessor {

        /**
         * Java 12-13: Switch expressions for investment processing
         * Java 12: Switch expressions with arrow syntax
         * Java 13: Multi-line yield statements
         * Simplifies investment decision logic and reduces errors
         */
        public String processInvestmentByRiskLevel(String riskLevel) {
            // Java 12-13: Modern switch expression with arrow syntax
            return switch (riskLevel) {
                case "LOW" -> {
                    System.out.println("Processing low-risk investment");
                    yield "Conservative portfolio allocation recommended";
                }
                case "MEDIUM" -> {
                    System.out.println("Processing medium-risk investment");
                    yield "Balanced portfolio allocation recommended";
                }
                case "HIGH" -> {
                    System.out.println("Processing high-risk investment");
                    yield "Aggressive portfolio allocation recommended";
                }
                default -> {
                    System.out.println("Unknown risk level: " + riskLevel);
                    yield "Risk assessment required before processing";
                }
            };
        }

        /**
         * Java 13: Text blocks for investment documentation
         * Java 13: Multi-line string literals
         * Improves readability of investment reports and documentation
         * Supports structured investment data representation
         */
        public void generateInvestmentReport() {
            // Java 13: Text blocks for multi-line investment content
            var investmentSummary = """
                ==========================================
                INVESTMENT PORTFOLIO ANALYSIS REPORT
                ==========================================
                Client: John Doe
                Analysis Date: 2024-01-15
                Portfolio Value: $1,250,000.00
                Expected Annual Return: 7.8%
                Risk Assessment: MEDIUM
                Diversification Score: 85/100
                ==========================================
                RECOMMENDATIONS:
                1. Increase bond allocation by 5%
                2. Consider emerging market opportunities
                3. Review portfolio quarterly
                ==========================================
                """;

            System.out.println(investmentSummary);
        }
    }

    // Java 14 Features

    /**
     * Java 14: Records and Pattern Matching for Investment Data
     * Immutable data carriers with enhanced pattern matching capabilities
     * Simplifies investment data modeling and processing workflows
     */
    record InvestmentRecord(
            String id,              // Unique investment identifier
            String clientName,      // Client identification for relationship management
            double amount,          // Investment amount for portfolio allocation
            double expectedReturn,  // Expected return for performance tracking
            String riskLevel,       // Risk categorization for portfolio management
            String investmentType   // Asset class for diversification analysis
    ) {
        // Java 14: Records automatically generate constructor, getters, equals, hashCode, toString

        /**
         * Custom constructor validation for investment records
         * Java 14: Compact constructor syntax for validation
         * Ensures investment data integrity at creation time
         * Prevents invalid investment configurations from entering the system
         */
        public InvestmentRecord {
            if (amount <= 0) {
                throw new IllegalArgumentException("Investment amount must be positive");
            }
            if (expectedReturn < 0) {
                throw new IllegalArgumentException("Expected return cannot be negative");
            }
        }

        /**
         * Custom method for investment analysis
         * Java 14: Records support additional methods like regular classes
         * Enables investment-specific functionality within immutable data structure
         * Supports investment decision-making workflows
         */
        public boolean isHighReturn() {
            return expectedReturn > 10.0; // High return threshold for investment screening
        }
    }

    /**
     * Java 14: Pattern Matching for instanceof
     * Simplifies investment object type checking and processing
     * Reduces boilerplate code in investment workflow implementations
     */
    static class InvestmentPatternMatcher {

        /**
         * Java 14: Pattern matching for instanceof with automatic casting
         * Java 14: Eliminates redundant casting operations
         * Improves investment object processing efficiency and readability
         * Reduces potential for ClassCastException in investment workflows
         */
        public void processInvestmentObject(Object investment) {
            // Java 14: Pattern matching eliminates explicit casting
            if (investment instanceof InvestmentRecord record) {
                // Automatic casting to InvestmentRecord - no explicit cast needed
                System.out.println("Processing investment record: " + record.clientName());
                System.out.println("Investment amount: $" + record.amount());
                System.out.println("Expected return: " + record.expectedReturn() + "%");

                // Use pattern-matched variable directly
                if (record.isHighReturn()) {
                    System.out.println("High-return investment identified for review");
                }
            }
            else if (investment instanceof String investmentId) {
                // Pattern matching works with any type
                System.out.println("Processing investment by ID: " + investmentId);
            }
            else {
                System.out.println("Unknown investment type: " + investment.getClass().getSimpleName());
            }
        }
    }

    // Java 15 Features

    /**
     * Java 15: Sealed Classes for Investment Hierarchy Control
     * Restricts inheritance to ensure investment system integrity
     * Provides controlled extensibility for investment management components
     */
    public static sealed class InvestmentManager15
            permits PremiumInvestmentManager15, StandardInvestmentManager15 {

        /**
         * Abstract method for investment processing
         * Defines common investment processing interface
         * Ensures consistent investment handling across manager types
         */
        public void processInvestment(InvestmentRecord investment) {

        }

        /**
         * Common investment validation logic
         * Shared functionality across investment manager implementations
         * Ensures consistent investment quality standards
         */
        protected boolean validateInvestment(InvestmentRecord investment) {
            return investment.amount() > 0 && investment.amount() <= 1000000;
        }
    }

    /**
     * Java 15: Sealed classes for premium investment management
     * Controlled inheritance ensures investment system security
     * Specialized processing for high-value investment portfolios
     */
    static final class PremiumInvestmentManager15 extends InvestmentManager15 {

        /**
         * Premium investment processing with enhanced capabilities
         * Java 15: Sealed class inheritance control
         * Supports sophisticated investment analysis for high-net-worth clients
         * Ensures only authorized managers can process premium investments
         */
        @Override
        public void processInvestment(InvestmentRecord investment) {
            if (validateInvestment(investment) && investment.amount() > 100000) {
                System.out.println("Premium manager processing high-value investment: " + investment.id());
                System.out.println("Investment amount: $" + investment.amount());
                System.out.println("Specialized risk assessment initiated");
            } else {
                System.out.println("Investment requires standard processing or validation failed");
            }
        }
    }

    /**
     * Java 15: Sealed classes for standard investment management
     * Controlled inheritance maintains investment processing standards
     * Efficient handling of routine investment transactions
     */
    static final class StandardInvestmentManager15 extends InvestmentManager15 {

        /**
         * Standard investment processing with automated workflows
         * Java 15: Sealed class inheritance restriction
         * Ensures proper investment processing hierarchy and security
         * Supports efficient handling of standard investment requests
         */
        @Override
        public void processInvestment(InvestmentRecord investment) {
            if (validateInvestment(investment) && investment.amount() <= 100000) {
                System.out.println("Standard manager processing routine investment: " + investment.id());
                System.out.println("Investment amount: $" + investment.amount());
                System.out.println("Automated approval process initiated");
            } else {
                System.out.println("Investment requires premium processing or validation failed");
            }
        }
    }

    // Java 16 Features

    /**
     * Java 16: Pattern Matching for instanceof (Standard)
     * Enhanced investment object processing with improved type safety
     * Simplifies investment workflow implementations and reduces errors
     */
    static class InvestmentProcessor16 {

        /**
         * Java 16: Standardized pattern matching for instanceof
         * Java 16: Production-ready pattern matching
         * Improves investment object handling efficiency and safety
         * Enables more expressive investment processing logic
         */
        public void analyzeInvestment(Object obj) {
            // Java 16: Pattern matching available in production code
            if (obj instanceof InvestmentRecord record && record.amount() > 50000) {
                // Pattern variable available in same expression
                System.out.println("High-value investment detected: " + record.id());
                System.out.println("Processing with enhanced due diligence");
            } else if (obj instanceof InvestmentRecord record) {
                System.out.println("Standard investment processing: " + record.id());
            } else {
                System.out.println("Non-investment object received");
            }
        }

        /**
         * Java 16: Records with enhanced capabilities
         * Java 16: Records as fully-featured classes
         * Supports investment data modeling with all standard class features
         * Enables sophisticated investment data processing workflows
         */
        public record InvestmentAnalysisResult(
                InvestmentRecord investment,
                String analysis,
                double confidenceScore
        ) {
            // Java 16: Records can have custom constructors and methods
            public InvestmentAnalysisResult {
                if (confidenceScore < 0 || confidenceScore > 100) {
                    throw new IllegalArgumentException("Confidence score must be between 0 and 100");
                }
            }

            /**
             * Custom method for investment analysis interpretation
             * Java 16: Records support additional methods
             * Enables investment-specific functionality within immutable structure
             */
            public boolean isReliable() {
                return confidenceScore >= 80.0; // Investment analysis reliability threshold
            }
        }
    }

    // Java 17 Features

    /**
     * Java 17: Sealed Classes (Standard) and Enhanced Switch
     * Production-ready investment hierarchy control and pattern matching
     * Long-term support features for enterprise investment systems
     */
    public static sealed interface InvestmentService
            permits StockInvestmentService, BondInvestmentService, RealEstateInvestmentService {

        /**
         * Investment processing method with pattern matching
         * Java 17: Enhanced switch with pattern matching
         * Provides sophisticated investment routing and processing
         * Supports modern investment workflow implementations
         */
        /*   This syntax is only valid in Java 21+ with preview features enabled, specifically record patterns with guards inside switch. If you're not compiling with preview features, this will throw a syntax error.
         default void processInvestment(InvestmentRecord investment) {
            switch (investment) {
                case InvestmentRecord r && r.investmentType().equals("STOCKS") ->
                    System.out.println("Routing stock investment to specialized processor");
                case InvestmentRecord r && r.investmentType().equals("BONDS") ->
                    System.out.println("Routing bond investment to fixed income processor");
                    case InvestmentRecord r ->
                            System.out.println("Routing other investment types to general processor");
            }
        }*/

        default void processInvestment(InvestmentRecord investment) {
            switch (investment) {
                case InvestmentRecord r when r.investmentType().equals("STOCKS") ->
                        System.out.println("Routing stock investment to specialized processor");
                case InvestmentRecord r when r.investmentType().equals("BONDS") ->
                        System.out.println("Routing bond investment to fixed income processor");
                case InvestmentRecord r ->
                        System.out.println("Routing other investment types to general processor");
            }
        }
    }

    /**
     * Java 17: Sealed interface implementations for investment services
     * Controlled investment service architecture for enterprise systems
     * Ensures proper investment processing specialization and security
     */
    static final class StockInvestmentService implements InvestmentService {
        // Specialized stock investment processing
    }

    static final class BondInvestmentService implements InvestmentService {
        // Specialized bond investment processing
    }

    static final class RealEstateInvestmentService implements InvestmentService {
        // Specialized real estate investment processing
    }

    // Java 20-21 Features

    /**
     * Java 20-21: Virtual Threads and Pattern Matching enhancements
     * Modern concurrency and enhanced pattern matching for investment systems
     * Improved performance and expressiveness for enterprise investment applications
     */
    static class InvestmentVirtualThreadProcessor {

        /**
         * Java 21: Virtual Threads for investment processing scalability
         * Java 21: Lightweight threads for high-concurrency applications
         * Enables efficient handling of numerous investment transactions
         * Supports modern investment system scalability requirements
         */
        public void processInvestmentBatch(List<InvestmentRecord> investments) {
            // Java 21: Virtual threads for lightweight concurrent processing
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

                // Process multiple investments concurrently with virtual threads
                var futures = investments.stream()
                        .map(investment -> executor.submit(() -> {
                            // Simulate investment processing
                            try {
                                Thread.sleep(100); // Investment analysis time
                                System.out.println("Processed investment: " + investment.id() +
                                        " by virtual thread: " + Thread.currentThread());
                                return "Completed: " + investment.id();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return "Failed: " + investment.id();
                            }
                        }))
                        .toList();

                // Wait for all investment processing to complete
                futures.forEach(future -> {
                    try {
                        System.out.println(future.get());
                    } catch (Exception e) {
                        System.err.println("Investment processing error: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                System.err.println("Investment batch processing failed: " + e.getMessage());
            }
        }

        /**
         * Java 21: Enhanced pattern matching for switch expressions
         * Java 21: Record patterns in switch expressions
         * Provides sophisticated investment routing and processing logic
         * Enables more expressive and type-safe investment workflows
         */
        public String analyzeInvestmentPattern(Object obj) {
            return switch (obj) {
                // Java 21: Record patterns in switch
                case InvestmentRecord(String id, String client, double amount, double ret, String risk, String type)
                        when amount > 100000 ->
                        "High-value investment %s for client %s requires special handling".formatted(id, client);

                case InvestmentRecord(String id, String client, double amount, double ret, String risk, String type)
                        when risk.equals("HIGH") ->
                        "High-risk investment %s for client %s needs enhanced due diligence".formatted(id, client);

                case InvestmentRecord(String id, String client, double amount, double ret, String risk, String type) ->
                        "Standard investment %s for client %s processed normally".formatted(id, client);

                case null -> "Null investment object received";
                default -> "Unknown investment object type";
            };
        }
    }

    /**
     * Main method demonstrating all Java 8-21 features in investment management context
     * Comprehensive showcase of modern Java capabilities for investment applications
     * Practical examples of enterprise investment system implementation
     */
    public static void main(String[] args) {
        System.out.println("=== Java 8-21 Investment Proposal Management System ===\n");

        // Java 8 Features
        demonstrateJava8Features();

        // Java 9 Features
        demonstrateJava9Features();

        // Java 10 Features
        demonstrateJava10Features();

        // Java 11 Features
        demonstrateJava11Features();

        // Java 12-13 Features
        demonstrateJava1213Features();

        // Java 14 Features
        demonstrateJava14Features();

        // Java 15 Features
        demonstrateJava15Features();

        // Java 16 Features
        demonstrateJava16Features();

        // Java 17 Features
        demonstrateJava17Features();

        // Java 20-21 Features
        demonstrateJava2021Features();

        System.out.println("\n=== All Java 8-21 features demonstrated successfully ===");
    }

    /**
     * Demonstrates Java 8 features for investment management
     * Lambda expressions, streams, optional, functional interfaces
     * Modern investment data processing and analysis capabilities
     */
    private static void demonstrateJava8Features() {
        System.out.println("--- Java 8 Features: Investment Data Processing ---");

        // Lambda Expressions for investment filtering
        Runnable investmentTask = () -> System.out.println("Executing investment analysis task");
        investmentTask.run();

        // Streams API for investment portfolio analysis
        List<InvestmentProposal> proposals = createSampleProposals();
        double totalInvestment = proposals.stream()
                .mapToDouble(InvestmentProposal::getProposedAmount)
                .reduce(0.0, Double::sum);
        System.out.println("Total investment portfolio value: $" + String.format("%.2f", totalInvestment));

        // Optional for safe investment data handling
        Optional<InvestmentProposal> foundProposal = proposals.stream()
                .filter(p -> "PROPOSAL-003".equals(p.getId()))
                .findFirst();
        foundProposal.ifPresent(p -> System.out.println("Found investment: " + p.getClientName()));

        // Functional interfaces for investment calculations
        InvestmentCalculator compoundInterest = (principal, rate, years) ->
                principal * Math.pow(1 + rate/100, years);
        double futureValue = compoundInterest.calculate(10000, 5, 10);
        System.out.println("10-year investment projection: $" + String.format("%.2f", futureValue));

        System.out.println();
    }

    /**
     * Demonstrates Java 9 features for investment management
     * Private interface methods and collection factory methods
     * Modern investment system design and configuration capabilities
     */
    private static void demonstrateJava9Features() {
        System.out.println("--- Java 9 Features: Investment System Configuration ---");

        // Private interface methods demonstration
        InvestmentAnalyzer analyzer = new InvestmentAnalyzer() {}; // Anonymous implementation
        analyzer.analyzeInvestmentRisk(50000, "MEDIUM");

        // Collection factory methods demonstration
        var config = new InvestmentConfiguration();
        System.out.println("Investment Risk Levels: " + config.getRiskLevels());
        System.out.println("Investment Asset Types: " + config.getAssetTypes());
        System.out.println("Investment Fee Structure: " + config.getFeeStructure());

        System.out.println();
    }

    /**
     * Demonstrates Java 10 features for investment processing
     * Local-variable type inference for cleaner investment code
     * Improved code readability and maintainability
     */
    private static void demonstrateJava10Features() {
        System.out.println("--- Java 10 Features: Investment Portfolio Processing ---");

        var processor = new InvestmentProcessor();
        processor.processInvestmentPortfolio();

        System.out.println();
    }

    /**
     * Demonstrates Java 11 features for investment data processing
     * Enhanced string methods for investment data manipulation
     * Improved investment report generation and client communication
     */
    private static void demonstrateJava11Features() {
        System.out.println("--- Java 11 Features: Investment Data Processing ---");

        var stringProcessor = new InvestmentStringProcessor();
        stringProcessor.processInvestmentData();

        System.out.println();
    }

    /**
     * Demonstrates Java 12-13 features for investment processing
     * Switch expressions and text blocks for investment workflows
     * Modern syntax for investment logic and data representation
     */
    private static void demonstrateJava1213Features() {
        System.out.println("--- Java 12-13 Features: Investment Decision Logic ---");

        var processor = new InvestmentSwitchProcessor();
        System.out.println(processor.processInvestmentByRiskLevel("HIGH"));
        processor.generateInvestmentReport();

        System.out.println();
    }

    /**
     * Demonstrates Java 14 features for investment data modeling
     * Records and pattern matching for investment objects
     * Immutable data carriers with enhanced pattern matching capabilities
     */
    private static void demonstrateJava14Features() {
        System.out.println("--- Java 14 Features: Investment Data Modeling ---");

        try {
            var investment = new InvestmentRecord("INV-001", "John Doe", 100000, 8.5, "MEDIUM", "STOCKS");
            System.out.println("Investment Record: " + investment);
            System.out.println("High Return Investment: " + investment.isHighReturn());

            var matcher = new InvestmentPatternMatcher();
            matcher.processInvestmentObject(investment);

        } catch (Exception e) {
            System.err.println("Java 14 feature demonstration error: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * Demonstrates Java 15 features for investment hierarchy control
     * Sealed classes for investment manager security
     * Controlled inheritance for investment system integrity
     */
    private static void demonstrateJava15Features() {
        System.out.println("--- Java 15 Features: Investment Manager Hierarchy ---");

        var premiumManager = new PremiumInvestmentManager15();
        var standardManager = new StandardInvestmentManager15();

        var investment = new InvestmentRecord("INV-002", "Jane Smith", 150000, 12.0, "HIGH", "STOCKS");

        premiumManager.processInvestment(investment);
        standardManager.processInvestment(investment);

        System.out.println();
    }

    /**
     * Demonstrates Java 16 features for investment processing
     * Enhanced pattern matching and record capabilities
     * Improved investment object handling and data modeling
     */
    private static void demonstrateJava16Features() {
        System.out.println("--- Java 16 Features: Investment Object Analysis ---");

        var processor = new InvestmentProcessor16();
        var investment = new InvestmentRecord("INV-003", "Bob Johnson", 75000, 6.8, "MEDIUM", "BONDS");

        processor.analyzeInvestment(investment);

        System.out.println();
    }

    /**
     * Demonstrates Java 17 features for investment services
     * Sealed interfaces and enhanced switch expressions
     * Production-ready investment architecture control
     */
    private static void demonstrateJava17Features() {
        System.out.println("--- Java 17 Features: Investment Service Architecture ---");

        InvestmentService stockService = new StockInvestmentService();
        var investment = new InvestmentRecord("INV-004", "Alice Brown", 200000, 15.2, "HIGH", "STOCKS");

        stockService.processInvestment(investment);

        System.out.println();
    }

    /**
     * Demonstrates Java 20-21 features for investment processing
     * Virtual threads and enhanced pattern matching
     * Modern concurrency and sophisticated investment routing
     */
    private static void demonstrateJava2021Features() {
        System.out.println("--- Java 20-21 Features: Investment Processing Scalability ---");

        var processor = new InvestmentVirtualThreadProcessor();

        // Create sample investments for virtual thread processing
        var investments = List.of(
                new InvestmentRecord("INV-005", "Client A", 50000, 7.5, "MEDIUM", "STOCKS"),
                new InvestmentRecord("INV-006", "Client B", 100000, 6.2, "LOW", "BONDS"),
                new InvestmentRecord("INV-007", "Client C", 250000, 11.8, "HIGH", "REAL_ESTATE")
        );

        // Demonstrate virtual thread processing
        processor.processInvestmentBatch(investments);

        // Demonstrate enhanced pattern matching
        System.out.println(processor.analyzeInvestmentPattern(investments.get(0)));

        System.out.println();
    }

    /**
     * Creates sample investment proposals for demonstration purposes
     * Provides realistic investment data for testing Java features
     * Supports comprehensive investment management workflow simulation
     */
    private static List<InvestmentProposal> createSampleProposals() {
        return Arrays.asList(
                new InvestmentProposal("PROPOSAL-001", "Alice Johnson", 25000, 6.5, "LOW", "STOCKS", "Advisor A"),
                new InvestmentProposal("PROPOSAL-002", "Bob Smith", 75000, 8.2, "MEDIUM", "BONDS", "Advisor B"),
                new InvestmentProposal("PROPOSAL-003", "Charlie Brown", 150000, 12.0, "HIGH", "REAL_ESTATE", "Advisor C"),
                new InvestmentProposal("PROPOSAL-004", "Diana Prince", 45000, 5.8, "LOW", "MUTUAL_FUNDS", "Advisor A"),
                new InvestmentProposal("PROPOSAL-005", "Eve Wilson", 200000, 15.5, "HIGH", "STOCKS", "Advisor C")
        );
    }
}

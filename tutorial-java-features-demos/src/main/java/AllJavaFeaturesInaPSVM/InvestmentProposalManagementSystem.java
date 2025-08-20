package main.java.AllJavaFeaturesInaPSVM;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Comprehensive Investment Proposal Management System using Java 8 Features
 * This class demonstrates how Java 8 features enhance investment management workflows
 */
public class InvestmentProposalManagementSystem {

    /**
     * Investment Proposal class representing a client's investment request
     * Models real-world investment data with all necessary attributes
     */
    static class InvestmentProposal {
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
         * Enables clear audit trails and status tracking
         */
        @Override
        public String toString() {
            return String.format("Proposal{id='%s', client='%s', amount=$%.2f, return=%.2f%%, risk='%s', approved=%s}",
                    id, clientName, proposedAmount, expectedReturn, riskLevel, approved);
        }
    }

    /**
     * Abstract base class for investment managers
     * Provides common functionality while allowing specialized implementations
     * Helps in creating a flexible approval hierarchy based on investment tiers
     */
    static abstract class InvestmentManager {
        public abstract void processProposal(InvestmentProposal proposal);
        public abstract String getManagerType();
    }

    /**
     * Standard Investment Manager for routine investment approvals
     * Handles lower-tier investments with automated processing capabilities
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
     * Handles complex, high-value investments requiring specialized review
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
     * Enables flexible financial modeling and scenario analysis
     * Supports various investment calculation methodologies
     */
    @FunctionalInterface
    interface InvestmentCalculator {
        double calculate(double amount, double rate, int years);
    }

    /**
     * Functional interface for risk assessment
     * Provides customizable risk evaluation logic for different investment types
     * Supports dynamic risk management based on market conditions
     */
    @FunctionalInterface
    interface RiskAssessor {
        boolean assessRisk(InvestmentProposal proposal);
    }

    /**
     * Main method demonstrating all Java 8 features in investment management context
     * Shows how modern Java features enhance operational efficiency and decision-making
     */
    public static void main(String[] args) {
        System.out.println("=== Java 8 Investment Proposal Management System ===\n");

        // Demonstrate each Java 8 feature with investment management applications
        demonstrateLambdaExpressions();
        demonstrateMethodReferences();
        demonstrateStreamsAPI();
        demonstrateOptional();
        demonstrateFunctionalInterfaces();
        demonstrateDefaultMethods();
        demonstrateDateTimeAPI();
        demonstrateCompletableFuture();
        demonstrateAdvancedStreams();

        System.out.println("\n=== All Java 8 features successfully applied to investment management ===");
    }

    /**
     * Demonstrates Lambda Expressions for investment filtering and processing
     * Enables concise, readable business logic for investment workflows
     * Reduces boilerplate code while maintaining clear investment criteria
     */
    private static void demonstrateLambdaExpressions() {
        System.out.println("--- 1. Lambda Expressions for Investment Filtering ---");

        // Simple lambda for automated investment tasks
        // Reduces code verbosity while maintaining clear investment logic
        Runnable investmentTask = () -> System.out.println("Executing automated investment analysis task");
        investmentTask.run();

        // Lambda with parameters for investment comparison
        // Enables flexible sorting criteria for portfolio optimization
        Comparator<InvestmentProposal> amountComparator = (p1, p2) ->
                Double.compare(p1.getProposedAmount(), p2.getProposedAmount());

        // Lambda with multiple statements for complex investment logic
        // Supports detailed risk assessment with comprehensive logging
        Predicate<InvestmentProposal> highRiskFilter = proposal -> {
            System.out.println("Assessing risk level for investment: " + proposal.getId());
            return "HIGH".equals(proposal.getRiskLevel());
        };

        System.out.println("Lambda expressions enable concise investment logic implementation\n");
    }

    /**
     * Demonstrates Method References for investment data processing
     * Improves code readability and performance for common investment operations
     * Eliminates redundancy while maintaining clear investment workflows
     */
    private static void demonstrateMethodReferences() {
        System.out.println("--- 2. Method References for Investment Data Processing ---");

        // Static method reference for client communication
        // Streamlines client notification processes with minimal code
        List<String> clientNames = Arrays.asList("Alice Johnson", "Bob Smith", "Charlie Brown");
        clientNames.forEach(System.out::println);

        // Instance method reference for investment proposal display
        // Simplifies reporting while maintaining consistent data presentation
        List<InvestmentProposal> proposals = createSampleProposals();
        proposals.forEach(System.out::println);

        // Constructor reference for investment manager creation
        // Enables dynamic manager assignment based on investment characteristics
        Supplier<InvestmentManager> standardManagerSupplier = StandardInvestmentManager::new;
        InvestmentManager manager = standardManagerSupplier.get();
        System.out.println("Created investment manager: " + manager.getManagerType());

        System.out.println("Method references streamline investment data processing workflows\n");
    }

    /**
     * Demonstrates Streams API for investment data analysis and filtering
     * Enables powerful portfolio analysis and client segmentation capabilities
     * Provides efficient processing of large investment datasets
     */
    private static void demonstrateStreamsAPI() {
        System.out.println("--- 3. Streams API for Investment Portfolio Analysis ---");

        List<InvestmentProposal> proposals = createSampleProposals();

        // Filter and collect high-value investment opportunities
        // Supports tier-based investment strategy and resource allocation
        List<InvestmentProposal> highAmountProposals = proposals.stream()
                .filter(p -> p.getProposedAmount() > 50000) // Investment threshold filtering
                .collect(Collectors.toList());
        System.out.println("High-value investment opportunities: " + highAmountProposals.size());

        // Map operation for investment amount extraction
        // Enables quick portfolio sizing and allocation analysis
        List<Double> amounts = proposals.stream()
                .map(InvestmentProposal::getProposedAmount) // Direct field access for efficiency
                .collect(Collectors.toList());
        System.out.println("Investment amounts for portfolio analysis: " + amounts);

        // Reduce operation for total portfolio value calculation
        // Provides real-time portfolio valuation for client reporting
        double totalInvestment = proposals.stream()
                .mapToDouble(InvestmentProposal::getProposedAmount) // Efficient numeric processing
                .reduce(0.0, Double::sum); // Concise aggregation for portfolio metrics
        System.out.println("Total portfolio value proposed: $" + String.format("%.2f", totalInvestment));

        // Sorting for investment priority ranking
        // Enables strategic investment sequencing and client service optimization
        List<InvestmentProposal> sortedByReturn = proposals.stream()
                .sorted(Comparator.comparing(InvestmentProposal::getExpectedReturn).reversed()) // ROI-based prioritization
                .collect(Collectors.toList());
        System.out.println("Top return investment opportunity: " + sortedByReturn.get(0).getExpectedReturn() + "%");

        System.out.println("Streams API enables efficient investment data processing and analysis\n");
    }

    /**
     * Demonstrates Optional Class for safe investment data handling
     * Prevents runtime errors in investment processing workflows
     * Ensures robust error handling for missing investment data
     */
    private static void demonstrateOptional() {
        System.out.println("--- 4. Optional Class for Investment Data Safety ---");

        List<InvestmentProposal> proposals = createSampleProposals();

        // Find specific investment proposal with null safety
        // Prevents NullPointerException in investment lookup operations
        Optional<InvestmentProposal> foundProposal = proposals.stream()
                .filter(p -> "PROPOSAL-003".equals(p.getId())) // Safe investment ID matching
                .findFirst();

        // Safe operations with Optional for investment processing
        // Ensures graceful handling of missing investment data
        foundProposal.ifPresent(proposal ->
                System.out.println("Located investment proposal for client: " + proposal.getClientName()));

        // Default value handling for missing investment information
        // Maintains workflow continuity with fallback values
        String clientName = foundProposal.map(InvestmentProposal::getClientName)
                .orElse("Unknown Client"); // Graceful degradation for reporting
        System.out.println("Client name for investment tracking: " + clientName);

        // Optional with filtering for investment qualification
        // Enables conditional investment processing based on criteria
        Optional<InvestmentProposal> approvedHighReturn = foundProposal
                .filter(p -> p.getExpectedReturn() > 8.0); // Performance-based filtering
        System.out.println("High-performance investment qualified: " + approvedHighReturn.isPresent());

        System.out.println("Optional class ensures robust investment data handling\n");
    }

    /**
     * Demonstrates Functional Interfaces for investment calculations and assessments
     * Enables flexible, reusable investment logic components
     * Supports modular investment processing with clear separation of concerns
     */
    private static void demonstrateFunctionalInterfaces() {
        System.out.println("--- 5. Functional Interfaces for Investment Calculations ---");

        // Custom functional interface for investment projections
        // Enables flexible financial modeling for client scenarios
        InvestmentCalculator compoundInterest = (principal, rate, years) ->
                principal * Math.pow(1 + rate/100, years); // Compound growth modeling

        double futureValue = compoundInterest.calculate(10000, 5, 10); // 10-year investment projection
        System.out.println("10-year investment projection: $" + String.format("%.2f", futureValue));

        // Built-in functional interfaces for investment processing
        // Leverages standard Java 8 interfaces for common investment operations
        Function<String, Integer> parseAmount = Integer::parseInt; // Investment amount parsing
        Predicate<InvestmentProposal> lowRisk = p -> "LOW".equals(p.getRiskLevel()); // Risk-based filtering
        Consumer<InvestmentProposal> approveProposal = p -> p.setApproved(true); // Investment approval action
        Supplier<LocalDateTime> currentTime = LocalDateTime::now; // Timestamp generation for compliance

        InvestmentProposal sample = new InvestmentProposal("TEST-001", "Test Client", 25000, 6.5, "LOW", "STOCKS", "Advisor A");
        if (lowRisk.test(sample)) { // Conditional investment processing
            approveProposal.accept(sample); // Automated approval for low-risk investments
            System.out.println("Investment proposal automatically approved: " + sample.isApproved());
        }

        System.out.println("Functional interfaces enable modular investment processing logic\n");
    }

    /**
     * Demonstrates Default Methods in Interfaces for investment analysis capabilities
     * Provides shared functionality while allowing specialized implementations
     * Enables consistent investment reporting across different analysis modules
     */
    private static void demonstrateDefaultMethods() {
        System.out.println("--- 6. Default Methods for Investment Analysis Reporting ---");

        // Interface with default method for investment analysis
        // Provides standardized reporting while allowing customization
        interface InvestmentAnalyzer {
            void analyze(InvestmentProposal proposal);

            // Default method for investment analysis header formatting
            // Ensures consistent reporting standards across analysis modules
            default void printAnalysisHeader() {
                System.out.println("=== Investment Analysis Report ===");
            }

            // Default method for investment summary generation
            // Provides standardized client communication templates
            default String generateSummary(InvestmentProposal proposal) {
                return String.format("Investment %s for client %s: $%.2f at %.2f%% expected return",
                        proposal.getId(), proposal.getClientName(),
                        proposal.getProposedAmount(), proposal.getExpectedReturn());
            }
        }

        // Implementation of investment analyzer with default method usage
        // Combines standard reporting with specialized analysis capabilities
        InvestmentAnalyzer analyzer = proposal -> {
            System.out.println("Conducting detailed analysis on investment: " + proposal.getId());
        };

        InvestmentProposal sample = new InvestmentProposal("ANALYSIS-001", "John Doe", 50000, 7.2, "MEDIUM", "BONDS", "Advisor B");
        analyzer.printAnalysisHeader(); // Standardized reporting header
        System.out.println(analyzer.generateSummary(sample)); // Consistent summary format
        analyzer.analyze(sample); // Specialized analysis execution

        System.out.println("Default methods ensure consistent investment analysis reporting\n");
    }

    /**
     * Demonstrates Date and Time API for investment timeline management
     * Enables precise investment tracking and compliance reporting
     * Supports temporal analysis for investment performance monitoring
     */
    private static void demonstrateDateTimeAPI() {
        System.out.println("--- 7. Date and Time API for Investment Timeline Management ---");

        // Current date and time for investment processing timestamps
        // Ensures accurate compliance tracking and audit trails
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        System.out.println("Current investment processing time: " + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("Today's date for investment tracking: " + today);

        // Date manipulation for investment maturity calculations
        // Enables forward-looking investment planning and client communication
        LocalDate nextReview = today.plusMonths(1); // Monthly investment review scheduling
        LocalDate oneYearAgo = today.minusYears(1); // Historical performance analysis
        System.out.println("Next investment review date: " + nextReview);
        System.out.println("Historical analysis start date: " + oneYearAgo);

        // Parsing dates for investment data integration
        // Supports import of external investment data with proper date handling
        LocalDate parsedDate = LocalDate.parse("2024-12-25"); // Holiday investment planning
        System.out.println("Parsed investment deadline: " + parsedDate);

        // Working with proposal dates for investment workflow management
        // Enables efficient investment pipeline tracking and client follow-up
        List<InvestmentProposal> proposals = createSampleProposals();
        proposals.stream()
                .filter(p -> p.getProposalDate().toLocalDate().isAfter(today.minusDays(30))) // Recent investment tracking
                .forEach(p -> System.out.println("Recently submitted investment: " + p.getId()));

        System.out.println("Date and Time API supports comprehensive investment timeline management\n");
    }

    /**
     * Demonstrates CompletableFuture for asynchronous investment processing
     * Enables concurrent investment analysis and risk assessment
     * Improves system responsiveness for complex investment workflows
     */
    private static void demonstrateCompletableFuture() {
        System.out.println("--- 8. CompletableFuture for Asynchronous Investment Processing ---");

        ExecutorService executor = Executors.newFixedThreadPool(2); // Investment processing thread pool

        // Asynchronous investment risk analysis
        // Enables parallel processing for faster investment decision-making
        CompletableFuture<String> riskAnalysis = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("Performing risk analysis on thread: " + Thread.currentThread().getName());
                    try { Thread.sleep(1000); } catch (InterruptedException e) {} // Simulated analysis time
                    return "Risk analysis completed - investment deemed suitable";
                }, executor);

        // Asynchronous investment compliance check
        // Ensures regulatory compliance without blocking investment workflows
        CompletableFuture<String> complianceCheck = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("Performing compliance check on thread: " + Thread.currentThread().getName());
                    try { Thread.sleep(1500); } catch (InterruptedException e) {} // Regulatory review time
                    return "Compliance check passed - investment meets regulatory standards";
                }, executor);

        // Combine investment analysis results for final decision
        // Enables comprehensive investment evaluation before approval
        CompletableFuture<Void> combined = CompletableFuture.allOf(riskAnalysis, complianceCheck);

        combined.thenRun(() -> {
            try {
                System.out.println(riskAnalysis.get()); // Risk assessment results
                System.out.println(complianceCheck.get()); // Compliance verification
                System.out.println("All investment checks completed, ready for client approval");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).join(); // Wait for all investment analyses to complete

        executor.shutdown(); // Proper resource cleanup for investment system
        System.out.println("CompletableFuture enables efficient asynchronous investment processing\n");
    }

    /**
     * Demonstrates Advanced Streams and Collectors for investment portfolio analysis
     * Enables sophisticated investment data aggregation and reporting
     * Supports data-driven investment decision-making and client reporting
     */
    private static void demonstrateAdvancedStreams() {
        System.out.println("--- 9. Advanced Streams for Investment Portfolio Analysis ---");

        List<InvestmentProposal> proposals = createSampleProposals();

        // Grouping investments by risk level for portfolio diversification
        // Enables strategic asset allocation and risk management
        Map<String, List<InvestmentProposal>> byRiskLevel = proposals.stream()
                .collect(Collectors.groupingBy(InvestmentProposal::getRiskLevel)); // Risk-based categorization
        System.out.println("Investments by risk level categories: " + byRiskLevel.keySet());

        // Partitioning investments for tier-based processing
        // Supports differentiated investment service levels
        Map<Boolean, List<InvestmentProposal>> approvedPartition = proposals.stream()
                .collect(Collectors.partitioningBy(p -> p.getProposedAmount() > 50000)); // Value-based segmentation
        System.out.println("High-value investment opportunities count: " + approvedPartition.get(true).size());

        // Summarizing investment statistics for portfolio analysis
        // Provides key performance indicators for investment management
        DoubleSummaryStatistics stats = proposals.stream()
                .collect(Collectors.summarizingDouble(InvestmentProposal::getProposedAmount)); // Statistical analysis
        System.out.println("Investment amount statistics - Average: $" + String.format("%.2f", stats.getAverage()));

        // Joining client names for investment reporting
        // Enables consolidated client communication and reporting
        String clientList = proposals.stream()
                .map(InvestmentProposal::getClientName) // Client name extraction
                .collect(Collectors.joining(", ", "Clients: ", "")); // Formatted client listing
        System.out.println(clientList);

        // Flat mapping for investment type analysis
        // Enables comprehensive asset class evaluation
        List<List<String>> investmentTypes = Arrays.asList(
                Arrays.asList("STOCKS", "BONDS"), // Traditional investments
                Arrays.asList("MUTUAL_FUNDS", "REAL_ESTATE"), // Alternative investments
                Arrays.asList("COMMODITIES", "CRYPTO") // Emerging investments
        );

        List<String> allTypes = investmentTypes.stream()
                .flatMap(Collection::stream) // Flatten nested investment categories
                .collect(Collectors.toList());
        System.out.println("All available investment types: " + allTypes);

        // Parallel streams for large-scale investment calculations
        // Enables efficient processing of extensive investment portfolios
        double parallelSum = IntStream.range(1, 1000)
                .parallel() // Parallel processing for efficiency
                .mapToDouble(i -> i * 1000.0) // Investment amount scaling
                .sum();
        System.out.println("Parallel investment calculation sum: $" + String.format("%.2f", parallelSum));

        System.out.println("Advanced streams enable sophisticated investment portfolio analysis\n");
    }

    /**
     * Creates sample investment proposals for demonstration purposes
     * Provides realistic investment data for testing Java 8 features
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

Here's a comprehensive Java class demonstrating features from Java 8 to Java 21 for investment proposal management:

```java
// Java 21: Record for immutable investment proposal data (preview in Java 14, finalized in Java 16)
// Records automatically generate constructor, getters, equals(), hashCode(), and toString()
record InvestmentProposal(
    String id,                    // Unique identifier for the proposal
    String title,                 // Title of the investment proposal
    String description,           // Detailed description of the investment
    double amount,                // Investment amount in USD
    double expectedReturnRate,    // Expected annual return rate as percentage
    int durationYears,            // Investment duration in years
    String riskLevel,             // Risk level: LOW, MEDIUM, HIGH
    java.time.LocalDate submissionDate // Submission date using Java 8 Time API
) implements Comparable<InvestmentProposal> { // Java 8: Interface with default method
    
    // Java 14: Compact constructor for validation
    public InvestmentProposal {
        // Validate inputs to ensure data integrity
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (expectedReturnRate < 0) throw new IllegalArgumentException("Return rate cannot be negative");
        if (durationYears <= 0) throw new IllegalArgumentException("Duration must be positive");
    }
    
    // Java 8: Default method implementation in record
    @Override
    public int compareTo(InvestmentProposal other) {
        return Double.compare(this.amount, other.amount);
    }
    
    // Java 8: Static method in record
    public static InvestmentProposal createHighRiskProposal(
            String id, String title, String description, 
            double amount, double returnRate, int years) {
        return new InvestmentProposal(id, title, description, amount, returnRate, years, "HIGH", java.time.LocalDate.now());
    }
}

// Java 15: Sealed classes (preview in Java 15, finalized in Java 17)
// Restricts which classes can extend this class
public sealed class InvestmentManager 
    permits PremiumInvestmentManager, StandardInvestmentManager {
    
    // Java 10: Local-variable type inference with var
    protected final var proposals = new java.util.concurrent.ConcurrentHashMap<String, InvestmentProposal>();
    
    // Java 9: Private interface methods (though shown here in class)
    private void validateProposal(InvestmentProposal proposal) {
        if (proposal == null) throw new IllegalArgumentException("Proposal cannot be null");
    }
    
    // Java 8: Default methods in interfaces (simulated here)
    public void addProposal(InvestmentProposal proposal) {
        validateProposal(proposal);
        proposals.put(proposal.id(), proposal);
    }
    
    // Java 8: Streams API for data processing
    public java.util.List<InvestmentProposal> getHighReturnProposals(double minReturnRate) {
        return proposals.values().stream()
                .filter(p -> p.expectedReturnRate() > minReturnRate) // Lambda expression
                .sorted(java.util.Comparator.comparing(InvestmentProposal::expectedReturnRate).reversed()) // Method reference
                .toList(); // Java 16: Collectors.toList() shortcut
    }
    
    // Java 8: Optional for null safety
    public java.util.Optional<InvestmentProposal> findProposal(String id) {
        return java.util.Optional.ofNullable(proposals.get(id));
    }
    
    // Java 9: Enhanced try-with-resources
    public void saveProposalsToFile(String filename) throws java.io.IOException {
        // Java 16: Stream.toList() collector
        var proposalList = proposals.values().stream().toList();
        
        // Java 9: Try-with-resources with effectively final variables
        final var writer = new java.io.FileWriter(filename);
        try (writer) { // Resource declared outside but managed inside
            // Java 15: Text blocks for multi-line strings
            var jsonTemplate = """
                {
                  "proposals": [
                    %s
                  ],
                  "totalValue": %.2f,
                  "reportDate": "%s"
                }
                """;
            
            // Java 8: String joining collector
            var proposalsJson = proposalList.stream()
                    .map(p -> String.format("""
                        {
                          "id": "%s",
                          "title": "%s",
                          "amount": %.2f,
                          "returnRate": %.2f
                        }""", p.id(), p.title(), p.amount(), p.expectedReturnRate()))
                    .collect(java.util.stream.Collectors.joining(",\n    "));
            
            var totalValue = proposalList.stream()
                    .mapToDouble(InvestmentProposal::amount)
                    .sum();
            
            writer.write(jsonTemplate.formatted(proposalsJson, totalValue, java.time.LocalDate.now()));
        }
    }
}

// Java 15: Non-sealed class to extend sealed hierarchy
non-sealed class PremiumInvestmentManager extends InvestmentManager {
    
    // Java 14: Switch expressions with pattern matching (preview in 14, refined in later versions)
    public String evaluateRisk(InvestmentProposal proposal) {
        return switch (proposal.riskLevel().toUpperCase()) {
            case "LOW" -> "Conservative investment with stable returns";
            case "MEDIUM" -> "Balanced risk-reward profile";
            case "HIGH" -> "High growth potential with significant risk";
            default -> "Risk level not recognized";
        };
    }
    
    // Java 19: Virtual threads (preview) - simulated with regular threads
    public void processProposalsConcurrently() {
        // Java 21: Virtual threads (preview in 19, refined in 21)
        // var executor = Executors.newVirtualThreadPerTaskExecutor();
        
        // Java 8: CompletableFuture for asynchronous processing
        var futures = proposals.values().stream()
                .map(proposal -> java.util.concurrent.CompletableFuture
                        .supplyAsync(() -> analyzeProposal(proposal)))
                .toArray(java.util.concurrent.CompletableFuture[]::new);
        
        // Java 8: CompletableFuture.allOf() to wait for all
        java.util.concurrent.CompletableFuture.allOf(futures)
                .thenRun(() -> System.out.println("All proposals analyzed"))
                .join();
    }
    
    // Java 8: Functional interfaces and lambda expressions
    private String analyzeProposal(InvestmentProposal proposal) {
        // Java 8: Built-in functional interfaces
        java.util.function.Function<InvestmentProposal, Double> calculateNPV = p -> 
            p.amount() * Math.pow(1 + p.expectedReturnRate()/100, p.durationYears());
        
        return "Proposal %s NPV: %.2f".formatted(proposal.id(), calculateNPV.apply(proposal));
    }
}

// Java 21: Main class with all modern features
public class InvestmentProposalManager {
    
    // Java 21: Main method can be simplified with var (since Java 10)
    public static void main(String[] args) {
        // Java 10: Local variable type inference
        var manager = new PremiumInvestmentManager();
        
        // Java 8: Time API for date handling
        var today = java.time.LocalDate.now();
        var nextYear = today.plusYears(1);
        
        // Java 16: Pattern matching for instanceof (preview in 14, finalized in 16)
        Object proposalObj = new InvestmentProposal(
            "INV-001", 
            "Tech Startup Investment", 
            "Investment in promising AI startup", 
            50000, 
            12.5, 
            3, 
            "HIGH", 
            today
        );
        
        // Java 16: Pattern matching eliminates cast
        if (proposalObj instanceof InvestmentProposal proposal) {
            manager.addProposal(proposal);
            System.out.println("Added proposal: " + proposal.title());
        }
        
        // Java 14: Helpful NullPointerExceptions (JVM feature)
        try {
            manager.addProposal(null);
        } catch (Exception e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }
        
        // Java 8: Optional API for safe operations
        manager.findProposal("INV-001")
               .ifPresentOrElse(
                   p -> System.out.println("Found: " + p.title()),
                   () -> System.out.println("Proposal not found")
               );
        
        // Java 12: Switch expressions (preview in 12, finalized in 14)
        var riskAssessment = manager instanceof PremiumInvestmentManager pm ? 
                            pm.evaluateRisk((InvestmentProposal) proposalObj) : 
                            "Standard risk assessment";
        System.out.println(riskAssessment);
        
        // Java 8: Streams for data processing
        var highReturnProposals = manager.getHighReturnProposals(10.0);
        System.out.println("High return proposals: " + highReturnProposals.size());
        
        // Java 9: Factory methods for collections
        var riskLevels = java.util.List.of("LOW", "MEDIUM", "HIGH");
        System.out.println("Risk levels: " + riskLevels);
        
        // Java 11: String utility methods
        var description = "  High growth potential investment opportunity  ";
        System.out.println("Trimmed description: '" + description.strip() + "'");
        System.out.println("Description lines: " + description.lines().count());
        
        // Java 17: Sealed classes runtime checking
        System.out.println("Manager class is sealed: " + 
            InvestmentManager.class.isSealed());
        
        // Java 21: Sequence collections (preview)
        // var sequence = Sequence.of(1, 2, 3, 4, 5);
        
        // Java 8: Files API enhancements
        try {
            manager.saveProposalsToFile("proposals.json");
            System.out.println("Proposals saved to file");
        } catch (java.io.IOException e) {
            System.err.println("Error saving proposals: " + e.getMessage());
        }
        
        // Java 19: Structured Concurrency (preview) - simulated
        try {
            manager.processProposalsConcurrently();
        } catch (Exception e) {
            System.err.println("Processing error: " + e.getMessage());
        }
        
        // Java 21: String templates (preview) - simulated with formatted strings
        var summary = """
            Investment Summary:
            - Total Proposals: %d
            - Report Generated: %s
            - System: %s
            """.formatted(
                manager.proposals.size(),
                java.time.LocalDateTime.now().toString(),
                System.getProperty("java.version")
            );
        System.out.println(summary);
    }
}
```

This comprehensive example demonstrates key Java features:

**Java 8 Features:**
- Lambda expressions and Streams API
- Optional class for null safety
- New Date/Time API (LocalDate, LocalDateTime)
- Default and static methods in interfaces
- CompletableFuture for asynchronous programming
- Method references

**Java 9 Features:**
- Private interface methods
- Enhanced try-with-resources
- Factory methods for collections (List.of, Set.of)
- Stream API enhancements (.of, .dropWhile, .takeWhile)

**Java 10 Features:**
- Local-variable type inference (var keyword)

**Java 11 Features:**
- String utility methods (isBlank, lines, strip, etc.)

**Java 12-13 Features:**
- Switch expressions (preview)

**Java 14 Features:**
- Switch expressions (standard)
- Helpful NullPointerExceptions
- Pattern matching for instanceof (preview)

**Java 15 Features:**
- Text blocks for multi-line strings
- Sealed classes (preview)

**Java 16 Features:**
- Pattern matching for instanceof (standard)
- Records (standard)
- Stream.toList() collector

**Java 17 Features:**
- Sealed classes (standard)

**Java 19-21 Features:**
- Virtual threads (preview)
- Structured Concurrency (preview)
- String templates (preview)
- Sequence collections (preview)

The code provides a complete investment proposal management system showcasing modern Java capabilities while maintaining clean, readable, and maintainable code.
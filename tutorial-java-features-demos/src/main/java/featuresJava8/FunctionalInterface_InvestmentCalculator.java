package main.java.featuresJava8;

/**
 * Functional interface for investment calculations
 * Enables flexible financial modeling and scenario analysis
 * Supports various investment calculation methodologies
 */
@FunctionalInterface
interface FunctionalInterface_InvestmentCalculator {
    double calculate(double amount, double rate, int years);

}


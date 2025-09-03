package com.uc.ipm.s2s.service;

import com.uc.ipm.s2s.dto.RiskScoreRequest;
import com.uc.ipm.s2s.dto.RiskScoreResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker; // Resilience4j annotation to fail fast after repeated failures.
import io.github.resilience4j.retry.annotation.Retry;   // Resilience4j annotation to automatically retry transient failures.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient; // Reactive HTTP client used by Spring WebFlux.
import reactor.core.publisher.Mono;                     // Reactive single-value publisher from Project Reactor.

@Service                                                // Registers the class as a Spring bean in the application context.
public class RiskScoreClient_resiliance4j {

    private static final Logger log =
            LoggerFactory.getLogger(RiskScoreClient_resiliance4j.class);

    private final WebClient client;

    public RiskScoreClient_resiliance4j(WebClient riskClient) {
        this.client = riskClient;
    }

    @Retry(name = "riskApi")                            // Apply retry policy named 'riskApi' (configured in application.yml).
    @CircuitBreaker(name = "riskApi", fallbackMethod = "fallBackMethod")       // When calls fail and CB opens (or errors persist), invoke 'fallBackMethod'.
    public Mono<RiskScoreResponse> getScore(Long proposalId) { // Public API: get risk score for a proposal, returns reactive Mono.
        return client.post()                            // Start building an HTTP POST request with WebClient.
                .uri("/v1/risk/score")                  // Append the relative URI path to the client's baseUrl.
                .contentType(MediaType.APPLICATION_JSON)// Set 'Content-Type: application/json' for the request.
                .accept(MediaType.APPLICATION_JSON)     // Set 'Accept: application/json' for the response.
                .bodyValue(new RiskScoreRequest(proposalId)) // Serialize request DTO as JSON body (reactively).
                .retrieve()                             // Execute the request and obtain a response spec (handles status codes).
                .bodyToMono(RiskScoreResponse.class);   // Convert JSON response body into RiskScoreResponse as a Mono.
    }

    private Mono<RiskScoreResponse> fallBackMethod(Long proposalId,Throwable t) {
        log.warn("Risk API fallBackMethod for proposal {}: {}", proposalId, t.toString());
        // Provide a neutral score or escalate
        return Mono.just(new RiskScoreResponse(         // Return a safe default response wrapped in a Mono to keep it reactive.
                proposalId,                             // Echo back the same proposal id.
                0.0,                                    // Neutral/zero score as a safe default.
                "UNKNOWN"));                            // Qualifier to indicate fallBackMethod source or unknown risk.
    }
}                                                       // End of class.

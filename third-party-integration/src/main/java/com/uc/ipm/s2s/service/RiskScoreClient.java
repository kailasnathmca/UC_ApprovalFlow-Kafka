package com.uc.ipm.s2s.service;

import com.uc.ipm.s2s.dto.RiskScoreRequest;
import com.uc.ipm.s2s.dto.RiskScoreResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RiskScoreClient {
    private static final Logger log = LoggerFactory.getLogger(RiskScoreClient.class);
    private final WebClient client;

    public RiskScoreClient(WebClient riskClient) {
        this.client = riskClient;
    }

    @Retry(name = "riskApi")
    @CircuitBreaker(name = "riskApi", fallbackMethod = "fallback")
    public Mono<RiskScoreResponse> getScore(Long proposalId) {
        return client.post()
                .uri("/v1/risk/score")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new RiskScoreRequest(proposalId))
                .retrieve()
                .bodyToMono(RiskScoreResponse.class);
    }

    private Mono<RiskScoreResponse> fallback(Long proposalId, Throwable t) {
        log.warn("Risk API fallback for proposal {}: {}", proposalId, t.toString());
        // Provide a neutral score or escalate
        return Mono.just(new RiskScoreResponse(proposalId, 0.0, "UNKNOWN"));
    }
}

package com.uc.ipm.s2s.web;

import com.uc.ipm.s2s.dto.RiskScoreResponse;
import com.uc.ipm.s2s.service.RiskScoreClient;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/s2s")
@Validated
public class RiskScoreController {

    private final RiskScoreClient riskScoreClient;

    public RiskScoreController(RiskScoreClient riskScoreClient) {
        this.riskScoreClient = riskScoreClient;
    }

    @GetMapping("/risk/score")
    public Mono<ResponseEntity<RiskScoreResponse>> score(@RequestParam("proposalId") @NotNull Long proposalId) {
        return riskScoreClient.getScore(proposalId)
                .map(ResponseEntity::ok);
    }
}

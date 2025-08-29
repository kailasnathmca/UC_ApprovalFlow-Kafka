package com.uc.proposalservice.controller;

import com.uc.proposalservice.dto.CreateProposalRequest;
import com.uc.proposalservice.dto.ProposalResponse;
import com.uc.proposalservice.entity.Proposal;
import com.uc.proposalservice.enums.ProposalStatus;
import com.uc.proposalservice.service.ProposalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/** CRUD-ish endpoints (no approval transitions here). */
@RestController
@RequestMapping("/api/proposals")
public class ProposalController {
    private final ProposalService svc;
    public ProposalController(ProposalService svc){ this.svc = svc; }

    @PostMapping
    public ProposalResponse create(@Valid @RequestBody CreateProposalRequest req){
        return svc.create(req);
    }

    @GetMapping("/{id}")
    public ProposalResponse get(@PathVariable Long id){ return svc.get(id); }

    @GetMapping
    public Page<Proposal> list(@RequestParam(required=false) ProposalStatus status, Pageable pageable){
        return svc.list(status, pageable);
    }
}

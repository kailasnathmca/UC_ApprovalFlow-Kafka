package com.uc.proposalservice.controller;

import com.uc.proposalservice.dto.ApproveRequest;
import com.uc.proposalservice.dto.RejectRequest;
import com.uc.proposalservice.entity.Proposal;
import com.uc.proposalservice.service.ApprovalWorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Approval actions: submit, approve, reject. */
@RestController
@RequestMapping("/api/proposals")
public class ApprovalController {
    private final ApprovalWorkflowService wf;
    public ApprovalController(ApprovalWorkflowService wf){ this.wf = wf; }

    /** Submit for review. Optional custom chain body ["TEAM_LEAD","RISK","CFO"]. */
    @PostMapping("/{id}/submit")
    public Proposal submit(@PathVariable Long id, @RequestBody(required=false) List<String> customChain){
        return wf.submit(id, customChain);
    }

    @PostMapping("/{id}/approve")
    public Proposal approve(@PathVariable Long id, @Valid @RequestBody ApproveRequest req){
        return wf.approve(id, req);
    }

    @PostMapping("/{id}/reject")
    public Proposal reject(@PathVariable Long id, @Valid @RequestBody RejectRequest req){
        return wf.reject(id, req);
    }
}

package com.uc.auditservice.controller;

import com.uc.auditservice.entity.AuditEntry;
import com.uc.auditservice.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/** Simple paging API to inspect audit rows. */
@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditService svc;
    public AuditController(AuditService svc){ this.svc = svc; }

    @GetMapping
    public Page<AuditEntry> list(@RequestParam(required=false) Long proposalId, Pageable pageable){
        return svc.list(proposalId, pageable);
    }
}

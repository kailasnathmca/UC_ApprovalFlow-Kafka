package com.uc.auditservice.service;

import com.uc.auditservice.entity.AuditEntry;
import com.uc.auditservice.repository.AuditRepository;
import com.uc.common.ProposalEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

/** Converts consumed event -> AuditEntry row. */
@Service
public class AuditService {
    private final AuditRepository repo;
    private final ObjectMapper om = new ObjectMapper();
    public AuditService(AuditRepository repo){ this.repo = repo; }

    public void save(ProposalEvent ev){
        try {
            AuditEntry e = new AuditEntry();
            e.setEventId(ev.getId());
            e.setEventType(ev.getType().name());
            e.setProposalId(ev.getProposalId());
            e.setPayloadJson(om.writeValueAsString(ev.getPayload()==null? Map.of(): ev.getPayload()));
            e.setAt(ev.getAt());
            repo.save(e);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Page<AuditEntry> list(Long proposalId, Pageable pageable){
        return proposalId == null ? repo.findAll(pageable) : repo.findByProposalId(proposalId, pageable);
    }
}

package com.uc.auditservice.repository;

import com.uc.auditservice.entity.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Paging repo, allows filtering by proposalId. */
public interface AuditRepository extends JpaRepository<AuditEntry, Long> {
    Page<AuditEntry> findByProposalId(Long proposalId, Pageable pageable);
}

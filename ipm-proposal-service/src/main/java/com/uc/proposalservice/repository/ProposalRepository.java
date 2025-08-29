package com.uc.proposalservice.repository;

import com.uc.proposalservice.entity.Proposal;
import com.uc.proposalservice.enums.ProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Basic JPA repo + finder by status for listing. */
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    Page<Proposal> findByStatus(ProposalStatus status, Pageable pageable);
}

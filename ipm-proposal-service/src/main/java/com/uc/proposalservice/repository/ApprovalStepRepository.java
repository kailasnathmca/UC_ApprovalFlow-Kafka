package com.uc.proposalservice.repository;

import com.uc.proposalservice.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;

/** Separate repo (not used heavily but kept for clarity). */
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> { }

package com.uc.proposalservice.enums;

/** Lifecycle of a proposal. */
public enum ProposalStatus {
    DRAFT,        // newly created, editable by user
    UNDER_REVIEW, // submitted to approval chain
    APPROVED,     // fully approved at final step
    REJECTED      // rejected at some step
}

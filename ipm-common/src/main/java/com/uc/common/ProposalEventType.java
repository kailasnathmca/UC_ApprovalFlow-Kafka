package com.uc.common;

/** Domain event types emitted by proposal-service and consumed by others. */
public enum ProposalEventType {
    PROPOSAL_SUBMITTED,
    STEP_APPROVED,
    STEP_REJECTED,
    PROPOSAL_APPROVED,
    PROPOSAL_REJECTED
}

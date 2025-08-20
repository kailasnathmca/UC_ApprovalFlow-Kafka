package com.uni.tutorialjava21featuresdemos.records_plus_Sealed_Interfaces_forWorkFlowSteps.CommandSide;

import org.springframework.stereotype.Service;

/**
 * Exhaustive switch: compiler forces us to handle all command types.
 * If we add a new command later, the switch won’t compile until updated.
 */
@Service
public class ApprovalHandler {

    public void handle(long proposalId, ApprovalCommand cmd) {
        switch (cmd) {
            case Approve(var approver, var comments) -> approve(proposalId, approver, comments);
            case Reject (var approver, var reason)   -> reject (proposalId, approver, reason);
        }
    }

    private void approve(long id, String by, String comments) {
        // 1) mutate domain state & persist
        // 2) emit ProposalApproved event
    }

    private void reject(long id, String by, String reason) {
        // 1) mutate domain state & persist
        // 2) emit ProposalRejected event
    }
}

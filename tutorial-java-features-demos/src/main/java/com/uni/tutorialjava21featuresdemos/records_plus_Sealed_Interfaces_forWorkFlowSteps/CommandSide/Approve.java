package com.uni.tutorialjava21featuresdemos.records_plus_Sealed_Interfaces_forWorkFlowSteps.CommandSide;


/**
 * Records = immutable, concise data carriers for commands.
 */
public record Approve(String approver, String comments) implements ApprovalCommand {
}


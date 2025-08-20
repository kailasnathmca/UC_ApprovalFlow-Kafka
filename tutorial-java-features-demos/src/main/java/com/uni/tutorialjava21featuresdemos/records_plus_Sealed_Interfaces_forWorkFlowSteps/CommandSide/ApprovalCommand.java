package com.uni.tutorialjava21featuresdemos.records_plus_Sealed_Interfaces_forWorkFlowSteps.CommandSide;


/**
 * Sealed interface = compile-time closed hierarchy.
 * Only the permitted types below can implement it.
 * This lets our switch be exhaustive and safe.
 */
public sealed interface ApprovalCommand
        permits Approve, Reject {
}


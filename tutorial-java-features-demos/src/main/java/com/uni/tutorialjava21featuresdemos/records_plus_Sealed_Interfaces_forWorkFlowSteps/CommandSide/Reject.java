package com.uni.tutorialjava21featuresdemos.records_plus_Sealed_Interfaces_forWorkFlowSteps.CommandSide;

public record Reject(String approver, String reason) implements ApprovalCommand { }


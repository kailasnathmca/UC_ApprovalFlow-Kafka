package com.uc.proposalservice.dto;

import jakarta.validation.constraints.NotBlank;

/** Payload for rejecting the current step. */
public class RejectRequest {
    @NotBlank private String approver;
    private String comments;

    public String getApprover() { return approver; } public void setApprover(String approver) { this.approver = approver; }
    public String getComments() { return comments; } public void setComments(String comments) { this.comments = comments; }
}

package com.uc.proposalservice.service;

import com.uc.common.ProposalEvent;
import com.uc.common.ProposalEventType;
import com.uc.proposalservice.dto.ApproveRequest;
import com.uc.proposalservice.dto.RejectRequest;
import com.uc.proposalservice.entity.ApprovalStep;
import com.uc.proposalservice.entity.Proposal;
import com.uc.proposalservice.enums.Decision;
import com.uc.proposalservice.enums.ProposalStatus;
import com.uc.proposalservice.exception.BadRequestException;
import com.uc.proposalservice.kafka.EventPublisher;
import com.uc.proposalservice.repository.ApprovalStepRepository;
import com.uc.proposalservice.repository.ProposalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Encapsulates state transitions:
 * DRAFT -> submit() -> UNDER_REVIEW
 * approve()/reject() step by step -> APPROVED/REJECTED
 * Emits Kafka events on each transition.
 */
@Service
public class ApprovalWorkflowService {
    private final ProposalRepository proposals;
    private final ApprovalStepRepository steps;
    private final EventPublisher publisher;

    @Value("${ipm.default-approval-chain:PEER_REVIEW,MANAGER_APPROVAL,COMPLIANCE}")
    private String defaultChain;

    public ApprovalWorkflowService(ProposalRepository proposals,
                                   ApprovalStepRepository steps,
                                   EventPublisher publisher) {
        this.proposals = proposals;
        this.steps = steps;
        this.publisher = publisher;
    }

    /** Move from DRAFT to UNDER_REVIEW with configured chain. */
    @Transactional
    public Proposal submit(Long id, List<String> customChain) {
        Proposal p = proposals.findById(id)
                .orElseThrow(() -> new BadRequestException("Proposal not found: " + id));

        if (p.getStatus() != ProposalStatus.DRAFT)
            throw new BadRequestException("Only DRAFT proposals can be submitted.");

        List<String> chain = (customChain == null || customChain.isEmpty())
                ? Arrays.asList(defaultChain.split(","))
                : customChain;

        // Rebuild steps for this submission
        p.getSteps().clear();
        for (int i = 0; i < chain.size(); i++) {
            ApprovalStep s = new ApprovalStep();
            s.setProposal(p);
            s.setStepOrder(i);
            s.setRole(chain.get(i).trim());
            s.setDecision(Decision.PENDING);
            p.getSteps().add(s);
        }
        p.setStatus(ProposalStatus.UNDER_REVIEW);
        p.setSubmittedAt(OffsetDateTime.now());
        p.setCurrentStepIndex(0);

        Proposal saved = proposals.save(p);

        publisher.publish(new ProposalEvent(UUID.randomUUID().toString(),
                ProposalEventType.PROPOSAL_SUBMITTED, saved.getId(), Map.of("chain", chain)));

        return saved;
    }

    /** Approve current step; advance pointer or finalize APPROVED. */
    @Transactional
    public Proposal approve(Long id, ApproveRequest req) {
        Proposal p = loadActive(id);
        ApprovalStep current = getCurrentStep(p);

        current.setApprover(req.getApprover());
        current.setComments(req.getComments());
        current.setDecision(Decision.APPROVED);
        current.setDecidedAt(OffsetDateTime.now());

        boolean lastStep = p.getCurrentStepIndex() >= p.getSteps().size() - 1;
        if (lastStep) {
            p.setStatus(ProposalStatus.APPROVED);
            publisher.publish(new ProposalEvent(UUID.randomUUID().toString(),
                    ProposalEventType.PROPOSAL_APPROVED, p.getId(),
                    Map.of("role", current.getRole(), "approver", req.getApprover())));
        } else {
            p.setCurrentStepIndex(p.getCurrentStepIndex() + 1);
            publisher.publish(new ProposalEvent(UUID.randomUUID().toString(),
                    ProposalEventType.STEP_APPROVED, p.getId(),
                    Map.of("role", current.getRole(), "approver", req.getApprover(), "nextStep", p.getCurrentStepIndex())));
        }
        return proposals.save(p);
    }

    /** Reject current step; finalize REJECTED. */
    @Transactional
    public Proposal reject(Long id, RejectRequest req) {
        Proposal p = loadActive(id);
        ApprovalStep current = getCurrentStep(p);

        current.setApprover(req.getApprover());
        current.setComments(req.getComments());
        current.setDecision(Decision.REJECTED);
        current.setDecidedAt(OffsetDateTime.now());

        p.setStatus(ProposalStatus.REJECTED);

        publisher.publish(new ProposalEvent(UUID.randomUUID().toString(),
                ProposalEventType.PROPOSAL_REJECTED, p.getId(),
                Map.of("role", current.getRole(), "approver", req.getApprover(), "reason", req.getComments())));

        return proposals.save(p);
    }

    // ---- helpers ----
    private Proposal loadActive(Long id) {
        Proposal p = proposals.findById(id)
                .orElseThrow(() -> new BadRequestException("Proposal not found: " + id));
        if (p.getStatus() != ProposalStatus.UNDER_REVIEW)
            throw new BadRequestException("Proposal is not UNDER_REVIEW; status=" + p.getStatus());
        if (p.getSteps().isEmpty())
            throw new BadRequestException("No approval steps configured.");
        return p;
    }

    private ApprovalStep getCurrentStep(Proposal p) {
        int idx = p.getCurrentStepIndex();
        if (idx < 0 || idx >= p.getSteps().size())
            throw new BadRequestException("Invalid current step index=" + idx);
        ApprovalStep s = p.getSteps().get(idx);
        if (s.getDecision() != Decision.PENDING)
            throw new BadRequestException("Current step already decided.");
        return s;
    }
}

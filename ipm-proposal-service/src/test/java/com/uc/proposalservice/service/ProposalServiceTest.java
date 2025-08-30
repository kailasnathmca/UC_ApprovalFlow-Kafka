package com.uc.proposalservice.service;

import com.uc.proposalservice.dto.CreateProposalRequest;
import com.uc.proposalservice.dto.ProposalResponse;
import com.uc.proposalservice.entity.Proposal;
import com.uc.proposalservice.enums.ProposalStatus;
import com.uc.proposalservice.repository.ProposalRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProposalServiceTest {

    @Test
    void create_shouldPersistDraftProposal_andReturnResponse() {
        ProposalRepository repo = Mockito.mock(ProposalRepository.class);
        ProposalService service = new ProposalService(repo);

        CreateProposalRequest req = new CreateProposalRequest();
        req.setTitle("Test Proposal");
        req.setApplicantName("Alice");
        req.setAmount(new BigDecimal("1000.00"));
        req.setDescription("Desc");

        when(repo.save(any(Proposal.class))).thenAnswer(invocation -> {
            Proposal p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProposalResponse resp = service.create(req);

        ArgumentCaptor<Proposal> captor = ArgumentCaptor.forClass(Proposal.class);
        verify(repo).save(captor.capture());
        Proposal saved = captor.getValue();
        assertEquals(ProposalStatus.DRAFT, saved.getStatus());
        assertEquals(req.getTitle(), saved.getTitle());
        assertEquals(req.getApplicantName(), saved.getApplicantName());
        assertEquals(req.getAmount(), saved.getAmount());
        assertEquals(req.getDescription(), saved.getDescription());

        assertEquals(1L, resp.getId());
        assertEquals(req.getTitle(), resp.getTitle());
        assertEquals(req.getApplicantName(), resp.getApplicantName());
        assertEquals(req.getAmount(), resp.getAmount());
        assertEquals(req.getDescription(), resp.getDescription());
        assertEquals(ProposalStatus.DRAFT, resp.getStatus());
    }
}


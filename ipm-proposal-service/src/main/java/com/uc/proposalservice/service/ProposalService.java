package com.uc.proposalservice.service;

import com.uc.proposalservice.dto.CreateProposalRequest;
import com.uc.proposalservice.dto.ProposalResponse;
import com.uc.proposalservice.entity.Proposal;
import com.uc.proposalservice.enums.ProposalStatus;
import com.uc.proposalservice.exception.NotFoundException;
import com.uc.proposalservice.repository.ProposalRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** CRUD-ish operations not involving approval logic. */
@Service
public class ProposalService {
    private final ProposalRepository repo;
    public ProposalService(ProposalRepository repo){ this.repo = repo; }

    /** Create DRAFT proposal. */
    public ProposalResponse create(CreateProposalRequest req){
        Proposal p = new Proposal();
        p.setTitle(req.getTitle());
        p.setApplicantName(req.getApplicantName());
        p.setAmount(req.getAmount());
        p.setDescription(req.getDescription());
        p.setStatus(ProposalStatus.DRAFT);
        Proposal saved = repo.save(p);
        return toResponse(saved);
    }

    public Proposal getEntity(Long id){
        return repo.findById(id).orElseThrow(()->new NotFoundException("Proposal not found: "+id));
    }

    public ProposalResponse get(Long id){ return toResponse(getEntity(id)); }

    /** List by status if provided, else all (paged). */
    public Page<Proposal> list(ProposalStatus st, Pageable pageable){
        return st != null ? repo.findByStatus(st, pageable) : repo.findAll(pageable);
    }

    public ProposalResponse toResponse(Proposal p){
        ProposalResponse r = new ProposalResponse();
        BeanUtils.copyProperties(p, r);
        r.setSteps(p.getSteps());
        return r;
    }
}

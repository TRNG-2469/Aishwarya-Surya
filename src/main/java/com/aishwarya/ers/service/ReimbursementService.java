package com.aishwarya.ers.service;

import com.aishwarya.ers.model.Reimbursement;
import com.aishwarya.ers.model.ReimbursementStatus;
import com.aishwarya.ers.repository.ReimbursementRepository;

import java.util.List;

public class ReimbursementService {

    private final ReimbursementRepository reimbursementRepository;

    public ReimbursementService(ReimbursementRepository reimbursementRepository) {
        this.reimbursementRepository = reimbursementRepository;
    }

    // submit a new reimbursement request
    public Reimbursement submit(Reimbursement reimbursement) {

        // every new request starts as PENDING, no matter what the client sends
        reimbursement.setStatus(ReimbursementStatus.PENDING);

        boolean wasCreated = reimbursementRepository.create(reimbursement);
        if (!wasCreated) {
            throw new RuntimeException("Failed to create reimbursement request");
        }

        return reimbursement;
    }

    // get one reimbursement by its id
    public Reimbursement getById(int id) {
        Reimbursement reimbursement = reimbursementRepository.findById(id);
        if (reimbursement == null) {
            throw new RuntimeException("No reimbursement with id " + id);
        }
        return reimbursement;
    }

    // get every reimbursement submitted by one user
    public List<Reimbursement> getByUserId(int userId) {
        return reimbursementRepository.findByUserId(userId);
    }

    // get every reimbursement in the system
    public List<Reimbursement> getAll() {
        return reimbursementRepository.findAll();
    }

    // get every reimbursement with a given status (PENDING, APPROVED, DENIED)
    public List<Reimbursement> getByStatus(ReimbursementStatus status) {
        return reimbursementRepository.findByStatus(status);
    }

    // update a request while it's still pending (amount, description, type)
    public Reimbursement updatePending(Reimbursement reimbursement) {
        boolean wasUpdated = reimbursementRepository.updatePending(reimbursement);
        if (!wasUpdated) {
            throw new RuntimeException(
                    "Could not update reimbursement with id " + reimbursement.getId() +
                            " (it may not exist, may not belong to this user, or is no longer pending)"
            );
        }
        return reimbursement;
    }

    // a manager approves a pending request
    public void approve(int reimbursementId, int resolverId) {
        boolean wasResolved = reimbursementRepository.resolve(
                reimbursementId,
                ReimbursementStatus.APPROVED,
                resolverId
        );
        if (!wasResolved) {
            throw new RuntimeException(
                    "Could not approve reimbursement with id " + reimbursementId +
                            " (it may not exist or is no longer pending)"
            );
        }
    }

    // a manager denies a pending request
    public void deny(int reimbursementId, int resolverId) {
        boolean wasResolved = reimbursementRepository.resolve(
                reimbursementId,
                ReimbursementStatus.DENIED,
                resolverId
        );
        if (!wasResolved) {
            throw new RuntimeException(
                    "Could not deny reimbursement with id " + reimbursementId +
                            " (it may not exist or is no longer pending)"
            );
        }
    }
}
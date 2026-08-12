package com.aishwarya.ers.service;

import com.aishwarya.ers.exception.ForbiddenException;
import com.aishwarya.ers.model.Reimbursement;
import com.aishwarya.ers.model.ReimbursementStatus;
import com.aishwarya.ers.repository.ReimbursementRepository;
import com.aishwarya.ers.exception.UserNotFoundException;
import com.aishwarya.ers.exception.ForbiddenException;

import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.repository.UserRepository;
import io.javalin.http.Context;

import java.util.List;

public class ReimbursementService {

    private final ReimbursementRepository repo;
    private final UserRepository userRepo;

    public ReimbursementService(ReimbursementRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    public Reimbursement submit(Reimbursement r) {
        r.setStatus(ReimbursementStatus.PENDING);
        if (!repo.create(r)) throw new RuntimeException("Failed to create reimbursement request");
        return r;
    }

    public Reimbursement getById(int id) {
        Reimbursement r = repo.findById(id);
        if (r == null) throw new RuntimeException("No reimbursement with id " + id);
        return r;
    }

    public List<Reimbursement> getByUserId(int userId) {
        return repo.findByUserId(userId);
    }

    public List<Reimbursement> getAll() {
        return repo.findAll();
    }

    public List<Reimbursement> getByStatus(ReimbursementStatus status) {
        return repo.findByStatus(status);
    }

    public List<Reimbursement> getByFilters(int callerId, ReimbursementStatus status, String department) {
        User caller = userRepo.findById(callerId);

        if (caller == null) {
            throw new UserNotFoundException(callerId);
        }

        if (caller.getRole() != Role.MANAGER) {
            throw new ForbiddenException("Only managers can view all reimbursements");
        }

        return repo.findByFilters(status, department);
    }

    public Reimbursement updatePending(Reimbursement r) {
        if (!repo.updatePending(r))
            throw new RuntimeException("Could not update reimbursement with id " + r.getId()
                    + " (it may not exist, may not belong to this user, or is no longer pending)");
        return r;
    }

    public void approve(int id, int resolverId) {
        resolve(id, ReimbursementStatus.APPROVED, resolverId, "approve");
    }

    public void deny(int id, int resolverId) {
        resolve(id, ReimbursementStatus.DENIED, resolverId, "deny");
    }

    private void resolve(int id, ReimbursementStatus status, int resolverId, String action) {

        User resolver = userRepo.findById(resolverId);

        if (resolver == null) {
            throw new RuntimeException("No user with id " + resolverId);
        }

        if (resolver.getRole() != Role.MANAGER) {
            throw new RuntimeException(
                    "User " + resolverId + " is not a Manager and cannot " + action + " reimbursements"
            );
        }

        if (!repo.resolve(id, status, resolverId))
            throw new RuntimeException("Could not " + action + " reimbursement with id " + id
                    + " (it may not exist or is no longer pending)");
    }

    public List<Reimbursement> getAllReimbursements(User loggedInUser) {
        if (loggedInUser == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (loggedInUser.getRole() != Role.MANAGER) {
            throw new ForbiddenException("Access Denied: Only Managers can view all reimbursements.");
        }

        return ReimbursementRepository.findAll();
    }
}
package com.aishwarya.ers.util;

import com.aishwarya.ers.model.Reimbursement;
import com.aishwarya.ers.model.ReimbursementStatus;
import com.aishwarya.ers.model.ReimbursementType;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.repository.ReimbursementRepository;
import com.aishwarya.ers.repository.UserRepository;
import com.aishwarya.ers.service.ReimbursementService;

import java.math.BigDecimal;
import java.util.List;

public class ReimbursementServiceTest {

    public static void main(String[] args) {

        UserRepository userRepository = new UserRepository();
        ReimbursementService reimbursementService =
                new ReimbursementService(new ReimbursementRepository());

        User user = userRepository.findByUsername("testuser1");
        if (user == null) {
            System.out.println("Test user was not found.");
            return;
        }

        Reimbursement request = new Reimbursement();
        request.setUserId(user.getId());
        request.setAmount(new BigDecimal("42.00"));
        request.setDescription("Client dinner");
        request.setType(ReimbursementType.MEALS);

        Reimbursement submitted = reimbursementService.submit(request);
        System.out.println("Submitted with status: " + submitted.getStatus());

        Reimbursement found = reimbursementService.getById(submitted.getId());
        System.out.println("Found reimbursement, status: " + found.getStatus());

        found.setDescription("Client dinner - updated");
        Reimbursement updated = reimbursementService.updatePending(found);
        System.out.println("Updated description: " + updated.getDescription());

        reimbursementService.approve(submitted.getId(), user.getId());
        Reimbursement resolved = reimbursementService.getById(submitted.getId());
        System.out.println("Status after approve: " + resolved.getStatus());

        try {
            resolved.setDescription("should fail");
            reimbursementService.updatePending(resolved);
            System.out.println("ERROR: updated a resolved reimbursement");
        } catch (RuntimeException e) {
            System.out.println("Confirmed resolved requests can't be edited: " + e.getMessage());
        }

        List<Reimbursement> userReimbursements = reimbursementService.getByUserId(user.getId());
        System.out.println("Total reimbursements for user: " + userReimbursements.size());
    }
}
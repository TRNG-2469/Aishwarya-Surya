package com.aishwarya.ers.util;

import com.aishwarya.ers.model.Reimbursement;
import com.aishwarya.ers.model.ReimbursementStatus;
import com.aishwarya.ers.model.ReimbursementType;
import com.aishwarya.ers.repository.ReimbursementRepository;
import com.aishwarya.ers.repository.UserRepository;
import com.aishwarya.ers.model.User;

import java.math.BigDecimal;

public class UpdatePendingGuardTest {

    public static void main(String[] args) {

        UserRepository userRepository = new UserRepository();
        ReimbursementRepository reimbursementRepository =
                new ReimbursementRepository();

        User user = userRepository.findByUsername("testuser1");

        if (user == null) {
            System.out.println("Test user was not found.");
            return;
        }

        Reimbursement r = new Reimbursement();
        r.setUserId(user.getId());
        r.setAmount(new BigDecimal("40.00"));
        r.setDescription("Original description");
        r.setType(ReimbursementType.MEALS);
        r.setStatus(ReimbursementStatus.PENDING);

        reimbursementRepository.create(r);
        System.out.println("Created reimbursement ID: " + r.getId());

        boolean resolved = reimbursementRepository.resolve(
                r.getId(), ReimbursementStatus.APPROVED, user.getId()
        );
        System.out.println("Resolved (approved): " + resolved);

        r.setDescription("Attempted edit after approval");
        boolean updateResult = reimbursementRepository.updatePending(r);

        System.out.println("Update allowed after approval? " + updateResult);
        System.out.println("Expected: false");

        Reimbursement afterAttempt = reimbursementRepository.findById(r.getId());
        System.out.println("Description in DB: " + afterAttempt.getDescription());
        System.out.println("Expected: Original description");
    }
}
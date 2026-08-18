package com.aishwarya.ers.exception;

public class ReimbursementNotFoundException extends RuntimeException {
    public ReimbursementNotFoundException(int id) {
        super("No reimbursement with id " + id);
    }
}

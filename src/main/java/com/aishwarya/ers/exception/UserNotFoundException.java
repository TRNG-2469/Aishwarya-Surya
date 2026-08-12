package com.aishwarya.ers.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(int id) {
        super("No user with id " + id);
    }
}

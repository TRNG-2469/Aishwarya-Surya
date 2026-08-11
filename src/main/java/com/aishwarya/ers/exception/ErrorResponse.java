package com.aishwarya.ers.exception;

public class ErrorResponse {

    private String error;
    private final long timestamp;

    public ErrorResponse(String error) {
        this.error = error;
        this.timestamp = System.currentTimeMillis();
    }

    public String getError() {
        return error;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
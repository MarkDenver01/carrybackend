package com.carry_guide.carry_guide_admin.presentation.handler;

public abstract class ApiException extends RuntimeException {
    private final int statusCode;

    protected ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

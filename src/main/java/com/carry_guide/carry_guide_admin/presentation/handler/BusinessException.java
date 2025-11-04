package com.carry_guide.carry_guide_admin.presentation.handler;

/**
 * Custom exception for business logic errors.
 * Use this for domain-specific rules or validation failures.
 */
public class BusinessException extends ApiException {

    public BusinessException(String message) {
        super(message, 409); // Conflict
    }

    public BusinessException(String message, int statusCode) {
        super(message, statusCode);
    }
}

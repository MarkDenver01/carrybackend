package com.carry_guide.carry_guide_admin.presentation.handler;

/**
 * Thrown when an input or domain validation rule fails.
 * Example: email already exists, invalid product size, etc.
 */
/**
 * Represents validation errors (e.g., invalid input, duplicate data).
 */
public class ValidationException extends ApiException {

    public ValidationException(String message) {
        super(message, 400); // Bad Request
    }

    public ValidationException(String message, int statusCode) {
        super(message, statusCode);
    }
}
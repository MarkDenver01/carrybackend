package com.carry_guide.carry_guide_admin.presentation.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Provides standard API response helpers for all controllers.
 * Extend this class to automatically gain consistent JSON response format.
 */
public abstract class BaseController {

    protected <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    protected <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    protected ResponseEntity<ApiResponse<?>> created(String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.message(message, HttpStatus.CREATED.value()));
    }

    protected ResponseEntity<ApiResponse<?>> deleted(String message) {
        return ResponseEntity.ok(ApiResponse.message(message, HttpStatus.OK.value()));
    }

    protected ResponseEntity<ApiResponse<?>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.message(message, HttpStatus.BAD_REQUEST.value()));
    }

    protected ResponseEntity<ApiResponse<?>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.message(message, HttpStatus.NOT_FOUND.value()));
    }

    protected ResponseEntity<ApiResponse<?>> internalServerError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.message(message, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}

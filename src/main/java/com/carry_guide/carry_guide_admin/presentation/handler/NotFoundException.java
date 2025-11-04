package com.carry_guide.carry_guide_admin.presentation.handler;

public class NotFoundException extends ApiException{

    public NotFoundException(String message) {
        super(message, 404);
    }

    public NotFoundException(String message, int statusCode) {
        super(message, statusCode);
    }}

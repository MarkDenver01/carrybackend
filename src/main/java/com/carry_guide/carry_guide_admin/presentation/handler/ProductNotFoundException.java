package com.carry_guide.carry_guide_admin.presentation.handler;

public class ProductNotFoundException  extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Product not found with ID: " + id);
    }
}

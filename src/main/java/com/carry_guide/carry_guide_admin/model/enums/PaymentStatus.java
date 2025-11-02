package com.carry_guide.carry_guide_admin.model.enums;

public enum PaymentStatus {
    PENDING,       // payment created but not yet confirmed
    INITIATED,     // request sent to provider
    PAID,          // provider confirmed payment
    FAILED,        // provider returned failure
    REFUNDED,      // fully refunded
    PARTIALLY_REFUNDED,
    CANCELLED
}

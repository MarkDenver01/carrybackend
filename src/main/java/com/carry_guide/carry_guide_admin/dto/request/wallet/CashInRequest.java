package com.carry_guide.carry_guide_admin.dto.request.wallet;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashInRequest {
    private String mobileNumber; // unique key
    private Long amount;       // amount in PHP (ex: 100 for ₱100)
    private String email;      // optional but recommended for invoice
}

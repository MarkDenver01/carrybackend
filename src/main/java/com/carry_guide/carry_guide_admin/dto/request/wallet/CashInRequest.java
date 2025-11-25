package com.carry_guide.carry_guide_admin.dto.request.wallet;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashInRequest {
    private Long userId;       // ID ng user sa system mo
    private Long amount;       // amount in PHP (ex: 100 for ₱100)
    private String email;      // optional but recommended for invoice
}

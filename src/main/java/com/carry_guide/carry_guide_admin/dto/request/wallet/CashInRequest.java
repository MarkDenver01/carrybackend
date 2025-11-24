package com.carry_guide.carry_guide_admin.dto.request.wallet;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashInRequest {
    private BigDecimal amount;
    private String email;        // or
    private String mobileNumber; // either one should be fille
}

package com.carry_guide.carry_guide_admin.dto.request.wallet;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateWalletRequest {
    private String mobileNumber;
    private BigDecimal amount;   // amount to add or deduct
    private boolean isDeduct;    // true = deduct, false = add
}

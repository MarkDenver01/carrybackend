package com.carry_guide.carry_guide_admin.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletResponse {
    private BigDecimal balance;

    public WalletResponse(BigDecimal balance) {
        this.balance = balance;
    }
}

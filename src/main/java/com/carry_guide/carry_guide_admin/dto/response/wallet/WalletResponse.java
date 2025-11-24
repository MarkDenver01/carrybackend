package com.carry_guide.carry_guide_admin.dto.response.wallet;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WalletResponse {
    private BigDecimal balance;
}

package com.carry_guide.carry_guide_admin.dto.response.wallet;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WalletResponse {
    private Long userId;
    private Long balance;
}

package com.carry_guide.carry_guide_admin.dto.response.wallet;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CashInInitResponse {
    private String checkoutUrl;   // URL to open in app (GCash/Xendit)
    private String referenceId;   // internal reference for matching webhook
}

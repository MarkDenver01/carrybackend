package com.carry_guide.carry_guide_admin.dto.request.wallet;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class XenditEwalletWebhookPayload {
    private String id;
    private String reference_id;
    private String status;
    private BigDecimal charge_amount;
}

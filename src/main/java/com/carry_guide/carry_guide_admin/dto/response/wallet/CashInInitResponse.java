package com.carry_guide.carry_guide_admin.dto.response.wallet;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CashInInitResponse {
    private String invoiceId;
    private String externalId;
    private String invoiceUrl;
    private String status;
}

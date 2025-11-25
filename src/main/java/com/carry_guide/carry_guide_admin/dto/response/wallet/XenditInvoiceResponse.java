package com.carry_guide.carry_guide_admin.dto.response.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class XenditInvoiceResponse {
    private String id;

    @JsonProperty("external_id")
    private String externalId;

    private Long amount;

    private String status;

    @JsonProperty("invoice_url")
    private String invoiceUrl;

    private String currency;
}
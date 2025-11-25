package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.response.wallet.XenditInvoiceResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;

@Service
@RequiredArgsConstructor
public class XenditService {

    private final RestTemplate restTemplate;

    @Value("${xendit.secretkey}")
    private String secretKey;

    @Value("${xendit.baseurl}")
    private String baseUrl;

    @Value("${xendit.success.redirect-url}")
    private String successRedirectUrl;

    @Value("${xendit.failed.redirect-url}")
    private String failedRedirectUrl;

    public XenditInvoiceResponse createGcashInvoice(String externalId, Long amount, String payerEmail) {
        String url = baseUrl + "/v2/invoices";

        Map<String, Object> body = new HashMap<>();
        body.put("external_id", externalId);
        body.put("amount", amount);
        body.put("currency", "PHP");
        body.put("description", "Wallet Cash-in via GCash");
        body.put("payment_methods", List.of("GCASH")); // IMPORTANT
        body.put("success_redirect_url", successRedirectUrl);
        body.put("failure_redirect_url", failedRedirectUrl);

        if (payerEmail != null && !payerEmail.isBlank()) {
            body.put("payer_email", payerEmail);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(secretKey, ""); // Xendit uses basic auth with secret key

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<XenditInvoiceResponse> response = restTemplate
                .exchange(url, HttpMethod.POST, entity, XenditInvoiceResponse.class);

        return response.getBody();
    }
}

package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.response.wallet.CashInInitResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class XenditService {
    private static final Logger log = LoggerFactory.getLogger(XenditService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${xendit.secretkey}")
    private String xenditSecretKey;

    @Value("${xendit.success-redirecturl}")
    private String successUrl;

    @Value("${xendit.failed-redirecturl}")
    private String failureUrl;

    public CashInInitResponse createGcashCharge(BigDecimal amount, String customerId) {
        try {
            String referenceId = "CASHIN-" + customerId + "-" + UUID.randomUUID();

            String url = "https://api.xendit.co/ewallets/charges";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(xenditSecretKey, ""); // secret key as username

            Map<String, Object> body = new HashMap<>();
            body.put("reference_id", referenceId);
            body.put("currency", "PHP");
            body.put("amount", amount);

            body.put("checkout_method", "ONE_TIME_PAYMENT");
            body.put("channel_code", "PH_GCASH");

            Map<String, Object> channelProps = new HashMap<>();
            channelProps.put("success_redirect_url", successUrl);
            channelProps.put("failure_redirect_url", failureUrl);
            body.put("channel_properties", channelProps);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Xendit ewallet charge failed: {}", response);
                throw new RuntimeException("Xendit charge failed");
            }

            JsonNode json = objectMapper.readTree(response.getBody());

            // You may need to adjust depending on actual Xendit response field
            String checkoutUrl = json.path("actions")
                    .path("desktop_web_checkout_url")
                    .asText(null);

            if (checkoutUrl == null || checkoutUrl.isEmpty()) {
                // fallback
                checkoutUrl = json.path("checkout_url").asText("");
            }

            return new CashInInitResponse(checkoutUrl, referenceId);

        } catch (Exception e) {
            log.error("Error creating Xendit GCash charge", e);
            throw new RuntimeException("Unable to create Xendit charge", e);
        }
    }
}

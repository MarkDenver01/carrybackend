package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/xendit")
public class XenditWebhookController {


    @Value("${xendit.callback-token}")
    private String callbackToken;

    private final ObjectMapper mapper = new ObjectMapper();
    private final WalletService walletService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) {
        try {
            StringBuilder rawBody = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;

            while ((line = reader.readLine()) != null) {
                rawBody.append(line);
            }

            String token = request.getHeader("x-callback-token");
            if (token == null || !callbackToken.equals(token)) {
                return ResponseEntity.status(401).body("Invalid callback token");
            }

            var json = mapper.readTree(rawBody.toString());

            System.out.println("📩 XENDIT WEBHOOK RECEIVED:");
            System.out.println(rawBody);

            String status = json.get("status").asText();          // PAID / EXPIRED / etc.
            String externalId = json.get("external_id").asText();
            Long paidAmount = json.has("paid_amount") && !json.get("paid_amount").isNull()
                    ? json.get("paid_amount").asLong()
                    : json.get("amount").asLong();

            walletService.handleInvoiceWebhook(status, externalId, paidAmount);

            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("WEBHOOK ERROR");
        }
    }
}

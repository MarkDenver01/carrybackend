package com.carry_guide.carry_guide_admin.presentation.controller;

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
@RequestMapping("/api/xendit")
public class XenditWebhookController {

    @Value("${xendit.callback-token}")
    private String callbackToken;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/webhook")
    public ResponseEntity<String> handleXenditInvoiceWebhook(HttpServletRequest request) {
        try {
            // Read body
            StringBuilder rawBody = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                rawBody.append(line);
            }

            // Validate callback token
            String token = request.getHeader("x-callback-token");
            if (token == null || !token.equals(callbackToken)) {
                return ResponseEntity.status(401).body("Invalid Xendit callback token");
            }

            // Parse JSON
            var json = mapper.readTree(rawBody.toString());
            System.out.println("📩 XENDIT WEBHOOK RECEIVED:");
            System.out.println(rawBody);

            // Extract fields
            String status = json.get("status").asText();
            String externalId = json.get("external_id").asText();
            int amount = json.get("paid_amount").asInt();

            // Handle statuses
            switch (status) {
                case "PAID":
                    System.out.println("💰 Invoice PAID: " + externalId + " amount: " + amount);
                    // TODO: update wallet / order
                    break;

                case "EXPIRED":
                    System.out.println("⏳ Invoice EXPIRED: " + externalId);
                    // TODO: mark expired
                    break;

                case "PAID_AFTER_EXPIRY":
                    System.out.println("⚠ Paid after expiry: " + externalId);
                    // TODO: handle late payment
                    break;

                default:
                    System.out.println("⚠ Unhandled invoice status: " + status);
            }

            return ResponseEntity.ok("Webhook received");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("WEBHOOK ERROR");
        }
    }
}

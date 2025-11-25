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
@RequiredArgsConstructor
@RequestMapping("/api/xendit")
public class XenditWebhookController {
    @Value("${xendit.callback-token}")
    private String callbackToken;

    private final ObjectMapper mapper = new ObjectMapper();

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
            if (!callbackToken.equals(token)) {
                return ResponseEntity.status(401).body("Invalid callback token");
            }

            var json = mapper.readTree(rawBody.toString());
            String event = json.get("event").asText();

            System.out.println("🔔 XENDIT WEBHOOK EVENT: " + event);
            System.out.println("RAW: " + rawBody);

            if ("ewallet.completed".equals(event)) {
                String referenceId = json.get("data").get("reference_id").asText();
                int amount = json.get("data").get("charge").get("amount").asInt();

                // TODO: credit to wallet
                System.out.println("CREDIT WALLET: " + referenceId + " amount: " + amount);
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("WEBHOOK ERROR");
        }
    }

}

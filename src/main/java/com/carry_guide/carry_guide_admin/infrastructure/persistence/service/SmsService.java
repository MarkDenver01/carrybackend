package com.carry_guide.carry_guide_admin.infrastructure.persistence.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class SmsService {
    @Value("${infobip.api.base-url}")
    private String baseUrl;

    @Value("${infobip.api.key}")
    private String apiKey;

    @Value("${infobip.api.sender}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendOtp(String mobileNumber, String otp) throws Exception {
        String text = "Your verification code is " + otp + ". It expires in 5 minutes.";

        Map<String, Object> payload = Map.of(
                "messages", List.of(
                        Map.of(
                                "from", senderName,
                                "destinations", List.of(Map.of("to", mobileNumber)),
                                "text", text
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "App " + apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        String url = baseUrl + "/sms/2/text/advanced";

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Failed to send OTP: " + response.getStatusCode() + " " + response.getBody());
        }
    }
}

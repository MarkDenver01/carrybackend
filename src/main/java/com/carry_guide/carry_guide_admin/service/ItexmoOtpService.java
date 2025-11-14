package com.carry_guide.carry_guide_admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ItexmoOtpService {
    @Value("${itexmo.email}")
    private String email;

    @Value("${itexmo.password}")
    private String password;

    @Value("${itexmo.api}")
    private String apiCode;

    @Value("${itexmo.otp.url}")
    private String otpUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendOtp(String mobileNumber, String otp) {

        String formatted = formatNumber(mobileNumber);

        // Construct Basic Auth Header
        String auth = Base64.getEncoder().encodeToString(
                (email + ":" + password).getBytes()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + auth);

        // JSON Body
        Map<String, Object> body = new HashMap<>();
        body.put("Email", email); // optional if using Basic Auth
        body.put("Password", password); // optional if using Basic Auth
        body.put("ApiCode", apiCode);
        body.put("Recipients", Collections.singletonList(formatted));
        body.put("Message", "Your WrapAndCarry OTP is " + otp + ".");
        body.put("SenderId", "ITEXMO SMS"); // optional, works if DIRECT plan

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            String response = restTemplate.postForObject(otpUrl, request, String.class);
            System.out.println("iTexMo OTP Response: " + response);
            return response;
        } catch (Exception ex) {
            System.out.println("iTexMo OTP Error: " + ex.getMessage());
            throw new RuntimeException("Failed to send OTP via iTexMo", ex);
        }
    }

    private String formatNumber(String number) {
        number = number.replaceAll("[^0-9]", "");

        if (number.startsWith("0")) return "63" + number.substring(1);
        if (number.startsWith("9")) return "63" + number;
        if (number.startsWith("63")) return number;

        throw new IllegalArgumentException("Invalid PH number: " + number);
    }
}

package com.carry_guide.carry_guide_admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SemaphoreSmsService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${semaphore.api.key}")
    private String apiKey;

    public void sendOtp(String mobileNumber, String otp) {
        String url = "https://api.semaphore.co/api/v4/messages";
        Map<String, String> params = new HashMap<>();
        params.put("apikey", apiKey);
        params.put("number", mobileNumber);
        params.put("message", "Your WrapAndCarry OTP is: " + otp);

        restTemplate.postForObject(url, params, String.class);
    }
}

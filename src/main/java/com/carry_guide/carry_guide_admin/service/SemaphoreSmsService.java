package com.carry_guide.carry_guide_admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class SemaphoreSmsService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${semaphore.api.key}")
    private String apiKey;

    public void sendOtp(String mobileNumber, String otp) {

        String formatted = formatTo63(mobileNumber);

        String url = "https://api.semaphore.co/api/v4/otp";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("apikey", apiKey);
        body.add("number", formatted);
        body.add("message", "Your WrapAndCarry OTP is {otp}. Please use within 5 minutes.");
        body.add("code", otp); // IMPORTANT!
        body.add("sendername", "WrapNCarry");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        restTemplate.postForObject(url, request, String.class);
    }

    private String formatTo63(String number) {
        number = number.replaceAll("[^0-9]", "");

        if (number.startsWith("0")) {
            return "63" + number.substring(1);
        }
        if (number.startsWith("9")) {
            return "63" + number;
        }
        if (number.startsWith("63")) {
            return number;
        }

        throw new IllegalArgumentException("Invalid mobile number: " + number);
    }
}

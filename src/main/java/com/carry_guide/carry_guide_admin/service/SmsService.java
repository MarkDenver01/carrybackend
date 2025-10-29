package com.carry_guide.carry_guide_admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class SmsService {

    @Value("${mocean.apiKey}")
    private String apiKey;

    @Value("${mocean.fromNumber}")
    private String fromNumber;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://rest.moceanapi.com")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();

    public Mono<String> sendOtp(String toNumber, String otp) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/2/sms")
                        .queryParam("mocean-from", fromNumber)
                        .queryParam("mocean-to", toNumber)
                        .queryParam("mocean-text", "Your OTP is: " + otp)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }


}

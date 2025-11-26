package com.carry_guide.carry_guide_admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatGPTService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate;

    public ChatGPTService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<String> getRecommendedKeywords(String userHistoryKeywords) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        String prompt = String.format("""
            You are an intelligent recommendation AI.
            The user has recently interacted with these products: [%s].
            Detect if any are Tagalog or in another language, translate them to English,
            and suggest up to 10 related product types or categories. 
            Return only a comma-separated list of product keywords.
        """, userHistoryKeywords);

        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, request, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("ChatGPT API returned status: {}", response.getStatusCode());
                return List.of();
            }

            Map<String, Object> result = response.getBody();
            if (result == null || !result.containsKey("choices")) {
                log.warn("Empty or invalid ChatGPT API response");
                return List.of();
            }

            String content = (String) ((Map) ((Map) ((List) result.get("choices")).get(0)).get("message")).get("content");

            return Arrays.stream(content.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .limit(10)
                    .collect(Collectors.toList());

        } catch (HttpClientErrorException e) {
            log.error("OpenAI Error Response: {}", e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("Unexpected error communicating with OpenAI: {}", e.getMessage());
            return List.of();
        }
    }
}

package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public List<Long> rankProductsByHistory(
            List<UserHistory> history,
            List<Product> candidates
    ) {
        try {
            // --- 1) Build compact payload for GPT ---
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("history", history.stream().map(h -> Map.of(
                    "productKeyword", h.getProductKeyword(),
                    "dateTime", h.getDateTime().toString()
            )).toList());

            payload.put("products", candidates.stream().map(p -> Map.of(
                    "productId", p.getProductId(),
                    "productName", p.getProductName(),
                    "category", p.getCategory() != null ? p.getCategory().getCategoryName() : "",
                    "description", p.getProductDescription()
            )).toList());

            String jsonPayload = objectMapper.writeValueAsString(payload);

            String prompt = """
                    You are an AI product recommendation ranking engine.

                    You will receive a JSON object containing:
                    - "history": list of user interactions, each with "productKeyword" and "dateTime"
                    - "products": list of products, each with "productId", "productName", "category", "description"

                    Task:
                    1. Understand what the user is interested in based on the history.
                    2. Rank the products from MOST relevant to LEAST relevant for this user.
                    3. Favor:
                       - Products whose names or descriptions closely match recent history keywords.
                       - More recent history should have more weight.
                    4. Always include all given products in the ranking.

                    Return ONLY a JSON object in this exact format:
                    {
                      "recommendations": [1, 5, 3, 2]
                    }

                    JSON to analyze:
                    """ + jsonPayload;

            // --- 2) Call OpenAI / ChatGPT here ---
            // TODO: Palitan mo ito ng actual call mo sa ChatGPT (existing OpenAI integration mo)
            String rawResponse = callOpenAiApi(prompt); // <--- ikaw na mag-wire dito

            // --- 3) Parse GPT response ---
            Map<?, ?> parsed = objectMapper.readValue(rawResponse, Map.class);
            Object recObj = parsed.get("recommendations");
            if (recObj instanceof List<?> list) {
                List<Long> result = new ArrayList<>();
                for (Object o : list) {
                    if (o instanceof Number n) {
                        result.add(n.longValue());
                    } else if (o instanceof String s) {
                        try {
                            result.add(Long.parseLong(s));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                return result;
            }

        } catch (Exception e) {
            // fallback sa baba
            e.printStackTrace();
        }

        // ⚠️ Fallback: walang AI / may error → no ranking
        return Collections.emptyList();
    }

    private String callOpenAiApi(String prompt) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.4
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    OPENAI_URL,
                    request,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("ChatGPT ranking API failed: {}", response.getStatusCode());
                return "{\"recommendations\":[]}";
            }

            Map<String, Object> result = response.getBody();

            if (result == null
                    || !result.containsKey("choices")) {
                log.error("Invalid ChatGPT ranking response");
                return "{\"recommendations\":[]}";
            }

            // Extract message content
            String content = (String) ((Map)
                    ((Map)
                            ((List) result.get("choices")).get(0)
                    ).get("message")
            ).get("content");

            content = content.trim();

            // If GPT replies with any text before the JSON, find the JSON part only
            if (!content.startsWith("{")) {
                int start = content.indexOf("{");
                int end = content.lastIndexOf("}");
                if (start != -1 && end != -1) {
                    content = content.substring(start, end + 1);
                }
            }

            log.info("GPT RANK RESPONSE = {}", content);

            return content; // <-- return JSON string

        } catch (Exception e) {
            log.error("Error calling ChatGPT ranking: {}", e.getMessage());
            return "{\"recommendations\":[]}";
        }
    }
}

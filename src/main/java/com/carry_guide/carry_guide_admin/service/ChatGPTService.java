package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class ChatGPTService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatGPTService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 🔹 HIGH-LEVEL keyword expansion (history → related categories)
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
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(OPENAI_URL, request, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("ChatGPT API returned status: {}", response.getStatusCode());
                return List.of();
            }

            Map<String, Object> result = response.getBody();
            if (result == null || !result.containsKey("choices")) {
                log.warn("Empty or invalid ChatGPT API response");
                return List.of();
            }

            String content = (String) ((Map)
                    ((Map) ((List) result.get("choices")).get(0))
                            .get("message"))
                    .get("content");

            return Arrays.stream(content.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .limit(10)
                    .toList();

        } catch (HttpClientErrorException e) {
            log.error("OpenAI Error Response: {}", e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("Unexpected error communicating with OpenAI: {}", e.getMessage());
            return List.of();
        }
    }

    // 🔹 RANK products based on user history (personalized list)
    public List<Long> rankProductsByHistory(
            List<UserHistory> history,
            List<Product> candidates
    ) {
        try {
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

            String rawResponse = callOpenAiApi(prompt);

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
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                return result;
            }

        } catch (Exception e) {
            log.error("Error parsing rankProductsByHistory response", e);
        }

        return Collections.emptyList();
    }

    // 🔹 CROSS-SELL / RELATED PRODUCTS
    public List<Long> suggestRelatedProducts(Product main, List<Product> candidates) {

        try {
            Map<String, Object> payload = new LinkedHashMap<>();

            payload.put("mainProduct", Map.of(
                    "productId", main.getProductId(),
                    "productName", main.getProductName(),
                    "category", main.getCategory() != null ? main.getCategory().getCategoryName() : "",
                    "description", main.getProductDescription()
            ));

            payload.put("candidates", candidates.stream().map(p -> Map.of(
                    "productId", p.getProductId(),
                    "productName", p.getProductName(),
                    "category", p.getCategory() != null ? p.getCategory().getCategoryName() : "",
                    "description", p.getProductDescription()
            )).toList());

            String jsonPayload = objectMapper.writeValueAsString(payload);

            String prompt = """
                You are an AI recommendation engine for grocery and retail items.

                You will receive:
                - mainProduct: The product the user is currently viewing or buying
                - candidates: List of other products in the store

                Task:
                1. Suggest items that are commonly bought together with the main product.
                2. Think like a Filipino shopper.
                3. Return the ranking (most relevant first).

                Example:
                If mainProduct = Bread → suggest Cheese Whiz, Coffee, Juice, Peanut Butter.

                IMPORTANT:
                Return ONLY a JSON object like this:
                {
                   "suggestions": [12, 34, 55]
                }

                Analyze this JSON:
                """ + jsonPayload;

            String raw = callOpenAiApi(prompt);

            Map<?, ?> parsed = objectMapper.readValue(raw, Map.class);
            Object obj = parsed.get("suggestions");

            if (obj instanceof List<?> list) {
                List<Long> result = new ArrayList<>();
                for (Object o : list) {
                    if (o instanceof Number n) {
                        result.add(n.longValue());
                    } else if (o instanceof String s) {
                        try {
                            result.add(Long.parseLong(s));
                        } catch (Exception ignored) {
                        }
                    }
                }
                return result;
            }

        } catch (Exception e) {
            log.error("AI related-products failed: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    // 🔧 Low-level OpenAI caller (shared)
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

            if (result == null || !result.containsKey("choices")) {
                log.error("Invalid ChatGPT ranking response");
                return "{\"recommendations\":[]}";
            }

            String content = (String) ((Map)
                    ((Map) ((List) result.get("choices")).get(0))
                            .get("message"))
                    .get("content");

            // Clean up markdown formatting if present
            content = content.trim()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            if (!content.startsWith("{")) {
                int start = content.indexOf("{");
                int end = content.lastIndexOf("}");
                if (start != -1 && end != -1) {
                    content = content.substring(start, end + 1);
                }
            }

            log.info("GPT RANK RESPONSE = {}", content);

            return content;

        } catch (Exception e) {
            log.error("Error calling ChatGPT ranking: {}", e.getMessage());
            return "{\"recommendations\":[]}";
        }
    }
}

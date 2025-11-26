package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class ChatGPTService {

    @Value("${openai.api.key}")
    private String openAiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/responses";
    private static final String MODEL = "gpt-4.1-mini";

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatGPTService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /* ============================================================
       🔹 UNIVERSAL CALLER (NEW API FORMAT)
       ============================================================ */
    private String callOpenAi(String prompt) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiKey);

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "input", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", 0.4
        );

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    OPENAI_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("❌ OpenAI error status: {}", response.getStatusCode());
                return null;
            }

            Map<String, Object> result = response.getBody();
            if (result == null) {
                log.error("❌ OpenAI returned empty body");
                return null;
            }

            List<String> texts = (List<String>) result.get("output_text");

            if (texts == null || texts.isEmpty()) {
                log.error("❌ OpenAI response missing output_text: {}", result);
                return null;
            }

            String raw = texts.get(0).trim();

            raw = raw.replace("```json", "")
                    .replace("```", "")
                    .trim();

            return raw;

        } catch (Exception e) {
            log.error("❌ OpenAI CRASH: {}", e.getMessage());
            return null;
        }
    }

    /* ============================================================
       🔹 1. EXPAND KEYWORDS
       ============================================================ */
    public List<String> getRecommendedKeywords(String history) {

        String prompt = """
            You are an AI category generator for a grocery store.

            User interacted with: [%s].

            Translate Tagalog terms → English.
            Generate up to 10 related product categories.

            Return ONLY comma-separated list.
        """.formatted(history);

        String out = callOpenAi(prompt);
        if (out == null) return List.of();

        return Arrays.stream(out.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .limit(10)
                .toList();
    }

    /* ============================================================
       🔹 2. RANK PRODUCTS BASED ON HISTORY
       ============================================================ */
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

            String prompt = """
                You are an AI product ranking engine.

                Receive:
                - history
                - products

                Determine user preference.
                Rank ALL products most relevant → least relevant.

                Return ONLY JSON:
                { "recommendations": [1,2,3] }

                Data:
                %s
            """.formatted(mapper.writeValueAsString(payload));

            String raw = callOpenAi(prompt);
            if (raw == null) return List.of();

            Map<?, ?> parsed = mapper.readValue(raw, Map.class);
            Object arr = parsed.get("recommendations");

            return extractLongList(arr);

        } catch (Exception e) {
            log.error("❌ Rank error: {}", e.getMessage());
            return List.of();
        }
    }

    /* ============================================================
       🔹 3. SUGGEST RELATED PRODUCTS
       ============================================================ */
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

            String prompt = """
                You are an AI cross-sell specialist.

                Suggest products commonly bought together.

                Return ONLY:
                { "suggestions": [11,12,55] }

                Data:
                %s
            """.formatted(mapper.writeValueAsString(payload));

            String raw = callOpenAi(prompt);
            if (raw == null) return List.of();

            Map<?, ?> parsed = mapper.readValue(raw, Map.class);
            Object arr = parsed.get("suggestions");

            return extractLongList(arr);

        } catch (Exception e) {
            log.error("❌ related-products error: {}", e.getMessage());
            return List.of();
        }
    }

    /* ============================================================
       🔧 Extract array → List<Long>
       ============================================================ */
    private List<Long> extractLongList(Object obj) {
        if (!(obj instanceof List<?> list)) return List.of();

        List<Long> out = new ArrayList<>();
        for (Object o : list) {
            try {
                if (o instanceof Number n) out.add(n.longValue());
                if (o instanceof String s) out.add(Long.parseLong(s));
            } catch (Exception ignored) {}
        }
        return out;
    }
}

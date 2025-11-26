package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import com.fasterxml.jackson.databind.JsonNode;
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

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4.1-mini";

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatGPTService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /* ============================================================
       🔹 UNIVERSAL OPENAI CALL — FIXED FOR RESPONSES API
       ============================================================ */
    private String callOpenAi(String prompt) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("input", prompt);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = mapper.readTree(response.getBody());

            // IMPORTANT: Responses API uses "output" array
            JsonNode output = root.get("output");

            if (output == null || !output.isArray() || output.size() == 0) {
                log.error("❌ Missing output array in OpenAI response");
                return "";
            }

            JsonNode first = output.get(0);

            // MUST BE: type=output_text, text=<generated text>
            if (!first.has("text")) {
                log.error("❌ Missing output_text");
                return "";
            }

            return first.get("text").asText();

        } catch (Exception e) {
            log.error("❌ OpenAI Error: {}", e.getMessage());
            return "";
        }
    }

    /* ============================================================
       🔹 1. EXPAND KEYWORDS
       ============================================================ */
    public List<String> getRecommendedKeywords(String history) {

        String prompt = """
            You are an AI category generator for a grocery store.

            User interacted with: [%s].

            Translate Tagalog terms to English.
            Generate up to 10 related product categories.

            Return ONLY a comma-separated list.
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
       🔹 2. RANK PRODUCTS
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

                Rank ALL products by relevance to user preference.

                Return ONLY JSON:
                { "recommendations": [1,2,3] }

                Data:
                %s
            """.formatted(mapper.writeValueAsString(payload));

            String raw = callOpenAi(prompt);
            if (raw == null) return List.of();

            Map<?, ?> parsed = mapper.readValue(raw, Map.class);
            return extractLongList(parsed.get("recommendations"));

        } catch (Exception e) {
            log.error("❌ ranking error: {}", e.getMessage());
            return List.of();
        }
    }

    /* ============================================================
       🔹 3. RELATED PRODUCTS
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

                Suggest products often bought together.

                Return ONLY:
                { "suggestions": [11,12,55] }

                Data:
                %s
            """.formatted(mapper.writeValueAsString(payload));

            String raw = callOpenAi(prompt);
            if (raw == null) return List.of();

            Map<?, ?> parsed = mapper.readValue(raw, Map.class);
            return extractLongList(parsed.get("suggestions"));

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

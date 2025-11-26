package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ChatGPTService {

    @Value("${openai.api.key}")
    private String openAiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4.1-mini";

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    // 🔹 Simple in-memory caches (per app instance)
    private final Map<String, List<String>> keywordCache = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> rankingCache = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> relatedCache = new ConcurrentHashMap<>();

    public ChatGPTService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /* ============================================================
       🔹 LOW-LEVEL CALLER with RETRY + LOGGING
       ============================================================ */
    private String callOpenAi(String prompt) {
        return callOpenAi(prompt, 0.4);
    }

    private String callOpenAi(String prompt, double temperature) {

        int attempts = 0;

        while (attempts < 3) {
            attempts++;

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(openAiKey);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", MODEL);
                requestBody.put("messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ));
                requestBody.put("temperature", temperature);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        OPENAI_URL,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                String body = response.getBody();
                log.info("RAW OPENAI: {}", body);

                return extractContent(body);

            } catch (HttpStatusCodeException e) {

                // Explicit HTTP status (e.g. 429)
                HttpStatus status = (HttpStatus) e.getStatusCode();
                String body = e.getResponseBodyAsString();

                log.error("❌ OpenAI HTTP error {}: {}", status.value(), body);

                if (status == HttpStatus.TOO_MANY_REQUESTS && attempts < 3) {
                    log.warn("⚠ Rate limit hit (429). Retrying attempt {} ...", attempts);
                    try {
                        Thread.sleep(1500L * attempts);
                    } catch (InterruptedException ignored) {
                    }
                    continue;
                }

                // Other HTTP errors → no retry
                return "";

            } catch (Exception e) {
                log.error("❌ OpenAI Error on attempt {}: {}", attempts, e.getMessage());

                if (attempts >= 3) {
                    return "";
                }

                // small backoff
                try {
                    Thread.sleep(1000L * attempts);
                } catch (InterruptedException ignored) {
                }
            }
        }

        log.error("❌ OpenAI failed after 3 attempts.");
        return "";
    }

    /* ============================================================
       🔹 Extract choices[0].message.content
       ============================================================ */
    private String extractContent(String json) {
        if (json == null || json.isBlank()) return "";

        try {
            JsonNode root = mapper.readTree(json);
            JsonNode content = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content");

            if (content.isMissingNode()) {
                log.error("❌ Missing 'content' in OpenAI response.");
                return "";
            }

            return content.asText();
        } catch (Exception e) {
            log.error("❌ JSON parse error: {}", e.getMessage());
            return "";
        }
    }

    /* ============================================================
       🔹 1. EXPAND KEYWORDS (PRODUCT NAMES → SEARCH TERMS)
       ============================================================ */
    public List<String> getRecommendedKeywords(String history) {

        if (history == null || history.isBlank()) return List.of();

        String cacheKey = history.trim().toLowerCase();
        if (keywordCache.containsKey(cacheKey)) {
            return keywordCache.get(cacheKey);
        }

        String prompt = """
                    You are an AI product similarity engine for a grocery store.
                
                    The user previously interacted with these product names:
                    [%s]
                
                    TASK:
                    - Identify the meaning of these product names.
                    - Generate up to 10 related product or search terms.
                    - Terms should be similar product names or common grocery items.
                    - Keep each term short (1–3 words max).
                    - Do NOT return full sentences.
                    - Return ONLY a comma-separated list.
                """.formatted(history);


        String out = callOpenAi(prompt, 0.3);
        if (out == null || out.isBlank()) return List.of();

        List<String> list = Arrays.stream(out.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(10)
                .toList();

        keywordCache.put(cacheKey, list);
        return list;
    }

    /* ============================================================
       🔹 2. RANK PRODUCTS BY USER HISTORY
       ============================================================ */
    public List<Long> rankProductsByHistory(
            List<UserHistory> history,
            List<Product> candidates
    ) {
        if (candidates == null || candidates.isEmpty()) return List.of();

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("history", history.stream().map(h -> Map.of(
                    "productKeyword", h.getProductKeyword(),
                    "dateTime", h.getDateTime() != null ? h.getDateTime().toString() : ""
            )).toList());

            payload.put("products", candidates.stream().map(p -> Map.of(
                    "productId", p.getProductId(),
                    "productName", p.getProductName()
            )).toList());

            String jsonPayload = mapper.writeValueAsString(payload);
            String cacheKey = "rank:" + Integer.toHexString(jsonPayload.hashCode());

            if (rankingCache.containsKey(cacheKey)) {
                return rankingCache.get(cacheKey);
            }

            String prompt = """
                        You are an AI ranking engine for a grocery store.
                    
                        RANK ALL candidate products by analyzing ONLY their product names.
                        Compare:
                        - similarity with the user's past product keywords
                        - typical co-purchased items
                        - natural product relationships
                    
                        DO NOT infer product description (there is none).
                        DO NOT add explanation.
                        DO NOT add extra fields.
                    
                        Return ONLY valid JSON:
                        { "recommendations": [1,2,3] }
                    
                        Data:
                        %s
                    """.formatted(jsonPayload);

            String raw = callOpenAi(prompt, 0.4);
            if (raw == null || raw.isBlank()) return List.of();

            String jsonOnly = extractJsonObject(raw);

            Map<?, ?> parsed = mapper.readValue(jsonOnly, Map.class);
            List<Long> ids = extractLongList(parsed.get("recommendations"));

            rankingCache.put(cacheKey, ids);
            return ids;

        } catch (Exception e) {
            log.error("❌ ranking error: {}", e.getMessage());
            return List.of();
        }
    }

    /* ============================================================
       🔹 3. RELATED PRODUCTS (CROSS-SELL)
       ============================================================ */
    public List<Long> suggestRelatedProducts(Product main, List<Product> candidates) {

        if (main == null || candidates == null || candidates.isEmpty()) return List.of();

        try {
            Map<String, Object> payload = new LinkedHashMap<>();

            payload.put("mainProduct", Map.of(
                    "productId", main.getProductId(),
                    "productName", main.getProductName()
            ));

            payload.put("candidates", candidates.stream().map(p -> Map.of(
                    "productId", p.getProductId(),
                    "productName", p.getProductName()
            )).toList());

            String jsonPayload = mapper.writeValueAsString(payload);
            String cacheKey = "rel:" + Integer.toHexString(jsonPayload.hashCode());

            if (relatedCache.containsKey(cacheKey)) {
                return relatedCache.get(cacheKey);
            }

            String prompt = """
                        You are an AI cross-sell recommendation engine.
                    
                        Given:
                        - One main product name
                        - A list of other product names
                    
                        TASK:
                        - Suggest products that consumers typically buy together
                          with the main product.
                        - Use ONLY productName similarities and typical grocery behavior.
                    
                        Return ONLY JSON:
                        { "suggestions": [11, 12, 55] }
                    
                        Data:
                        %s
                    """.formatted(jsonPayload);

            String raw = callOpenAi(prompt, 0.4);
            if (raw == null || raw.isBlank()) return List.of();

            String jsonOnly = extractJsonObject(raw);

            Map<?, ?> parsed = mapper.readValue(jsonOnly, Map.class);
            List<Long> ids = extractLongList(parsed.get("suggestions"));

            relatedCache.put(cacheKey, ids);
            return ids;

        } catch (Exception e) {
            log.error("❌ related-products error: {}", e.getMessage());
            return List.of();
        }
    }

    /* ============================================================
       🔧 Helper: Extract JSON object from raw text
       ============================================================ */
    private String extractJsonObject(String raw) {
        if (raw == null) return "";
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }

    /* ============================================================
       🔧 Helper: convert list -> List<Long>
       ============================================================ */
    private List<Long> extractLongList(Object obj) {
        if (!(obj instanceof List<?> list)) return List.of();

        List<Long> out = new ArrayList<>();
        for (Object o : list) {
            try {
                if (o instanceof Number n) {
                    out.add(n.longValue());
                } else if (o instanceof String s) {
                    out.add(Long.parseLong(s.trim()));
                }
            } catch (Exception ignored) {
            }
        }
        return out;
    }
}

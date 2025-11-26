package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import com.carry_guide.carry_guide_admin.repository.JpaUserHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final JpaUserHistoryRepository userHistoryRepository;
    private final JpaProductRepository productRepository;
    private final ChatGPTService chatGPTService;

    public List<Product> getRecommendationsForUser(Long customerId) {

        // 1️⃣ Get history (latest first)
        List<UserHistory> history =
                userHistoryRepository.findByCustomerIdOrderByDateTimeDesc(customerId);

        if (history.isEmpty()) {
            // Walang history → default
            return productRepository.findTop20ByOrderByProductIdDesc();
        }

        // 2️⃣ Build candidate product list based **only** on history keywords
        //    Para sure na kung ano nasa history, yun din core ng recommendation.
        Map<Long, Product> candidateMap = new LinkedHashMap<>();

        for (UserHistory h : history) {
            String keyword = h.getProductKeyword();

            // Match by product name
            List<Product> byName =
                    productRepository.findByProductNameContainingIgnoreCase(keyword);
            byName.forEach(p -> candidateMap.putIfAbsent(p.getProductId(), p));

            // Optional: match by category name
            List<Product> byCategory =
                    productRepository.findByCategory_CategoryNameContainingIgnoreCase(keyword);
            byCategory.forEach(p -> candidateMap.putIfAbsent(p.getProductId(), p));
        }

        List<Product> candidates = new ArrayList<>(candidateMap.values());

        if (candidates.isEmpty()) {
            // Wala pa ring match → default
            return productRepository.findTop20ByOrderByProductIdDesc();
        }

        // 3️⃣ Let ChatGPT rank candidates
        List<Long> rankedIds = chatGPTService.rankProductsByHistory(history, candidates);

        if (rankedIds == null || rankedIds.isEmpty()) {
            // AI failed / no ranking → fallback to original candidate order
            return candidates;
        }

        // 4️⃣ Build ordered list based on AI ranking
        Map<Long, Product> byId = new HashMap<>();
        for (Product p : candidates) {
            byId.put(p.getProductId(), p);
        }

        List<Product> ordered = new ArrayList<>();

        // 4.1 Add products in the exact AI-ranked order
        for (Long id : rankedIds) {
            Product p = byId.remove(id);
            if (p != null) {
                ordered.add(p);
            }
        }

        // 4.2 Kung may natira pang candidates na wala sa list ni GPT → append sa dulo
        ordered.addAll(byId.values());

        return ordered;
    }
}

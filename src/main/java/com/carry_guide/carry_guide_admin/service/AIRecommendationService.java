package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import com.carry_guide.carry_guide_admin.repository.JpaUserHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final JpaUserHistoryRepository historyRepo;
    private final JpaProductRepository productRepo;
    private final ChatGPTService gpt;

    /**
     * Main recommendation for homepage / user dashboard
     */
    public List<Product> getRecommendationsForUser(Long customerId) {

        List<UserHistory> history = historyRepo.findByCustomerIdOrderByDateTimeDesc(customerId);
        List<Product> all = productRepo.findAllActiveProducts(); // only Available

        if (all.isEmpty()) return List.of();

        // 🔹 Walang history → newest products lang
        if (history.isEmpty()) {
            return all.stream()
                    .sorted(Comparator.comparing(Product::getProductInDate).reversed())
                    .limit(12)
                    .toList();
        }

        // 🔹 Build history keywords string for GPT
        String historyKeywords = String.join(", ",
                history.stream().map(UserHistory::getProductKeyword).toList()
        );

        // 🔹 Step 1: Expand keywords via GPT
        List<String> expandedKeywords = gpt.getRecommendedKeywords(historyKeywords);
        log.info("Expanded keywords from GPT: {}", expandedKeywords);

        // 🔹 Step 2: Local filter ng candidates using keywords
        List<Product> candidates = all.stream()
                .filter(p -> matches(p, expandedKeywords))
                .toList();

        if (candidates.isEmpty()) {
            log.info("No candidate match by keywords. Falling back to ALL active.");
            candidates = all;
        }

        // 🔹 Step 3: Rank via GPT → returns recommended product IDs
        List<Long> rankedIds = gpt.rankProductsByHistory(history, candidates);
        log.info("GPT ranked product IDs: {}", rankedIds);

        // 🔥 IMPORTANT: dito natin ginagamit yung "recommendations" galing kay GPT
        if (!rankedIds.isEmpty()) {

            // Kunin lang products na kasama sa GPT IDs
            List<Product> recommended = productRepo.findByProductIds(rankedIds);

            // Sort by GPT order
            List<Product> sorted = sortByRanking(recommended, rankedIds);

            // Limit to 12 for UI
            return sorted.stream().limit(12).toList();
        }

        // 🔹 Fallback: kahit walang GPT ranking, gamitin pa rin local candidates
        return candidates.stream().limit(12).toList();
    }

    /**
     * Related products for Product Detail screen
     */
    public List<Product> getRelatedProducts(Product main) {

        List<Product> all = productRepo.findAllActiveProducts();

        // Huwag isama yung current product
        all = all.stream()
                .filter(p -> !p.getProductId().equals(main.getProductId()))
                .toList();

        if (all.isEmpty()) return List.of();

        // 🔹 GPT suggestions by product ID
        List<Long> ranked = gpt.suggestRelatedProducts(main, all);
        log.info("GPT related product IDs: {}", ranked);

        if (!ranked.isEmpty()) {
            List<Product> recommended = productRepo.findByProductIds(ranked);
            List<Product> sorted = sortByRanking(recommended, ranked);
            return sorted.stream().limit(10).toList();
        }

        // 🔹 Fallback by same category
        return all.stream()
                .filter(p -> p.getCategory() != null && main.getCategory() != null &&
                        p.getCategory().getCategoryId().equals(main.getCategory().getCategoryId()))
                .limit(10)
                .toList();
    }

    // ============================================================
    // 🔧 Helpers
    // ============================================================

    private boolean matches(Product p, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return true;

        String target = (p.getProductName() + " " + p.getProductDescription()).toLowerCase();

        for (String k : keywords) {
            if (k == null || k.isBlank()) continue;
            if (target.contains(k.toLowerCase())) return true;
        }
        return false;
    }

    /**
     * Sorts `src` products according to order of `rankingIds`.
     * Extra products (not in rankingIds) will be appended at the end.
     */
    private List<Product> sortByRanking(List<Product> src, List<Long> rankingIds) {
        Map<Long, Product> map = new HashMap<>();
        src.forEach(p -> map.put(p.getProductId(), p));

        List<Product> sorted = new ArrayList<>();

        // 1) Unahin yung galing sa GPT order
        for (Long id : rankingIds) {
            Product p = map.get(id);
            if (p != null && !sorted.contains(p)) {
                sorted.add(p);
            }
        }

        // 2) Append lahat ng hindi kasama sa GPT list
        for (Product p : src) {
            if (!rankingIds.contains(p.getProductId())) {
                sorted.add(p);
            }
        }

        return sorted;
    }
}

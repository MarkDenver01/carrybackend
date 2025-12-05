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
        List<Product> all = productRepo.findAllActiveProducts();  // Only Available products

        // =============================
        // 🔹 CASE 1: No history → newest
        // =============================
        if (history.isEmpty()) {
            return all.stream()
                    .sorted(Comparator.comparing(Product::getProductInDate).reversed())
                    .limit(12)
                    .toList();
        }

        // =============================
        // 🔥 STEP 1 — HISTORY FIRST LOGIC
        // =============================

        List<Product> historyBased = new ArrayList<>();

        for (UserHistory h : history) {
            String keyword = h.getProductKeyword();

            // Find matches for this exact historical keyword
            List<Product> matches = productRepo.searchLoose(keyword);

            for (Product p : matches) {
                if (!historyBased.contains(p)) { // Avoid duplicates
                    historyBased.add(p);
                }
            }

            if (historyBased.size() >= 12) break;
        }

        // If enough products already, return
        if (historyBased.size() >= 12) {
            return historyBased.subList(0, 12);
        }

        // ============================================
        // 🔥 STEP 2 — GPT-BASED FILLER RECOMMENDATIONS
        // ============================================

        // Build string of all user history keywords
        String historyKeywords = String.join(", ",
                history.stream().map(UserHistory::getProductKeyword).toList()
        );

        // GPT expands keywords
        List<String> expanded = gpt.getRecommendedKeywords(historyKeywords);

        // Find candidates from GPT expanded keywords
        List<Product> gptCandidates = all.stream()
                .filter(p -> matches(p, expanded))
                .toList();

        if (gptCandidates.isEmpty()) {
            gptCandidates = all; // fallback
        }

        // GPT ranking by history
        List<Long> rankedIds = gpt.rankProductsByHistory(history, gptCandidates);

        // Fetch products by GPT IDs
        List<Product> ranked = productRepo.findByProductIds(rankedIds);
        List<Product> rankedSorted = sortByRanking(ranked, rankedIds);

        // Remove items already recommended
        rankedSorted = rankedSorted.stream()
                .filter(p -> !historyBased.contains(p))
                .toList();

        // =============================
        // 🔥 FINAL COMBINATION
        // =============================

        List<Product> finalList = new ArrayList<>(historyBased);

        for (Product p : rankedSorted) {
            if (finalList.size() >= 12) break;
            finalList.add(p);
        }

        return finalList;
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

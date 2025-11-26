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

    public List<Product> getRecommendationsForUser(Long customerId) {

        List<UserHistory> history = historyRepo.findByCustomerIdOrderByDateTimeDesc(customerId);
        List<Product> all = productRepo.findAllActiveProducts();

        if (all.isEmpty()) return List.of();

        if (history.isEmpty()) {
            return all.stream()
                    .sorted(Comparator.comparing(Product::getProductInDate).reversed())
                    .limit(12)
                    .toList();
        }

        String historyKeywords = String.join(", ",
                history.stream().map(UserHistory::getProductKeyword).toList()
        );

        List<String> expandedKeywords = gpt.getRecommendedKeywords(historyKeywords);

        List<Product> candidates = all.stream()
                .filter(p -> matches(p, expandedKeywords))
                .toList();

        if (candidates.isEmpty()) candidates = all;

        List<Long> rankedIds = gpt.rankProductsByHistory(history, candidates);

        if (!rankedIds.isEmpty()) {
            return sortByRanking(candidates, rankedIds);
        }

        return candidates.stream().limit(12).toList();
    }

    public List<Product> getRelatedProducts(Product main) {

        List<Product> all = productRepo.findAllActiveProducts();

        all = all.stream()
                .filter(p -> !p.getProductId().equals(main.getProductId()))
                .toList();

        List<Long> ranked = gpt.suggestRelatedProducts(main, all);

        if (!ranked.isEmpty()) {
            return sortByRanking(all, ranked).stream().limit(10).toList();
        }

        return all.stream()
                .filter(p -> p.getCategory() != null && main.getCategory() != null &&
                        p.getCategory().getCategoryId().equals(main.getCategory().getCategoryId()))
                .limit(10)
                .toList();
    }

    private boolean matches(Product p, List<String> keywords) {
        String target = (p.getProductName() + " " + p.getProductDescription()).toLowerCase();

        for (String k : keywords) {
            if (target.contains(k.toLowerCase())) return true;
        }
        return false;
    }

    private List<Product> sortByRanking(List<Product> src, List<Long> rankingIds) {
        Map<Long, Product> map = new HashMap<>();
        src.forEach(p -> map.put(p.getProductId(), p));

        List<Product> sorted = new ArrayList<>();

        for (Long id : rankingIds) {
            if (map.containsKey(id)) sorted.add(map.get(id));
        }

        for (Product p : src) {
            if (!rankingIds.contains(p.getProductId())) sorted.add(p);
        }

        return sorted;
    }
}

package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.ProductMapper;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductPriceDTO;
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

    // 🔥 PERSONALIZED RECOMMENDATIONS (home screen / order list)
    public List<Product> getRecommendationsForUser(Long customerId) {

        // 1️⃣ Get history (latest first)
        List<UserHistory> history =
                userHistoryRepository.findByCustomerIdOrderByDateTimeDesc(customerId);

        if (history.isEmpty()) {
            return productRepository.findTop20ByOrderByProductIdDesc();
        }

        // 2️⃣ Candidate products based ONLY on history keywords
        Map<Long, Product> candidateMap = new LinkedHashMap<>();

        for (UserHistory h : history) {
            String keyword = h.getProductKeyword();

            List<Product> byName =
                    productRepository.findByProductNameContainingIgnoreCase(keyword);
            byName.forEach(p -> candidateMap.putIfAbsent(p.getProductId(), p));

            List<Product> byCategory =
                    productRepository.findByCategory_CategoryNameContainingIgnoreCase(keyword);
            byCategory.forEach(p -> candidateMap.putIfAbsent(p.getProductId(), p));
        }

        List<Product> candidates = new ArrayList<>(candidateMap.values());

        if (candidates.isEmpty()) {
            return productRepository.findTop20ByOrderByProductIdDesc();
        }

        // 3️⃣ Rank via ChatGPT
        List<Long> rankedIds = chatGPTService.rankProductsByHistory(history, candidates);

        if (rankedIds == null || rankedIds.isEmpty()) {
            return candidates;
        }

        Map<Long, Product> byId = new HashMap<>();
        for (Product p : candidates) {
            byId.put(p.getProductId(), p);
        }

        List<Product> ordered = new ArrayList<>();

        // 4️⃣ Apply AI ranking
        for (Long id : rankedIds) {
            Product p = byId.remove(id);
            if (p != null) {
                ordered.add(p);
            }
        }

        // 5️⃣ Append anything not ranked (fallback)
        ordered.addAll(byId.values());

        return ordered;
    }

    // 🔥 RELATED PRODUCTS (Product Detail → “Frequently Bought Together”)
    public List<ProductPriceDTO> getRelatedProducts(Long productId) {

        Product main = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        List<Product> candidates = productRepository.findByProductStatus("Available")
                .stream()
                .filter(p -> !Objects.equals(p.getProductId(), productId))
                .toList();

        if (candidates.isEmpty()) return List.of();

        List<Long> aiIds = chatGPTService.suggestRelatedProducts(main, candidates);

        Map<Long, Product> map = candidates.stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        List<Product> ordered = new ArrayList<>();

        for (Long id : aiIds) {
            Product p = map.remove(id);
            if (p != null) ordered.add(p);
        }

        // If GPT did not cover all products, append them at the end
        ordered.addAll(map.values());

        return ordered.stream()
                .map(ProductMapper::toDto)
                .limit(10)
                .toList();
    }
}

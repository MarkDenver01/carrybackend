package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.request.product.RecommendationRuleRequest;
import com.carry_guide.carry_guide_admin.dto.response.product.RecommendationRuleDTO;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.RecommendationRule;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import com.carry_guide.carry_guide_admin.repository.JpaRecommendationRuleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationRuleService {

    private final JpaRecommendationRuleRepository recommendationRuleRepository;
    private final JpaProductRepository productRepository;

    public RecommendationRuleDTO createRule(RecommendationRuleRequest req) {
        // 1️⃣ Fetch main product
        Product mainProduct = productRepository.findById(req.baseProductId())
                .orElseThrow(() -> new RuntimeException("Main product not found"));

        // 2️⃣ Fetch recommended products
        List<Product> recommended = productRepository.findAllById(req.recommendedProductIds());
        if (recommended.isEmpty()) {
            throw new RuntimeException("At least one recommended product must be provided");
        }

        // 3️⃣ Prevent self-assignment
        if (recommended.stream().anyMatch(p -> p.getProductId().equals(req.baseProductId()))) {
            throw new RuntimeException("Main product cannot recommend itself");
        }

        // 4️⃣ Prevent duplicate assignment
        List<RecommendationRule> existingRules = recommendationRuleRepository.findByProduct_ProductId(req.baseProductId());
        for (RecommendationRule existing : existingRules) {
            for (Product recProduct : existing.getRecommendedProducts()) {
                if (req.recommendedProductIds().contains(recProduct.getProductId())) {
                    throw new RuntimeException("Product '" + recProduct.getProductName()
                            + "' is already assigned as a recommendation for '"
                            + mainProduct.getProductName() + "'.");
                }
            }
        }

        // 5️⃣ Create and save rule
        RecommendationRule rule = new RecommendationRule();
        rule.setProduct(mainProduct);
        rule.setRecommendedProducts(recommended);
        rule.setEffectiveDate(req.effectiveDate());
        rule.setExpiryDate(req.expiryDate());
        rule.setActive(true);

        RecommendationRule saved = recommendationRuleRepository.save(rule);
        return toDTO(saved);
    }

    public List<Product> getRecommendationsForProduct(Long productId) {
        LocalDate now = LocalDate.now();

        return recommendationRuleRepository.findByProduct_ProductId(productId)
                .stream()
                .filter(r ->
                        r.isActive() &&
                                !now.isBefore(r.getEffectiveDate()) &&
                                (r.getExpiryDate() == null || !now.isAfter(r.getExpiryDate()))
                )
                .flatMap(r -> r.getRecommendedProducts().stream())
                .distinct()
                .toList();
    }

    public List<RecommendationRuleDTO> getAllRules() {
        return recommendationRuleRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public void deleteRule(Long id) {
        recommendationRuleRepository.deleteById(id);
    }

    private RecommendationRuleDTO toDTO(RecommendationRule r) {
        return new RecommendationRuleDTO(
                r.getId(),
                r.getProduct().getProductId(),
                r.getProduct().getProductName(),
                r.getProduct().getCategory() != null ? r.getProduct().getCategory().getCategoryName() : null,
                r.getRecommendedProducts().stream().map(Product::getProductName).toList(),
                r.getEffectiveDate(),
                r.getExpiryDate(),
                r.isActive()
        );
    }
}

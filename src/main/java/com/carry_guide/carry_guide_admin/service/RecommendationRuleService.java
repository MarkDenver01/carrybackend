package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.request.product.RecommendationRuleRequest;
import com.carry_guide.carry_guide_admin.dto.response.product.RecommendationRuleDTO;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.RecommendationRule;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import com.carry_guide.carry_guide_admin.repository.JpaRecommendationRuleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationRuleService {

    @Autowired
    JpaRecommendationRuleRepository recommendationRuleRepository;

    @Autowired
    JpaProductRepository productRepository;

    public RecommendationRuleDTO createRule(RecommendationRuleRequest req) {

        Product mainProduct = productRepository.findById(req.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<Product> recommended = productRepository.findAllById(req.recommendedProductIds());

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
                                !now.isAfter(r.getExpiryDate())
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
                r.getProduct().getCategory().getCategoryName(),
                r.getRecommendedProducts().stream().map(Product::getProductName).toList(),
                r.getEffectiveDate(),
                r.getExpiryDate(),
                r.isActive()
        );
    }
}

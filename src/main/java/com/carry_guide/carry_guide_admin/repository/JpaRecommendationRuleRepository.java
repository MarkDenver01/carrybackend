package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.RecommendationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaRecommendationRuleRepository extends JpaRepository<RecommendationRule, Long> {
    List<RecommendationRule> findByProduct_ProductId(Long productId);

    List<RecommendationRule> findByActiveTrue();

    List<RecommendationRule> findByProduct_Category_CategoryId(Long categoryId);
}

package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.ProductRecommended;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaProductRecommendedRepository extends JpaRepository<ProductRecommended, Long> {
    List<ProductRecommended> findByProduct_ProductId(Long productId);
}

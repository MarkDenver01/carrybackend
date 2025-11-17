package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaProductPriceRepository extends JpaRepository<ProductPrice, Long> {
    List<ProductPrice> findByProduct_ProductId(Long productProductId);
}

package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaProductPriceRepository extends JpaRepository<ProductPrice, Long> {
    List<ProductPrice> findByProduct_ProductId(Long productProductId);

    @Query("""
        SELECT pp FROM ProductPrice pp
        WHERE pp.product.productStatus = 'Available'
    """)
    List<ProductPrice> findAllAvailable();
}

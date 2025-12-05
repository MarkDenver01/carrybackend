package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.dto.response.analytics.CategorySalesDTO;
import com.carry_guide.carry_guide_admin.dto.response.analytics.ProductSalesDTO;
import com.carry_guide.carry_guide_admin.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaSalesAnalyticsRepository extends JpaRepository<OrderItem, Long> {

    // 🔹 TOTAL SALES PER CATEGORY
    @Query("""
    SELECT new com.carry_guide.carry_guide_admin.dto.response.analytics.CategorySalesDTO(
        c.categoryName,
        SUM(oi.lineTotal)
    )
    FROM OrderItem oi
    JOIN oi.product p
    JOIN p.category c
    JOIN oi.order ord
    WHERE ord.status = com.carry_guide.carry_guide_admin.domain.enums.OrderStatus.DELIVERED
      AND ord.createdAt BETWEEN :start AND :end
    GROUP BY c.categoryName
    ORDER BY SUM(oi.lineTotal) DESC
""")
    List<CategorySalesDTO> getCategorySales(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    // 🔹 TOP SELLING PRODUCTS
    @Query("""
    SELECT new com.carry_guide.carry_guide_admin.dto.response.analytics.ProductSalesDTO(
        p.productName,
        SUM(oi.lineTotal)
    )
    FROM OrderItem oi
    JOIN oi.product p
    JOIN oi.order ord
    WHERE ord.status = com.carry_guide.carry_guide_admin.domain.enums.OrderStatus.DELIVERED
      AND ord.createdAt BETWEEN :start AND :end
    GROUP BY p.productName
    ORDER BY SUM(oi.lineTotal) DESC
""")
    List<ProductSalesDTO> getProductSales(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
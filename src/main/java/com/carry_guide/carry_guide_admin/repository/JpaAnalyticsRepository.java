package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.Order;
import com.carry_guide.carry_guide_admin.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface JpaAnalyticsRepository extends JpaRepository<Order, Long> {

    // ⭐ TOTAL SALES (DELIVERED ONLY)
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.status = com.carry_guide.carry_guide_admin.domain.enums.OrderStatus.DELIVERED
    """)
    BigDecimal getTotalSales();


    // ⭐ DAILY SALES (LAST 7 DAYS)
    @Query("""
        SELECT FUNCTION('DATE_TRUNC','day', o.createdAt),
               COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.status = com.carry_guide.carry_guide_admin.domain.enums.OrderStatus.DELIVERED
        GROUP BY FUNCTION('DATE_TRUNC','day', o.createdAt)
        ORDER BY FUNCTION('DATE_TRUNC','day', o.createdAt)
    """)
    List<Object[]> getDailySales();


    // ⭐ MONTHLY SALES (LAST 6 MONTHS)
    @Query("""
        SELECT FUNCTION('DATE_TRUNC','month', o.createdAt),
               COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.status = com.carry_guide.carry_guide_admin.domain.enums.OrderStatus.DELIVERED
        GROUP BY FUNCTION('DATE_TRUNC','month', o.createdAt)
        ORDER BY FUNCTION('DATE_TRUNC','month', o.createdAt)
    """)
    List<Object[]> getMonthlySales();


    // ⭐ YEARLY SALES (LAST 3 YEARS)
    @Query("""
        SELECT FUNCTION('DATE_TRUNC','year', o.createdAt),
               COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.status = com.carry_guide.carry_guide_admin.domain.enums.OrderStatus.DELIVERED
        GROUP BY FUNCTION('DATE_TRUNC','year', o.createdAt)
        ORDER BY FUNCTION('DATE_TRUNC','year', o.createdAt)
    """)
    List<Object[]> getYearlySales();

}

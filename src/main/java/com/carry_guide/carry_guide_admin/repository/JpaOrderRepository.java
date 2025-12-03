package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.domain.enums.OrderStatus;
import com.carry_guide.carry_guide_admin.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.status = 'DELIVERED'
    """)
    BigDecimal getTotalSales();

    // ⭐ TOTAL ORDERS (lahat ng nasa orders table)
    long count();

    // ⭐ OPTIONAL: Count by specific status (pending, delivered, cancelled)
    long countByStatus(OrderStatus status);
    @Query("""
        SELECT o.customer.customerId, COUNT(o)
        FROM Order o
        GROUP BY o.customer.customerId
    """)
    List<Object[]> countOrdersPerCustomer();

    // Active customers per month
    @Query("""
        SELECT FUNCTION('DATE_TRUNC','month', o.createdAt) AS month,
               COUNT(DISTINCT o.customer.customerId)
        FROM Order o
        WHERE o.createdAt IS NOT NULL
        GROUP BY FUNCTION('DATE_TRUNC','month', o.createdAt)
        ORDER BY FUNCTION('DATE_TRUNC','month', o.createdAt)
    """)
    List<Object[]> countActiveCustomersPerMonth();
}


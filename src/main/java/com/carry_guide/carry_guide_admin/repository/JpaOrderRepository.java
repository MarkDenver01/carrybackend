package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.domain.enums.OrderStatus;
import com.carry_guide.carry_guide_admin.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

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
}

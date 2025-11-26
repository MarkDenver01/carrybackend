package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.domain.enums.OrderStatus;
import com.carry_guide.carry_guide_admin.model.entity.Order;
import com.carry_guide.carry_guide_admin.repository.JpaCustomerRepository;
import com.carry_guide.carry_guide_admin.repository.JpaOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardAnalyticsService {

    private final JpaOrderRepository orderRepository;
    private final JpaCustomerRepository customerRepository;

    public BigDecimal getTotalSales() {
        List<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.CONFIRMED);
        return orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getTotalOrders() {
        return (long) orderRepository
                .findByStatusOrderByCreatedAtDesc(OrderStatus.CONFIRMED)
                .size();
    }

    public Long getTotalCustomers() {
        return customerRepository.count();
    }
}

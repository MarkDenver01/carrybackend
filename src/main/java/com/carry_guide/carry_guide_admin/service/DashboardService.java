package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.repository.JpaOrderRepository;
import com.carry_guide.carry_guide_admin.repository.JpaCustomerRepository;
import com.carry_guide.carry_guide_admin.repository.JpaRiderRepository;
import com.carry_guide.carry_guide_admin.domain.enums.RiderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JpaOrderRepository orderRepository;
    private final JpaCustomerRepository customerRepository;
    private final JpaRiderRepository riderRepository;

    public long getTotalOrders() {
        return orderRepository.count();
    }

    public BigDecimal getTotalSales() {
        return orderRepository.getTotalSales();
    }

    public long getTotalCustomers() {
        return customerRepository.count();
    }

    public long getAvailableRiders() {
        return riderRepository.countByStatus(RiderStatus.AVAILABLE);
    }
    }

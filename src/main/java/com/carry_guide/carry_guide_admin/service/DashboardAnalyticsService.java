package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.domain.enums.OrderStatus;
import com.carry_guide.carry_guide_admin.dto.dashboard.CustomerGrowthPointDTO;
import com.carry_guide.carry_guide_admin.dto.dashboard.ReturningVsNewDTO;
import com.carry_guide.carry_guide_admin.dto.dashboard.CustomerAnalyticsDTO;
import com.carry_guide.carry_guide_admin.model.entity.Order;
import com.carry_guide.carry_guide_admin.repository.JpaCustomerRepository;
import com.carry_guide.carry_guide_admin.repository.JpaOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
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
    public CustomerAnalyticsDTO getCustomerAnalytics() {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Manila"));
        LocalDate today = now.toLocalDate();

        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfYear = today.withDayOfYear(1).atStartOfDay();

        LocalDateTime sevenDaysAgo = startOfToday.minusDays(6);

        long last7Days = customerRepository.countByCreatedDateBetween(sevenDaysAgo, startOfToday);
        long thisMonth = customerRepository.countByCreatedDateBetween(startOfMonth, startOfToday);
        long thisYear = customerRepository.countByCreatedDateBetween(startOfYear, startOfToday);
        long total = customerRepository.count();

        return new CustomerAnalyticsDTO(
                last7Days,
                thisMonth,
                thisYear,
                total
        );
    }

    // ======================
    // RETURNING VS NEW CUSTOMERS
    // ======================

    public ReturningVsNewDTO getReturningVsNewCustomers() {

        var rows = orderRepository.countOrdersPerCustomer();

        long newCustomers = 0;
        long returningCustomers = 0;

        for (Object[] row : rows) {
            long orderCount = ((Number) row[1]).longValue();

            if (orderCount <= 1) newCustomers++;
            else returningCustomers++;
        }

        long total = customerRepository.count();

        return new ReturningVsNewDTO(newCustomers, returningCustomers, total);
    }

    // ======================
    // CUSTOMER GROWTH GRAPH
    // ======================

    public List<CustomerGrowthPointDTO> getCustomerGrowth() {

        var registrations = customerRepository.countRegistrationsPerMonth();
        var active = orderRepository.countActiveCustomersPerMonth();

        Map<YearMonth, Long> regMap = new LinkedHashMap<>();
        Map<YearMonth, Long> activeMap = new HashMap<>();

        for (Object[] row : registrations) {
            LocalDateTime dt = (LocalDateTime) row[0];
            YearMonth ym = YearMonth.from(dt.toLocalDate());
            regMap.put(ym, ((Number) row[1]).longValue());
        }

        for (Object[] row : active) {
            LocalDateTime dt = (LocalDateTime) row[0];
            YearMonth ym = YearMonth.from(dt.toLocalDate());
            activeMap.put(ym, ((Number) row[1]).longValue());
        }

        List<CustomerGrowthPointDTO> result = new ArrayList<>();

        for (Map.Entry<YearMonth, Long> e : regMap.entrySet()) {

            YearMonth ym = e.getKey();
            long reg = e.getValue();
            long act = activeMap.getOrDefault(ym, 0L);

            String label = ym.getMonth().name().substring(0, 3) + " " + ym.getYear();

            result.add(new CustomerGrowthPointDTO(label, reg, act));
        }

        return result;
    }
}

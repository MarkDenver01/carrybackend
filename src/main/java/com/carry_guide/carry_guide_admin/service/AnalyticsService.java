package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.domain.enums.OrderStatus;
import com.carry_guide.carry_guide_admin.repository.JpaAnalyticsRepository;
import com.carry_guide.carry_guide_admin.repository.JpaCustomerRepository;
import com.carry_guide.carry_guide_admin.repository.JpaOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final JpaAnalyticsRepository analyticsRepo;
    private final JpaOrderRepository orderRepo;
    private final JpaCustomerRepository customerRepo;

    // ⭐ SUMMARY CARD DATA
    public Map<String, Object> getSummary() {
        Map<String, Object> map = new HashMap<>();

        map.put("totalSales", analyticsRepo.getTotalSales());
        map.put("totalOrders", orderRepo.count());
        map.put("totalCustomers", customerRepo.count());

        map.put("deliveredOrders", orderRepo.countByStatus(OrderStatus.DELIVERED));
        map.put("pendingOrders", orderRepo.countByStatus(OrderStatus.PENDING));
        map.put("cancelledOrders", orderRepo.countByStatus(OrderStatus.CANCELLED));

        return map;
    }

    // ⭐ GRAPH DATA FORMATTING
    private List<Map<String, Object>> convert(List<Object[]> raw, String pattern) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String, Object> m = new HashMap<>();
            m.put("label", fmt.format((java.time.LocalDateTime) row[0]));
            m.put("totalSales", row[1]);
            list.add(m);
        }
        return list;
    }

    public List<Map<String, Object>> getDailySales() {
        return convert(analyticsRepo.getDailySales(), "MMM dd");
    }

    public List<Map<String, Object>> getMonthlySales() {
        return convert(analyticsRepo.getMonthlySales(), "MMM");
    }

    public List<Map<String, Object>> getYearlySales() {
        return convert(analyticsRepo.getYearlySales(), "yyyy");
    }
}

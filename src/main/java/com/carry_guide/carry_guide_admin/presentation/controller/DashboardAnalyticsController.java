package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.service.DashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/dashboard")
public class DashboardAnalyticsController {

    private final DashboardAnalyticsService dashboardService;

    @GetMapping("/total-sales")
    public ResponseEntity<Map<String, BigDecimal>> getTotalSales() {
        return ResponseEntity.ok(Map.of("totalSales", dashboardService.getTotalSales()));
    }

    @GetMapping("/total-orders")
    public ResponseEntity<Map<String, Long>> getTotalOrders() {
        return ResponseEntity.ok(Map.of("totalOrders", dashboardService.getTotalOrders()));
    }

    @GetMapping("/total-customers")
    public ResponseEntity<Map<String, Long>> getTotalCustomers() {
        return ResponseEntity.ok(Map.of("totalCustomers", dashboardService.getTotalCustomers()));
    }
}

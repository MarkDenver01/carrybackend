package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ⭐ SUMMARY (TOTAL SALES, CUSTOMERS, ORDERS)
    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        return analyticsService.getSummary();
    }

    // ⭐ SINGLE GRAPH (DAILY, MONTHLY, YEARLY)
    @GetMapping("/sales")
    public List<Map<String, Object>> getSales(@RequestParam String range) {
        return switch (range.toLowerCase()) {
            case "date" -> analyticsService.getDailySales();
            case "month" -> analyticsService.getMonthlySales();
            case "year" -> analyticsService.getYearlySales();
            default -> throw new IllegalArgumentException("Invalid range!");
        };
    }
}
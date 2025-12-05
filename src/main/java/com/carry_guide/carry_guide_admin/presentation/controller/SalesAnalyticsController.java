package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.response.analytics.SalesAnalyticsResponse;
import com.carry_guide.carry_guide_admin.service.SalesAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/analytics")
@RequiredArgsConstructor
public class SalesAnalyticsController {

    private final SalesAnalyticsService service;

    @GetMapping("/sales")
    public SalesAnalyticsResponse getSalesAnalytics(@RequestParam String range) {
        return service.getSalesReport(range);
    }
}
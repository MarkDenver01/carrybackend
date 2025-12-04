package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/admin/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/total-orders")
    public long getTotalOrders() {
        return dashboardService.getTotalOrders();
    }

    @GetMapping("/total-sales")
    public BigDecimal getTotalSales() {
        return dashboardService.getTotalSales();
    }

    @GetMapping("/total-customers")
    public long getTotalCustomers() {
        return dashboardService.getTotalCustomers();
    }

    @GetMapping("/available-riders")
    public long getAvailableRiders() {
        return dashboardService.getAvailableRiders();
    }
}
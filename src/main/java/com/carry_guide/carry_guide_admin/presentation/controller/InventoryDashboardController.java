package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.response.analytics.InventoryMetricsResponseDTO;
import com.carry_guide.carry_guide_admin.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard/inventory")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InventoryDashboardController {

    private final InventoryService inventoryService;

    @GetMapping("/metrics")
    public InventoryMetricsResponseDTO getInventoryMetrics() {
        return inventoryService.getMetrics();
    }
}
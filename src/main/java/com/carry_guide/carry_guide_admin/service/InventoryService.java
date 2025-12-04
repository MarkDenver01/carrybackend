package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.response.analytics.InventoryMetricsResponseDTO;

public interface InventoryService {
    InventoryMetricsResponseDTO getMetrics();
}
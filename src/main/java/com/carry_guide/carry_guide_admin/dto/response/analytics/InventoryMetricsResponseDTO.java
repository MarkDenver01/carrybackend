package com.carry_guide.carry_guide_admin.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryMetricsResponseDTO {
    private long lowStockItems;
    private long outOfStockItems;
    private long expiringSoonItems;
}
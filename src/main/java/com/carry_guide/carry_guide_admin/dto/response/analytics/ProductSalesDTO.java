package com.carry_guide.carry_guide_admin.dto.response.analytics;

public record ProductSalesDTO(
        String productName,
        java.math.BigDecimal totalSales
) {}

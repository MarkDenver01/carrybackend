package com.carry_guide.carry_guide_admin.dto.response.analytics;


public record CategorySalesDTO(
        String categoryName,
        java.math.BigDecimal totalSales
) {}

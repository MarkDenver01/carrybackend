package com.carry_guide.carry_guide_admin.dto.response.analytics;

import java.util.List;

public record SalesAnalyticsResponse(
        String range,
        List<CategorySalesDTO> categorySales,
        List<ProductSalesDTO> productSales
) {}
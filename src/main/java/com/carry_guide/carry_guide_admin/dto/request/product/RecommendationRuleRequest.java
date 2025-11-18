package com.carry_guide.carry_guide_admin.dto.request.product;

import java.time.LocalDate;
import java.util.List;

public record RecommendationRuleRequest(
        Long productId,
        List<Long> recommendedProductIds,
        LocalDate effectiveDate,
        LocalDate expiryDate
) {}

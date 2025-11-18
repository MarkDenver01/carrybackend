package com.carry_guide.carry_guide_admin.dto.response.product;

import java.time.LocalDate;
import java.util.List;

public record RecommendationRuleDTO(
        Long id,
        Long productId,
        String productName,
        String categoryName,
        List<String> recommendedNames,
        LocalDate effectiveDate,
        LocalDate expiryDate,
        boolean active
) {}
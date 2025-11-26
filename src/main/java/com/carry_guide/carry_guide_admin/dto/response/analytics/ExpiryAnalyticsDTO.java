package com.carry_guide.carry_guide_admin.dto.response.analytics;

public record ExpiryAnalyticsDTO(
        long freshItems,
        long moderateItems,
        long nearExpiryItems,
        long expiringOrExpiredItems
) {}

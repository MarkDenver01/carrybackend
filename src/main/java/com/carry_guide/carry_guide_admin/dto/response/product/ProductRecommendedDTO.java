package com.carry_guide.carry_guide_admin.dto.response.product;

import java.time.LocalDateTime;

public record ProductRecommendedDTO(
        Long productRecommendedId,
        String productCode,
        String productName,
        String productDescription,
        String productSize,
        LocalDateTime expiryDate,
        LocalDateTime createdDate,
        String productImgUrl
) {
}

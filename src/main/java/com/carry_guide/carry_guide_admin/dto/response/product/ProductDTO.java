package com.carry_guide.carry_guide_admin.dto.response.product;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProductDTO(
        Long productId,
        String productCode,
        String productName,
        String productDescription,
        int stocks,
        String productSize,
        String productStatus,
        String productImgUrl,
        LocalDateTime expiryDate,
        LocalDateTime productInDate,
        Long categoryId,
        String categoryName
) {
}

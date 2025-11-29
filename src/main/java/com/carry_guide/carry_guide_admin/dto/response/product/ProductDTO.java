package com.carry_guide.carry_guide_admin.dto.response.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ProductDTO(
        Long productId,
        String productCode,
        String productName,
        String productDescription,
        int stocks,
        String productSize,
        String productStatus,
        String productImgUrl,

        // 👉 RAW ISO datetime, e.g. "2025-01-05T10:00:00"
        LocalDateTime expiryDate,

        // 👉 RAW ISO din, pero JSON field name = "stockInDate" (para match sa frontend)
        @JsonProperty("stockInDate")
        LocalDateTime productInDate,

        Long categoryId,
        String categoryName
) {}

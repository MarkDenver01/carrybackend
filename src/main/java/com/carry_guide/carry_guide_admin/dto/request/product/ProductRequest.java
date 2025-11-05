package com.carry_guide.carry_guide_admin.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ProductRequest(
        @NotBlank(message = "Product code is required")
        @Size(max = 50)
        String productCode,
        @NotBlank(message = "Product name is required")
        @Size(max = 255)
        String productName,
        @NotBlank(message = "Description is required")
        @Size(max = 255)
        String productDescription,
        @PositiveOrZero(message = "Stocks must be 0 or greater")
        int stocks,

        @NotBlank(message = "Product size is required")
        @Size(max = 50)
        String productSize,
        @NotBlank(message = "Product status is required")
        String productStatus,

        @NotBlank(message = "Product image URL is required")
        @Size(max = 255)
        String productImgUrl,
        LocalDateTime expiryDate,
        LocalDateTime productInDate
) {}

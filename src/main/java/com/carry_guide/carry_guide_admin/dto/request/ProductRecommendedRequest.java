package com.carry_guide.carry_guide_admin.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ProductRecommendedRequest (
        @NotBlank(message = "Product code is required")
        @Size(max = 50)
        String productCode,
        @NotBlank(message = "Product name is required")
        @Size(max = 100)
        String productName,
        @NotBlank(message = "Description is required")
        @Size(max = 200)
        String productDescription,
        @NotBlank(message = "Product size is required")
        @Size(max = 100)
        String productSize,
        @NotBlank(message = "Product image URL is required")
        @Size(max = 200)
        String productImgUrl,
        @FutureOrPresent(message = "Expiry date must not be in the past")
        LocalDateTime expiryDate
){
}

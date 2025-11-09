package com.carry_guide.carry_guide_admin.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
        @NotBlank(message = "Product code is required")
        @Size(max = 50)
        private String productCode;
        @NotBlank(message = "Product name is required")
        @Size(max = 255)
        private String productName;
        @NotBlank(message = "Description is required")
        @Size(max = 255)
        private  String productDescription;
        @PositiveOrZero(message = "Stocks must be 0 or greater")
        int stocks;

        @NotBlank(message = "Product size is required")
        @Size(max = 50)
        private   String productSize;
        @NotBlank(message = "Product status is required")
        private   String productStatus;

        @NotBlank(message = "Product image URL is required")
        @Size(max = 255)
        private  String productImgUrl;
        private   LocalDateTime expiryDate;
        private  LocalDateTime productInDate;
}

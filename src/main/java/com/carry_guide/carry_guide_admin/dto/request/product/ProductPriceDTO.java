package com.carry_guide.carry_guide_admin.dto.request.product;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProductPriceDTO {
    private Long priceId;
    private Long productId;

    private Double basePrice;
    private LocalDate effectiveDate;

    private String productName;
    private String productDescription;
    private String productCode;
    private String productSize;
    private String productImgUrl;
    private int stocks;
    private String categoryName;
}


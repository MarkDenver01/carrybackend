package com.carry_guide.carry_guide_admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private String productImgUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal lineTotal;
}

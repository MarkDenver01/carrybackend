package com.carry_guide.carry_guide_admin.dto.banner;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SnowballPromoResponse {

    private Long id;
    private String title;
    private String reward;
    private int requiredQty;

    private boolean hasExpiry;
    private LocalDate expiry;

    private String terms;

    private List<ProductItem> products;

    private Map<Long, Double> promoPrices;

    @Data
    @AllArgsConstructor
    public static class ProductItem {
        private Long productId;
        private String name;
        private String categoryName;
        private String imageUrl;
    }
}
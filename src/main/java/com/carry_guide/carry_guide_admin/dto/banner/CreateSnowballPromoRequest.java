package com.carry_guide.carry_guide_admin.dto.banner;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class CreateSnowballPromoRequest {

    private String title;
    private String reward;
    private int requiredQty;

    private boolean hasExpiry;
    private LocalDate expiry;

    private String terms;

    private List<Long> productIds;

    // 🔥 productId → promoPrice
    private Map<Long, Double> promoPrices;
}
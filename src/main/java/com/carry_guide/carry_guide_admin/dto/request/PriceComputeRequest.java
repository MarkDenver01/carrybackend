package com.carry_guide.carry_guide_admin.dto.request;

import com.carry_guide.carry_guide_admin.model.enums.DiscountCategory;
import lombok.Data;

@Data
public class PriceComputeRequest {
    private int priceId;
    private DiscountCategory category; // SENIOR, PWD, STUDENT, REGULAR
    private String couponCode;
    private int userId;
}

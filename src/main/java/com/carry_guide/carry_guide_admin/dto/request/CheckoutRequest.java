package com.carry_guide.carry_guide_admin.dto.request;

import com.carry_guide.carry_guide_admin.domain.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckoutRequest {
    private Long customerId;

    private PaymentMethod paymentMethod;   // COD / WALLET / CARD

    // front-end computed values (optional, pwede i-recompute sa backend)
    private BigDecimal deliveryFee;
    private BigDecimal discount;
    private BigDecimal totalAmount;

    private String deliveryAddress;
    private String notes;

    private List<OrderItemRequest> items;
}

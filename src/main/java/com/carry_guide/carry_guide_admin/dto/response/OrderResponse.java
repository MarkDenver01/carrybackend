package com.carry_guide.carry_guide_admin.dto.response;

import com.carry_guide.carry_guide_admin.domain.enums.OrderStatus;
import com.carry_guide.carry_guide_admin.domain.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long orderId;
    private Long customerId;
    private OrderStatus status;
    private PaymentMethod paymentMethod;

    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal discount;
    private BigDecimal totalAmount;

    private String deliveryAddress;
    private String notes;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;

    private Long riderId;
    private String riderName;

}

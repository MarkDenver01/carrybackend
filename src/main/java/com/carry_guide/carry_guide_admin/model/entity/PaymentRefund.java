package com.carry_guide.carry_guide_admin.model.entity;

import com.carry_guide.carry_guide_admin.model.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "payment_refund")
public class PaymentRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long refundId;

    @Column(name = "amount", precision = 12, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal amount;

    @Column(name = "reason")
    private String reason;

    @Column(name = "provider_refund_id")
    private String providerRefundId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status; // e.g., REFUNDED or PARTIALLY_REFUNDED

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
}

package com.carry_guide.carry_guide_admin.model.entity;

import com.carry_guide.carry_guide_admin.model.enums.PaymentMethod;
import com.carry_guide.carry_guide_admin.model.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @NotNull
    @Column(name = "amount", precision = 12, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private PaymentStatus status;

    @Column(name = "provider") // e.g., "GCASH", "PAYMAYA", "STRIPE"
    private String provider;

    @Column(name = "transaction_id", unique = true) // provider transaction id
    private String transactionId;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // optional JSON string

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Link to order
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    // Optional link to coupon usage (if a coupon was applied to this payment)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_usage_id")
    private CouponUsage couponUsage;

    // Refunds/transactions list (one payment might have refunds later)
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentRefund> refunds = new ArrayList<>();
}

package com.carry_guide.carry_guide_admin.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private int couponId;

    @NotBlank
    @Column(name = "coupon_code", unique = true)
    private String couponCode;

    @DecimalMin("0.00")
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(name = "is_active")
    private boolean isActive = true;

    // 🔥 NEW FIELDS
    @Column(name = "max_usage_limit")
    private Integer maxUsageLimit; // Total uses allowed globally

    @Column(name = "max_usage_per_user")
    private Integer maxUsagePerUser; // How many times a single user can use this coupon

    @Column(name = "current_usage_count")
    private Integer currentUsageCount = 0; // Tracks total usage count

    @Column(name = "is_stackable")
    private boolean isStackable = false; // Whether can combine with other discounts

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_id", nullable = false)
    private Price price;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CouponUsage> couponUsages = new ArrayList<>();
}
package com.carry_guide.carry_guide_admin.model.entity;

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
@Table(name = "price")
public class Price {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int priceId;

    @Column(precision = 10, scale = 2)
    private BigDecimal basePrice;

    private LocalDateTime effectiveDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL)
    private List<Discount> discounts = new ArrayList<>();

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL)
    private List<Tax> taxes = new ArrayList<>();

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL)
    private List<Coupon> coupons = new ArrayList<>();

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL)
    private List<PriceHistory> history = new ArrayList<>();
}

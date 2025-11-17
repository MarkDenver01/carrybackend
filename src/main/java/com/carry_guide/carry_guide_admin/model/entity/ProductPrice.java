package com.carry_guide.carry_guide_admin.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "product_prices")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long priceId;

    @Column(nullable = false)
    private Double basePrice;

    @Column(nullable = false)
    private Double taxPercentage;

    @Column(nullable = false)
    private Double discountPercentage;

    @Column(nullable = false)
    private String discountCategory; // PROMO, NONE, MEMBER, SEASONAL

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public Double getComputedFinalPrice() {
        double taxed = basePrice + (basePrice * (taxPercentage / 100));
        double discounted = taxed - (taxed * (discountPercentage / 100));
        return Math.round(discounted * 100.0) / 100.0;
    }
}

package com.carry_guide.carry_guide_admin.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "snowball_promo")
public class SnowballPromo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 Basic info
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String reward;

    @Column(nullable = false)
    private int requiredQty;

    private boolean hasExpiry;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiry;

    @Column(columnDefinition = "TEXT")
    private String terms;

    // 🔥 Attach products
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "snowball_promo_products",
            joinColumns = @JoinColumn(name = "promo_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

    // 🔥 Promo price per product (JSON stored)
    @ElementCollection
    @CollectionTable(
            name = "snowball_promo_prices",
            joinColumns = @JoinColumn(name = "promo_id")
    )
    @MapKeyColumn(name = "product_id")
    @Column(name = "promo_price")
    private Map<Long, Double> promoPrices = new HashMap<>();
}
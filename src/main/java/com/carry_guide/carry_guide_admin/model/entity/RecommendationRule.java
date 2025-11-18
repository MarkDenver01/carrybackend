package com.carry_guide.carry_guide_admin.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recommendation_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // WHEN product is selected
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Recommended Products (Many-to-Many)
    @ManyToMany
    @JoinTable(
            name = "recommendation_rule_items",
            joinColumns = @JoinColumn(name = "rule_id"),
            inverseJoinColumns = @JoinColumn(name = "recommended_product_id")
    )
    private List<Product> recommendedProducts = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private boolean active = true;
}

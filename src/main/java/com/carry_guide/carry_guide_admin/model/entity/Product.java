package com.carry_guide.carry_guide_admin.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    @NotBlank
    @Size(max = 255)
    @Column(name = "product_name", nullable = false)
    private String productName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "product_description", nullable = false)
    private String productDescription;

    @Column(name = "stocks", nullable = false)
    private int stocks;

    @NotBlank
    @Size(max = 50)
    @Column(name = "product_size", nullable = false)
    private String productSize;

    @Column(name = "product_status")
    private String productStatus;

    @NotBlank
    @Size(max = 255)
    @Column(name = "product_img_url", nullable = false)
    private String productImgUrl;

    @JsonFormat(pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @JsonFormat(pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    @Column(name = "product_in_date")
    private LocalDateTime productInDate;

    // 🟢 Category Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    // 🟢 Prices Relationship (One product → many prices)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductPrice> productPrices = new ArrayList<>();

    // 🟢 Recommendation Rules (One product → many rules where this product is the base)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecommendationRule> recommendationRules = new ArrayList<>();
}

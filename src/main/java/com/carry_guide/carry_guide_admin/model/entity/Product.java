package com.carry_guide.carry_guide_admin.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;
    @NotBlank
    @Size(max = 50)
    @Column(name = "product_code")
    private String productCode;

    @NotBlank
    @Size(max = 255)
    @Column(name = "product_name")
    private String productName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "product_description")
    private String productDescription;

    @Column(name = "stocks")
    private int stocks;

    @NotBlank
    @Size(max = 50)
    @Column(name = "product_size")
    private String productSize;
    private String productStatus;

    @NotBlank
    @Size(max = 255)
    @Column(name = "product_img_url")
    private String productImgUrl;

    @JsonFormat(pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @JsonFormat(pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    @Column(name = "product_in_date")
    private LocalDateTime productInDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private Price price;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ProductRecommended> recommendations = new ArrayList<>();
}


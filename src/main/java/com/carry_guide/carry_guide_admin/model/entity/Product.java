package com.carry_guide.carry_guide_admin.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productId;

    private String productCode;
    private String productName;
    private String productDescription;
    private int stocks;
    private String productSize;
    private String productStatus;
    private String productImgUrl;

    @JsonFormat(pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    private LocalDateTime expiryDate;

    @JsonFormat(pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    private LocalDateTime productInDate;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private Price price;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductRecommended> recommendations = new ArrayList<>();
}


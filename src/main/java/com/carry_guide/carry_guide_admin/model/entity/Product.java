package com.carry_guide.carry_guide_admin.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private int productId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "product_code")
    private String productCode;

    @NotBlank
    @Size(max = 50)
    @Column(name = "product_name")
    private String productName;

    @NotBlank
    @Size(max = 200)
    @Column(name = "product_description")
    private String productDescription;

    @Min(0)
    @Column(name = "stocks")
    private int stocks;

    @NotBlank
    @Size(max = 100)
    @Column(name = "product_size")
    private String productSize;

    @NotBlank
    @Size(max = 50)
    @Column(name = "product_status")
    private String productStatus;

    @Column(name = "expiry_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    private LocalDateTime expiryDate;

    @Column(name = "product_in_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    private LocalDateTime productInDate;

    @NotBlank
    @Size(max = 200)
    @Column(name = "product_img_url")
    private String productImgUrl;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Price price;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductRecommended> productRecommended = new ArrayList<>();
}

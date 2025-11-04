package com.carry_guide.carry_guide_admin.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "product_recommended")
public class ProductRecommended {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_recommended_id")
    private Long productRecommendedId;

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

    @NotBlank
    @Size(max = 100)
    @Column(name = "product_size")
    private String productSize;

    @Column(name = "expiry_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    private LocalDateTime expiryDate;

    @Column(name = "created_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy hh:mm a", timezone = "Asia/Manila")
    private LocalDateTime createdDate;

    @NotBlank
    @Size(max = 200)
    @Column(name = "product_img_url")
    private String productImgUrl;

    @OneToOne(mappedBy = "productRecommended", cascade = CascadeType.ALL, orphanRemoval = true)
    private Price price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;
}

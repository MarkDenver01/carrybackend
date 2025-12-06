package com.carry_guide.carry_guide_admin.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bannerId;

    @Column(nullable = false, length = 500)
    private String bannerUrl;      // Cloudinary image URL

    @Column(nullable = false, length = 500)
    private String bannerUrlLink;  // Redirect URL (landing page / product link)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.ProductBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaProductBannerRepository extends JpaRepository<ProductBanner, Long> {

    // Latest first (for admin + mobile)
    List<ProductBanner> findAllByOrderByCreatedAtDesc();
}
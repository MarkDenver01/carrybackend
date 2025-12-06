package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.banner.CreateBannerRequest;
import com.carry_guide.carry_guide_admin.dto.banner.BannerResponse;
import com.carry_guide.carry_guide_admin.service.ProductBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-banners")
public class ProductBannerController {

    private final ProductBannerService productBannerService;

    /* ============================
       ADMIN ENDPOINTS (PORTAL)
    ============================ */

    // GET all banners (for admin table – ProductBannerPage)
    @GetMapping
    public ResponseEntity<List<BannerResponse>> getAllBanners() {
        return ResponseEntity.ok(productBannerService.getAllBanners());
    }

    // CREATE banner (React form – using Cloudinary URL)
    @PostMapping
    public ResponseEntity<BannerResponse> createBanner(
            @RequestBody CreateBannerRequest request
    ) {
        BannerResponse created = productBannerService.createBanner(request);
        return ResponseEntity.ok(created);
    }

    // DELETE banner (Trash icon in React UI)
    @DeleteMapping("/{bannerId}")
    public ResponseEntity<Void> deleteBanner(
            @PathVariable Long bannerId
    ) {
        productBannerService.deleteBanner(bannerId);
        return ResponseEntity.noContent().build();
    }

    /* ============================
       MOBILE / PUBLIC ENDPOINT
    ============================ */

    // This will be called by MOBILE APP to display banners
    // Example URL: GET /api/product-banners/mobile
    @GetMapping("/mobile")
    public ResponseEntity<List<BannerResponse>> getBannersForMobile() {
        return ResponseEntity.ok(productBannerService.getBannersForMobile());
    }
}
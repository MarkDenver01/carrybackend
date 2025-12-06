package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.banner.CreateBannerRequest;
import com.carry_guide.carry_guide_admin.dto.banner.BannerResponse;
import com.carry_guide.carry_guide_admin.model.entity.ProductBanner;
import com.carry_guide.carry_guide_admin.repository.JpaProductBannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductBannerService {

    private final JpaProductBannerRepository productBannerRepository;

    /* ============= HELPERS ============= */

    private BannerResponse toResponse(ProductBanner entity) {
        return BannerResponse.builder()
                .bannerId(entity.getBannerId())
                .bannerUrl(entity.getBannerUrl())
                .bannerUrlLink(entity.getBannerUrlLink())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /* ============= ADMIN SIDE ============= */

    // For admin table (sorted latest → oldest)
    public List<BannerResponse> getAllBanners() {
        return productBannerRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Admin add banner (coming from your React ProductBannerPage)
    public BannerResponse createBanner(CreateBannerRequest request) {
        ProductBanner entity = ProductBanner.builder()
                .bannerUrl(request.getBannerUrl())
                .bannerUrlLink(request.getBannerUrlLink())
                .build();

        ProductBanner saved = productBannerRepository.save(entity);
        return toResponse(saved);
    }

    // Admin delete
    public void deleteBanner(Long bannerId) {
        if (!productBannerRepository.existsById(bannerId)) {
            throw new IllegalArgumentException("Banner not found with id: " + bannerId);
        }
        productBannerRepository.deleteById(bannerId);
    }

    /* ============= MOBILE SIDE ============= */

    // For MOBILE APP: simple list for home banner carousel
    public List<BannerResponse> getBannersForMobile() {
        return getAllBanners(); // same for now, pwedeng i-filter later (active, schedule, etc.)
    }
}
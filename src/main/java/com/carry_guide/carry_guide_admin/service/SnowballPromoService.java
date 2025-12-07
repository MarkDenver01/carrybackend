package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.banner.CreateSnowballPromoRequest;
import com.carry_guide.carry_guide_admin.dto.banner.SnowballPromoResponse;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.SnowballPromo;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import com.carry_guide.carry_guide_admin.repository.JpaSnowballPromoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SnowballPromoService {

    private final JpaSnowballPromoRepository promoRepo;
    private final JpaProductRepository productRepo;

    public SnowballPromoResponse create(CreateSnowballPromoRequest req) {

        SnowballPromo promo = new SnowballPromo();

        promo.setTitle(req.getTitle());
        promo.setReward(req.getReward());
        promo.setRequiredQty(req.getRequiredQty());
        promo.setHasExpiry(req.isHasExpiry());
        promo.setExpiry(req.isHasExpiry() ? req.getExpiry() : null);
        promo.setTerms(req.getTerms());

        // Attach product entities
        List<Product> products = productRepo.findAllById(req.getProductIds());
        promo.setProducts(products);

        // Save promo price map
        promo.setPromoPrices(req.getPromoPrices());

        promoRepo.save(promo);

        return mapToResponse(promo);
    }

    public List<SnowballPromoResponse> getAll() {
        return promoRepo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void delete(Long id) {
        promoRepo.deleteById(id);
    }

    public List<SnowballPromoResponse> getForMobile() {
        return promoRepo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private SnowballPromoResponse mapToResponse(SnowballPromo promo) {
        return new SnowballPromoResponse(
                promo.getId(),
                promo.getTitle(),
                promo.getReward(),
                promo.getRequiredQty(),
                promo.isHasExpiry(),
                promo.getExpiry(),
                promo.getTerms(),
                promo.getProducts().stream().map(
                        p -> new SnowballPromoResponse.ProductItem(
                                p.getProductId(),
                                p.getProductName(),
                                p.getCategory() != null ? p.getCategory().getCategoryName() : null,
                                p.getProductImgUrl()
                        )
                ).toList(),
                promo.getPromoPrices()
        );
    }
}
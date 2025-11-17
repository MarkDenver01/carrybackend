package com.carry_guide.carry_guide_admin.dto.request.product;

import com.carry_guide.carry_guide_admin.model.entity.ProductPrice;
import org.springframework.stereotype.Component;

@Component
public class ProductPriceMapper {
    public ProductPriceDTO toDto(ProductPrice entity) {
        ProductPriceDTO dto = new ProductPriceDTO();
        dto.setPriceId(entity.getPriceId());
        dto.setProductId(entity.getProduct().getProductId());
        dto.setProductName(entity.getProduct().getProductName());
        dto.setProductCode(entity.getProduct().getProductCode());

        dto.setBasePrice(entity.getBasePrice());
        dto.setTaxPercentage(entity.getTaxPercentage());
        dto.setDiscountPercentage(entity.getDiscountPercentage());
        dto.setDiscountCategory(entity.getDiscountCategory());
        dto.setEffectiveDate(entity.getEffectiveDate());

        dto.setComputedFinalPrice(entity.getComputedFinalPrice());

        return dto;
    }
}

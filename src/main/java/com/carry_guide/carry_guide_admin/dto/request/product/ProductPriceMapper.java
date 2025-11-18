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
        dto.setProductSize(entity.getProduct().getProductSize());
        dto.setProductImgUrl(entity.getProduct().getProductImgUrl());
        dto.setStocks(entity.getProduct().getStocks());
        dto.setCategoryName(entity.getProduct().getCategory().getCategoryName());
        dto.setBasePrice(entity.getBasePrice());
        dto.setEffectiveDate(entity.getEffectiveDate());
        return dto;
    }
}

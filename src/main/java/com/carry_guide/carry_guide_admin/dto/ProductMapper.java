package com.carry_guide.carry_guide_admin.dto;

import com.carry_guide.carry_guide_admin.dto.request.product.ProductPriceDTO;
import com.carry_guide.carry_guide_admin.model.entity.Product;

public class ProductMapper {
    public static ProductPriceDTO toDto(Product p) {
        ProductPriceDTO dto = new ProductPriceDTO();

        dto.setPriceId(
                p.getProductPrices().isEmpty() ? null :
                        p.getProductPrices().get(0).getPriceId()
        );

        dto.setProductId(p.getProductId());
        dto.setBasePrice(
                p.getProductPrices().isEmpty() ? null :
                        p.getProductPrices().get(0).getBasePrice()
        );

        dto.setEffectiveDate(
                p.getProductPrices().isEmpty() ? null :
                        p.getProductPrices().get(0).getEffectiveDate()
        );

        dto.setProductName(p.getProductName());
        dto.setProduceDescription(p.getProductDescription());
        dto.setProductCode(p.getProductCode());
        dto.setProductSize(p.getProductSize());
        dto.setProductImgUrl(p.getProductImgUrl());
        dto.setStocks(p.getStocks());
        dto.setCategoryName(
                p.getCategory() != null ? p.getCategory().getCategoryName() : ""
        );

        return dto;
    }
}

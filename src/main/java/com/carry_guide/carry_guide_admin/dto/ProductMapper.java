package com.carry_guide.carry_guide_admin.dto;

import com.carry_guide.carry_guide_admin.dto.request.product.ProductPriceDTO;
import com.carry_guide.carry_guide_admin.dto.response.product.ProductDTO;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.ProductPrice;

public class ProductMapper {

    // ===========================
    //  🔹 Existing: Product → ProductPriceDTO
    // ===========================
    public static ProductPriceDTO toDto(Product p) {
        ProductPriceDTO dto = new ProductPriceDTO();

        ProductPrice firstPrice = null;
        if (p.getProductPrices() != null && !p.getProductPrices().isEmpty()) {
            firstPrice = p.getProductPrices().get(0);
        }

        if (firstPrice != null) {
            dto.setPriceId(firstPrice.getPriceId());
            dto.setBasePrice(firstPrice.getBasePrice());
            dto.setEffectiveDate(firstPrice.getEffectiveDate());
        }

        dto.setProductId(p.getProductId());
        dto.setProductName(p.getProductName());
        dto.setProduceDescription(p.getProductDescription() != null ?
                p.getProductDescription() : "");
        dto.setProductCode(p.getProductCode());
        dto.setProductSize(p.getProductSize());
        dto.setProductImgUrl(p.getProductImgUrl());
        dto.setStocks(p.getStocks());
        dto.setCategoryName(
                p.getCategory() != null ? p.getCategory().getCategoryName() : ""
        );

        return dto;
    }

    // ===========================
    //  🔥 NEW: Product → ProductDTO (pang-UI for recommendation)
    // ===========================
    public static ProductDTO toProductDTO(Product p) {
        return new ProductDTO(
                p.getProductId(),
                p.getProductCode(),
                p.getProductName(),
                p.getProductDescription() != null ? p.getProductDescription() : "",
                p.getStocks(),
                p.getProductSize(),
                p.getProductStatus(),
                p.getProductImgUrl(),
                p.getExpiryDate(),
                p.getProductInDate(),
                p.getCategory() != null ? p.getCategory().getCategoryId() : null,
                p.getCategory() != null ? p.getCategory().getCategoryName() : ""
        );
    }
}

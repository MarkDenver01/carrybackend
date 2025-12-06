package com.carry_guide.carry_guide_admin.dto.request.product;

import com.carry_guide.carry_guide_admin.model.entity.ProductPrice;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ProductPriceMapper {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ProductPriceDTO toDto(ProductPrice entity) {
        ProductPriceDTO dto = new ProductPriceDTO();
        dto.setPriceId(entity.getPriceId());
        dto.setProductId(entity.getProduct().getProductId());
        dto.setBasePrice(entity.getBasePrice());
        dto.setEffectiveDate(entity.getEffectiveDate());
        dto.setExpiryDate(
                entity.getProduct().getExpiryDate() != null
                        ? entity.getProduct().getExpiryDate().format(DATE_FORMATTER)
                        : null
        );
        dto.setProductName(entity.getProduct().getProductName());
        dto.setProductDescription(entity.getProduct().getProductDescription());
        dto.setProductCode(entity.getProduct().getProductCode());
        dto.setProductSize(entity.getProduct().getProductSize());
        dto.setProductImgUrl(entity.getProduct().getProductImgUrl());
        dto.setStocks(entity.getProduct().getStocks());
        dto.setCategoryName(entity.getProduct().getCategory().getCategoryName());

        return dto;
    }
}

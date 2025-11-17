package com.carry_guide.carry_guide_admin.dto.request.product;

import lombok.Data;

@Data
public class ProductCategoryDTO {
    private Long categoryId;
    private String categoryName;
    private String categoryDescription;
}

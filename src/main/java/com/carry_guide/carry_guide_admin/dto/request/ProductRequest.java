package com.carry_guide.carry_guide_admin.dto.request;

import lombok.Data;

@Data
public class ProductRequest {
    private String productCode;
    private String productName;
    private String productDescription;
    private String productCategory;
    private int stocks;
    private String productStatus;
    private String expiryDate;
    private String productInDate;
    private String productImgUrl;
}

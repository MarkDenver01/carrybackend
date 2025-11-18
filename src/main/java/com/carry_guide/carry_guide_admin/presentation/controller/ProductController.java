package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.response.product.ProductDTO;
import com.carry_guide.carry_guide_admin.dto.response.product.ProductRecommendedDTO;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductRecommendedRequest;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductRequest;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductStatusUpdateRequest;
import com.carry_guide.carry_guide_admin.presentation.handler.BaseController;
import com.carry_guide.carry_guide_admin.presentation.handler.ValidationException;
import com.carry_guide.carry_guide_admin.service.FileUploadService;
import com.carry_guide.carry_guide_admin.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class ProductController extends BaseController {
    @Autowired
    ProductService productService;

    @Autowired
    FileUploadService fileUploadService;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @GetMapping("/api/product/get_recommendations")
    public ResponseEntity<?> getAllProductsWithRecommendations() {
        List<ProductDTO> products = productService.getAllProducts();
        return ok(products, "Fetched all products successfully");
    }

    @PostMapping(value = "/api/product/add", consumes = "multipart/form-data")
    public ResponseEntity<?> addProduct(
            @RequestPart("product") ProductRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        if ((request.getProductImgUrl() == null || request.getProductImgUrl().isBlank()) && (file == null || file.isEmpty())) {
            throw new ValidationException("Product image is required");
        }

        // If file exists, upload it (store to disk or cloud) and set URL
        if (file != null && !file.isEmpty()) {
            String imageUrl = fileUploadService.save(file); // your own service
            request.setProductImgUrl(imageUrl);
            log.info("img Url {}", imageUrl);
        }

        ProductDTO product = productService.addProduct(request);
        return ok(product, "Product created successfully");
    }

    @PutMapping(value = "/api/product/{productId}/update", consumes = "multipart/form-data")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @RequestPart("product") @Valid ProductRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        if (file != null && !file.isEmpty()) {
            String imageUrl = fileUploadService.save(file);
            request.setProductImgUrl(imageUrl);
        }

        ProductDTO product = productService.updateProduct(productId, request);
        return ok(product, "Product updated successfully");
    }


    @PatchMapping("/api/product/{productId}/update_status")
    public ResponseEntity<?> updateProductStatus(
            @PathVariable Long productId,
            @RequestBody ProductStatusUpdateRequest request
    ) {
        ProductDTO product = productService.updateProductStatus(productId, request);
        return ok(product, "Product status updated successfully");
    }

    @DeleteMapping("/api/product/{productId}/delete")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return deleted("Product deleted successfully");
    }

}

package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.response.product.ProductDTO;
import com.carry_guide.carry_guide_admin.dto.response.product.ProductRecommendedDTO;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductRecommendedRequest;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductRequest;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductStatusUpdateRequest;
import com.carry_guide.carry_guide_admin.presentation.handler.BaseController;
import com.carry_guide.carry_guide_admin.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController extends BaseController {
    @Autowired
    ProductService productService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/get_recommendations")
    public ResponseEntity<?> getAllProductsWithRecommendations() {
        List<ProductDTO> products = productService.getAllProductsWithRecommendations();
        return ok(products, "Fetched all products successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequest request) {
        ProductDTO product = productService.addProduct(request);
        return ok(product, "Product created successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{productId}/update")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request
    ) {
        ProductDTO product = productService.updateProduct(productId, request);
        return ok(product, "Product updated successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PatchMapping("/{productId}/update_status")
    public ResponseEntity<?> updateProductStatus(
            @PathVariable Long productId,
            @RequestBody ProductStatusUpdateRequest request
    ) {
        ProductDTO product = productService.updateProductStatus(productId, request);
        return ok(product, "Product status updated successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{productId}/delete")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return deleted("Product deleted successfully");
    }


    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/{productId}/recommended")
    public ResponseEntity<?> addRecommendedProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRecommendedRequest request
    ) {
        ProductRecommendedDTO rec = productService.addRecommendedProduct(productId, request);
        return ok(rec, "Recommended product added successfully");
    }
}

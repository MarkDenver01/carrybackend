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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class ProductController extends BaseController {
    @Autowired
    ProductService productService;

    @Value("${app.upload.folder}")
    private String uploadFolder;

    @Value("${app.upload.base-url}")
    private String baseUrl;

    @GetMapping("/api/product/get_recommendations")
    public ResponseEntity<?> getAllProductsWithRecommendations() {
        List<ProductDTO> products = productService.getAllProductsWithRecommendations();
        return ok(products, "Fetched all products successfully");
    }

    @PostMapping("/api/product/add")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequest request) {
        ProductDTO product = productService.addProduct(request);
        return ok(product, "Product created successfully");
    }

    @PutMapping("/api/product/{productId}/update")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request
    ) {
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


    @PostMapping("/api/product/{productId}/recommended")
    public ResponseEntity<?> addRecommendedProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRecommendedRequest request
    ) {
        ProductRecommendedDTO rec = productService.addRecommendedProduct(productId, request);
        return ok(rec, "Recommended product added successfully");
    }

    @PostMapping("/api/file/upload")
    public ResponseEntity<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded"));
            }

            // Ensure upload directory exists
            Path uploadPath = Paths.get(uploadFolder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique file name
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Build full URL
            String imageUrl = baseUrl + "/" + uploadFolder + fileName;

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "File upload failed"));
        }
    }
}

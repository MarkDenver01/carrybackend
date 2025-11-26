package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.response.product.ProductDTO;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductRequest;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductStatusUpdateRequest;
import com.carry_guide.carry_guide_admin.infrastructure.security.AuthTokenFilter;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.ProductCategory;
import com.carry_guide.carry_guide_admin.presentation.handler.BusinessException;
import com.carry_guide.carry_guide_admin.presentation.handler.ValidationException;
import com.carry_guide.carry_guide_admin.repository.JpaProductCategoryRepository;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import com.carry_guide.carry_guide_admin.dto.response.analytics.ExpiryAnalyticsDTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.List;




@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {


    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    private final JpaProductRepository productRepository;
    private final JpaProductCategoryRepository productCategoryRepository;

    /** Get all products (clean, no recommendation list) */
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToProductDTO)
                .toList();
    }

    /** Add product */
    public ProductDTO addProduct(ProductRequest productRequest) {
        logger.info("👉 ADD PRODUCT incoming categoryId={}", productRequest.getCategoryId());

        Product product = new Product();
        applyProductRequest(product, productRequest);
        productRepository.save(product);

        return mapToProductDTO(product);
    }

    /** Update product */
    public ProductDTO updateProduct(Long productId, ProductRequest productRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found with id: " + productId));

        logger.info("👉 UPDATE PRODUCT productId={}, incoming categoryId={}",
                productId, productRequest.getCategoryId());

        applyProductRequest(product, productRequest);
        productRepository.save(product);

        return mapToProductDTO(product);
    }

    /** Update only product status */
    public ProductDTO updateProductStatus(Long productId, ProductStatusUpdateRequest req) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found with id: " + productId));

        product.setProductStatus(req.productStatus());
        productRepository.save(product);

        return mapToProductDTO(product);
    }

    /** Delete product */
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ValidationException("Product not found with id: " + productId);
        }
        productRepository.deleteById(productId);
    }

    /** Common mapper for updating/creating */
    private void applyProductRequest(Product product, ProductRequest request) {
        if (request.getCategoryId() == null) {
            throw new ValidationException("Category is required for product.");
        }

        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException("Category not found with id: " + request.getCategoryId()));

        product.setProductCode(request.getProductCode());
        product.setProductName(request.getProductName());
        product.setProductDescription(request.getProductDescription());
        product.setStocks(request.getStocks());
        product.setProductSize(request.getProductSize());
        product.setProductStatus(request.getProductStatus());
        product.setProductImgUrl(request.getProductImgUrl());
        product.setExpiryDate(request.getExpiryDate());
        product.setProductInDate(request.getProductInDate());
        product.setCategory(category);
    }

    /** Final ProductDTO mapping (clean) */
    private ProductDTO mapToProductDTO(Product product) {

        Long categoryId = product.getCategory() != null
                ? product.getCategory().getCategoryId()
                : null;

        String categoryName = product.getCategory() != null
                ? product.getCategory().getCategoryName()
                : null;

        return new ProductDTO(
                product.getProductId(),
                product.getProductCode(),
                product.getProductName(),
                product.getProductDescription(),
                product.getStocks(),
                product.getProductSize(),
                product.getProductStatus(),
                product.getProductImgUrl(),
                product.getExpiryDate(),
                product.getProductInDate(),
                categoryId,
                categoryName


        );



    }
    /**
     * Food & expiry analytics:
     * - Fresh: > 30 days
     * - Moderate: 7–30 days
     * - Near expiry: 3–6 days
     * - Expiring/Expired: 0–2 days OR already expired (negative days)
     */
    public ExpiryAnalyticsDTO getExpiryAnalytics() {
        // You can switch to findAllActiveProducts() if you only want "Available"
        List<Product> products = productRepository.findAll();

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Manila"));

        long fresh = 0;
        long moderate = 0;
        long nearExpiry = 0;
        long expiringOrExpired = 0;

        for (Product p : products) {
            if (p.getExpiryDate() == null) {
                // skip products without expiry date
                continue;
            }

            LocalDate expiryDate = p.getExpiryDate().toLocalDate();
            long daysLeft = ChronoUnit.DAYS.between(today, expiryDate);

            if (daysLeft > 30) {
                // Fresh
                fresh++;
            } else if (daysLeft >= 7) {
                // 7–30
                moderate++;
            } else if (daysLeft >= 3) {
                // 3–6
                nearExpiry++;
            } else {
                // 0–2 days + already expired (negative)
                expiringOrExpired++;
            }

            // 👉 If you prefer to use stocks instead of product count,
            // replace "++" with "+= p.getStocks()" above.
        }

        return new ExpiryAnalyticsDTO(
                fresh,
                moderate,
                nearExpiry,
                expiringOrExpired
        );
    }
}

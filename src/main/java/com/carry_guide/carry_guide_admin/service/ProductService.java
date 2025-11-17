package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.response.product.ProductDTO;
import com.carry_guide.carry_guide_admin.dto.response.product.ProductRecommendedDTO;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductRecommendedRequest;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductRequest;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductStatusUpdateRequest;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.ProductCategory;
import com.carry_guide.carry_guide_admin.model.entity.ProductRecommended;
import com.carry_guide.carry_guide_admin.presentation.handler.BusinessException;
import com.carry_guide.carry_guide_admin.presentation.handler.ProductNotFoundException;
import com.carry_guide.carry_guide_admin.presentation.handler.ValidationException;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.carry_guide.carry_guide_admin.repository.JpaProductCategoryRepository;
import com.carry_guide.carry_guide_admin.model.entity.ProductCategory;

@Service
@Transactional
public class ProductService {
    @Autowired
    JpaProductRepository productRepository;

    @Autowired
    JpaProductCategoryRepository productCategoryRepository;

    public List<ProductDTO> getAllProductsWithRecommendations() {
        return productRepository.findAll().stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO addProduct(ProductRequest productRequest) {
        Product product = new Product();
        applyProductRequest(product, productRequest);
        productRepository.save(product);
        return mapToProductDTO(product);
    }

    public ProductDTO updateProduct(Long productId, ProductRequest productRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found with id: " + productId));
        applyProductRequest(product, productRequest);
        productRepository.save(product);
        return mapToProductDTO(product);
    }

    public ProductDTO updateProductStatus(Long productId, ProductStatusUpdateRequest productStatusUpdateRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found with id: " + productId));
        product.setProductStatus(productStatusUpdateRequest.productStatus());
        productRepository.save(product);
        return mapToProductDTO(product);
    }

    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ValidationException("Product not found with id: " + productId);
        }
        productRepository.deleteById(productId);
    }

    public ProductRecommendedDTO addRecommendedProduct(Long productId, ProductRecommendedRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductRecommended rec = new ProductRecommended();
        rec.setProduct(product);
        rec.setProductCode(request.productCode());
        rec.setProductName(request.productName());
        rec.setProductDescription(request.productDescription());
        rec.setProductSize(request.productSize());
        rec.setProductImgUrl(request.productImgUrl());
        rec.setExpiryDate(request.expiryDate());
        rec.setCreatedDate(LocalDateTime.now());

        product.getRecommendations().add(rec);
        productRepository.save(product);

        return mapToRecommendedDTO(rec);
    }

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

    private ProductDTO mapToProductDTO(Product product) {
        List<ProductRecommendedDTO> recommendations = product.getRecommendations().stream()
                .map(this::mapToRecommendedDTO)
                .collect(Collectors.toList());

        Long categoryId = null;
        String categoryName = null;

        if (product.getCategory() != null) {
            categoryId = product.getCategory().getCategoryId();
            categoryName = product.getCategory().getCategoryName();
        }

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
                categoryName,
                recommendations
        );
    }


    private ProductRecommendedDTO mapToRecommendedDTO(ProductRecommended rec) {
        return new ProductRecommendedDTO(
                rec.getProductRecommendedId(),
                rec.getProductCode(),
                rec.getProductName(),
                rec.getProductDescription(),
                rec.getProductSize(),
                rec.getExpiryDate(),
                rec.getCreatedDate(),
                rec.getProductImgUrl()
        );
    }
}

package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.ProductDTO;
import com.carry_guide.carry_guide_admin.dto.ProductRecommendedDTO;
import com.carry_guide.carry_guide_admin.dto.request.ProductRecommendedRequest;
import com.carry_guide.carry_guide_admin.dto.request.ProductRequest;
import com.carry_guide.carry_guide_admin.dto.request.ProductStatusUpdateRequest;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.ProductRecommended;
import com.carry_guide.carry_guide_admin.presentation.handler.BusinessException;
import com.carry_guide.carry_guide_admin.presentation.handler.ProductNotFoundException;
import com.carry_guide.carry_guide_admin.presentation.handler.ValidationException;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    JpaProductRepository productRepository;

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
        product.setProductCode(request.productCode());
        product.setProductName(request.productName());
        product.setProductDescription(request.productDescription());
        product.setStocks(request.stocks());
        product.setProductSize(request.productSize());
        product.setProductStatus(request.productStatus());
        product.setProductImgUrl(request.productImgUrl());
        product.setExpiryDate(request.expiryDate());
        product.setProductInDate(request.productInDate());
    }

    private ProductDTO mapToProductDTO(Product product) {
        List<ProductRecommendedDTO> recommendations = product.getRecommendations().stream()
                .map(this::mapToRecommendedDTO)
                .collect(Collectors.toList());

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

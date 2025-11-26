package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.ProductMapper;
import com.carry_guide.carry_guide_admin.dto.request.UserHistoryDTO;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductCategoryDTO;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductPriceDTO;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductPriceMapper;
import com.carry_guide.carry_guide_admin.dto.response.product.ProductDTO;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.ProductPrice;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import com.carry_guide.carry_guide_admin.repository.JpaUserHistoryRepository;
import com.carry_guide.carry_guide_admin.service.AIRecommendationService;
import com.carry_guide.carry_guide_admin.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/public")
@RequiredArgsConstructor
public class ProductRecommendationController {

    @Autowired
    ProductCategoryService categoryService;

    private final JpaUserHistoryRepository userHistoryRepository;
    private final JpaProductRepository productRepository;
    private final AIRecommendationService aiRecommendationService;
    private final ProductPriceMapper productPriceMapper;

    // ============================================================
    //  🔹 Save user history (search / click / purchase)
    // ============================================================
    @PostMapping("/history/save")
    public UserHistory saveHistory(@RequestBody UserHistoryDTO dto) {

        // 🛑 Avoid duplicate entries for the same keyword + customer
        if (userHistoryRepository.existsByCustomerIdAndProductKeyword(
                dto.getCustomerId(), dto.getProductKeyword()
        )) {
            // Option 1: update datetime of existing history (optional)
            // Option 2: just ignore duplicates
            return null;
        }

        UserHistory h = UserHistory.builder()
                .customerId(dto.getCustomerId())
                .productKeyword(dto.getProductKeyword())
                .dateTime(LocalDateTime.now())
                .build();

        return userHistoryRepository.save(h);
    }

    // 🔹 RETURN USER HISTORY
    @GetMapping("/history/{customerId}")
    public List<UserHistory> getHistory(@PathVariable Long customerId) {
        return userHistoryRepository.findByCustomerIdOrderByDateTimeDesc(customerId);
    }

    // ============================================================
    //  🔹 1. RECOMMENDATION BY CUSTOMER
    // ============================================================
    @GetMapping("/recommend/{customerId}")
    public List<ProductPriceDTO> getRecommendations(@PathVariable Long customerId) {

        List<Product> products = aiRecommendationService.getRecommendationsForUser(customerId);

        return products.stream()
                .map(p -> {
                    ProductPrice price = getLatestPrice(p);
                    if (price == null) return null;
                    return productPriceMapper.toDto(price);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    // ============================================================
    //  🔹 2. RELATED PRODUCTS FOR PRODUCT DETAIL
    // ============================================================
    @GetMapping("/product/{productId}/related")
    public List<ProductPriceDTO> getRelated(@PathVariable Long productId) {

        Product main = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found: " + productId));

        List<Product> related = aiRecommendationService.getRelatedProducts(main);

        return related.stream()
                .map(p -> {
                    ProductPrice price = getLatestPrice(p);
                    if (price == null) return null;
                    return productPriceMapper.toDto(price);
                })
                .filter(Objects::nonNull)
                .toList();
    }


    private ProductPrice getLatestPrice(Product product) {
        return product.getProductPrices().stream()
                .sorted(Comparator.comparing(ProductPrice::getEffectiveDate).reversed())
                .findFirst()
                .orElse(null);
    }



    @GetMapping("/all/product_category")
    public List<ProductCategoryDTO> getAll() {
        return categoryService.getAll();
    }
}

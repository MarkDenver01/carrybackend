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
import com.carry_guide.carry_guide_admin.service.ChatGPTService;
import com.carry_guide.carry_guide_admin.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
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
    private final ChatGPTService gpt;

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

    @GetMapping("/search")
    public List<ProductPriceDTO> aiSmartSearch(@RequestParam String query) {

        if (query == null || query.isBlank()) {
            return List.of(); // return nothing if empty
        }

        // STEP 1: expand query using GPT
        List<String> expanded = gpt.getRecommendedKeywords(query);

        // Add original query to ensure match
        expanded.add(query.toLowerCase());

        // STEP 2: get all available + in-stock products
        List<Product> candidates = productRepository.findAllActiveProducts().stream()
                .filter(p -> p.getStocks() > 0)
                .toList();

        // STEP 3: filter products matching any expanded term
        List<Product> matched = candidates.stream()
                .filter(p -> matchesProduct(p, expanded))
                .toList();

        if (matched.isEmpty()) {
            matched = candidates; // fallback to all
        }

        // STEP 4: AI ranking (use synthetic UserHistory so GPT ranking works)
        List<UserHistory> synthetic = List.of(
                UserHistory.builder()
                        .productKeyword(query)
                        .dateTime(LocalDateTime.now())
                        .build()
        );

        List<Long> ranking = gpt.rankProductsByHistory(synthetic, matched);

        // STEP 5: fetch products ordered by ranking
        List<Product> sorted = sortByRanking(matched, ranking);

        // STEP 6: convert to DTO
        return sorted.stream()
                .map(p -> {
                    ProductPrice price = getLatestPrice(p);
                    return (price != null) ? productPriceMapper.toDto(price) : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean matchesProduct(Product p, List<String> keywords) {
        String text = (p.getProductName() + " " + p.getProductDescription())
                .toLowerCase();

        for (String k : keywords) {
            if (text.contains(k.toLowerCase())) return true;
        }
        return false;
    }

    private List<Product> sortByRanking(List<Product> list, List<Long> rankingIds) {
        Map<Long, Product> map = list.stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        List<Product> sorted = new ArrayList<>();

        for (Long id : rankingIds) {
            if (map.containsKey(id)) {
                sorted.add(map.get(id));
            }
        }

        // add remaining
        for (Product p : list) {
            if (!sorted.contains(p)) {
                sorted.add(p);
            }
        }

        return sorted;
    }

}

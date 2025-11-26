package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.ProductMapper;
import com.carry_guide.carry_guide_admin.dto.request.UserHistoryDTO;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductPriceDTO;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import com.carry_guide.carry_guide_admin.repository.JpaUserHistoryRepository;
import com.carry_guide.carry_guide_admin.service.AIRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/user/public")
@RequiredArgsConstructor
public class ProductRecommendationController {
    private final JpaUserHistoryRepository userHistoryRepository;
    private final AIRecommendationService aiRecommendationService;


    @PostMapping("/history/save")
    public ResponseEntity<?> saveHistory(@RequestBody UserHistoryDTO dto) {

        if (userHistoryRepository.existsByCustomerIdAndProductKeyword(
                dto.getCustomerId(), dto.getProductKeyword())) {
            // 204 No Content – mobile just checks isSuccessful = true
            return ResponseEntity.noContent().build();
        }

        LocalDateTime parsedDate = LocalDateTime.parse(dto.getDateTime());

        UserHistory history = UserHistory.builder()
                .customerId(dto.getCustomerId())
                .productKeyword(dto.getProductKeyword())
                .dateTime(parsedDate)
                .build();

        UserHistory saved = userHistoryRepository.save(history);
        return ResponseEntity.ok(saved);
    }

    // ✅ LOAD HISTORY FOR CUSTOMER
    @GetMapping("/history/{customerId}")
    public List<UserHistory> getHistory(@PathVariable Long customerId) {
        return userHistoryRepository.findByCustomerIdOrderByDateTimeDesc(customerId);
    }

    // ✅ GET AI RECOMMENDATIONS BASED ON HISTORY
    @GetMapping("/recommendations/{customerId}")
    public List<ProductPriceDTO> getRecommendations(@PathVariable Long customerId) {
        List<Product> products = aiRecommendationService.getRecommendationsForUser(customerId);
        return products.stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    // 🔥 RELATED PRODUCTS / FREQUENTLY BOUGHT TOGETHER
    @GetMapping("/recommendations/related/{productId}")
    public ResponseEntity<List<ProductPriceDTO>> getRelatedProducts(
            @PathVariable Long productId
    ) {
        List<ProductPriceDTO> related = aiRecommendationService.getRelatedProducts(productId);
        return ResponseEntity.ok(related);
    }
}
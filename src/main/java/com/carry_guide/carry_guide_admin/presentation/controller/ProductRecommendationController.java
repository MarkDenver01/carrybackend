package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.ProductMapper;
import com.carry_guide.carry_guide_admin.dto.request.UserHistoryDTO;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductPriceDTO;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import com.carry_guide.carry_guide_admin.repository.JpaUserHistoryRepository;
import com.carry_guide.carry_guide_admin.service.AIRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/public")
@RequiredArgsConstructor
public class ProductRecommendationController {

    private final JpaUserHistoryRepository historyRepo;
    private final JpaProductRepository productRepo;
    private final AIRecommendationService aiService;

    // 🔹 SAVE USER SEARCH / CLICK HISTORY
    @PostMapping("/history/save")
    public ResponseEntity<?> saveHistory(@RequestBody UserHistoryDTO dto) {

        if (historyRepo.existsByCustomerIdAndProductKeyword(dto.getCustomerId(), dto.getProductKeyword())) {
            return ResponseEntity.ok("Duplicate skipped");
        }

        LocalDateTime parsedDate = LocalDateTime.parse(dto.getDateTime());

        UserHistory h = UserHistory.builder()
                .customerId(dto.getCustomerId())
                .productKeyword(dto.getProductKeyword())
                .dateTime(parsedDate)
                .build();

        historyRepo.save(h);
        return ResponseEntity.ok(h);
    }

    // 🔹 RETURN USER HISTORY
    @GetMapping("/history/{customerId}")
    public List<UserHistory> getHistory(@PathVariable Long customerId) {
        return historyRepo.findByCustomerIdOrderByDateTimeDesc(customerId);
    }

    // 🔹 MAIN HOMEPAGE RECOMMENDATION — DTO VERSION
    @GetMapping("/recommend/{customerId}")
    public ResponseEntity<?> recommend(@PathVariable Long customerId) {

        List<Product> recommended = aiService.getRecommendationsForUser(customerId);

        List<ProductPriceDTO> dtoList = recommended.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    // 🔹 RELATED PRODUCTS — DTO VERSION
    @GetMapping("/related/{productId}")
    public ResponseEntity<?> related(@PathVariable Long productId) {
        Product p = productRepo.findByProductId(productId);
        if (p == null) return ResponseEntity.badRequest().body("Product not found");

        List<Product> related = aiService.getRelatedProducts(p);

        List<ProductPriceDTO> dtoList = related.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
}

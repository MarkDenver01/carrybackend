package com.carry_guide.carry_guide_admin.presentation.controller;

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
    public UserHistory saveHistory(@RequestBody UserHistoryDTO dto) {

        // 🛑 Skip duplicate entries
        if (userHistoryRepository.existsByCustomerIdAndProductKeyword(
                dto.getCustomerId(), dto.getProductKeyword())) {
            return null; // do not save duplicate
        }

        // Convert incoming string date → LocalDateTime
        LocalDateTime parsedDate = LocalDateTime.parse(dto.getDateTime());

        UserHistory history = UserHistory.builder()
                .customerId(dto.getCustomerId())
                .productKeyword(dto.getProductKeyword())
                .dateTime(parsedDate)
                .build();

        return userHistoryRepository.save(history);
    }

    // ✅ LOAD HISTORY FOR CUSTOMER
    @GetMapping("/history/{customerId}")
    public List<UserHistory> getHistory(@PathVariable Long customerId) {
        return userHistoryRepository.findByCustomerId(customerId);
    }

    // ✅ GET AI RECOMMENDATIONS BASED ON HISTORY
    @GetMapping("/recommendations/{customerId}")
    public List<?> getRecommendations(@PathVariable Long customerId) {
        return aiRecommendationService.getRecommendationsForUser(customerId);
    }
}

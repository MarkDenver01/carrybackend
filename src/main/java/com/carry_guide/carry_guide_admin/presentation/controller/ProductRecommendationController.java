package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.UserHistoryDTO;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import com.carry_guide.carry_guide_admin.repository.JpaUserHistoryRepository;
import com.carry_guide.carry_guide_admin.service.AIRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ProductRecommendationController {
    private final JpaUserHistoryRepository userHistoryRepository;
    private final AIRecommendationService aiRecommendationService;


    @PostMapping("/history/save")
    public UserHistory saveHistory(@RequestBody UserHistoryDTO dto) {
        UserHistory history = UserHistory.builder()
                .customerId(dto.getCustomerId())
                .productKeyword(dto.getProductKeyword())
                .dateTime(dto.getDateTime())
                .build();
        return userHistoryRepository.save(history);
    }

    @GetMapping("/history/{customerId}")
    public List<UserHistory> getHistory(@PathVariable Long customerId) {
        return userHistoryRepository.findByCustomerId(customerId);
    }

    @GetMapping("/recommendations/{customerId}")
    public List<Product> getRecommendations(@PathVariable Long customerId) {
        return aiRecommendationService.getRecommendationsForUser(customerId);
    }
}

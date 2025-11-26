package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import com.carry_guide.carry_guide_admin.repository.JpaUserHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIRecommendationService {
    private final JpaUserHistoryRepository userHistoryRepository;
    private final JpaProductRepository productRepository;
    private final ChatGPTService chatGPTService;

    public List<Product> getRecommendationsForUser(Long customerId) {
        List<UserHistory> history = userHistoryRepository.findByCustomerId(customerId);

        if (history.isEmpty()) {
            return productRepository.findTop20ByOrderByProductIdDesc();
        }

        String keywords = history.stream()
                .map(UserHistory::getProductKeyword)
                .distinct()
                .collect(Collectors.joining(", "));

        List<String> aiKeywords = chatGPTService.getRecommendedKeywords(keywords);

        Set<Product> recommendations = new HashSet<>();
        for (String key : aiKeywords) {
            recommendations.addAll(productRepository.findByProductNameContainingIgnoreCase(key));
            recommendations.addAll(productRepository.findByCategory_CategoryNameContainingIgnoreCase(key));
        }

        if (recommendations.isEmpty()) {
            return productRepository.findTop20ByOrderByProductIdDesc();
        }

        return new ArrayList<>(recommendations);
    }
}

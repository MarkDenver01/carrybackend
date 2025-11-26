package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.request.product.ProductPriceDTO;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductPriceMapper;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.ProductPrice;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import com.carry_guide.carry_guide_admin.repository.JpaProductPriceRepository;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import com.carry_guide.carry_guide_admin.repository.JpaUserHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final JpaUserHistoryRepository userHistoryRepository;
    private final JpaProductRepository productRepository;
    private final JpaProductPriceRepository priceRepository;
    private final ProductPriceMapper mapper;
    private final ChatGPTService chatGPTService;

    public List<ProductPriceDTO> getRecommendationsForUser(Long customerId) {

        List<UserHistory> history = userHistoryRepository.findByCustomerId(customerId);

        // NO HISTORY → DEFAULT PRODUCTS
        if (history.isEmpty()) {
            return productRepository.findTop20ByOrderByProductIdDesc()
                    .stream()
                    .map(this::convertProductToDTO)
                    .filter(Objects::nonNull)
                    .toList();
        }

        // Extract keywords
        String keywords = history.stream()
                .map(UserHistory::getProductKeyword)
                .distinct()
                .collect(Collectors.joining(", "));

        // Call AI
        List<String> aiKeywords = chatGPTService.getRecommendedKeywords(keywords);

        Set<Product> productMatches = new HashSet<>();

        for (String key : aiKeywords) {
            productMatches.addAll(productRepository.findByProductNameContainingIgnoreCase(key));
            productMatches.addAll(productRepository.findByCategory_CategoryNameContainingIgnoreCase(key));
        }

        // IF AI has zero matches → fallback
        if (productMatches.isEmpty()) {
            return productRepository.findTop20ByOrderByProductIdDesc()
                    .stream()
                    .map(this::convertProductToDTO)
                    .filter(Objects::nonNull)
                    .toList();
        }

        // Convert MATCHES → DTO with price
        return productMatches.stream()
                .map(this::convertProductToDTO)
                .filter(Objects::nonNull)
                .toList();
    }

    private ProductPriceDTO convertProductToDTO(Product product) {
        ProductPrice latest = priceRepository.findByProduct_ProductId(product.getProductId())
                .stream()
                .findFirst()
                .orElse(null);

        if (latest == null) return null;

        return mapper.toDto(latest);
    }
}

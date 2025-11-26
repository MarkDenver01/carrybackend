package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
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
    private final ChatGPTService chatGPTService;

    public List<Product> getRecommendationsForUser(Long customerId) {

        // 1️⃣ Kunin lahat ng history, PINAKABAGO MUNA
        List<UserHistory> history = userHistoryRepository
                .findByCustomerIdOrderByDateTimeDesc(customerId);

        if (history.isEmpty()) {
            // Walang history → default top 20 products
            return productRepository.findTop20ByOrderByProductIdDesc();
        }

        // 2️⃣ Gagamit tayo ng LinkedHashSet para:
        //    - WALA duplications
        //    - MAINTAIN ang INSERT ORDER (important sa "pinaka-latest una")
        LinkedHashSet<Product> ordered = new LinkedHashSet<>();

        for (UserHistory h : history) {
            String keyword = h.getProductKeyword();

            // 🔹 2.1 Match by product name (pangunahing logic)
            List<Product> byName =
                    productRepository.findByProductNameContainingIgnoreCase(keyword);
            ordered.addAll(byName);

            // 🔹 2.2 Optional: match by category (kung gusto mo pa rin ito)
            List<Product> byCategory =
                    productRepository.findByCategory_CategoryNameContainingIgnoreCase(keyword);
            ordered.addAll(byCategory);
        }

        // 3️⃣ OPTIONAL: kung gusto mo pa rin ng AI expansion (add-on lang, hindi nauuna)
        //    Comment out block na 'to kung ayaw mo na si ChatGPT magdagdag.
        String allKeywords = history.stream()
                .map(UserHistory::getProductKeyword)
                .distinct()
                .collect(Collectors.joining(", "));

        List<String> aiKeywords = chatGPTService.getRecommendedKeywords(allKeywords);
        for (String key : aiKeywords) {
            ordered.addAll(productRepository.findByProductNameContainingIgnoreCase(key));
            ordered.addAll(productRepository.findByCategory_CategoryNameContainingIgnoreCase(key));
        }

        // 4️⃣ Kung sakaling wala pa rin nahanap (edge case)
        if (ordered.isEmpty()) {
            return productRepository.findTop20ByOrderByProductIdDesc();
        }

        return new ArrayList<>(ordered); // ⬅️ ORDERED na based sa history dateTime
    }
}

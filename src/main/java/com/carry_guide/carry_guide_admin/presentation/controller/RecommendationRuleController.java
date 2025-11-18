package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.product.RecommendationRuleRequest;
import com.carry_guide.carry_guide_admin.service.RecommendationRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/recommendation-rules")
@RequiredArgsConstructor
public class RecommendationRuleController {

    @Autowired
    RecommendationRuleService ruleService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody RecommendationRuleRequest req) {
        return ResponseEntity.ok(ruleService.createRule(req));
    }

    @GetMapping
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(ruleService.getAllRules());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        ruleService.deleteRule(id);
        return ResponseEntity.ok("Deleted");
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> recommendationsForProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ruleService.getRecommendationsForProduct(productId));
    }
}

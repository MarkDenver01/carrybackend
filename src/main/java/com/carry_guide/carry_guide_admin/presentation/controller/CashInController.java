package com.carry_guide.carry_guide_admin.presentation.controller;


import com.carry_guide.carry_guide_admin.service.XenditService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ewallet")
public class CashInController {
    private final XenditService xenditService;

    @PostMapping("/create")
    public ResponseEntity<?> createCashIn(@RequestParam String userId,
                                          @RequestParam int amount) {
        try {
            JsonNode result = xenditService.createGCashPayment(userId, amount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}

package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.banner.CreateSnowballPromoRequest;
import com.carry_guide.carry_guide_admin.dto.banner.SnowballPromoResponse;
import com.carry_guide.carry_guide_admin.service.SnowballPromoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/snowball")
public class SnowballPromoController {

    private final SnowballPromoService promoService;

    @PostMapping
    public ResponseEntity<SnowballPromoResponse> create(@RequestBody CreateSnowballPromoRequest req) {
        return ResponseEntity.ok(promoService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<SnowballPromoResponse>> getAll() {
        return ResponseEntity.ok(promoService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        promoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // MOBILE ENDPOINT
    @GetMapping("/mobile")
    public ResponseEntity<List<SnowballPromoResponse>> getMobile() {
        return ResponseEntity.ok(promoService.getForMobile());
    }
}
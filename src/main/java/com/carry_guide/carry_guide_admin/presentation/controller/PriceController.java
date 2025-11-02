package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.PriceComputeRequest;
import com.carry_guide.carry_guide_admin.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
public class PriceController {
    @Autowired
    PriceService priceService;

    @PostMapping("/compute")
    public ResponseEntity<BigDecimal> computePrice(@RequestBody PriceComputeRequest request) {
        BigDecimal finalPrice = priceService.computeFinalPriceWithTaxAndCoupon(
                request.getPriceId(),
                request.getCategory(),
                request.getCouponCode(),
                request.getUserId()
        );
        return ResponseEntity.ok(finalPrice);
    }
}

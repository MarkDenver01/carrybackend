package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.model.entity.Coupon;
import com.carry_guide.carry_guide_admin.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    @Autowired
    CouponService couponService;

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllActiveCoupons());
    }

    @PostMapping("/apply")
    public ResponseEntity<String> applyCoupon(
            @RequestParam int priceId,
            @RequestParam String couponCode,
            @RequestParam int userId
    ) {
        couponService.applyCoupon(priceId, couponCode, userId);
        return ResponseEntity.ok("Coupon successfully applied.");
    }
}

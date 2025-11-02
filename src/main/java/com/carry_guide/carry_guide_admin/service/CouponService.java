package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.Coupon;
import com.carry_guide.carry_guide_admin.model.entity.CouponUsage;
import com.carry_guide.carry_guide_admin.model.entity.Price;
import com.carry_guide.carry_guide_admin.repository.JpaCouponRepository;
import com.carry_guide.carry_guide_admin.repository.JpaCouponUsageRepository;
import com.carry_guide.carry_guide_admin.repository.JpaPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponService {
    @Autowired
    JpaCouponRepository couponRepository;

    @Autowired
    JpaCouponUsageRepository couponUsageRepository;

    @Autowired
    JpaPriceRepository priceRepository;

    public List<Coupon> getAllActiveCoupons() {
        return couponRepository.findByIsActiveTrue();
    }

    public void applyCoupon(int priceId, String couponCode, int userId) {
        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new RuntimeException("Price not found"));

        Coupon coupon = couponRepository.findByCouponCodeAndIsActiveTrue(couponCode)
                .orElseThrow(() -> new RuntimeException("Invalid or expired coupon"));

        if (coupon.getValidFrom().isAfter(LocalDateTime.now()) ||
                coupon.getValidTo().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon expired or not yet valid");
        }

        // Check usage limits
        long totalUsage = couponUsageRepository.countByCoupon(coupon);
        if (coupon.getMaxUsageLimit() != null && totalUsage >= coupon.getMaxUsageLimit()) {
            throw new RuntimeException("Coupon has reached global usage limit");
        }

        long userUsage = couponUsageRepository.countByCouponAndUserId(coupon, userId);
        if (coupon.getMaxUsagePerUser() != null && userUsage >= coupon.getMaxUsagePerUser()) {
            throw new RuntimeException("User reached coupon usage limit");
        }

        // Record usage
        CouponUsage usage = new CouponUsage();
        usage.setCoupon(coupon);
        usage.setPrice(price);
        usage.setUserId(userId);
        usage.setAppliedAt(LocalDateTime.now());
        couponUsageRepository.save(usage);

        // Update global count
        coupon.setCurrentUsageCount(coupon.getCurrentUsageCount() + 1);
        couponRepository.save(coupon);
    }
}

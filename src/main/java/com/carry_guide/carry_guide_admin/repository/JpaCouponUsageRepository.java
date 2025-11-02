package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.Coupon;
import com.carry_guide.carry_guide_admin.model.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaCouponUsageRepository extends JpaRepository<CouponUsage, Integer> {
    // count total uses for a given coupon
    long countByCoupon(Coupon coupon);

    // count how many times a specific user used a given coupon
    long countByCouponAndUserId(Coupon coupon, int userId);

    // list usages for a coupon (useful for reporting)
    List<CouponUsage> findByCoupon(Coupon coupon);

    // list usages for a user
    List<CouponUsage> findByUserId(int userId);

    // optional: find usage for specific user + coupon
    List<CouponUsage> findByCouponAndUserId(Coupon coupon, int userId);
}

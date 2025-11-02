package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCouponRepository extends JpaRepository<Coupon, Integer> {
    // Return all active coupons
    List<Coupon> findByIsActiveTrue();

    // Find a single active coupon by code
    Optional<Coupon> findByCouponCodeAndIsActiveTrue(String couponCode);

    // (optional convenience methods)
    Optional<Coupon> findByCouponCode(String couponCode);
}

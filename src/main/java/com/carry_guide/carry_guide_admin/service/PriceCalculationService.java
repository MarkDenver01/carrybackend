package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.Coupon;
import com.carry_guide.carry_guide_admin.model.entity.Discount;
import com.carry_guide.carry_guide_admin.model.entity.Price;
import com.carry_guide.carry_guide_admin.model.entity.Tax;
import com.carry_guide.carry_guide_admin.model.enums.DiscountCategory;
import com.carry_guide.carry_guide_admin.repository.JpaCouponRepository;
import com.carry_guide.carry_guide_admin.repository.JpaDiscountRepository;
import com.carry_guide.carry_guide_admin.repository.JpaPriceRepository;
import com.carry_guide.carry_guide_admin.repository.JpaTaxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PriceCalculationService {

    @Autowired
    JpaTaxRepository taxRepository;

    @Autowired
    JpaCouponRepository couponRepository;

    @Autowired
    JpaPriceRepository priceRepository;

    @Autowired
    JpaDiscountRepository discountRepository;

    /**
     * Compute final price for productId applying:
     * - base price (BigDecimal)
     * - discount by category (senior/pwd/student)
     * - coupon (if present)
     * - tax (if not exempt)
     *
     * Returns rounded double (2 decimal places).
     */
    public double computeFinalPrice(int productId, String couponCode, String userType) {
        BigDecimal basePrice = getBasePrice(productId);

        DiscountCategory category = parseDiscountCategory(userType);

        BigDecimal categoryDiscountPercent = getDiscountPercentForCategory(category);
        BigDecimal couponDiscountPercent = getCouponDiscountPercent(couponCode);
        BigDecimal taxPercent = getActiveTaxPercent(category);

        // Apply category discount first, then coupon
        BigDecimal discountTotalPercent = categoryDiscountPercent.add(couponDiscountPercent);

        BigDecimal hundred = BigDecimal.valueOf(100);
        BigDecimal discountedPrice = basePrice.multiply(hundred.subtract(discountTotalPercent))
                .divide(hundred);

        // Apply tax if any
        BigDecimal finalPrice = discountedPrice.multiply(BigDecimal.ONE.add(taxPercent.divide(hundred)));

        // Round to 2 decimal places and return double
        finalPrice = finalPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
        return finalPrice.doubleValue();
    }

    private BigDecimal getBasePrice(int productId) {
        Optional<Price> priceOpt = priceRepository.findByProduct_ProductId(productId);
        return priceOpt.map(Price::getBasePrice).orElse(BigDecimal.ZERO);
    }

    private DiscountCategory parseDiscountCategory(String userType) {
        if (userType == null) return DiscountCategory.NONE;
        try {
            return DiscountCategory.valueOf(userType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return DiscountCategory.NONE;
        }
    }

    private BigDecimal getDiscountPercentForCategory(DiscountCategory category) {
        if (category == null || category == DiscountCategory.NONE) return BigDecimal.ZERO;
        Optional<Discount> discountOpt = discountRepository.findByDiscountCategory(category);
        return discountOpt.map(Discount::getDiscountPercentage).orElse(BigDecimal.ZERO);
    }

    private BigDecimal getCouponDiscountPercent(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) return BigDecimal.ZERO;
        Optional<Coupon> couponOpt = couponRepository.findByCouponCodeAndIsActiveTrue(couponCode);
        return couponOpt.map(Coupon::getDiscountPercentage).orElse(BigDecimal.ZERO);
    }

    private BigDecimal getActiveTaxPercent(DiscountCategory category) {
        // If tax is exempt for this category, return 0
        LocalDateTime now = LocalDateTime.now();
        Optional<Tax> taxOpt = taxRepository.findByEffectiveFromBeforeAndEffectiveToAfter(now, now);

        if (taxOpt.isEmpty()) return BigDecimal.ZERO;

        Tax tax = taxOpt.get();

        boolean isExempt = false;
        if (category == DiscountCategory.SENIOR && Boolean.TRUE.equals(tax.isExemptForSenior())) isExempt = true;
        if (category == DiscountCategory.PWD && Boolean.TRUE.equals(tax.isExemptForPwd())) isExempt = true;
        if (category == DiscountCategory.STUDENT && Boolean.TRUE.equals(tax.isExemptForStudent())) isExempt = true;

        return isExempt ? BigDecimal.ZERO : tax.getTaxPercentage();
    }
}

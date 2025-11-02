package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.*;
import com.carry_guide.carry_guide_admin.model.enums.DiscountCategory;
import com.carry_guide.carry_guide_admin.repository.JpaCouponRepository;
import com.carry_guide.carry_guide_admin.repository.JpaDiscountRepository;
import com.carry_guide.carry_guide_admin.repository.JpaPriceHistoryRepository;
import com.carry_guide.carry_guide_admin.repository.JpaPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PriceService {
    @Autowired
    JpaPriceRepository priceRepository;

    @Autowired
    JpaDiscountRepository discountRepository;

    @Autowired
    JpaPriceHistoryRepository priceHistoryRepository;

    @Autowired
    JpaCouponRepository couponRepository;

    public Discount addDiscount(int priceId, DiscountCategory category, BigDecimal percentage,
                                LocalDateTime validFrom, LocalDateTime validTo) {

        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new RuntimeException("Price not found"));

        Discount discount = new Discount();
        discount.setPrice(price);
        discount.setDiscountCategory(category);
        discount.setDiscountPercentage(percentage);
        discount.setValidFrom(validFrom);
        discount.setValidTo(validTo);

        return discountRepository.save(discount);
    }

    public BigDecimal computeFinalPrice(int priceId, DiscountCategory category) {
        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new RuntimeException("Price not found"));

        BigDecimal base = price.getBasePrice();
        Optional<Discount> validDiscount = price.getDiscounts().stream()
                .filter(d -> d.getDiscountCategory() == category
                        && d.getValidFrom().isBefore(LocalDateTime.now())
                        && d.getValidTo().isAfter(LocalDateTime.now()))
                .findFirst();

        if (validDiscount.isEmpty()) return base;

        BigDecimal discountValue = base.multiply(validDiscount.get().getDiscountPercentage())
                .divide(BigDecimal.valueOf(100));
        return base.subtract(discountValue);
    }

    public void updateBasePrice(int priceId, BigDecimal newPrice) {
        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new RuntimeException("Price not found"));

        BigDecimal oldPrice = price.getBasePrice();
        price.setBasePrice(newPrice);
        price.setEffectiveDate(LocalDateTime.now());
        priceRepository.save(price);

        PriceHistory history = new PriceHistory();
        history.setPrice(price);
        history.setOldPrice(oldPrice);
        history.setNewPrice(newPrice);
        priceHistoryRepository.save(history);
    }

    public BigDecimal computeFinalPriceWithTaxAndCoupon(
            int priceId,
            DiscountCategory category,
            String couponCode,
            int userId
    ) {
        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new RuntimeException("Price not found"));

        BigDecimal base = price.getBasePrice();

        // 1️⃣ Category Discount
        Optional<Discount> validDiscount = price.getDiscounts().stream()
                .filter(d -> d.getDiscountCategory() == category
                        && d.getValidFrom().isBefore(LocalDateTime.now())
                        && d.getValidTo().isAfter(LocalDateTime.now()))
                .findFirst();

        if (validDiscount.isPresent()) {
            BigDecimal discountValue = base.multiply(validDiscount.get().getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100));
            base = base.subtract(discountValue);
        }

        // 2️⃣ Coupon Logic
        if (couponCode != null && !couponCode.isEmpty()) {
            Optional<Coupon> validCoupon = price.getCoupons().stream()
                    .filter(c -> c.getCouponCode().equalsIgnoreCase(couponCode)
                            && c.isActive()
                            && c.getValidFrom().isBefore(LocalDateTime.now())
                            && c.getValidTo().isAfter(LocalDateTime.now()))
                    .findFirst();

            if (validCoupon.isPresent()) {
                Coupon coupon = validCoupon.get();

                // Validate usage limits
                long userUsageCount = coupon.getCouponUsages().stream()
                        .filter(u -> u.getUserId() == userId)
                        .count();

                if (coupon.getMaxUsagePerUser() != null &&
                        userUsageCount >= coupon.getMaxUsagePerUser()) {
                    throw new RuntimeException("Coupon usage limit reached for user.");
                }

                if (coupon.getMaxUsageLimit() != null &&
                        coupon.getCurrentUsageCount() >= coupon.getMaxUsageLimit()) {
                    throw new RuntimeException("Coupon has reached its global limit.");
                }

                // Apply coupon discount
                BigDecimal couponValue = base.multiply(coupon.getDiscountPercentage())
                        .divide(BigDecimal.valueOf(100));
                base = base.subtract(couponValue);

                // Track usage
                CouponUsage usage = new CouponUsage();
                usage.setCoupon(coupon);
                usage.setPrice(price);
                usage.setUserId(userId);
                usage.setAppliedAt(LocalDateTime.now());
                coupon.getCouponUsages().add(usage);

                coupon.setCurrentUsageCount(coupon.getCurrentUsageCount() + 1);
                couponRepository.save(coupon);
            }
        }

        // 3️⃣ Tax (with exemption)
        Optional<Tax> currentTax = price.getTaxes().stream()
                .filter(t -> t.getEffectiveFrom().isBefore(LocalDateTime.now())
                        && t.getEffectiveTo().isAfter(LocalDateTime.now()))
                .findFirst();

        if (currentTax.isPresent()) {
            Tax tax = currentTax.get();

            boolean isExempt = (category == DiscountCategory.SENIOR && tax.isExemptForSenior())
                    || (category == DiscountCategory.PWD && tax.isExemptForPwd())
                    || (category == DiscountCategory.STUDENT && tax.isExemptForStudent());

            if (!isExempt) {
                BigDecimal taxValue = base.multiply(tax.getTaxPercentage())
                        .divide(BigDecimal.valueOf(100));
                base = base.add(taxValue);
            }
        }

        return base;
    }


    // TODO USAGE:
    // Create a base price
//    Price milkPrice = priceService.createPrice(new BigDecimal("100.00"), milkProduct);
//
//// Add discounts
//priceService.addDiscount(milkPrice.getPriceId(), DiscountCategory.SENIOR, new BigDecimal("5.00"),
//        LocalDateTime.now(), LocalDateTime.now().plusMonths(12));
//
//priceService.addDiscount(milkPrice.getPriceId(), DiscountCategory.PWD, new BigDecimal("10.00"),
//        LocalDateTime.now(), LocalDateTime.now().plusMonths(12));
//
//    // Compute final price for a senior
//    BigDecimal seniorPrice = priceService.computeFinalPrice(milkPrice.getPriceId(), DiscountCategory.SENIOR);
//System.out.println("Senior price: " + seniorPrice); // -> 95.00

// Base price
//Price price = priceService.createPrice(new BigDecimal("100.00"), product);
//
//    // Add VAT (12%)
//    Tax vat = new Tax();
//vat.setTaxName("VAT");
//vat.setTaxPercentage(new BigDecimal("12"));
//vat.setEffectiveFrom(LocalDateTime.now());
//vat.setEffectiveTo(LocalDateTime.now().plusYears(5));
//vat.setPrice(price);
//taxRepository.save(vat);
//
//    // Add Coupon
//    Coupon coupon = new Coupon();
//coupon.setCouponCode("PROMO2025");
//coupon.setDiscountPercentage(new BigDecimal("10"));
//coupon.setValidFrom(LocalDateTime.now());
//coupon.setValidTo(LocalDateTime.now().plusMonths(6));
//coupon.setPrice(price);
//couponRepository.save(coupon);
//
//    // Compute Final Price for a Senior + VAT + Coupon
//    BigDecimal finalPrice = priceService.computeFinalPriceWithTaxAndCoupon(
//            price.getPriceId(),
//            DiscountCategory.SENIOR,
//            "PROMO2025"
//    );
//System.out.println("Final Price: ₱" + finalPrice);

}

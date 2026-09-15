package com.kartik.ridehailing.service;

import com.kartik.ridehailing.model.Coupon;
import com.kartik.ridehailing.repository.CouponRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public Coupon addCoupon(String code, BigDecimal discountPercentage) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Coupon code cannot be empty");
        }

        if (discountPercentage == null
                || discountPercentage.compareTo(BigDecimal.ZERO) <= 0
                || discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException(
                    "Discount percentage must be between 0 and 100");
        }

        if (couponRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Coupon already exists: " + code);
        }

        Coupon coupon = new Coupon(
                code,
                discountPercentage.setScale(2, RoundingMode.HALF_UP)
        );

        couponRepository.save(coupon);

        return coupon;
    }

    public void deleteCoupon(String code) {
        if (couponRepository.findByCode(code).isEmpty()) {
            throw new IllegalArgumentException("Coupon not found: " + code);
        }

        couponRepository.deleteByCode(code);
    }

    public Coupon getCoupon(String code) {
        return couponRepository.findByCode(code)
                .orElseThrow(() ->
                        new IllegalArgumentException("Coupon not found: " + code));
    }

    public BigDecimal applyCoupon(String code, BigDecimal fare) {
        if (fare == null || fare.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fare cannot be negative");
        }

        Coupon coupon = getCoupon(code);

        BigDecimal discount = fare
                .multiply(coupon.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return fare.subtract(discount).max(BigDecimal.ZERO);
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }
}
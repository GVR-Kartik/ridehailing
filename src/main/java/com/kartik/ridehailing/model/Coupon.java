package com.kartik.ridehailing.model;

import java.math.BigDecimal;

public class Coupon {

    private final String code;
    private final BigDecimal discountPercentage;

    public Coupon(String code, BigDecimal discountPercentage) {
        this.code = code;
        this.discountPercentage = discountPercentage;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
}
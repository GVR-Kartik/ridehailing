package com.kartik.ridehailing.repository;

import com.kartik.ridehailing.model.Coupon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryCouponRepository implements CouponRepository {

    private final Map<String, Coupon> coupons = new HashMap<>();

    @Override
    public void save(Coupon coupon) {
        coupons.put(coupon.getCode(), coupon);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return Optional.ofNullable(coupons.get(code));
    }

    @Override
    public void deleteByCode(String code) {
        coupons.remove(code);
    }

    @Override
    public List<Coupon> findAll() {
        return new ArrayList<>(coupons.values());
    }
}

package com.kartik.ridehailing.repository;

import com.kartik.ridehailing.model.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {

    void save(Coupon coupon);

    Optional<Coupon> findByCode(String code);

    void deleteByCode(String code);

    List<Coupon> findAll();
}
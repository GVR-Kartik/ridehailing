package com.kartik.ridehailing.service;

import com.kartik.ridehailing.model.Coupon;
import com.kartik.ridehailing.repository.CouponRepository;
import com.kartik.ridehailing.repository.InMemoryCouponRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CouponServiceTest {

    private final CouponRepository repository =
            new InMemoryCouponRepository();

    private final CouponService service =
            new CouponService(repository);

    @Test
    void shouldAddCoupon() {
        Coupon coupon = service.addCoupon(
                "SAVE10",
                BigDecimal.TEN
        );

        assertEquals("SAVE10", coupon.getCode());
        assertEquals(
                BigDecimal.TEN.setScale(2),
                coupon.getDiscountPercentage()
        );
    }

    @Test
    void shouldApplyCoupon() {
        service.addCoupon("SAVE10", BigDecimal.TEN);

        BigDecimal result =
                service.applyCoupon(
                        "SAVE10",
                        BigDecimal.valueOf(200)
                );

        assertEquals(
                BigDecimal.valueOf(180.00).setScale(2),
                result
        );
    }

    @Test
    void shouldDeleteCoupon() {
        service.addCoupon("SAVE10", BigDecimal.TEN);

        service.deleteCoupon("SAVE10");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getCoupon("SAVE10")
        );
    }
}
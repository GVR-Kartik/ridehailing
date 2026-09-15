package com.kartik.ridehailing.strategy.pricing;

import com.kartik.ridehailing.enums.CarType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TieredPricingStrategy implements PricingStrategy {

    private static final BigDecimal MINIMUM_FARE = BigDecimal.valueOf(50);
    private static final BigDecimal FIRST_TIER_RATE = BigDecimal.valueOf(10);
    private static final BigDecimal SECOND_TIER_RATE = BigDecimal.valueOf(8);
    private static final BigDecimal THIRD_TIER_RATE = BigDecimal.valueOf(5);

    @Override
    public BigDecimal calculateFare(double distanceInKm, CarType carType) {
        if (distanceInKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }

        if (carType == null) {
            throw new IllegalArgumentException("Car type cannot be null");
        }

        BigDecimal distance = BigDecimal.valueOf(distanceInKm);

        BigDecimal fare;

        if (distance.compareTo(BigDecimal.valueOf(2)) <= 0) {
            fare = distance.multiply(FIRST_TIER_RATE);
        } else if (distance.compareTo(BigDecimal.valueOf(5)) <= 0) {
            fare = BigDecimal.valueOf(2).multiply(FIRST_TIER_RATE)
                    .add(
                            distance.subtract(BigDecimal.valueOf(2))
                                    .multiply(SECOND_TIER_RATE)
                    );
        } else {
            fare = BigDecimal.valueOf(2).multiply(FIRST_TIER_RATE)
                    .add(
                            BigDecimal.valueOf(3).multiply(SECOND_TIER_RATE)
                    )
                    .add(
                            distance.subtract(BigDecimal.valueOf(5))
                                    .multiply(THIRD_TIER_RATE)
                    );
        }

        // Sedan pricing is intentionally kept the same for now.
        // The assessment requirements don't specify a Sedan multiplier.
        return fare.max(MINIMUM_FARE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
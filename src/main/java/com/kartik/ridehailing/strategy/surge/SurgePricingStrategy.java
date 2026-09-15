package com.kartik.ridehailing.strategy.surge;

import com.kartik.ridehailing.enums.CarType;
import com.kartik.ridehailing.strategy.pricing.PricingStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SurgePricingStrategy
        implements PricingStrategy {

    private final PricingStrategy basePricingStrategy;
    private final SurgeMultiplierProvider multiplierProvider;

    public SurgePricingStrategy(
            PricingStrategy basePricingStrategy,
            SurgeMultiplierProvider multiplierProvider) {

        if (basePricingStrategy == null) {
            throw new IllegalArgumentException(
                    "Base pricing strategy cannot be null"
            );
        }

        if (multiplierProvider == null) {
            throw new IllegalArgumentException(
                    "Surge multiplier provider cannot be null"
            );
        }

        this.basePricingStrategy = basePricingStrategy;
        this.multiplierProvider = multiplierProvider;
    }

    @Override
    public BigDecimal calculateFare(
            double distanceInKm,
            CarType carType) {

        BigDecimal baseFare =
                basePricingStrategy.calculateFare(
                        distanceInKm,
                        carType
                );

        BigDecimal multiplier =
                multiplierProvider.getMultiplier();

        if (multiplier.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalStateException(
                    "Surge multiplier cannot be below 1.0"
            );
        }

        return baseFare
                .multiply(multiplier)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }
}
package com.kartik.ridehailing.strategy.pricing;

import com.kartik.ridehailing.enums.CarType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TieredPricingStrategy implements PricingStrategy {

    private static final BigDecimal MINIMUM_FARE = BigDecimal.valueOf(50);

    private final BigDecimal hatchbackFirstTierRate;
    private final BigDecimal hatchbackSecondTierRate;
    private final BigDecimal hatchbackThirdTierRate;

    private final BigDecimal sedanFirstTierRate;
    private final BigDecimal sedanSecondTierRate;
    private final BigDecimal sedanThirdTierRate;

    public TieredPricingStrategy(
            BigDecimal hatchbackFirstTierRate,
            BigDecimal hatchbackSecondTierRate,
            BigDecimal hatchbackThirdTierRate,
            BigDecimal sedanFirstTierRate,
            BigDecimal sedanSecondTierRate,
            BigDecimal sedanThirdTierRate) {

        this.hatchbackFirstTierRate = hatchbackFirstTierRate;
        this.hatchbackSecondTierRate = hatchbackSecondTierRate;
        this.hatchbackThirdTierRate = hatchbackThirdTierRate;

        this.sedanFirstTierRate = sedanFirstTierRate;
        this.sedanSecondTierRate = sedanSecondTierRate;
        this.sedanThirdTierRate = sedanThirdTierRate;
    }

    @Override
    public BigDecimal calculateFare(
            double distanceInKm,
            CarType carType) {

        if (distanceInKm < 0) {
            throw new IllegalArgumentException(
                    "Distance cannot be negative"
            );
        }

        if (carType == null) {
            throw new IllegalArgumentException(
                    "Car type cannot be null"
            );
        }

        BigDecimal firstTierRate;
        BigDecimal secondTierRate;
        BigDecimal thirdTierRate;

        if (carType == CarType.HATCHBACK) {
            firstTierRate = hatchbackFirstTierRate;
            secondTierRate = hatchbackSecondTierRate;
            thirdTierRate = hatchbackThirdTierRate;
        } else {
            firstTierRate = sedanFirstTierRate;
            secondTierRate = sedanSecondTierRate;
            thirdTierRate = sedanThirdTierRate;
        }

        BigDecimal distance =
                BigDecimal.valueOf(distanceInKm);

        BigDecimal fare;

        if (distance.compareTo(BigDecimal.valueOf(2)) <= 0) {

            fare = distance.multiply(firstTierRate);

        } else if (distance.compareTo(BigDecimal.valueOf(5)) <= 0) {

            fare = BigDecimal.valueOf(2)
                    .multiply(firstTierRate)
                    .add(
                            distance
                                    .subtract(BigDecimal.valueOf(2))
                                    .multiply(secondTierRate)
                    );

        } else {

            fare = BigDecimal.valueOf(2)
                    .multiply(firstTierRate)
                    .add(
                            BigDecimal.valueOf(3)
                                    .multiply(secondTierRate)
                    )
                    .add(
                            distance
                                    .subtract(BigDecimal.valueOf(5))
                                    .multiply(thirdTierRate)
                    );
        }

        return fare
                .max(MINIMUM_FARE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
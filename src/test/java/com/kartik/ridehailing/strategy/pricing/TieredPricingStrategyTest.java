package com.kartik.ridehailing.strategy.pricing;

import com.kartik.ridehailing.enums.CarType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TieredPricingStrategyTest {

    private final PricingStrategy pricingStrategy =
            new TieredPricingStrategy();

    @Test
    void shouldApplyMinimumFare() {
        BigDecimal fare = pricingStrategy.calculateFare(
                1.0,
                CarType.HATCHBACK
        );

        assertEquals(
                BigDecimal.valueOf(50.00).setScale(2),
                fare
        );
    }

    @Test
    void shouldCalculateFirstTier() {
        BigDecimal fare = pricingStrategy.calculateFare(
                2.0,
                CarType.HATCHBACK
        );

        assertEquals(
                BigDecimal.valueOf(50.00).setScale(2),
                fare
        );
    }

    @Test
    void shouldCalculateSecondTierProgressively() {
        BigDecimal fare = pricingStrategy.calculateFare(
                3.0,
                CarType.HATCHBACK
        );

        // 2 * 10 + 1 * 8 = 28, minimum fare applies
        assertEquals(
                BigDecimal.valueOf(50.00).setScale(2),
                fare
        );
    }

    @Test
    void shouldCalculateFiveKmFare() {
        BigDecimal fare = pricingStrategy.calculateFare(
                5.0,
                CarType.HATCHBACK
        );

        // 2 * 10 + 3 * 8 = 44, minimum fare applies
        assertEquals(
                BigDecimal.valueOf(50.00).setScale(2),
                fare
        );
    }

    @Test
    void shouldCalculateAboveFiveKmFare() {
        BigDecimal fare = pricingStrategy.calculateFare(
                6.0,
                CarType.HATCHBACK
        );

        // 2 * 10 + 3 * 8 + 1 * 5 = 49
        // minimum fare applies
        assertEquals(
                BigDecimal.valueOf(50.00).setScale(2),
                fare
        );
    }

    @Test
    void shouldCalculateFareAboveMinimum() {
        BigDecimal fare = pricingStrategy.calculateFare(
                10.0,
                CarType.HATCHBACK
        );

        // 2 * 10 + 3 * 8 + 5 * 5 = 69
        assertEquals(
                BigDecimal.valueOf(69.00).setScale(2),
                fare
        );
    }

    @Test
    void shouldRejectNegativeDistance() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pricingStrategy.calculateFare(
                        -1.0,
                        CarType.HATCHBACK
                )
        );
    }

    @Test
    void shouldRejectNullCarType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pricingStrategy.calculateFare(
                        5.0,
                        null
                )
        );
    }
}
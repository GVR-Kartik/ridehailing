package com.kartik.ridehailing.strategy.pricing;

import com.kartik.ridehailing.enums.CarType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TieredPricingStrategyTest {

    private PricingStrategy pricingStrategy;

    @BeforeEach
    void setUp() {

        /*
         * Pricing configuration:
         *
         *              0-2 km    2-5 km    >5 km
         * Hatchback     ₹10        ₹8        ₹5
         * Sedan         ₹12       ₹10        ₹7
         *
         * Minimum fare = ₹50
         */

        pricingStrategy =
                new TieredPricingStrategy(
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(8),
                        BigDecimal.valueOf(5),
                        BigDecimal.valueOf(12),
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(7)
                );
    }

    @Test
    void shouldApplyMinimumFareForShortHatchbackRide() {

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        1,
                        CarType.HATCHBACK
                );

        assertEquals(
                BigDecimal.valueOf(50).setScale(2),
                fare
        );
    }

    @Test
    void shouldApplyMinimumFareForShortSedanRide() {

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        1,
                        CarType.SEDAN
                );

        assertEquals(
                BigDecimal.valueOf(50).setScale(2),
                fare
        );
    }

    @Test
    void shouldCalculateHatchbackFareForFirstTier() {

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        2,
                        CarType.HATCHBACK
                );

        /*
         * 2 km × ₹10 = ₹20
         * Minimum fare applies.
         */

        assertEquals(
                BigDecimal.valueOf(50).setScale(2),
                fare
        );
    }

    @Test
    void shouldCalculateHatchbackFareForSecondTier() {

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        5,
                        CarType.HATCHBACK
                );

        /*
         * 2 km × ₹10 = ₹20
         * 3 km × ₹8  = ₹24
         * Total = ₹44
         * Minimum fare applies.
         */

        assertEquals(
                BigDecimal.valueOf(50).setScale(2),
                fare
        );
    }

    @Test
    void shouldCalculateHatchbackFareForThirdTier() {

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        10,
                        CarType.HATCHBACK
                );

        /*
         * 2 × ₹10 = ₹20
         * 3 × ₹8  = ₹24
         * 5 × ₹5  = ₹25
         *
         * Total = ₹69
         */

        assertEquals(
                BigDecimal.valueOf(69).setScale(2),
                fare
        );
    }

    @Test
    void shouldCalculateSedanFareForFirstTier() {

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        2,
                        CarType.SEDAN
                );

        /*
         * 2 × ₹12 = ₹24
         * Minimum fare applies.
         */

        assertEquals(
                BigDecimal.valueOf(50).setScale(2),
                fare
        );
    }

    @Test
    void shouldCalculateSedanFareForSecondTier() {

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        5,
                        CarType.SEDAN
                );

        /*
         * 2 × ₹12 = ₹24
         * 3 × ₹10 = ₹30
         *
         * Total = ₹54
         */

        assertEquals(
                BigDecimal.valueOf(54).setScale(2),
                fare
        );
    }

    @Test
    void shouldCalculateSedanFareForThirdTier() {

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        10,
                        CarType.SEDAN
                );

        /*
         * 2 × ₹12 = ₹24
         * 3 × ₹10 = ₹30
         * 5 × ₹7  = ₹35
         *
         * Total = ₹89
         */

        assertEquals(
                BigDecimal.valueOf(89).setScale(2),
                fare
        );
    }

    @Test
    void shouldHaveDifferentRatesForSedanAndHatchback() {

        BigDecimal hatchbackFare =
                pricingStrategy.calculateFare(
                        10,
                        CarType.HATCHBACK
                );

        BigDecimal sedanFare =
                pricingStrategy.calculateFare(
                        10,
                        CarType.SEDAN
                );

        assertEquals(
                BigDecimal.valueOf(69).setScale(2),
                hatchbackFare
        );

        assertEquals(
                BigDecimal.valueOf(89).setScale(2),
                sedanFare
        );
    }

    @Test
    void shouldApplyMinimumFareForZeroDistance() {

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        0,
                        CarType.HATCHBACK
                );

        assertEquals(
                BigDecimal.valueOf(50).setScale(2),
                fare
        );
    }

    @Test
    void shouldRejectNegativeDistance() {

        assertThrows(
                IllegalArgumentException.class,
                () -> pricingStrategy.calculateFare(
                        -1,
                        CarType.HATCHBACK
                )
        );
    }

    @Test
    void shouldRejectNullCarType() {

        assertThrows(
                IllegalArgumentException.class,
                () -> pricingStrategy.calculateFare(
                        5,
                        null
                )
        );
    }
}
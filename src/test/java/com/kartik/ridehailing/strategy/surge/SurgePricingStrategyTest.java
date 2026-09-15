package com.kartik.ridehailing.strategy.surge;

import com.kartik.ridehailing.enums.CarType;
import com.kartik.ridehailing.strategy.pricing.PricingStrategy;
import com.kartik.ridehailing.strategy.pricing.TieredPricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SurgePricingStrategyTest {

    private DemandSupplySurgeMultiplierProvider provider;
    private PricingStrategy pricingStrategy;

    @BeforeEach
    void setUp() {

        provider =
                new DemandSupplySurgeMultiplierProvider();

        PricingStrategy basePricing =
                new TieredPricingStrategy(
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(8),
                        BigDecimal.valueOf(5),
                        BigDecimal.valueOf(12),
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(7)
                );

        pricingStrategy =
                new SurgePricingStrategy(
                        basePricing,
                        provider
                );
    }

    @Test
    void shouldUseNormalPricingWhenDemandIsLow() {

        provider.updateDemandAndSupply(
                5,
                10
        );

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        10,
                        CarType.HATCHBACK
                );

        assertEquals(
                new BigDecimal("69.00"),
                fare
        );
    }

    @Test
    void shouldApplyOnePointTwoFiveSurge() {

        provider.updateDemandAndSupply(
                15,
                10
        );

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        10,
                        CarType.HATCHBACK
                );

        assertEquals(
                new BigDecimal("86.25"),
                fare
        );
    }

    @Test
    void shouldApplyOnePointFiveSurge() {

        provider.updateDemandAndSupply(
                25,
                10
        );

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        10,
                        CarType.HATCHBACK
                );

        assertEquals(
                new BigDecimal("103.50"),
                fare
        );
    }

    @Test
    void shouldApplyTwoTimesSurge() {

        provider.updateDemandAndSupply(
                50,
                10
        );

        BigDecimal fare =
                pricingStrategy.calculateFare(
                        10,
                        CarType.HATCHBACK
                );

        assertEquals(
                new BigDecimal("138.00"),
                fare
        );
    }
}
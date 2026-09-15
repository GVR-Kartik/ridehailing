package com.kartik.ridehailing.strategy.pricing;

import com.kartik.ridehailing.enums.CarType;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculateFare(double distanceInKm, CarType carType);
}
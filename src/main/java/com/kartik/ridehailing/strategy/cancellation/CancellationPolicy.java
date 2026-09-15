package com.kartik.ridehailing.strategy.cancellation;

import com.kartik.ridehailing.model.Ride;

import java.math.BigDecimal;

public interface CancellationPolicy {

    BigDecimal calculateFee(Ride ride);
}
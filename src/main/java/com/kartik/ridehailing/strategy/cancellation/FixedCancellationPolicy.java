package com.kartik.ridehailing.strategy.cancellation;

import com.kartik.ridehailing.model.Ride;

import java.math.BigDecimal;

public class FixedCancellationPolicy
        implements CancellationPolicy {

    private final BigDecimal cancellationFee;

    public FixedCancellationPolicy(
            BigDecimal cancellationFee) {

        if (cancellationFee == null
                || cancellationFee.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Cancellation fee cannot be negative"
            );
        }

        this.cancellationFee = cancellationFee;
    }

    @Override
    public BigDecimal calculateFee(Ride ride) {

        if (ride == null) {
            throw new IllegalArgumentException(
                    "Ride cannot be null"
            );
        }

        return cancellationFee;
    }
}
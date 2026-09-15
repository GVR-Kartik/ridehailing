package com.kartik.ridehailing.strategy.surge;

import java.math.BigDecimal;

public interface SurgeMultiplierProvider {

    BigDecimal getMultiplier();
}
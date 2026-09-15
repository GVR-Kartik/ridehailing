package com.kartik.ridehailing.strategy.surge;

import java.math.BigDecimal;

public class DemandSupplySurgeMultiplierProvider
        implements SurgeMultiplierProvider {

    private int demand;
    private int availableDrivers;

    public DemandSupplySurgeMultiplierProvider() {

        this.demand = 0;
        this.availableDrivers = 1;
    }

    public synchronized void updateDemandAndSupply(
            int demand,
            int availableDrivers) {

        if (demand < 0) {
            throw new IllegalArgumentException(
                    "Demand cannot be negative"
            );
        }

        if (availableDrivers < 0) {
            throw new IllegalArgumentException(
                    "Available drivers cannot be negative"
            );
        }

        this.demand = demand;
        this.availableDrivers = availableDrivers;
    }

    public synchronized int getDemand() {
        return demand;
    }

    public synchronized int getAvailableDrivers() {
        return availableDrivers;
    }

    @Override
    public synchronized BigDecimal getMultiplier() {

        /*
         * No active demand means normal pricing.
         */
        if (demand == 0) {
            return BigDecimal.ONE;
        }

        /*
         * If there are no available drivers,
         * use the highest configured multiplier.
         */
        if (availableDrivers == 0) {
            return BigDecimal.valueOf(2.0);
        }

        double ratio =
                (double) demand / availableDrivers;

        if (ratio <= 1.0) {
            return BigDecimal.valueOf(1.0);
        }

        if (ratio <= 2.0) {
            return BigDecimal.valueOf(1.25);
        }

        if (ratio <= 3.0) {
            return BigDecimal.valueOf(1.50);
        }

        return BigDecimal.valueOf(2.0);
    }
}
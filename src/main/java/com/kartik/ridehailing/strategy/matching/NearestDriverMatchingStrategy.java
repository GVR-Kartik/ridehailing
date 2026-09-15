package com.kartik.ridehailing.strategy.matching;

import com.kartik.ridehailing.enums.CarType;
import com.kartik.ridehailing.enums.DriverStatus;
import com.kartik.ridehailing.model.Driver;
import com.kartik.ridehailing.model.Location;
import com.kartik.ridehailing.util.DistanceCalculator;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class NearestDriverMatchingStrategy implements DriverMatchingStrategy {

    private static final double MAX_RADIUS_KM = 5.0;

    @Override
    public Optional<Driver> findDriver(
            List<Driver> drivers,
            Location pickupLocation,
            CarType requestedCarType) {

        // First try to find the nearest driver with the exact
        // requested car type.
        Optional<Driver> exactMatch = drivers.stream()
                .filter(driver -> driver.getStatus() == DriverStatus.AVAILABLE)
                .filter(driver ->
                        driver.getVehicle().getCarType() == requestedCarType)
                .filter(driver ->
                        DistanceCalculator.calculate(
                                driver.getCurrentLocation(),
                                pickupLocation) <= MAX_RADIUS_KM)
                .min(Comparator.comparingDouble(driver ->
                        DistanceCalculator.calculate(
                                driver.getCurrentLocation(),
                                pickupLocation)));

        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        // Free upgrade: if Hatchback is requested but no Hatchback
        // is available within the radius, allow a Sedan.
        if (requestedCarType == CarType.HATCHBACK) {
            return drivers.stream()
                    .filter(driver ->
                            driver.getStatus() == DriverStatus.AVAILABLE)
                    .filter(driver ->
                            driver.getVehicle().getCarType() == CarType.SEDAN)
                    .filter(driver ->
                            DistanceCalculator.calculate(
                                    driver.getCurrentLocation(),
                                    pickupLocation) <= MAX_RADIUS_KM)
                    .min(Comparator.comparingDouble(driver ->
                            DistanceCalculator.calculate(
                                    driver.getCurrentLocation(),
                                    pickupLocation)));
        }

        return Optional.empty();
    }
}
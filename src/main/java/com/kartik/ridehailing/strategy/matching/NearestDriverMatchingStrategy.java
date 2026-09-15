package com.kartik.ridehailing.strategy.matching;

import com.kartik.ridehailing.enums.CarType;
import com.kartik.ridehailing.enums.DriverStatus;
import com.kartik.ridehailing.model.Driver;
import com.kartik.ridehailing.model.Location;

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

        return drivers.stream()
                .filter(driver -> driver.getStatus() == DriverStatus.AVAILABLE)
                .filter(driver -> isEligibleCarType(
                        driver.getVehicle().getCarType(),
                        requestedCarType))
                .filter(driver ->
                        calculateDistance(
                                driver.getCurrentLocation(),
                                pickupLocation) <= MAX_RADIUS_KM)
                .min(Comparator.comparingDouble(driver ->
                        calculateDistance(
                                driver.getCurrentLocation(),
                                pickupLocation)));
    }

    private boolean isEligibleCarType(
            CarType actualCarType,
            CarType requestedCarType) {

        if (actualCarType == requestedCarType) {
            return true;
        }

        // Free upgrade: Hatchback request can be fulfilled by Sedan.
        return requestedCarType == CarType.HATCHBACK
                && actualCarType == CarType.SEDAN;
    }

    private double calculateDistance(Location first, Location second) {
        final double earthRadiusKm = 6371.0;

        double lat1 = Math.toRadians(first.getLatitude());
        double lat2 = Math.toRadians(second.getLatitude());

        double deltaLat = Math.toRadians(
                second.getLatitude() - first.getLatitude());

        double deltaLon = Math.toRadians(
                second.getLongitude() - first.getLongitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1)
                * Math.cos(lat2)
                * Math.sin(deltaLon / 2)
                * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }
}
package com.kartik.ridehailing.service;

import com.kartik.ridehailing.enums.CarType;
import com.kartik.ridehailing.enums.DriverStatus;
import com.kartik.ridehailing.enums.RideStatus;
import com.kartik.ridehailing.model.Driver;
import com.kartik.ridehailing.model.Location;
import com.kartik.ridehailing.model.Ride;
import com.kartik.ridehailing.model.User;
import com.kartik.ridehailing.repository.DriverRepository;
import com.kartik.ridehailing.repository.RideRepository;
import com.kartik.ridehailing.repository.UserRepository;
import com.kartik.ridehailing.strategy.matching.DriverMatchingStrategy;
import com.kartik.ridehailing.strategy.pricing.PricingStrategy;
import com.kartik.ridehailing.util.DistanceCalculator;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class RideService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;
    private final DriverMatchingStrategy driverMatchingStrategy;
    private final PricingStrategy pricingStrategy;
    private final CouponService couponService;

    public RideService(UserRepository userRepository,
                       DriverRepository driverRepository,
                       RideRepository rideRepository,
                       DriverMatchingStrategy driverMatchingStrategy,
                       PricingStrategy pricingStrategy,
                       CouponService couponService) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
        this.driverMatchingStrategy = driverMatchingStrategy;
        this.pricingStrategy = pricingStrategy;
        this.couponService = couponService;
    }

    public Ride bookRide(String userId,
                         Location pickupLocation,
                         Location dropLocation,
                         CarType requestedCarType) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + userId));

        if (pickupLocation == null || dropLocation == null) {
            throw new IllegalArgumentException(
                    "Pickup and drop locations are required");
        }

        if (requestedCarType == null) {
            throw new IllegalArgumentException("Car type is required");
        }

        List<Driver> drivers = driverRepository.findAll();

        Driver driver = driverMatchingStrategy
                .findDriver(drivers, pickupLocation, requestedCarType)
                .orElseThrow(() ->
                        new IllegalArgumentException("No driver available"));

        CarType actualCarType = driver.getVehicle().getCarType();

        // Reserve the driver for this ride.
        driver.setStatus(DriverStatus.ON_RIDE);
        driverRepository.save(driver);

        Ride ride = new Ride(
                UUID.randomUUID().toString(),
                user,
                driver,
                pickupLocation,
                dropLocation,
                requestedCarType,
                actualCarType
        );

        rideRepository.save(ride);

        return ride;
    }

    public BigDecimal endRide(String rideId) {
        return endRide(rideId, null);
    }

    public BigDecimal endRide(String rideId, String couponCode) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ride not found: " + rideId));

        if (ride.getStatus() != RideStatus.ONGOING) {
            throw new IllegalArgumentException("Ride is already completed");
        }

        // Calculate trip distance using pickup and drop locations.
        double distance = DistanceCalculator.calculate(
                ride.getPickupLocation(),
                ride.getDropLocation()
        );

        // Calculate fare using the actual vehicle type.
        BigDecimal fare = pricingStrategy.calculateFare(
                distance,
                ride.getActualCarType()
        );

        // Apply coupon if provided.
        if (couponCode != null && !couponCode.isBlank()) {
            fare = couponService.applyCoupon(couponCode, fare);
        }

        // Complete the ride.
        ride.complete(fare);

        // Driver becomes available at the ride's drop location.
        Driver driver = ride.getDriver();
        driver.updateLocation(ride.getDropLocation());
        driver.setStatus(DriverStatus.AVAILABLE);

        driverRepository.save(driver);
        rideRepository.save(ride);

        return fare;
    }

    public List<Ride> getUserRideHistory(String userId) {
        return rideRepository.findByUserId(userId);
    }

    public List<Ride> getDriverRideHistory(String driverId) {
        return rideRepository.findByDriverId(driverId);
    }

    public Ride getRide(String rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ride not found: " + rideId));
    }
}
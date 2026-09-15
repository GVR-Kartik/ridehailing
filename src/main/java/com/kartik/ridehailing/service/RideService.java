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

    public RideService(
            UserRepository userRepository,
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

    public Ride bookRide(
            String userId,
            Location pickupLocation,
            Location dropLocation,
            CarType requestedCarType,
            String couponCode) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        ));

        if (pickupLocation == null || dropLocation == null) {
            throw new IllegalArgumentException(
                    "Pickup and drop locations are required"
            );
        }

        if (requestedCarType == null) {
            throw new IllegalArgumentException(
                    "Car type is required"
            );
        }

        /*
         * Validate and resolve the driver before changing driver state.
         */
        List<Driver> drivers = driverRepository.findAll();

        Driver driver = driverMatchingStrategy
                .findDriver(
                        drivers,
                        pickupLocation,
                        requestedCarType
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No driver available"
                        ));

        CarType actualCarType =
                driver.getVehicle().getCarType();

        /*
         * Calculate fare at booking time because the coupon
         * must be applied when the ride starts.
         */
        double distance = DistanceCalculator.calculate(
                pickupLocation,
                dropLocation
        );

        BigDecimal fare = pricingStrategy.calculateFare(
                distance,
                actualCarType
        );

        /*
         * Validate and apply coupon BEFORE reserving the driver.
         *
         * This prevents an invalid coupon from leaving the
         * selected driver stuck in ON_RIDE state.
         */
        if (couponCode != null && !couponCode.isBlank()) {
            fare = couponService.applyCoupon(
                    couponCode,
                    fare
            );
        }

        /*
         * Only reserve the driver after every booking validation
         * has succeeded.
         */
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

        ride.setFare(fare);

        rideRepository.save(ride);

        return ride;
    }

    /*
     * Convenience overload when no coupon is required.
     */
    public Ride bookRide(
            String userId,
            Location pickupLocation,
            Location dropLocation,
            CarType requestedCarType) {

        return bookRide(
                userId,
                pickupLocation,
                dropLocation,
                requestedCarType,
                null
        );
    }

    public BigDecimal endRide(String rideId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ride not found: " + rideId
                        ));

        if (ride.getStatus() != RideStatus.ONGOING) {
            throw new IllegalArgumentException(
                    "Ride is already completed"
            );
        }

        /*
         * Fare was already calculated when the ride started.
         * End ride only completes the ride and releases the driver.
         */
        ride.complete();

        Driver driver = ride.getDriver();

        driver.updateLocation(
                ride.getDropLocation()
        );

        driver.setStatus(
                DriverStatus.AVAILABLE
        );

        driverRepository.save(driver);
        rideRepository.save(ride);

        return ride.getFare();
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
                        new IllegalArgumentException(
                                "Ride not found: " + rideId
                        ));
    }
}
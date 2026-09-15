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
import com.kartik.ridehailing.strategy.cancellation.CancellationPolicy;
import com.kartik.ridehailing.strategy.cancellation.FixedCancellationPolicy;
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
    private final CancellationPolicy cancellationPolicy;

    public RideService(
            UserRepository userRepository,
            DriverRepository driverRepository,
            RideRepository rideRepository,
            DriverMatchingStrategy driverMatchingStrategy,
            PricingStrategy pricingStrategy,
            CouponService couponService) {

        this(
                userRepository,
                driverRepository,
                rideRepository,
                driverMatchingStrategy,
                pricingStrategy,
                couponService,
                new FixedCancellationPolicy(
                        BigDecimal.valueOf(20)
                )
        );
    }

    public RideService(
            UserRepository userRepository,
            DriverRepository driverRepository,
            RideRepository rideRepository,
            DriverMatchingStrategy driverMatchingStrategy,
            PricingStrategy pricingStrategy,
            CouponService couponService,
            CancellationPolicy cancellationPolicy) {

        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
        this.driverMatchingStrategy = driverMatchingStrategy;
        this.pricingStrategy = pricingStrategy;
        this.couponService = couponService;
        this.cancellationPolicy = cancellationPolicy;
    }

    public Ride bookRide(
            String userId,
            Location pickupLocation,
            Location dropLocation,
            CarType requestedCarType,
            String couponCode) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found: " + userId
                                )
                        );

        if (pickupLocation == null
                || dropLocation == null) {

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
         * A driver can be selected by the matching strategy
         * and then lost to another concurrent booking.
         *
         * We therefore retry matching when atomic reservation fails.
         */
        int maxAttempts =
                Math.max(
                        driverRepository.findAll().size(),
                        1
                );

        for (int attempt = 0;
             attempt < maxAttempts;
             attempt++) {

            List<Driver> drivers =
                    driverRepository.findAll();

            Driver candidate =
                    driverMatchingStrategy
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
                    candidate.getVehicle().getCarType();

            /*
             * Calculate the fare before reserving the driver.
             * This ensures invalid coupons never reserve a driver.
             */
            double distance =
                    DistanceCalculator.calculate(
                            pickupLocation,
                            dropLocation
                    );

            BigDecimal fare =
                    pricingStrategy.calculateFare(
                            distance,
                            actualCarType
                    );

            if (couponCode != null
                    && !couponCode.isBlank()) {

                fare = couponService.applyCoupon(
                        couponCode,
                        fare
                );
            }

            /*
             * Atomic AVAILABLE -> ON_RIDE transition.
             */
            boolean reserved =
                    driverRepository.reserveDriver(
                            candidate.getDriverId()
                    );

            if (!reserved) {
                continue;
            }

            Ride ride = new Ride(
                    UUID.randomUUID().toString(),
                    user,
                    candidate,
                    pickupLocation,
                    dropLocation,
                    requestedCarType,
                    actualCarType
            );

            ride.setFare(fare);

            rideRepository.save(ride);

            return ride;
        }

        throw new IllegalArgumentException(
                "No driver available"
        );
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

        Ride ride =
                rideRepository.findById(rideId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Ride not found: " + rideId
                                )
                        );

        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Ride is already completed"
            );
        }

        if (ride.getStatus() == RideStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Ride is already cancelled"
            );
        }

        /*
         * Fare was already calculated at booking time.
         */
        ride.complete();

        Driver driver =
                ride.getDriver();

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

    public BigDecimal cancelRide(String rideId) {

        Ride ride =
                rideRepository.findById(rideId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Ride not found: " + rideId
                                )
                        );

        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Completed ride cannot be cancelled"
            );
        }

        if (ride.getStatus() == RideStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Ride is already cancelled"
            );
        }

        BigDecimal cancellationFee =
                cancellationPolicy.calculateFee(ride);

        ride.cancel(cancellationFee);

        /*
         * A cancelled ride releases the driver.
         * The driver remains at the pickup/current location.
         */
        Driver driver =
                ride.getDriver();

        driver.setStatus(
                DriverStatus.AVAILABLE
        );

        driverRepository.save(driver);
        rideRepository.save(ride);

        return cancellationFee;
    }

    public List<Ride> getUserRideHistory(
            String userId) {

        return rideRepository.findByUserId(userId);
    }

    public List<Ride> getDriverRideHistory(
            String driverId) {

        return rideRepository.findByDriverId(driverId);
    }

    public Ride getRide(String rideId) {

        return rideRepository.findById(rideId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ride not found: " + rideId
                        )
                );
    }
}
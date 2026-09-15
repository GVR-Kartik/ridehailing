package com.kartik.ridehailing.service;

import com.kartik.ridehailing.enums.CarType;
import com.kartik.ridehailing.enums.DriverStatus;
import com.kartik.ridehailing.model.Location;
import com.kartik.ridehailing.model.Ride;
import com.kartik.ridehailing.model.User;
import com.kartik.ridehailing.model.Vehicle;
import com.kartik.ridehailing.repository.*;
import com.kartik.ridehailing.strategy.matching.DriverMatchingStrategy;
import com.kartik.ridehailing.strategy.matching.NearestDriverMatchingStrategy;
import com.kartik.ridehailing.strategy.pricing.PricingStrategy;
import com.kartik.ridehailing.strategy.pricing.TieredPricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RideServiceTest {

    private UserService userService;
    private DriverService driverService;
    private RideService rideService;

    @BeforeEach
    void setUp() {

        UserRepository userRepository =
                new InMemoryUserRepository();

        DriverRepository driverRepository =
                new InMemoryDriverRepository();

        RideRepository rideRepository =
                new InMemoryRideRepository();

        CouponRepository couponRepository =
                new InMemoryCouponRepository();

        userService = new UserService(userRepository);
        driverService = new DriverService(driverRepository);

        CouponService couponService =
                new CouponService(couponRepository);

        PricingStrategy pricingStrategy =
                new TieredPricingStrategy();

        DriverMatchingStrategy matchingStrategy =
                new NearestDriverMatchingStrategy();

        rideService = new RideService(
                userRepository,
                driverRepository,
                rideRepository,
                matchingStrategy,
                pricingStrategy,
                couponService
        );
    }

    @Test
    void shouldBookRideWithAvailableDriver() {

        User user = userService.registerUser(
                "U1",
                "Kartik"
        );

        driverService.registerDriver(
                "D1",
                "Rahul",
                new Vehicle("KA01", CarType.HATCHBACK),
                new Location(12.9716, 77.5946)
        );

        Ride ride = rideService.bookRide(
                "U1",
                new Location(12.9716, 77.5946),
                new Location(12.9352, 77.6245),
                CarType.HATCHBACK
        );

        assertNotNull(ride);
        assertEquals(CarType.HATCHBACK,
                ride.getActualCarType());

        assertEquals(
                DriverStatus.ON_RIDE,
                ride.getDriver().getStatus()
        );
    }

    @Test
    void shouldUpgradeHatchbackRequestToSedan() {

        userService.registerUser("U1", "Kartik");

        driverService.registerDriver(
                "D1",
                "Rahul",
                new Vehicle("KA01", CarType.SEDAN),
                new Location(12.9716, 77.5946)
        );

        Ride ride = rideService.bookRide(
                "U1",
                new Location(12.9716, 77.5946),
                new Location(12.9352, 77.6245),
                CarType.HATCHBACK
        );

        assertEquals(
                CarType.HATCHBACK,
                ride.getRequestedCarType()
        );

        assertEquals(
                CarType.SEDAN,
                ride.getActualCarType()
        );
    }

    @Test
    void shouldRejectRideWhenNoDriverAvailable() {

        userService.registerUser("U1", "Kartik");

        assertThrows(
                IllegalArgumentException.class,
                () -> rideService.bookRide(
                        "U1",
                        new Location(12.9716, 77.5946),
                        new Location(12.9352, 77.6245),
                        CarType.HATCHBACK
                )
        );
    }

    @Test
    void shouldEndRideAndCalculateFare() {

        userService.registerUser("U1", "Kartik");

        driverService.registerDriver(
                "D1",
                "Rahul",
                new Vehicle("KA01", CarType.HATCHBACK),
                new Location(12.9716, 77.5946)
        );

        Ride ride = rideService.bookRide(
                "U1",
                new Location(12.9716, 77.5946),
                new Location(12.9352, 77.6245),
                CarType.HATCHBACK
        );

        BigDecimal fare =
                rideService.endRide(ride.getRideId());

        assertNotNull(fare);
        assertTrue(fare.compareTo(BigDecimal.valueOf(50)) >= 0);

        assertEquals(
                com.kartik.ridehailing.enums.RideStatus.COMPLETED,
                ride.getStatus()
        );

        assertEquals(
                DriverStatus.AVAILABLE,
                ride.getDriver().getStatus()
        );
    }
}
package com.kartik.ridehailing.service;

import com.kartik.ridehailing.enums.CarType;
import com.kartik.ridehailing.enums.DriverStatus;
import com.kartik.ridehailing.enums.RideStatus;
import com.kartik.ridehailing.model.Driver;
import com.kartik.ridehailing.model.Location;
import com.kartik.ridehailing.model.Ride;
import com.kartik.ridehailing.model.User;
import com.kartik.ridehailing.model.Vehicle;
import com.kartik.ridehailing.repository.DriverRepository;
import com.kartik.ridehailing.repository.InMemoryDriverRepository;
import com.kartik.ridehailing.repository.InMemoryRideRepository;
import com.kartik.ridehailing.repository.InMemoryUserRepository;
import com.kartik.ridehailing.repository.RideRepository;
import com.kartik.ridehailing.repository.UserRepository;
import com.kartik.ridehailing.strategy.matching.DriverMatchingStrategy;
import com.kartik.ridehailing.strategy.matching.NearestDriverMatchingStrategy;
import com.kartik.ridehailing.strategy.pricing.PricingStrategy;
import com.kartik.ridehailing.strategy.pricing.TieredPricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RideServiceTest {

    private UserRepository userRepository;
    private DriverRepository driverRepository;
    private RideRepository rideRepository;

    private UserService userService;
    private DriverService driverService;
    private CouponService couponService;
    private RideService rideService;

    private final Location pickup =
            new Location(12.9716, 77.5946);

    private final Location drop =
            new Location(12.9352, 77.6245);

    @BeforeEach
    void setUp() {

        userRepository =
                new InMemoryUserRepository();

        driverRepository =
                new InMemoryDriverRepository();

        rideRepository =
                new InMemoryRideRepository();

        couponService =
                new CouponService(
                        new com.kartik.ridehailing.repository
                                .InMemoryCouponRepository()
                );

        userService =
                new UserService(userRepository);

        driverService =
                new DriverService(driverRepository);

        DriverMatchingStrategy matchingStrategy =
                new NearestDriverMatchingStrategy();

        PricingStrategy pricingStrategy =
                new TieredPricingStrategy(
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(8),
                        BigDecimal.valueOf(5),
                        BigDecimal.valueOf(12),
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(7)
                );

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

        User user =
                userService.registerUser(
                        "U1",
                        "Kartik"
                );

        Driver driver =
                driverService.registerDriver(
                        "D1",
                        "Rahul",
                        new Vehicle(
                                "KA01AB1234",
                                CarType.HATCHBACK
                        ),
                        pickup
                );

        Ride ride =
                rideService.bookRide(
                        user.getUserId(),
                        pickup,
                        drop,
                        CarType.HATCHBACK
                );

        assertNotNull(ride);

        assertEquals(
                user.getUserId(),
                ride.getUser().getUserId()
        );

        assertEquals(
                driver.getDriverId(),
                ride.getDriver().getDriverId()
        );

        assertEquals(
                CarType.HATCHBACK,
                ride.getRequestedCarType()
        );

        assertEquals(
                CarType.HATCHBACK,
                ride.getActualCarType()
        );

        assertEquals(
                RideStatus.ONGOING,
                ride.getStatus()
        );

        assertNotNull(ride.getFare());

        assertEquals(
                DriverStatus.ON_RIDE,
                driverRepository
                        .findById("D1")
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void shouldUpgradeHatchbackRequestToSedanWhenHatchbackUnavailable() {

        User user =
                userService.registerUser(
                        "U1",
                        "Kartik"
                );

        Driver sedan =
                driverService.registerDriver(
                        "D1",
                        "Rahul",
                        new Vehicle(
                                "KA01AB1234",
                                CarType.SEDAN
                        ),
                        pickup
                );

        Ride ride =
                rideService.bookRide(
                        user.getUserId(),
                        pickup,
                        drop,
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

        assertEquals(
                sedan.getDriverId(),
                ride.getDriver().getDriverId()
        );
    }

    @Test
    void shouldNotBookRideWhenNoDriverIsAvailable() {

        User user =
                userService.registerUser(
                        "U1",
                        "Kartik"
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> rideService.bookRide(
                                user.getUserId(),
                                pickup,
                                drop,
                                CarType.HATCHBACK
                        )
                );

        assertEquals(
                "No driver available",
                exception.getMessage()
        );
    }

    @Test
    void shouldEndRideAndMakeDriverAvailable() {

        User user =
                userService.registerUser(
                        "U1",
                        "Kartik"
                );

        driverService.registerDriver(
                "D1",
                "Rahul",
                new Vehicle(
                        "KA01AB1234",
                        CarType.HATCHBACK
                ),
                pickup
        );

        Ride ride =
                rideService.bookRide(
                        user.getUserId(),
                        pickup,
                        drop,
                        CarType.HATCHBACK
                );

        BigDecimal fare =
                rideService.endRide(
                        ride.getRideId()
                );

        assertNotNull(fare);

        assertTrue(
                fare.compareTo(BigDecimal.valueOf(50)) >= 0
        );

        Ride completedRide =
                rideService.getRide(
                        ride.getRideId()
                );

        assertEquals(
                RideStatus.COMPLETED,
                completedRide.getStatus()
        );

        assertEquals(
                fare,
                completedRide.getFare()
        );

        Driver driver =
                driverRepository
                        .findById("D1")
                        .orElseThrow();

        assertEquals(
                DriverStatus.AVAILABLE,
                driver.getStatus()
        );
    }

    @Test
    void shouldUpdateDriverLocationToDropLocationAfterRide() {

        User user =
                userService.registerUser(
                        "U1",
                        "Kartik"
                );

        driverService.registerDriver(
                "D1",
                "Rahul",
                new Vehicle(
                        "KA01AB1234",
                        CarType.HATCHBACK
                ),
                pickup
        );

        Ride ride =
                rideService.bookRide(
                        user.getUserId(),
                        pickup,
                        drop,
                        CarType.HATCHBACK
                );

        rideService.endRide(
                ride.getRideId()
        );

        Driver driver =
                driverRepository
                        .findById("D1")
                        .orElseThrow();

        assertEquals(
                drop.getLatitude(),
                driver.getCurrentLocation().getLatitude()
        );

        assertEquals(
                drop.getLongitude(),
                driver.getCurrentLocation().getLongitude()
        );
    }

    @Test
    void shouldReturnOngoingRideInUserHistory() {

        User user =
                userService.registerUser(
                        "U1",
                        "Kartik"
                );

        driverService.registerDriver(
                "D1",
                "Rahul",
                new Vehicle(
                        "KA01AB1234",
                        CarType.HATCHBACK
                ),
                pickup
        );

        Ride ride =
                rideService.bookRide(
                        user.getUserId(),
                        pickup,
                        drop,
                        CarType.HATCHBACK
                );

        List<Ride> history =
                rideService.getUserRideHistory(
                        user.getUserId()
                );

        assertEquals(1, history.size());

        assertEquals(
                ride.getRideId(),
                history.get(0).getRideId()
        );

        assertEquals(
                RideStatus.ONGOING,
                history.get(0).getStatus()
        );
    }

    @Test
    void shouldReturnCompletedRideInUserHistory() {

        User user =
                userService.registerUser(
                        "U1",
                        "Kartik"
                );

        driverService.registerDriver(
                "D1",
                "Rahul",
                new Vehicle(
                        "KA01AB1234",
                        CarType.HATCHBACK
                ),
                pickup
        );

        Ride ride =
                rideService.bookRide(
                        user.getUserId(),
                        pickup,
                        drop,
                        CarType.HATCHBACK
                );

        rideService.endRide(
                ride.getRideId()
        );

        List<Ride> history =
                rideService.getUserRideHistory(
                        user.getUserId()
                );

        assertEquals(1, history.size());

        assertEquals(
                RideStatus.COMPLETED,
                history.get(0).getStatus()
        );
    }

    @Test
    void shouldReturnDriverRideHistory() {

        User user =
                userService.registerUser(
                        "U1",
                        "Kartik"
                );

        Driver driver =
                driverService.registerDriver(
                        "D1",
                        "Rahul",
                        new Vehicle(
                                "KA01AB1234",
                                CarType.HATCHBACK
                        ),
                        pickup
                );

        Ride ride =
                rideService.bookRide(
                        user.getUserId(),
                        pickup,
                        drop,
                        CarType.HATCHBACK
                );

        rideService.endRide(
                ride.getRideId()
        );

        List<Ride> history =
                rideService.getDriverRideHistory(
                        driver.getDriverId()
                );

        assertEquals(1, history.size());

        assertEquals(
                ride.getRideId(),
                history.get(0).getRideId()
        );
    }

    @Test
    void shouldNotAllowCompletingRideTwice() {

        User user =
                userService.registerUser(
                        "U1",
                        "Kartik"
                );

        driverService.registerDriver(
                "D1",
                "Rahul",
                new Vehicle(
                        "KA01AB1234",
                        CarType.HATCHBACK
                ),
                pickup
        );

        Ride ride =
                rideService.bookRide(
                        user.getUserId(),
                        pickup,
                        drop,
                        CarType.HATCHBACK
                );

        rideService.endRide(
                ride.getRideId()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> rideService.endRide(
                                ride.getRideId()
                        )
                );

        assertEquals(
                "Ride is already completed",
                exception.getMessage()
        );
    }

    @Test
    void shouldApplyCouponWhenStartingRide() {

        User user =
                userService.registerUser(
                        "U1",
                        "Kartik"
                );

        driverService.registerDriver(
                "D1",
                "Rahul",
                new Vehicle(
                        "KA01AB1234",
                        CarType.HATCHBACK
                ),
                pickup
        );

        couponService.addCoupon(
                "SAVE10",
                BigDecimal.TEN
        );

        Ride ride =
                rideService.bookRide(
                        user.getUserId(),
                        pickup,
                        drop,
                        CarType.HATCHBACK,
                        "SAVE10"
                );

        assertNotNull(ride.getFare());

        /*
         * The fare must already contain the discount
         * while the ride is still ONGOING.
         */
        assertEquals(
                RideStatus.ONGOING,
                ride.getStatus()
        );

        BigDecimal baseFare =
                new TieredPricingStrategy(
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(8),
                        BigDecimal.valueOf(5),
                        BigDecimal.valueOf(12),
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(7)
                ).calculateFare(
                        com.kartik.ridehailing.util.DistanceCalculator.calculate(
                                pickup,
                                drop
                        ),
                        CarType.HATCHBACK
                );

        BigDecimal expectedFare =
                baseFare
                        .multiply(
                                BigDecimal.valueOf(90)
                                        .divide(
                                                BigDecimal.valueOf(100)
                                        )
                        )
                        .setScale(
                                2,
                                java.math.RoundingMode.HALF_UP
                        );

        assertEquals(
                expectedFare,
                ride.getFare()
        );
    }

    @Test
    void shouldRejectInvalidCouponWithoutReservingDriver() {

        User user =
                userService.registerUser(
                        "U1",
                        "Kartik"
                );

        driverService.registerDriver(
                "D1",
                "Rahul",
                new Vehicle(
                        "KA01AB1234",
                        CarType.HATCHBACK
                ),
                pickup
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> rideService.bookRide(
                        user.getUserId(),
                        pickup,
                        drop,
                        CarType.HATCHBACK,
                        "INVALID"
                )
        );

        Driver driver =
                driverRepository
                        .findById("D1")
                        .orElseThrow();

        assertEquals(
                DriverStatus.AVAILABLE,
                driver.getStatus()
        );
    }

    @Test
    void shouldNotBookRideWithDriverOutsideRadius() {

        User user =
                userService.registerUser(
                        "U1",
                        "Kartik"
                );

        driverService.registerDriver(
                "D1",
                "Rahul",
                new Vehicle(
                        "KA01AB1234",
                        CarType.HATCHBACK
                ),
                new Location(
                        13.10,
                        77.70
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> rideService.bookRide(
                        user.getUserId(),
                        pickup,
                        drop,
                        CarType.HATCHBACK
                )
        );
    }
}
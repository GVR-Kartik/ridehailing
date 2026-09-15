package com.kartik.ridehailing.strategy.matching;

import com.kartik.ridehailing.enums.CarType;
import com.kartik.ridehailing.model.Driver;
import com.kartik.ridehailing.model.Location;
import com.kartik.ridehailing.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NearestDriverMatchingStrategyTest {

    private NearestDriverMatchingStrategy strategy;

    private final Location pickupLocation =
            new Location(12.9716, 77.5946);

    @BeforeEach
    void setUp() {
        strategy = new NearestDriverMatchingStrategy();
    }

    @Test
    void shouldSelectNearestHatchbackWhenHatchbackIsAvailable() {
        Driver nearbySedan = driver(
                "D1",
                CarType.SEDAN,
                new Location(12.9720, 77.5950)
        );

        Driver hatchback = driver(
                "D2",
                CarType.HATCHBACK,
                new Location(12.9730, 77.5960)
        );

        Optional<Driver> result = strategy.findDriver(
                List.of(nearbySedan, hatchback),
                pickupLocation,
                CarType.HATCHBACK
        );

        assertTrue(result.isPresent());
        assertEquals("D2", result.get().getDriverId());
        assertEquals(CarType.HATCHBACK,
                result.get().getVehicle().getCarType());
    }

    @Test
    void shouldUpgradeToSedanWhenHatchbackIsUnavailable() {
        Driver sedan = driver(
                "D1",
                CarType.SEDAN,
                new Location(12.9720, 77.5950)
        );

        Optional<Driver> result = strategy.findDriver(
                List.of(sedan),
                pickupLocation,
                CarType.HATCHBACK
        );

        assertTrue(result.isPresent());
        assertEquals("D1", result.get().getDriverId());
        assertEquals(CarType.SEDAN,
                result.get().getVehicle().getCarType());
    }

    @Test
    void shouldNotMatchHatchbackForSedanRequest() {
        Driver hatchback = driver(
                "D1",
                CarType.HATCHBACK,
                new Location(12.9720, 77.5950)
        );

        Optional<Driver> result = strategy.findDriver(
                List.of(hatchback),
                pickupLocation,
                CarType.SEDAN
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIgnoreDriverOutsideRadius() {
        Driver farAwayDriver = driver(
                "D1",
                CarType.HATCHBACK,
                new Location(13.10, 77.70)
        );

        Optional<Driver> result = strategy.findDriver(
                List.of(farAwayDriver),
                pickupLocation,
                CarType.HATCHBACK
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSelectNearestDriverWhenMultipleSameTypeDriversAreAvailable() {
        Driver fartherDriver = driver(
                "D1",
                CarType.HATCHBACK,
                new Location(12.9800, 77.6000)
        );

        Driver nearerDriver = driver(
                "D2",
                CarType.HATCHBACK,
                new Location(12.9720, 77.5950)
        );

        Optional<Driver> result = strategy.findDriver(
                List.of(fartherDriver, nearerDriver),
                pickupLocation,
                CarType.HATCHBACK
        );

        assertTrue(result.isPresent());
        assertEquals("D2", result.get().getDriverId());
    }

    private Driver driver(
            String driverId,
            CarType carType,
            Location location) {

        return new Driver(
                driverId,
                "Driver " + driverId,
                new Vehicle("KA01" + driverId, carType),
                location
        );
    }
}
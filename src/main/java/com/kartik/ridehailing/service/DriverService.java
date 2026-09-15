package com.kartik.ridehailing.service;

import com.kartik.ridehailing.enums.DriverStatus;
import com.kartik.ridehailing.model.Driver;
import com.kartik.ridehailing.model.Location;
import com.kartik.ridehailing.model.Vehicle;
import com.kartik.ridehailing.repository.DriverRepository;

import java.util.List;

public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Driver registerDriver(
            String driverId,
            String name,
            Vehicle vehicle,
            Location currentLocation) {

        if (driverId == null || driverId.isBlank()) {
            throw new IllegalArgumentException("Driver ID cannot be empty");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Driver name cannot be empty");
        }

        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }

        if (currentLocation == null) {
            throw new IllegalArgumentException("Driver location cannot be null");
        }

        if (driverRepository.findById(driverId).isPresent()) {
            throw new IllegalArgumentException("Driver already exists: " + driverId);
        }

        Driver driver = new Driver(
                driverId,
                name,
                vehicle,
                currentLocation
        );

        driverRepository.save(driver);

        return driver;
    }

    public Driver getDriver(String driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Driver not found: " + driverId));
    }

    public void updateDriverLocation(String driverId, Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }

        Driver driver = getDriver(driverId);
        driver.updateLocation(location);
        driverRepository.save(driver);
    }

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    public void markAvailable(String driverId) {
        Driver driver = getDriver(driverId);
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepository.save(driver);
    }

    public void markOnRide(String driverId) {
        Driver driver = getDriver(driverId);
        driver.setStatus(DriverStatus.ON_RIDE);
        driverRepository.save(driver);
    }
}
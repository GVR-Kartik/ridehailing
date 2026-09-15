package com.kartik.ridehailing.repository;

import com.kartik.ridehailing.enums.DriverStatus;
import com.kartik.ridehailing.model.Driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryDriverRepository implements DriverRepository {

    private final Map<String, Driver> drivers = new HashMap<>();

    @Override
    public synchronized void save(Driver driver) {

        drivers.put(
                driver.getDriverId(),
                driver
        );
    }

    @Override
    public synchronized Optional<Driver> findById(
            String driverId) {

        return Optional.ofNullable(
                drivers.get(driverId)
        );
    }

    @Override
    public synchronized List<Driver> findAll() {

        return new ArrayList<>(
                drivers.values()
        );
    }

    @Override
    public synchronized boolean reserveDriver(
            String driverId) {

        Driver driver = drivers.get(driverId);

        if (driver == null) {
            return false;
        }

        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            return false;
        }

        driver.setStatus(DriverStatus.ON_RIDE);

        return true;
    }
}
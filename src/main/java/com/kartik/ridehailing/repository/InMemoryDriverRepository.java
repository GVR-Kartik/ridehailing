package com.kartik.ridehailing.repository;

import com.kartik.ridehailing.model.Driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryDriverRepository implements DriverRepository {

    private final Map<String, Driver> drivers = new HashMap<>();

    @Override
    public void save(Driver driver) {
        drivers.put(driver.getDriverId(), driver);
    }

    @Override
    public Optional<Driver> findById(String driverId) {
        return Optional.ofNullable(drivers.get(driverId));
    }

    @Override
    public List<Driver> findAll() {
        return new ArrayList<>(drivers.values());
    }
}
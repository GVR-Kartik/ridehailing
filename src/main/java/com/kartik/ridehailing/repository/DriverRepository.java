package com.kartik.ridehailing.repository;

import com.kartik.ridehailing.model.Driver;

import java.util.List;
import java.util.Optional;

public interface DriverRepository {

    void save(Driver driver);

    Optional<Driver> findById(String driverId);

    List<Driver> findAll();

    /*
     * Atomically changes AVAILABLE -> ON_RIDE.
     *
     * Returns true when reservation succeeds.
     * Returns false when the driver was already reserved.
     */
    boolean reserveDriver(String driverId);
}
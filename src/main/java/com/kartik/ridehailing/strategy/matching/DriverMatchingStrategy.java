package com.kartik.ridehailing.strategy.matching;

import com.kartik.ridehailing.enums.CarType;
import com.kartik.ridehailing.model.Driver;
import com.kartik.ridehailing.model.Location;

import java.util.List;
import java.util.Optional;

public interface DriverMatchingStrategy {

    Optional<Driver> findDriver(
            List<Driver> drivers,
            Location pickupLocation,
            CarType requestedCarType
    );
}
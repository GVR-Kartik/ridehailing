package com.kartik.ridehailing.repository;

import com.kartik.ridehailing.model.Ride;

import java.util.List;
import java.util.Optional;

public interface RideRepository {

    void save(Ride ride);

    Optional<Ride> findById(String rideId);

    List<Ride> findAll();

    List<Ride> findByUserId(String userId);

    List<Ride> findByDriverId(String driverId);
}
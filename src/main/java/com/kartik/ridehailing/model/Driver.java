package com.kartik.ridehailing.model;

import com.kartik.ridehailing.enums.DriverStatus;

public class Driver {

    private final String driverId;
    private final String name;
    private final Vehicle vehicle;

    private Location currentLocation;
    private DriverStatus status;

    public Driver(String driverId, String name, Vehicle vehicle, Location currentLocation) {
        this.driverId = driverId;
        this.name = name;
        this.vehicle = vehicle;
        this.currentLocation = currentLocation;
        this.status = DriverStatus.AVAILABLE;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getName() {
        return name;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void updateLocation(Location location) {
        this.currentLocation = location;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }
}
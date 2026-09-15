package com.kartik.ridehailing.model;

import com.kartik.ridehailing.enums.CarType;

public class Vehicle {

    private final String vehicleNumber;
    private final CarType carType;

    public Vehicle(String vehicleNumber, CarType carType) {
        this.vehicleNumber = vehicleNumber;
        this.carType = carType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public CarType getCarType() {
        return carType;
    }
}
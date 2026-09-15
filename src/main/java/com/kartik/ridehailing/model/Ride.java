package com.kartik.ridehailing.model;

import com.kartik.ridehailing.enums.CarType;
import com.kartik.ridehailing.enums.RideStatus;

import java.math.BigDecimal;

public class Ride {

    private final String rideId;
    private final User user;
    private final Driver driver;

    private final Location pickupLocation;
    private final Location dropLocation;

    private final CarType requestedCarType;
    private final CarType actualCarType;

    private RideStatus status;
    private BigDecimal fare;
    private BigDecimal cancellationFee;

    public Ride(
            String rideId,
            User user,
            Driver driver,
            Location pickupLocation,
            Location dropLocation,
            CarType requestedCarType,
            CarType actualCarType) {

        this.rideId = rideId;
        this.user = user;
        this.driver = driver;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.requestedCarType = requestedCarType;
        this.actualCarType = actualCarType;
        this.status = RideStatus.ONGOING;
    }

    public String getRideId() {
        return rideId;
    }

    /*
     * Alias used by the CLI.
     */
    public String getId() {
        return rideId;
    }

    public User getUser() {
        return user;
    }

    public Driver getDriver() {
        return driver;
    }

    public Location getPickupLocation() {
        return pickupLocation;
    }

    public Location getDropLocation() {
        return dropLocation;
    }

    public CarType getRequestedCarType() {
        return requestedCarType;
    }

    public CarType getActualCarType() {
        return actualCarType;
    }

    public RideStatus getStatus() {
        return status;
    }

    public BigDecimal getFare() {
        return fare;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }

    /*
     * Fare is finalized at booking time because the coupon
     * is applied when the ride starts.
     */
    public void setFare(BigDecimal fare) {

        if (fare == null) {
            throw new IllegalArgumentException(
                    "Fare cannot be null"
            );
        }

        this.fare = fare;
    }

    public void complete() {

        if (status != RideStatus.ONGOING) {
            throw new IllegalStateException(
                    "Only an ongoing ride can be completed"
            );
        }

        this.status = RideStatus.COMPLETED;
    }

    public void cancel(BigDecimal cancellationFee) {

        if (status != RideStatus.ONGOING) {
            throw new IllegalStateException(
                    "Only an ongoing ride can be cancelled"
            );
        }

        if (cancellationFee == null
                || cancellationFee.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Cancellation fee cannot be negative"
            );
        }

        this.cancellationFee = cancellationFee;
        this.status = RideStatus.CANCELLED;
    }
}
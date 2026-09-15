package com.kartik.ridehailing;

import com.kartik.ridehailing.enums.CarType;
import com.kartik.ridehailing.model.Location;
import com.kartik.ridehailing.model.Ride;
import com.kartik.ridehailing.model.User;
import com.kartik.ridehailing.model.Vehicle;
import com.kartik.ridehailing.repository.CouponRepository;
import com.kartik.ridehailing.repository.DriverRepository;
import com.kartik.ridehailing.repository.InMemoryCouponRepository;
import com.kartik.ridehailing.repository.InMemoryDriverRepository;
import com.kartik.ridehailing.repository.InMemoryRideRepository;
import com.kartik.ridehailing.repository.InMemoryUserRepository;
import com.kartik.ridehailing.repository.RideRepository;
import com.kartik.ridehailing.repository.UserRepository;
import com.kartik.ridehailing.repository.DriverRepository;
import com.kartik.ridehailing.service.CouponService;
import com.kartik.ridehailing.service.DriverService;
import com.kartik.ridehailing.service.RideService;
import com.kartik.ridehailing.service.UserService;
import com.kartik.ridehailing.strategy.matching.DriverMatchingStrategy;
import com.kartik.ridehailing.strategy.matching.NearestDriverMatchingStrategy;
import com.kartik.ridehailing.strategy.pricing.PricingStrategy;
import com.kartik.ridehailing.strategy.pricing.TieredPricingStrategy;

import java.math.BigDecimal;

public class Main {

    public static void main(String[] args) {

        UserRepository userRepository =
                new InMemoryUserRepository();

        DriverRepository driverRepository =
                new InMemoryDriverRepository();

        RideRepository rideRepository =
                new InMemoryRideRepository();

        CouponRepository couponRepository =
                new InMemoryCouponRepository();

        UserService userService =
                new UserService(userRepository);

        DriverService driverService =
                new DriverService(driverRepository);

        CouponService couponService =
                new CouponService(couponRepository);

        PricingStrategy pricingStrategy =
                new TieredPricingStrategy();

        DriverMatchingStrategy matchingStrategy =
                new NearestDriverMatchingStrategy();

        RideService rideService =
                new RideService(
                        userRepository,
                        driverRepository,
                        rideRepository,
                        matchingStrategy,
                        pricingStrategy,
                        couponService
                );

        User user = userService.registerUser(
                "U1",
                "Kartik"
        );

        driverService.registerDriver(
                "D1",
                "Rahul",
                new Vehicle(
                        "KA01AB1234",
                        CarType.HATCHBACK
                ),
                new Location(
                        12.9716,
                        77.5946
                )
        );

        couponService.addCoupon(
                "SAVE10",
                BigDecimal.TEN
        );

        Ride ride = rideService.bookRide(
                user.getUserId(),
                new Location(12.9716, 77.5946),
                new Location(12.9352, 77.6245),
                CarType.HATCHBACK
        );

        BigDecimal fare =
                rideService.endRide(
                        ride.getRideId(),
                        "SAVE10"
                );

        System.out.println("Ride completed");
        System.out.println("Ride ID: " + ride.getRideId());
        System.out.println("Driver: " + ride.getDriver().getName());
        System.out.println("Requested Car: "
                + ride.getRequestedCarType());
        System.out.println("Actual Car: "
                + ride.getActualCarType());
        System.out.println("Final Fare: ₹" + fare);
    }
}
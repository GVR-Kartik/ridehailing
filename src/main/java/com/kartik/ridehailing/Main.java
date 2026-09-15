package com.kartik.ridehailing;

import com.kartik.ridehailing.enums.CarType;
import com.kartik.ridehailing.model.Coupon;
import com.kartik.ridehailing.model.Driver;
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
import com.kartik.ridehailing.service.CouponService;
import com.kartik.ridehailing.service.DriverService;
import com.kartik.ridehailing.service.RideService;
import com.kartik.ridehailing.service.UserService;
import com.kartik.ridehailing.strategy.cancellation.FixedCancellationPolicy;
import com.kartik.ridehailing.strategy.matching.DriverMatchingStrategy;
import com.kartik.ridehailing.strategy.matching.NearestDriverMatchingStrategy;
import com.kartik.ridehailing.strategy.pricing.PricingStrategy;
import com.kartik.ridehailing.strategy.pricing.TieredPricingStrategy;
import com.kartik.ridehailing.strategy.surge.DemandSupplySurgeMultiplierProvider;
import com.kartik.ridehailing.strategy.surge.SurgePricingStrategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {

    private final Scanner scanner =
            new Scanner(System.in);

    private final UserRepository userRepository =
            new InMemoryUserRepository();

    private final DriverRepository driverRepository =
            new InMemoryDriverRepository();

    private final RideRepository rideRepository =
            new InMemoryRideRepository();

    private final CouponRepository couponRepository =
            new InMemoryCouponRepository();

    private final UserService userService =
            new UserService(userRepository);

    private final DriverService driverService =
            new DriverService(driverRepository);

    private final CouponService couponService =
            new CouponService(couponRepository);

    private final DemandSupplySurgeMultiplierProvider
            surgeMultiplierProvider =
            new DemandSupplySurgeMultiplierProvider();

    private final RideService rideService;

    public Main() {

        PricingStrategy pricingStrategy =
                createPricingStrategy();

        DriverMatchingStrategy matchingStrategy =
                new NearestDriverMatchingStrategy();

        rideService =
                new RideService(
                        userRepository,
                        driverRepository,
                        rideRepository,
                        matchingStrategy,
                        pricingStrategy,
                        couponService,
                        new FixedCancellationPolicy(
                                BigDecimal.valueOf(20)
                        )
                );
    }

    public static void main(String[] args) {

        Main application =
                new Main();

        application.start();
    }

    private PricingStrategy createPricingStrategy() {

        /*
         * Base pricing:
         *
         *              0-2 km    2-5 km    >5 km
         * Hatchback     ₹10        ₹8        ₹5
         * Sedan         ₹12       ₹10        ₹7
         *
         * Minimum fare = ₹50
         */

        BigDecimal hatchbackFirstTierRate =
                BigDecimal.valueOf(10);

        BigDecimal hatchbackSecondTierRate =
                BigDecimal.valueOf(8);

        BigDecimal hatchbackThirdTierRate =
                BigDecimal.valueOf(5);

        BigDecimal sedanFirstTierRate =
                BigDecimal.valueOf(12);

        BigDecimal sedanSecondTierRate =
                BigDecimal.valueOf(10);

        BigDecimal sedanThirdTierRate =
                BigDecimal.valueOf(7);

        PricingStrategy basePricing =
                new TieredPricingStrategy(
                        hatchbackFirstTierRate,
                        hatchbackSecondTierRate,
                        hatchbackThirdTierRate,
                        sedanFirstTierRate,
                        sedanSecondTierRate,
                        sedanThirdTierRate
                );

        printPricingConfiguration(
                hatchbackFirstTierRate,
                hatchbackSecondTierRate,
                hatchbackThirdTierRate,
                sedanFirstTierRate,
                sedanSecondTierRate,
                sedanThirdTierRate
        );

        return new SurgePricingStrategy(
                basePricing,
                surgeMultiplierProvider
        );
    }

    private void printPricingConfiguration(
            BigDecimal hatchbackFirstTierRate,
            BigDecimal hatchbackSecondTierRate,
            BigDecimal hatchbackThirdTierRate,
            BigDecimal sedanFirstTierRate,
            BigDecimal sedanSecondTierRate,
            BigDecimal sedanThirdTierRate) {

        System.out.println();
        System.out.println("========================================");
        System.out.println("          PRICING CONFIGURATION");
        System.out.println("========================================");

        System.out.printf(
                "%-15s %-10s %-10s %-10s%n",
                "Car Type",
                "0-2 km",
                "2-5 km",
                ">5 km"
        );

        System.out.printf(
                "%-15s ₹%-9s ₹%-9s ₹%-9s%n",
                "Hatchback",
                hatchbackFirstTierRate,
                hatchbackSecondTierRate,
                hatchbackThirdTierRate
        );

        System.out.printf(
                "%-15s ₹%-9s ₹%-9s ₹%-9s%n",
                "Sedan",
                sedanFirstTierRate,
                sedanSecondTierRate,
                sedanThirdTierRate
        );

        System.out.println("----------------------------------------");
        System.out.println("Minimum Fare: ₹50");
        System.out.println(
                "Initial Surge Multiplier: "
                        + surgeMultiplierProvider.getMultiplier()
                        + "x"
        );
        System.out.println("Cancellation Fee: ₹20");
        System.out.println("========================================");
        System.out.println();
    }

    private void start() {

        System.out.println("========================================");
        System.out.println("       RIDE HAILING SERVICE");
        System.out.println("========================================");

        while (true) {

            printMenu();

            String choice =
                    scanner.nextLine().trim();

            try {

                switch (choice) {

                    case "1":
                        registerUser();
                        break;

                    case "2":
                        registerDriver();
                        break;

                    case "3":
                        updateDriverLocation();
                        break;

                    case "4":
                        bookRide();
                        break;

                    case "5":
                        endRide();
                        break;

                    case "6":
                        cancelRide();
                        break;

                    case "7":
                        viewUserRideHistory();
                        break;

                    case "8":
                        viewDriverRideHistory();
                        break;

                    case "9":
                        addCoupon();
                        break;

                    case "10":
                        deleteCoupon();
                        break;

                    case "11":
                        viewCoupons();
                        break;

                    case "12":
                        updateSurgeDemandAndSupply();
                        break;

                    case "0":
                        System.out.println(
                                "Exiting application..."
                        );
                        return;

                    default:
                        System.out.println(
                                "Invalid option."
                        );
                }

            } catch (Exception e) {

                System.out.println(
                        "Error: " + e.getMessage()
                );
            }

            System.out.println();
        }
    }

    private void printMenu() {

        System.out.println();
        System.out.println("------------- MENU ----------------");
        System.out.println("1. Register User");
        System.out.println("2. Register Driver");
        System.out.println("3. Update Driver Location");
        System.out.println("4. Book Ride");
        System.out.println("5. End Ride");
        System.out.println("6. Cancel Ride");
        System.out.println("7. View User Ride History");
        System.out.println("8. View Driver Ride History");
        System.out.println("9. Add Coupon");
        System.out.println("10. Delete Coupon");
        System.out.println("11. View Coupons");
        System.out.println("12. Update Surge Demand/Supply");
        System.out.println("0. Exit");
        System.out.println("-----------------------------------");
        System.out.print("Enter choice: ");
    }

    private void registerUser() {

        System.out.print("Enter user ID: ");
        String id = scanner.nextLine();

        System.out.print("Enter user name: ");
        String name = scanner.nextLine();

        User user =
                userService.registerUser(
                        id,
                        name
                );

        System.out.println(
                "User registered successfully: "
                        + user.getUserId()
        );
    }

    private void registerDriver() {

        System.out.print("Enter driver ID: ");
        String id = scanner.nextLine();

        System.out.print("Enter driver name: ");
        String name = scanner.nextLine();

        System.out.print("Enter vehicle number: ");
        String vehicleNumber =
                scanner.nextLine();

        CarType carType =
                readCarType();

        System.out.print(
                "Enter current latitude: "
        );

        double latitude =
                Double.parseDouble(
                        scanner.nextLine()
                );

        System.out.print(
                "Enter current longitude: "
        );

        double longitude =
                Double.parseDouble(
                        scanner.nextLine()
                );

        Vehicle vehicle =
                new Vehicle(
                        vehicleNumber,
                        carType
                );

        Location location =
                new Location(
                        latitude,
                        longitude
                );

        Driver driver =
                driverService.registerDriver(
                        id,
                        name,
                        vehicle,
                        location
                );

        System.out.println(
                "Driver registered successfully: "
                        + driver.getDriverId()
        );
    }

    private void updateDriverLocation() {

        System.out.print("Enter driver ID: ");
        String driverId =
                scanner.nextLine();

        System.out.print("Enter latitude: ");
        double latitude =
                Double.parseDouble(
                        scanner.nextLine()
                );

        System.out.print("Enter longitude: ");
        double longitude =
                Double.parseDouble(
                        scanner.nextLine()
                );

        driverService.updateDriverLocation(
                driverId,
                new Location(
                        latitude,
                        longitude
                )
        );

        System.out.println(
                "Driver location updated successfully."
        );
    }

    private void bookRide() {

        System.out.print("Enter user ID: ");
        String userId =
                scanner.nextLine();

        System.out.print(
                "Enter pickup latitude: "
        );

        double pickupLatitude =
                Double.parseDouble(
                        scanner.nextLine()
                );

        System.out.print(
                "Enter pickup longitude: "
        );

        double pickupLongitude =
                Double.parseDouble(
                        scanner.nextLine()
                );

        System.out.print(
                "Enter drop latitude: "
        );

        double dropLatitude =
                Double.parseDouble(
                        scanner.nextLine()
                );

        System.out.print(
                "Enter drop longitude: "
        );

        double dropLongitude =
                Double.parseDouble(
                        scanner.nextLine()
                );

        CarType requestedCarType =
                readCarType();

        System.out.print(
                "Enter coupon code "
                        + "(press Enter for none): "
        );

        String couponCode =
                scanner.nextLine().trim();

        if (couponCode.isEmpty()) {
            couponCode = null;
        }

        Ride ride =
                rideService.bookRide(
                        userId,
                        new Location(
                                pickupLatitude,
                                pickupLongitude
                        ),
                        new Location(
                                dropLatitude,
                                dropLongitude
                        ),
                        requestedCarType,
                        couponCode
                );

        System.out.println();
        System.out.println(
                "Ride booked successfully!"
        );
        System.out.println(
                "-----------------------------------"
        );

        System.out.println(
                "Ride ID: "
                        + ride.getRideId()
        );

        System.out.println(
                "Driver: "
                        + ride.getDriver().getName()
        );

        System.out.println(
                "Requested Car: "
                        + ride.getRequestedCarType()
        );

        System.out.println(
                "Actual Car: "
                        + ride.getActualCarType()
        );

        System.out.println(
                "Status: "
                        + ride.getStatus()
        );

        System.out.printf(
                "Fare: ₹%.2f%n",
                ride.getFare()
        );

        System.out.println(
                "-----------------------------------"
        );
    }

    private void endRide() {

        System.out.print("Enter ride ID: ");
        String rideId =
                scanner.nextLine();

        BigDecimal fare =
                rideService.endRide(rideId);

        Ride ride =
                rideService.getRide(rideId);

        System.out.println();
        System.out.println(
                "Ride completed successfully!"
        );

        System.out.println(
                "-----------------------------------"
        );

        System.out.println(
                "Ride ID: "
                        + ride.getRideId()
        );

        System.out.println(
                "Driver: "
                        + ride.getDriver().getName()
        );

        System.out.println(
                "Status: "
                        + ride.getStatus()
        );

        System.out.printf(
                "Final Fare: ₹%.2f%n",
                fare
        );

        System.out.println(
                "-----------------------------------"
        );
    }

    private void cancelRide() {

        System.out.print("Enter ride ID: ");
        String rideId =
                scanner.nextLine();

        BigDecimal cancellationFee =
                rideService.cancelRide(rideId);

        Ride ride =
                rideService.getRide(rideId);

        System.out.println();
        System.out.println(
                "Ride cancelled successfully!"
        );

        System.out.println(
                "-----------------------------------"
        );

        System.out.println(
                "Ride ID: "
                        + ride.getRideId()
        );

        System.out.println(
                "Status: "
                        + ride.getStatus()
        );

        System.out.printf(
                "Cancellation Fee: ₹%.2f%n",
                cancellationFee
        );

        System.out.println(
                "Driver is now AVAILABLE."
        );

        System.out.println(
                "-----------------------------------"
        );
    }

    private void viewUserRideHistory() {

        System.out.print("Enter user ID: ");
        String userId =
                scanner.nextLine();

        List<Ride> rides =
                rideService.getUserRideHistory(
                        userId
                );

        if (rides.isEmpty()) {

            System.out.println(
                    "No rides found."
            );

            return;
        }

        System.out.println();
        System.out.println(
                "USER RIDE HISTORY"
        );

        for (Ride ride : rides) {
            printRide(ride);
        }
    }

    private void viewDriverRideHistory() {

        System.out.print(
                "Enter driver ID: "
        );

        String driverId =
                scanner.nextLine();

        List<Ride> rides =
                rideService.getDriverRideHistory(
                        driverId
                );

        if (rides.isEmpty()) {

            System.out.println(
                    "No rides found."
            );

            return;
        }

        System.out.println();
        System.out.println(
                "DRIVER RIDE HISTORY"
        );

        for (Ride ride : rides) {
            printRide(ride);
        }
    }

    private void printRide(Ride ride) {

        System.out.println(
                "-----------------------------------"
        );

        System.out.println(
                "Ride ID: "
                        + ride.getRideId()
        );

        System.out.println(
                "User: "
                        + ride.getUser().getName()
        );

        System.out.println(
                "Driver: "
                        + ride.getDriver().getName()
        );

        System.out.println(
                "Requested Car: "
                        + ride.getRequestedCarType()
        );

        System.out.println(
                "Actual Car: "
                        + ride.getActualCarType()
        );

        System.out.println(
                "Status: "
                        + ride.getStatus()
        );

        if (ride.getFare() != null) {

            System.out.printf(
                    "Fare: ₹%.2f%n",
                    ride.getFare()
            );
        }

        if (ride.getCancellationFee() != null) {

            System.out.printf(
                    "Cancellation Fee: ₹%.2f%n",
                    ride.getCancellationFee()
            );
        }
    }

    private void addCoupon() {

        System.out.print(
                "Enter coupon code: "
        );

        String code =
                scanner.nextLine();

        System.out.print(
                "Enter discount percentage: "
        );

        BigDecimal discount =
                new BigDecimal(
                        scanner.nextLine()
                );

        Coupon coupon =
                couponService.addCoupon(
                        code,
                        discount
                );

        System.out.println(
                "Coupon added: "
                        + coupon.getCode()
                        + " ("
                        + coupon.getDiscountPercentage()
                        + "%)"
        );
    }

    private void deleteCoupon() {

        System.out.print(
                "Enter coupon code: "
        );

        String code =
                scanner.nextLine();

        couponService.deleteCoupon(code);

        System.out.println(
                "Coupon deleted successfully."
        );
    }

    private void viewCoupons() {

        List<Coupon> coupons =
                couponService.getAllCoupons();

        if (coupons.isEmpty()) {

            System.out.println(
                    "No coupons available."
            );

            return;
        }

        System.out.println();
        System.out.println(
                "AVAILABLE COUPONS"
        );

        for (Coupon coupon : coupons) {

            System.out.println(
                    coupon.getCode()
                            + " - "
                            + coupon.getDiscountPercentage()
                            + "%"
            );
        }
    }

    private void updateSurgeDemandAndSupply() {

        System.out.print(
                "Enter current demand: "
        );

        int demand =
                Integer.parseInt(
                        scanner.nextLine()
                );

        System.out.print(
                "Enter available drivers: "
        );

        int availableDrivers =
                Integer.parseInt(
                        scanner.nextLine()
                );

        surgeMultiplierProvider
                .updateDemandAndSupply(
                        demand,
                        availableDrivers
                );

        System.out.println();

        System.out.println(
                "Surge configuration updated."
        );

        System.out.println(
                "Demand: "
                        + surgeMultiplierProvider.getDemand()
        );

        System.out.println(
                "Available Drivers: "
                        + surgeMultiplierProvider
                                .getAvailableDrivers()
        );

        System.out.println(
                "Current Surge Multiplier: "
                        + surgeMultiplierProvider
                                .getMultiplier()
                        + "x"
        );
    }

    private CarType readCarType() {

        while (true) {

            System.out.print(
                    "Enter car type "
                            + "(HATCHBACK/SEDAN): "
            );

            String input =
                    scanner.nextLine()
                            .trim()
                            .toUpperCase();

            try {

                return CarType.valueOf(
                        input
                );

            } catch (IllegalArgumentException e) {

                System.out.println(
                        "Invalid car type. "
                                + "Please enter HATCHBACK or SEDAN."
                );
            }
        }
    }
}
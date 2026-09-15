# Ride Hailing Service Backend

AI-assisted machine coding implementation of a ride-hailing backend similar to Uber.

The application is implemented as a Java CLI application with in-memory storage and automated tests.

---

## Tech Stack

- Java 17
- Maven
- JUnit 5
- In-memory repositories
- CLI application
- Strategy Pattern for pricing and driver matching

---

# Features

## Mandatory Features

- Register a user
- Register a driver
- Book a ride
- Find an available driver within a configurable matching radius
- Update driver location
- End a ride
- Calculate ride fare
- User ride history
- Driver ride history
- Hatchback and Sedan support
- Free Hatchback → Sedan upgrade when Hatchback is unavailable
- Add coupon
- Delete coupon
- Apply valid coupon when starting a ride
- Tiered pricing
- Automated tests

---

# Architecture

The application is divided into the following layers:

```text
com.kartik.ridehailing
│
├── model
│   ├── User
│   ├── Driver
│   ├── Vehicle
│   ├── Ride
│   ├── Coupon
│   └── Location
│
├── enums
│   ├── CarType
│   ├── DriverStatus
│   └── RideStatus
│
├── service
│   ├── UserService
│   ├── DriverService
│   ├── RideService
│   └── CouponService
│
├── repository
│   ├── UserRepository
│   ├── DriverRepository
│   ├── RideRepository
│   ├── CouponRepository
│   └── InMemory implementations
│
├── strategy
│   ├── pricing
│   │   ├── PricingStrategy
│   │   └── TieredPricingStrategy
│   │
│   └── matching
│       ├── DriverMatchingStrategy
│       └── NearestDriverMatchingStrategy
│
└── util
    └── DistanceCalculator
```

---

# Ride Booking Flow

```text
User requests ride
        |
        v
Validate user and input
        |
        v
Find available driver within radius
        |
        v
Determine actual car type
        |
        v
Calculate trip distance
        |
        v
Calculate fare
        |
        v
Apply coupon if supplied
        |
        v
Reserve driver
        |
        v
Create ONGOING ride
```

When the ride ends:

```text
End Ride
   |
   v
Mark ride COMPLETED
   |
   v
Move driver to drop location
   |
   v
Mark driver AVAILABLE
   |
   v
Return final fare
```

The fare is calculated and the coupon is applied when the ride starts. This keeps the booking price fixed throughout the ride.

---

# Pricing Strategy

Pricing is implemented using the Strategy Pattern.

`PricingStrategy` defines the pricing contract:

```java
BigDecimal calculateFare(double distanceInKm, CarType carType);
```

`TieredPricingStrategy` contains the pricing calculation algorithm.

The actual business rates are supplied from `Main`, making the pricing configuration independent from the pricing algorithm.

## Current Pricing Configuration

```text
              0–2 km     2–5 km     >5 km
Hatchback      ₹10         ₹8         ₹5
Sedan          ₹12        ₹10         ₹7
```

Minimum fare:

```text
₹50
```

## Pricing Assumption

The specification gives an illustrative tier structure but does not fully specify the Sedan rates.

I chose the following rates:

### Hatchback

```text
0–2 km  : ₹10/km
2–5 km  : ₹8/km
>5 km   : ₹5/km
```

### Sedan

```text
0–2 km  : ₹12/km
2–5 km  : ₹10/km
>5 km   : ₹7/km
```

The pricing is progressive across tiers.

For example, a 10 km Hatchback ride is:

```text
2 × ₹10 = ₹20
3 × ₹8  = ₹24
5 × ₹5  = ₹25

Total = ₹69
```

For a 10 km Sedan ride:

```text
2 × ₹12 = ₹24
3 × ₹10 = ₹30
5 × ₹7  = ₹35

Total = ₹89
```

The minimum fare is applied after calculation.

---

# Driver Matching

Driver matching is separated from booking logic using:

```text
DriverMatchingStrategy
```

The current implementation is:

```text
NearestDriverMatchingStrategy
```

## Matching Rules

- Only AVAILABLE drivers can be selected.
- Driver must be within a 5 km radius of the pickup location.
- Exact requested car type is preferred.
- When Hatchback is requested and no Hatchback is available within the radius, an available Sedan can be selected.
- The Sedan upgrade is free.
- The nearest eligible driver is selected.

This allows the matching algorithm to be replaced without changing `RideService`.

For example, another strategy could later select:

- Highest-rated driver
- Shortest ETA
- Driver with lowest cancellation rate

---

# Car Type Upgrade

The ride stores both:

```text
requestedCarType
actualCarType
```

This is intentional.

Example:

```text
Requested: HATCHBACK
Available: SEDAN

Result:

Requested Car = HATCHBACK
Actual Car    = SEDAN
```

This preserves what the customer requested while recording what was actually assigned.

---

# Coupons

Coupons are managed through `CouponService`.

Supported operations:

```text
Add coupon
Delete coupon
Apply coupon
```

Coupons currently use percentage discounts.

Example:

```text
Base Fare = ₹100
Coupon    = 10%

Final Fare = ₹90
```

## Coupon Application Timing

The coupon is applied when the ride starts, not when the ride ends.

This ensures:

- Invalid coupons fail during booking.
- A booking cannot start with an invalid coupon.
- The driver is not reserved before coupon validation succeeds.
- The ride stores the final fare from the beginning.

This also prevents an invalid coupon from leaving a driver stuck in `ON_RIDE`.

---

# Ride Lifecycle

A ride has two states:

```text
ONGOING
COMPLETED
```

Driver states:

```text
AVAILABLE
ON_RIDE
```

## Start Ride

```text
Driver AVAILABLE
        |
        v
Driver selected
        |
        v
Driver ON_RIDE
        |
        v
Ride ONGOING
```

## End Ride

```text
Ride ONGOING
      |
      v
Ride COMPLETED

Driver moves to drop location
      |
      v
Driver AVAILABLE
```

---

# Repository Design

Repositories abstract data storage from the services.

For example:

```text
UserRepository
DriverRepository
RideRepository
CouponRepository
```

The current implementations are in-memory:

```text
InMemoryUserRepository
InMemoryDriverRepository
InMemoryRideRepository
InMemoryCouponRepository
```

This keeps the business logic independent from the storage implementation.

A database-backed implementation could be introduced later without changing the service layer significantly.

---

# Distance Calculation

Distance calculation is centralized in:

```text
DistanceCalculator
```

The application uses the Haversine formula to calculate distance between two latitude/longitude coordinates.

This same utility is used by:

- Driver matching
- Ride fare calculation

Keeping distance calculation in one place avoids duplicated geographic logic.

---

# Design Decisions

## Strategy Pattern

Used for pricing and driver matching.

Benefits:

- Easier extension
- Less coupling
- Booking logic does not need to know the matching algorithm
- New pricing strategies can be introduced independently

---

## Repository Pattern

Used to separate business logic from persistence.

The current application uses in-memory repositories because persistence was not required for the assessment.

---

## BigDecimal for Money

`BigDecimal` is used for fares and discounts instead of `double`.

This avoids floating-point precision issues when calculating monetary values.

---

## Configuration in Main

Pricing rates are supplied from `Main`.

Example:

```java
new TieredPricingStrategy(
    BigDecimal.valueOf(10),
    BigDecimal.valueOf(8),
    BigDecimal.valueOf(5),
    BigDecimal.valueOf(12),
    BigDecimal.valueOf(10),
    BigDecimal.valueOf(7)
);
```

This keeps business configuration separate from pricing calculation logic.

For a production system, these values would ideally come from configuration management rather than source code.

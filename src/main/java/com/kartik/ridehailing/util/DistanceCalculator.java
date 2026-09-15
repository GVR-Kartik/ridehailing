package com.kartik.ridehailing.util;

import com.kartik.ridehailing.model.Location;

public final class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private DistanceCalculator() {
    }

    public static double calculate(
            Location first,
            Location second) {

        if (first == null || second == null) {
            throw new IllegalArgumentException(
                    "Locations cannot be null");
        }

        double latitude1 =
                Math.toRadians(first.getLatitude());

        double latitude2 =
                Math.toRadians(second.getLatitude());

        double deltaLatitude =
                Math.toRadians(
                        second.getLatitude()
                                - first.getLatitude());

        double deltaLongitude =
                Math.toRadians(
                        second.getLongitude()
                                - first.getLongitude());

        double a =
                Math.sin(deltaLatitude / 2)
                        * Math.sin(deltaLatitude / 2)
                        + Math.cos(latitude1)
                        * Math.cos(latitude2)
                        * Math.sin(deltaLongitude / 2)
                        * Math.sin(deltaLongitude / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
package com.lochana.parkingassistant;

import java.util.List;

public class NearestParkingFinder {

    public static Location findNearestParking(double userLat, double userLon, List<Location> spots) {
        Location nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Location spot : spots) {
            double distance = haversine(userLat, userLon, spot.getLatitude(), spot.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = spot;
            }
        }

        return nearest;
    }

    // Haversine formula to calculate distance between two lat/lon points
    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of Earth in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // in kilometers
    }
}


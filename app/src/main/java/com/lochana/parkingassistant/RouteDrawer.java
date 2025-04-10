package com.lochana.parkingassistant;

import android.graphics.Color;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

public class RouteDrawer {
    private static Polyline previousRoute = null;

    public static void fetchRoute(GeoPoint start, GeoPoint end, MapView mapView) {
        String url = "https://router.project-osrm.org/route/v1/driving/"
                + start.getLongitude() + "," + start.getLatitude() + ";"
                + end.getLongitude() + "," + end.getLatitude()
                + "?overview=full&geometries=polyline";

        new Thread(() -> {
            try {
                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");

                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                bufferedReader.close();
                connection.disconnect();

                // Parse and decode polyline
                String polyline = parsePolyline(response.toString());
                List<GeoPoint> routePoints = decodePolyline(polyline);

                // Draw the route on the map
                mapView.post(() -> drawRoute(mapView, routePoints));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static String parsePolyline(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray routes = jsonObject.getJSONArray("routes");
        if (routes.length() > 0) {
            return routes.getJSONObject(0).getString("geometry");
        }
        return "";
    }

    private static List<GeoPoint> decodePolyline(String encoded) {
        List<GeoPoint> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            polyline.add(new GeoPoint((lat / 1E5), (lng / 1E5)));
        }
        return polyline;
    }

    private static void drawRoute(MapView map, List<GeoPoint> routePoints) {
        // Remove the previous route before drawing a new one
        if (previousRoute != null) {
            map.getOverlayManager().remove(previousRoute);
        }

        // Create a new Polyline for the route
        Polyline polyline = new Polyline();
        polyline.setPoints(routePoints);
        polyline.setColor(Color.BLUE);
        polyline.setWidth(10f);

        // Store this new route as the previous one
        previousRoute = polyline;

        // Add the new route to the map
        map.getOverlayManager().add(polyline);
        map.invalidate(); // Refresh the map

    }

    /**
     * Calculates the total distance of a polyline (route).
     */
    private static double calculateRouteDistance(List<GeoPoint> routePoints) {
        double totalDistance = 0.0;

        if (routePoints.size() < 2) return 0;

        for (int i = 0; i < routePoints.size() - 1; i++) {
            GeoPoint start = routePoints.get(i);
            GeoPoint end = routePoints.get(i + 1);
            totalDistance += calculateDistance(start, end);
        }

        return totalDistance; // Distance in kilometers
    }

    /**
     * Calculates the distance between two GeoPoints using the Haversine formula.
     */
    public static Double calculateDistance(GeoPoint point1, GeoPoint point2) {
        double lat1 = Math.toRadians(point1.getLatitude());
        double lon1 = Math.toRadians(point1.getLongitude());
        double lat2 = Math.toRadians(point2.getLatitude());
        double lon2 = Math.toRadians(point2.getLongitude());

        double R = 6371.0; // Earth's radius in km

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return Math.round(distance * 100.0)/100.0;
    }

}

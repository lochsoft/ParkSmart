package com.lochana.parkingassistant;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.function.Consumer;

public class MapHandler {
    private MapView mapView;
    private LocationHelper locationHelper;
    private TextView distanceBanner;
    private View navButton;

    public MapHandler(MapView mapView, LocationHelper locationHelper, TextView distanceBanner, View navButton) {
        this.mapView = mapView;
        this.locationHelper = locationHelper;
        this.distanceBanner = distanceBanner;
        this.navButton = navButton;
    }

    public void handleSearchItemClick(Location selectedLocation, Consumer<GeoPoint> destinationCallback) {
        GeoPoint point = new GeoPoint(selectedLocation.getLatitude(), selectedLocation.getLongitude());
        String name = selectedLocation.getName();
        Log.d("MapHandler", "Selected Location: " + name + ", Lat: " + point.getLatitude() + ", Lon: " + point.getLongitude());

        // Move the map to the searched location
        mapView.getController().setCenter(point);
        mapView.getController().setZoom(20.0);

        // Add marker at the searched location
        addMarker(point, name);

        // Draw the route to searched location
        GeoPoint userLocation = locationHelper.getUserLocation();
        if (userLocation != null) {
            RouteDrawer.fetchRoute(userLocation, point, mapView);
        }

        // Show the distance to selected location
        Double distance = RouteDrawer.calculateDistance(userLocation, point);
        String distanceText = "Distance to " + name + ": " + distance + " km";
        distanceBanner.setText(distanceText);
        distanceBanner.setVisibility(View.VISIBLE);

        // Show navigate button
        navButton.setVisibility(View.VISIBLE);

        // Pass the destination back
        destinationCallback.accept(point);
    }

    private void addMarker(GeoPoint point, String name) {
        // Your marker-adding logic
    }
}

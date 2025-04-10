// display markers on the map view for the locations fetched from firebase

package com.lochana.parkingassistant;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import com.lochana.parkingassistant.Location;
import com.lochana.parkingassistant.R;

import java.util.List;

public class LocationOverlayManager {
    private final MapView mapView;
    private final Context context;

    public LocationOverlayManager(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
    }

    /**
     * Adds multiple markers to the map for the given list of locations.
     */
    public void addLocationMarkers(List<Location> locations) {
        clearExistingMarkers();

        for (Location location : locations) {
            GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
            addMarker(point, location);
        }

        mapView.invalidate(); // Refresh the map to display markers
    }

    /**
     * Clears existing markers before adding new ones.
     */
    private void clearExistingMarkers() {
        List<Overlay> overlays = mapView.getOverlays();
        overlays.removeIf(overlay -> overlay instanceof Marker);
    }

    /**
     * Adds a single marker at the specified location.
     */
    private void addMarker(GeoPoint point, Location location) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(location.getName());
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        Drawable icon = ContextCompat.getDrawable(context, R.drawable.parking_sign);
        marker.setIcon(icon);

        // Create and set the custom InfoWindow
        CustomParkingInfoWindow infoWindow = new CustomParkingInfoWindow(R.layout.custom_info_window, mapView);
        // Assuming your Location class has fields for availability and price
        //infoWindow.setParkingDetails(location.getAvailability(), location.getPrice());
        infoWindow.setParkingDetails("Available", "Free");
        marker.setInfoWindow(infoWindow);

        // Set OnMarkerClickListener to handle InfoWindow display
        marker.setOnMarkerClickListener((clickedMarker, vw) -> {
            CustomParkingInfoWindow.closeAllInfoWindowsOn(mapView);
            clickedMarker.showInfoWindow();
            return true; // Consume the event
        });

        mapView.getOverlays().add(marker);
    }
}

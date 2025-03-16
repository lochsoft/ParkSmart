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
            addMarker(point, location.getName());
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
    private void addMarker(GeoPoint point, String title) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        Drawable icon = ContextCompat.getDrawable(context, R.drawable.parking_sign);
        marker.setIcon(icon);

        mapView.getOverlays().add(marker);
    }
}

// display markers on the map view for the locations fetched from firebase

package com.lochana.parkingassistant;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.lochana.parkingassistant.Location;
import com.lochana.parkingassistant.R;
import com.lochana.parkingassistant.ui.home.HomeFragment;

import java.util.List;

public class LocationOverlayManager {
    private final MapView mapView;
    private final Context context;
    private final LocationHelper locationHelper;

    public LocationOverlayManager(Context context, MapView mapView, LocationHelper locationHelper) {
        this.context = context;
        this.mapView = mapView;
        this.locationHelper = locationHelper;
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
        /*CustomParkingInfoWindow infoWindow = new CustomParkingInfoWindow(R.layout.custom_info_window, mapView);
        // Assuming your Location class has fields for availability and price
        //infoWindow.setParkingDetails(location.getAvailability(), location.getPrice());
        infoWindow.setParkingDetails("Available", "Free");
        marker.setInfoWindow(infoWindow);*/

        // Set OnMarkerClickListener to handle InfoWindow display
        /*marker.setOnMarkerClickListener((clickedMarker, vw) -> {
            CustomParkingInfoWindow.closeAllInfoWindowsOn(mapView);
            clickedMarker.showInfoWindow();
            return true; // Consume the event
        });*/
        marker.setOnMarkerClickListener((clickedMarker, vw) -> {
            showParkingBottomSheet(location); // new method to show bottom sheet
            return true; // consume the event
        });

        mapView.getOverlays().add(marker);
    }

    private void showParkingBottomSheet(Location location) {
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView title = bottomSheetView.findViewById(R.id.info_window_title);
        TextView availabilityView = bottomSheetView.findViewById(R.id.info_window_availability);
        TextView priceView = bottomSheetView.findViewById(R.id.info_window_price);
        Button navigateButton = bottomSheetView.findViewById(R.id.button4);

        title.setText(location.getName());
        availabilityView.setText("Available");
        priceView.setText("Price");

        /*navigateButton.setOnClickListener(v -> {
            bottomSheetDialog.dismiss(); // Dismiss the bottom sheet
            navigateToLocation(context, location.getLatitude(), location.getLongitude());
        });*/

        navigateButton.setOnClickListener(v -> {
            GeoPoint userLocation = locationHelper.getUserLocation();

            GeoPoint destination = new GeoPoint(location.getLatitude(), location.getLongitude());

            NavigationHelper.navigateToSelectedLocation(this.context, userLocation, destination);
        });

        bottomSheetDialog.show();
    }

    public void navigateToLocation(Context context, double latitude, double longitude) {
        String uri = "google.navigation:q=" + latitude + "," + longitude + "&mode=d"; // d = driving, w = walking, b = biking
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Google Maps is not installed", Toast.LENGTH_SHORT).show();
        }
    }

}

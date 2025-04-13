// display markers on the map view for the locations fetched from firebase, info window, delete. edit places

package com.lochana.parkingassistant;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
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

        // Pass both marker and location
        marker.setOnMarkerClickListener((clickedMarker, vw) -> {
            showParkingBottomSheet(location, clickedMarker);
            return true;
        });

        mapView.getOverlays().add(marker);
    }

    private void showParkingBottomSheet(Location location, Marker marker) {
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView title = bottomSheetView.findViewById(R.id.info_window_title);
        TextView availabilityView = bottomSheetView.findViewById(R.id.info_window_availability);
        TextView priceView = bottomSheetView.findViewById(R.id.info_window_price);
        Button navigateButton = bottomSheetView.findViewById(R.id.button4);
        Button deleteButton = bottomSheetView.findViewById(R.id.button);
        RatingBar ratingBar = bottomSheetView.findViewById(R.id.ratingBar2);
        Button editButton = bottomSheetView.findViewById(R.id.button3);

        /// setting informations of locations ///
        title.setText(location.getName());
        availabilityView.setText(location.getAvailability());
        priceView.setText(location.getPrice());
        ratingBar.setRating(location.getRating());

        navigateButton.setOnClickListener(v -> {
            GeoPoint userLocation = locationHelper.getUserLocation();
            GeoPoint destination = new GeoPoint(location.getLatitude(), location.getLongitude());
            NavigationHelper.navigateToSelectedLocation(this.context, userLocation, destination);
        });

        deleteButton.setOnClickListener(v -> {
            deleteLocationFromFirestore(location.getDocumentid(), marker);
            bottomSheetDialog.dismiss(); // close the bottom sheet after deletion
        });


        bottomSheetDialog.show();
    }

    private void deleteLocationFromFirestore(String documentId, Marker marker) {
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Confirm Location deletion")
                .setMessage("Are you sure you want to delete this location?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("locations").document(documentId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Location Deleted", Toast.LENGTH_SHORT).show();
                                mapView.getOverlays().remove(marker);
                                mapView.invalidate();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Error deleting", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss(); // Close the dialog
                })
                .show();
    }

    private void editLocations(GeoPoint point){

    }

}

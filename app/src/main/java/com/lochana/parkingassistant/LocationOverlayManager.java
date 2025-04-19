// display markers on the map view for the locations fetched from firebase, info window, delete. edit places

package com.lochana.parkingassistant;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
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
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lochana.parkingassistant.ui.dashboard.DashboardFragment;
import com.lochana.parkingassistant.ui.home.HomeFragment;

import java.util.Arrays;
import java.util.List;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LocationOverlayManager {
    private final MapView mapView;
    private final Context context;
    private final LocationHelper locationHelper;
    private final GeoPoint selectedDestination;
    private final addNewLocation addNewLocation;
    private HomeFragment homeFragment;
    private DashboardFragment dashboardFragment;
    private Marker newParkingMarker;
    private ParkingLocationHelper parkingLocationHelper;
    private ExistingParkingData selectedPoint;

    public LocationOverlayManager(Context context, MapView mapView, LocationHelper locationHelper, GeoPoint selectedDestination, com.lochana.parkingassistant.addNewLocation addNewLocation, HomeFragment homeFragment, ParkingLocationHelper parkingLocationHelper) {
        this.context = context;
        this.mapView = mapView;
        this.locationHelper = locationHelper;
        this.selectedDestination = selectedDestination;
        this.addNewLocation = addNewLocation;
        this.homeFragment = homeFragment;
        this.parkingLocationHelper = parkingLocationHelper;
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
        try{
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapView.getController().animateTo(geoPoint);

        marker.showInfoWindow();
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
        TextView description = bottomSheetView.findViewById(R.id.textView3);
        Button saveButton = bottomSheetView.findViewById(R.id.button2);

        // setting data to existing data class
        selectedPoint = new ExistingParkingData(location.getName(), location.getLatitude(), location.getLongitude(), location.getAvailability(), location.getPrice(), location.getRating(), location.getDescription(), location.getDocumentid());

        /// setting informations of locations ///
        title.setText(location.getName());
        availabilityView.setText(location.getAvailability());
        priceView.setText(location.getPrice());
        ratingBar.setRating(location.getRating());
        description.setText(location.getDescription());

        navigateButton.setOnClickListener(v -> {
            GeoPoint userLocation = locationHelper.getUserLocation();
            GeoPoint destination = new GeoPoint(location.getLatitude(), location.getLongitude());
            NavigationHelper.navigateToSelectedLocation(this.context, userLocation, destination);
        });

        deleteButton.setOnClickListener(v -> {
            deleteLocationFromFirestore(location.getDocumentid(), marker);
            bottomSheetDialog.dismiss(); // close the bottom sheet after deletion
        });
        editButton.setOnClickListener(v -> {
            editLocations(selectedDestination);
            bottomSheetDialog.dismiss(); // close the bottom sheet after deletion
        });

        saveButton.setOnClickListener(v -> {
            saveParkingLocation(location);
        });

        RecyclerView imageRecyclerView = bottomSheetView.findViewById(R.id.imageRecyclerView);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        // Add sample image URLs
        List<String> imageUrls = Arrays.asList(
                "https://cdn.pixabay.com/photo/2025/03/18/17/03/dog-9478487_1280.jp",
                "https://cdn.pixabay.com/photo/2024/03/11/12/05/easter-8626470_1280.jp",
                "https://cdn.pixabay.com/photo/2023/09/13/15/41/mountain-8251186_1280.jp"
        );

        ImageAdapter imageAdapter = new ImageAdapter(context, imageUrls);
        imageRecyclerView.setAdapter(imageAdapter);

        // Close info window when bottom sheet is dismissed
        bottomSheetDialog.setOnDismissListener(dialog -> marker.closeInfoWindow());

        bottomSheetDialog.show();
        } catch (Exception e) {
            Log.d("showParkingBottomSheet", "error " + e.getMessage());
        }
    }

    private void saveParkingLocation(Location location){
        try {
            ParkingData data = new ParkingData(
                    location.getDocumentid(),
                    location.getName(),
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAvailability(),
                    location.getPrice(),
                    location.getRating(),
                    location.getDescription()
            );

            AppDatabase.getInstance(context).parkingDataDao().insert(data);
            Toast.makeText(context, "Location Saved", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.d("saveParkingLocation", "error " + e.getMessage());
        }
    }

    private void deleteLocationFromFirestore(String documentId, Marker marker) {
        try {
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
                        marker.closeInfoWindow();
                        HomeFragment homeFragment = parkingLocationHelper.getHomeFragment();
                        if (homeFragment != null) {
                            homeFragment.fetchLocations();
                        }

                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss(); // Close the dialog
                    })
                    .show();
        } catch (Exception e) {
            Log.d("deleteLocations","error " + e.getMessage());
        }
    }

    private void editLocations(GeoPoint point){
        try {
            NewParkingDetailsBottomSheet bottomSheet = new NewParkingDetailsBottomSheet(
                    context, point, addNewLocation, mapView, newParkingMarker, parkingLocationHelper, selectedPoint);
            bottomSheet.show(((HomeFragment) homeFragment).getChildFragmentManager(), "ParkingDetailsBottomSheet");
            Log.d("edit Locations", "passed");

        } catch (Exception e) {
            Log.d("edit Locations", "error "+e.getMessage());
        }
    }

}
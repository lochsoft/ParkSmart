package com.lochana.parkingassistant.ui.home;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lochana.parkingassistant.ExistingParkingBottomSheet;
import com.lochana.parkingassistant.InfoBanner;
import com.lochana.parkingassistant.Location;
import com.lochana.parkingassistant.LocationHelper;
import com.lochana.parkingassistant.LocationOverlayManager;
import com.lochana.parkingassistant.MapHandler;
import com.lochana.parkingassistant.NavigationHelper;
import com.lochana.parkingassistant.NearestParkingFinder;
import com.lochana.parkingassistant.NewParkingDetailsBottomSheet;
import com.lochana.parkingassistant.R;
import com.lochana.parkingassistant.RouteDrawer;
import com.lochana.parkingassistant.addNewLocation;
import com.lochana.parkingassistant.ParkingLocationHelper; // Import the helper class

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements MapEventsReceiver { // Implement MapEventsReceiver

    private MapView mapView;
    private AutoCompleteTextView searchView;
    private ArrayAdapter<String> adapter;
    private Button addNewLocationBtn, nav_btn;
    private FirebaseFirestore db;
    private addNewLocation addNewLocation;
    private Button userLocateBtn, nearestParkingBtn, allParksBtn, saveCurrentSpot;
    private List<Location> locations;
    private static final BoundingBox SRI_LANKA_BOUNDS = new BoundingBox(
            6.804, 79.902, 6.797, 79.895 // Sri Lanka bounding box
    );
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationHelper locationHelper;
    private Marker userMarker, newParkingMarker ;
    private ParkingLocationHelper parkingLocationHelper; // Parking Location Helper
    private LocationOverlayManager locationOverlayManager;
    private TextView distanceBanner;
    private GeoPoint selectedDestination; // To store the selected location
    private HomeFragment HomeFragment;
    private ProgressBar progressBar;
    private boolean isMenuOpen = false;
    private Polygon userAccuracyCircle;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mapView = root.findViewById(R.id.openStreetMap);
        searchView = root.findViewById(R.id.searchAutoComplete);
        distanceBanner = root.findViewById(R.id.distanceBannar);
        nearestParkingBtn = root.findViewById(R.id.locate_user4);
        progressBar = root.findViewById(R.id.progressBar);
        allParksBtn = root.findViewById(R.id.all_parks_btn);
        saveCurrentSpot = root.findViewById(R.id.saveCurrentSpot);
        Button mainFab = root.findViewById(R.id.controlBtn);
        Button refreshBtn = root.findViewById(R.id.refreshBtn);

        // Initialize LocationHelper
        locationHelper = new LocationHelper(requireContext());

        // Initialize ParkingLocationHelper
        parkingLocationHelper = new ParkingLocationHelper(requireContext(), mapView, new addNewLocation(), this);
        locationOverlayManager = new LocationOverlayManager(requireContext(), mapView, locationHelper, selectedDestination, new addNewLocation(), this, new ParkingLocationHelper(requireContext(), mapView, new addNewLocation(), this)); // Initialize the marker manager

        setupMap();
        fetchLocations();

        // nav btn click
        nav_btn = root.findViewById(R.id.btn_navigate);
        //nav_btn.setOnClickListener(v -> navigateToSelectedLocation());
        nav_btn.setOnClickListener(v -> {
            GeoPoint userLocation = locationHelper.getUserLocation();
            GeoPoint destination = new GeoPoint(selectedDestination.getLatitude(), selectedDestination.getLongitude());

            NavigationHelper.navigateToSelectedLocation(requireContext(), userLocation, destination);
        });

        // nearest parking finder button
        nearestParkingBtn.setOnClickListener(v -> locateNearestParking());

        addNewLocation = new addNewLocation();
        // add new parking place button
        addNewLocationBtn = root.findViewById(R.id.add_new_parking_place_button);
        addNewLocationBtn.setOnClickListener(v -> addNewParking());
        //addNewLocationBtn.setOnClickListener(v -> parkingLocationHelper.startAddingParking()); // Use ParkingLocationHelper

        userLocateBtn = root.findViewById(R.id.locate_user);
        userLocateBtn.setOnClickListener(v -> locateUser());

        // initialize search view

        // save current spot
        saveCurrentSpot.setOnClickListener(v -> saveMyCurrentSpot(userMarker.getPosition()));

        // handle button menu
        LinearLayout fabMenu = root.findViewById(R.id.btnMenu);

        mainFab.setOnClickListener(v -> {
            toggleFabMenu(fabMenu, mainFab, isMenuOpen, org.osmdroid.library.R.drawable.sharp_add_black_36, org.osmdroid.library.R.drawable.sharp_remove_black_36);
            isMenuOpen = !isMenuOpen;
        });

        // refresh btn logic
        refreshBtn.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Update Locations")
                    .setMessage("Are you sure you want to refresh the locations?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        fetchLocations();
                        Toast.makeText(requireContext(), "Locations Updated!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });


        return root;
    }

    // handle the button menu
    private void toggleFabMenu(View fabMenu, Button mainFab, boolean isMenuOpen, int openIconRes, int closeIconRes) {
        if (!isMenuOpen) {
            fabMenu.setVisibility(View.VISIBLE);
            fabMenu.setAlpha(0f);
            fabMenu.animate().alpha(1f).setDuration(200).start();
            mainFab.setForeground(ContextCompat.getDrawable(requireContext(), closeIconRes));
        } else {
            fabMenu.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                fabMenu.setVisibility(View.GONE);
            }).start();
            mainFab.setForeground(ContextCompat.getDrawable(requireContext(), openIconRes));
        }
    }


    // save current parking spot
    private void saveMyCurrentSpot(GeoPoint point){
        newParkingMarker = new Marker(mapView);
        try {
            NewParkingDetailsBottomSheet bottomSheet = new NewParkingDetailsBottomSheet(
                    requireContext(), point, addNewLocation, mapView, newParkingMarker, parkingLocationHelper, null);
            bottomSheet.show(requireFragmentManager(), "ParkingDetailsBottomSheet");
            Log.d("Save current spot", "passed");
        } catch (Exception e) {
            Log.d("Save current spot", "error "+e.getMessage());
        }
    }

    // show all parks
    private void showAllParks(){
        ExistingParkingBottomSheet bottomSheet = new ExistingParkingBottomSheet(locations);
        bottomSheet.show(requireFragmentManager(), bottomSheet.getTag());
        }

    // locate nearest parking
    private void locateNearestParking() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            GeoPoint userLocation = locationHelper.getUserLocation();
            Location nearest = NearestParkingFinder.findNearestParking(userLocation.getLatitude(), userLocation.getLongitude(), locations);

            Log.d("NearestLocation", "Nearest Location: " + nearest.getName() + ", Lat: " + nearest.getLatitude() + ", Lon: " + nearest.getLongitude());

            progressBar.setVisibility(View.GONE);

            // Show confirmation dialog
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Navigate to Nearest Parking Spot?")
                    .setMessage("Nearest location is: " + nearest.getName() + "\nDo you want to show the route?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // User clicked Yes
                        RouteDrawer.fetchRoute(userLocation, new GeoPoint(nearest.getLatitude(), nearest.getLongitude()), mapView);
                    })
                    .setNegativeButton("No", null)
                    .show();
        } catch (Exception e) {
            Log.d("locateNearestParking", "Error finding nearest location: " + e.getMessage());
        }
    }

    // add new parking
    private void addNewParking() {
        parkingLocationHelper.startAddingParking();
    }

    // fetching locations from firebase database
    public void fetchLocations() {
        try {
            locations = new ArrayList<>();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("locations")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("name");
                                Double latitude = document.getDouble("latitude");
                                Double longitude = document.getDouble("longitude");
                                String availability = document.getString("availability");
                                Integer rating = document.getLong("rating").intValue();
                                Double price = document.getDouble("price");
                                String description = document.getString("description");

                                if (description == null) {
                                    description = "No description available";
                                }

                                Log.d("FirestoreData", "Name: " + name + ", Lat: " + latitude + ", Lon: " + longitude);

                                Location location = new Location(name, latitude, longitude, availability, rating, price, document.getId(), description);
                                // add location names to list
                                locations.add(location);
                            }

                            initializeLocations(locations);
                            locationOverlayManager.addLocationMarkers(locations);

                            // updating all parking view
                            allParksBtn.setOnClickListener(v -> showAllParks());

                            // Ensure user marker is not removed
                            if (userMarker != null) {
                                mapView.getOverlays().add(userMarker);
                            }
                            mapView.invalidate(); // Refresh the map
                        } else {
                            Log.w("FirestoreData", "Error getting documents.", task.getException());
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error fetching locations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Initialize the adapter and set it to the AutoCompleteTextView
    private void initializeLocations(List<Location> locations) {
        try {
            this.locations = locations; // Store the Location list

            List<String> locationNames = new ArrayList<>();
            for (Location location : locations) {
                locationNames.add(location.getName()); // Extract names for the adapter
            }

            Log.d("Location names", "Location Names: " + locationNames.toString());

            adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, locationNames);
            searchView.setAdapter(adapter);
            searchView.setThreshold(1);

            // handling search item clicks
            MapHandler mapHandler = new MapHandler(mapView, locationHelper, distanceBanner, nav_btn);

            searchView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedName = (String) parent.getItemAtPosition(position);
                Log.d("SearchView", "Selected item from dropdown: " + selectedName);

                // Find the Location object that matches the selected name
                for (Location location : locations) {
                    if (location.getName().equals(selectedName)) {
                        mapHandler.handleSearchItemClick(location, destination -> {
                            selectedDestination = destination;
                        });
                        break;
                    }
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Configures the map settings.
     */
    private void setupMap() {
        try{
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        if (locationHelper.checkLocationPermission()) {
            locateUser();
        } else {
            locationHelper.requestLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE);
        }

        mapView.setMinZoomLevel(5.0);
        mapView.setMaxZoomLevel(20.0);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(mapEventsOverlay); // Add MapEventsOverlay
            }
            catch (Exception e) {
                Toast.makeText(requireContext(), "Error setting up map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locateUser();
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                // Optionally set a default location or show a message
                mapView.getController().setZoom(20);
                mapView.getController().setCenter(new GeoPoint(7.8731, 80.7718)); // Default center: Sri Lanka
            }
        }
    }

    private void locateUser() {
        try {
        /*
        GeoPoint userLocation = locationHelper.getUserLocation();
        if (userLocation != null) {
            mapView.getController().animateTo(userLocation);
            mapView.getController().setZoom(20.0); // Zoom in closer to the user

            // Update the marker's position
            updateUserMarkerPosition(userLocation);
        } else {
            // Optionally set a default location or show a message
            mapView.getController().setZoom(20.0);
            mapView.getController().setCenter(new GeoPoint(7.8731, 80.7718));
            Toast.makeText(requireContext(), "Could not get user location", Toast.LENGTH_SHORT).show();
        }*/

            LocationHelper locationHelper = new LocationHelper(requireContext());
            locationHelper.initFusedLocation();

            locationHelper.getAccurateLocation(location -> {
                if (location != null) {
                    GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mapView.getController().animateTo(userLocation);
                    mapView.getController().setZoom(19.0);
                    updateUserMarkerPosition(userLocation); // update the marker position
                    Log.d("UserLocation", "Lat: " + userLocation.getLatitude() + ", Lon: " + userLocation.getLongitude());
                    // You can now place your pin here
                } else {
                    Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.d("LocateUser", "Error locating user: " + e.getMessage());
            Toast.makeText(requireContext(), "Error locating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserMarkerPosition(GeoPoint point) {
        try {
            if (userMarker == null) {
                userMarker = new Marker(mapView);
                userMarker.setIcon(getResources().getDrawable(R.drawable.userlocation));
                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                userMarker.setTitle("You're Here!");
                // set info window
                userMarker.setInfoWindow(new InfoBanner(R.layout.info_banner, mapView));
                mapView.getOverlays().add(userMarker);
            }

            userMarker.setPosition(point);
            showUserAccuracyCircle(point, mapView);

            // Ensure it stays on top
            mapView.getOverlays().remove(userMarker);
            mapView.getOverlays().add(userMarker);

            mapView.invalidate(); // Refresh the map
        }catch (Exception e){
            Toast.makeText(requireContext(), "Error updating user marker position: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("updateUserMarkerPosition", "Error updating user marker position: " + e.getMessage());
        }
    }

    // add a circle surrounding user location
    private void showUserAccuracyCircle(GeoPoint userLocation, MapView mapView) {
        try {
            if (userAccuracyCircle == null) {
                userAccuracyCircle = new Polygon(); // Create a polygon once
                userAccuracyCircle.setStrokeColor(Color.parseColor("#3366AA")); // Border color
                userAccuracyCircle.setFillColor(Color.parseColor("#503366AA")); // Fill color with transparency
                userAccuracyCircle.setStrokeWidth(2f);
                mapView.getOverlays().add(userAccuracyCircle); // Add only once
            }

            userAccuracyCircle.setPoints(Polygon.pointsAsCircle(userLocation, 50)); // Just update points
            mapView.invalidate(); // Refresh the map
        } catch (Exception e) {
            Log.d("showUserAccuracyCircle", "Error showing user accuracy circle: " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        parkingLocationHelper.handleMapTap(p);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }
}
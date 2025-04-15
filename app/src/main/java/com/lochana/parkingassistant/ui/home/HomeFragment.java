package com.lochana.parkingassistant.ui.home;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lochana.parkingassistant.Location;
import com.lochana.parkingassistant.LocationHelper;
import com.lochana.parkingassistant.LocationOverlayManager;
import com.lochana.parkingassistant.MapHandler;
import com.lochana.parkingassistant.NavigationHelper;
import com.lochana.parkingassistant.NearestParkingFinder;
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
    private Button userLocateBtn, nearestParkingBtn;
    private List<Location> locations;
    private static final BoundingBox SRI_LANKA_BOUNDS = new BoundingBox(
            6.804, 79.902, 6.797, 79.895 // Sri Lanka bounding box
    );
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationHelper locationHelper;
    private Marker userMarker;
    private ParkingLocationHelper parkingLocationHelper; // Parking Location Helper
    private LocationOverlayManager locationOverlayManager;
    private TextView distanceBanner;
    private GeoPoint selectedDestination; // To store the selected location
    private HomeFragment HomeFragment;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mapView = root.findViewById(R.id.openStreetMap);
        searchView = root.findViewById(R.id.searchAutoComplete);
        distanceBanner = root.findViewById(R.id.distanceBannar);
        nearestParkingBtn = root.findViewById(R.id.locate_user4);
        progressBar = root.findViewById(R.id.progressBar);

        // Initialize LocationHelper
        locationHelper = new LocationHelper(requireContext());

        // Initialize ParkingLocationHelper
        parkingLocationHelper = new ParkingLocationHelper(requireContext(), mapView, new addNewLocation(), this);
        //parkingLocationHelper = new ParkingLocationHelper(requireContext(), mapView, addNewLocation);
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


        return root;
    }

    // locate nearest parking
    private void locateNearestParking() {
        progressBar.setVisibility(View.VISIBLE);
        GeoPoint userLocation = locationHelper.getUserLocation();
        Location nearest = NearestParkingFinder.findNearestParking(userLocation.getLatitude(), userLocation.getLongitude(), locations);

        Log.d("NearestLocation", "Nearest Location: " + nearest.getName() + ", Lat: " + nearest.getLatitude() + ", Lon: " + nearest.getLongitude());

        progressBar.setVisibility(View.GONE);

        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Navigate to Nearest Parking Spot?")
                .setMessage("Nearest location is: " + nearest.getName() + "\nDo you want to show the route?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User clicked Yes
                    RouteDrawer.fetchRoute(userLocation, new GeoPoint(nearest.getLatitude(), nearest.getLongitude()), mapView);
                })
                .setNegativeButton("No", null)
                .show();
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

            MapHandler mapHandler = new MapHandler(mapView, locationHelper, distanceBanner, nav_btn);

            searchView.setOnItemClickListener((parent, view, position, id) -> {
                Location selectedLocation = locations.get(position);
                mapHandler.handleSearchItemClick(selectedLocation, destination -> {
                    selectedDestination = destination;
                });
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
                mapView.getController().setZoom(30.0);
                updateUserMarkerPosition(userLocation); // update the marker position
                Log.d("UserLocation", "Lat: " + userLocation.getLatitude() + ", Lon: " + userLocation.getLongitude());
                // You can now place your pin here
            } else {
                Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateUserMarkerPosition(GeoPoint point) {
        if (userMarker == null) {
            userMarker = new Marker(mapView);
            userMarker.setIcon(getResources().getDrawable(R.drawable.user_location_pin));
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            userMarker.setTitle("You're Here!");
            mapView.getOverlays().add(userMarker);
        }

        userMarker.setPosition(point);

        // Ensure it stays on top
        mapView.getOverlays().remove(userMarker);
        mapView.getOverlays().add(userMarker);

        mapView.invalidate(); // Refresh the map
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
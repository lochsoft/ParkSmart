package com.lochana.parkingassistant.ui.home;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lochana.parkingassistant.Location;
import com.lochana.parkingassistant.LocationHelper;
import com.lochana.parkingassistant.R;
import com.lochana.parkingassistant.addNewLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private MapView mapView;
    private AutoCompleteTextView searchView;
    private ListView listView;
    private List<String> locationNames;
    private ArrayAdapter<String> adapter;
    private Button addNewLocationBtn;
    private FirebaseFirestore db;
    private addNewLocation addNewLocation;
    private Button testBtn;
    private List<Location> locations;
    private static final BoundingBox SRI_LANKA_BOUNDS = new BoundingBox(
            6.804, 79.902, 6.797, 79.895 // Sri Lanka bounding box
    );
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationHelper locationHelper;
    private Marker userMarker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mapView = root.findViewById(R.id.openStreetMap);
        searchView = root.findViewById(R.id.searchAutoComplete);
        listView = root.findViewById(R.id.listView);

        // Initialize LocationHelper
        locationHelper = new LocationHelper(requireContext());

        // Initialize userMarker here in onCreateView
        userMarker = new Marker(mapView);
        userMarker.setIcon(getResources().getDrawable(org.osmdroid.library.R.drawable.osm_ic_center_map));
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(userMarker);

        setupMap();
        fetchLocations();
        setupSearch();

        addNewLocation = new addNewLocation();
        // add new parking place button
        addNewLocationBtn = root.findViewById(R.id.add_new_parking_place_button);
        addNewLocationBtn.setOnClickListener(v -> addNewLocation.addNewLocation("New Location Name", 12.3456, 78.9012));

        // test for fetch locations
        //testBtn = root.findViewById(R.id.button4);
        //testBtn.setOnClickListener(v -> {fetchLocations();});

        return root;
    }

    // fetching locations from firebase database
    private void fetchLocations() {
        locations = new ArrayList<>(); // Initialize the list

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            Log.d("FirestoreData", "Document ID: " + document.getId());
                            Log.d("FirestoreData", "Name: " + name);
                            Log.d("FirestoreData", "Latitude: " + latitude);
                            Log.d("FirestoreData", "Longitude: " + longitude);

                            Location location = new Location(name, latitude, longitude);
                            locations.add(location);
                        }
                        initializeLocations(locations);
                    } else {
                        Log.w("FirestoreData", "Error getting documents.", task.getException());
                    }
                });
    }

    // Initialize the adapter and set it to the AutoCompleteTextView
    private void initializeLocations(List<Location> locations) {
        try {
            this.locations = locations; // Store the Location list

            List<String> locationNames = new ArrayList<>();
            for (Location location : locations) {
                locationNames.add(location.getName()); // Extract names for the adapter
            }

            adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, locationNames);
            searchView.setAdapter(adapter);

            // Handle item clicks for pinpointing
            searchView.setOnItemClickListener((parent, view, position, id) -> {
                Location selectedLocation = this.locations.get(position); // Get the Location object
                GeoPoint point = new GeoPoint(selectedLocation.getLatitude(), selectedLocation.getLongitude());
                String name = selectedLocation.getName();

                // Move the map to the searched location
                mapView.getController().setCenter(point);
                mapView.getController().setZoom(20.0);

                // Add marker at the searched location
                addMarker(point, name);
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Configures the map settings.
     */
    private void setupMap() {
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
                mapView.getController().setZoom(20.0);
                mapView.getController().setCenter(new GeoPoint(7.8731, 80.7718)); // Default center: Sri Lanka
            }
        }
    }

    private void locateUser() {
        GeoPoint userLocation = locationHelper.getUserLocation();
        if (userLocation != null) {
            mapView.getController().setCenter(userLocation);
            mapView.getController().setZoom(20.0); // Zoom in closer to the user

            // Update the marker's position
            updateUserMarkerPosition(userLocation);
        } else {
            // Optionally set a default location or show a message
            mapView.getController().setZoom(20.0);
            mapView.getController().setCenter(new GeoPoint(7.8731, 80.7718)); // Default center: Sri Lanka
        }
    }

    private void updateUserMarkerPosition(GeoPoint point) {
        if (userMarker != null) {
            userMarker.setPosition(point);
            mapView.invalidate(); // Refresh the map
        }
    }

    /**
     * Sets up the search functionality.
     */
    private void setupSearch() {
        searchView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLocation = (String) parent.getItemAtPosition(position);
            locatePlace(selectedLocation);
        });
    }

    /**
     * Searches for a location and moves the map to that position.
     */
    private void locatePlace(String placeName) {
        if (getContext() == null) return;

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(placeName, 1);
            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = addresses.get(0);
            GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());

            // Move the map to the searched location
            mapView.getController().setCenter(point);
            mapView.getController().setZoom(20.0);

            // Add marker at the searched location
            addMarker(point, placeName);

        } catch (IOException e) {
            Toast.makeText(getContext(), "Error finding location", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adds a marker at the specified location.
     */
    private void addMarker(GeoPoint point, String title) {
        mapView.getOverlays().clear(); // Clear previous markers

        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);

        mapView.invalidate();
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
}
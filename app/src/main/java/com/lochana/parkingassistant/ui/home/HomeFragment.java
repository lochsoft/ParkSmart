package com.lochana.parkingassistant.ui.home;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
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
    private static final BoundingBox SRI_LANKA_BOUNDS = new BoundingBox(
            9.9, 81.9, 5.85, 79.5 // Sri Lanka bounding box
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mapView = root.findViewById(R.id.openStreetMap);
        searchView = root.findViewById(R.id.searchAutoComplete);
        listView = root.findViewById(R.id.listView);

        setupMap();
        //initializeLocations("abc");
        fetchLocations();
        setupSearch();

        addNewLocation = new addNewLocation();
        // add new parking place button
        addNewLocationBtn = root.findViewById(R.id.button3);
        addNewLocationBtn.setOnClickListener(v -> addNewLocation.addNewLocation("New Location Name", 12.3456, 78.9012));

        // test for fetch locations
        testBtn.findViewById(R.id.button4);
        testBtn.setOnClickListener(v -> {fetchLocations();});

        return root;
    }

    // fetching locations from firebase database
    private void fetchLocations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("locations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Access the data for each document
                            String name = document.getString("name");
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            // Log the data (or display it in your UI)
                            Log.d("FirestoreData", "Document ID: " + document.getId());
                            Log.d("FirestoreData", "Name: " + name);
                            Log.d("FirestoreData", "Latitude: " + latitude);
                            Log.d("FirestoreData", "Longitude: " + longitude);

                            // Example: Call a method to handle each location
                            initializeLocations(name); // Modified to pass all three values
                        }
                    } else {
                        Log.w("FirestoreData", "Error getting documents.", task.getException());
                    }
                });
    }
    /**
     * Configures the map settings.
     */
    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(7.5);
        mapView.getController().setCenter(new GeoPoint(7.8731, 80.7718)); // Default center: Sri Lanka
        mapView.setScrollableAreaLimitDouble(SRI_LANKA_BOUNDS);
        mapView.setMinZoomLevel(5.0);
        mapView.setMaxZoomLevel(20.0);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
    }

    /**
     * Initializes predefined locations for search suggestions.
     */
    private void initializeLocations(String locationName) {
        locationNames = new ArrayList<>();
        locationNames.add(locationName);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, locationNames);
        searchView.setAdapter(adapter); // Set adapter for suggestions
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

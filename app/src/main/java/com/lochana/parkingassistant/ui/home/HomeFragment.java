package com.lochana.parkingassistant.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lochana.parkingassistant.AppDatabase;
import com.lochana.parkingassistant.ExistingParkingBottomSheet;
import com.lochana.parkingassistant.GeofenceBroadcastReceiver;
import com.lochana.parkingassistant.InfoBanner;
import com.lochana.parkingassistant.Location;
import com.lochana.parkingassistant.LocationHelper;
import com.lochana.parkingassistant.LocationOverlayManager;
import com.lochana.parkingassistant.MapHandler;
import com.lochana.parkingassistant.NavigationHelper;
import com.lochana.parkingassistant.NearestParkingFinder;
import com.lochana.parkingassistant.NewParkingDetailsBottomSheet;
import com.lochana.parkingassistant.ParkingLocationEntity;
import com.lochana.parkingassistant.R;
import com.lochana.parkingassistant.RouteDrawer;
import com.lochana.parkingassistant.addNewLocation;
import com.lochana.parkingassistant.ParkingLocationHelper; // Import the helper class

import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements MapEventsReceiver { // Implement MapEventsReceiver

    private MapView mapView;
    private AutoCompleteTextView searchView;
    private ArrayAdapter<String> adapter;
    private Button addNewLocationBtn, nav_btn, layerChanger;
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
    private boolean isMenuOpen = false;
    private Polygon userAccuracyCircle;
    private int currentLayer;
    private boolean isFollowingUser = true;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // geofencing setup
        geofencingClient = LocationServices.getGeofencingClient(requireContext());

        mapView = root.findViewById(R.id.openStreetMap);
        searchView = root.findViewById(R.id.searchAutoComplete);
        distanceBanner = root.findViewById(R.id.distanceBannar);
        nearestParkingBtn = root.findViewById(R.id.locate_user4);
        allParksBtn = root.findViewById(R.id.all_parks_btn);
        saveCurrentSpot = root.findViewById(R.id.saveCurrentSpot);
        Button mainFab = root.findViewById(R.id.controlBtn);
        Button refreshBtn = root.findViewById(R.id.refreshBtn);
        layerChanger = root.findViewById(R.id.layerChangeBtn);
        currentLayer = 0;

        // bottom buttons
        Button locateUserBtn = root.findViewById(R.id.locate_user2);
        Button addParkingBtn = root.findViewById(R.id.add_new_parking_place_button2);
        Button nearestParkBtn = root.findViewById(R.id.locate_user3);
        Button refreshBtn2 = root.findViewById(R.id.refreshBtn2);

        Button infoBtn = root.findViewById(R.id.infoBtn);
        infoBtn.setOnClickListener(v -> {
            View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.pinpoint_details_bottomsheet, null);
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        });

        locateUserBtn.setOnClickListener(v -> locateUser());
        addParkingBtn.setOnClickListener(v -> addNewParking());
        nearestParkBtn.setOnClickListener(v -> locateNearestParking());

        // Initialize LocationHelper
        locationHelper = new LocationHelper(requireContext());

        // Initialize ParkingLocationHelper
        parkingLocationHelper = new ParkingLocationHelper(requireContext(), mapView, new addNewLocation(requireContext()), this);
        locationOverlayManager = new LocationOverlayManager(requireContext(), mapView, locationHelper, selectedDestination, new addNewLocation(requireContext()), this, new ParkingLocationHelper(requireContext(), mapView, new addNewLocation(requireContext()), this)); // Initialize the marker manager

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

        addNewLocation = new addNewLocation(requireContext());
        // add new parking place button
        addNewLocationBtn = root.findViewById(R.id.add_new_parking_place_button);
        addNewLocationBtn.setOnClickListener(v -> addNewParking());

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
        setupRefreshButton(refreshBtn);
        setupRefreshButton(refreshBtn2);

        // rotate to user heading direction
        locationHelper.setHeadingListener(azimuth -> {
            mapView.setMapOrientation(-azimuth);
            mapView.invalidate();
            if (userMarker != null) {
                userMarker.setRotation(azimuth + mapView.getMapOrientation());
                mapView.invalidate();
            }
        });

        return root;
    }

    // advance search function
    public void openSearchDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.search_dialog, null);
        bottomSheetDialog.setContentView(dialogView);

// Initialize your views
        Chip checkPlenty = dialogView.findViewById(R.id.checkPlenty);
        Chip checkLimited = dialogView.findViewById(R.id.checkLimited);
        Chip checkNoSpace = dialogView.findViewById(R.id.checkNoSpace);
        Chip checkUnknown = dialogView.findViewById(R.id.checkUnknown);
        Button btnSearch = dialogView.findViewById(R.id.searchBtn2);
        RecyclerView recyclerResults = dialogView.findViewById(R.id.recyclerResults);

// Show the dialog
        bottomSheetDialog.show();

// Set up RecyclerView layout manager
        recyclerResults.setLayoutManager(new LinearLayoutManager(requireContext()));

// Set search button click listener
        btnSearch.setOnClickListener(v -> {
            // Build your filter list
            List<String> selectedAvailabilities = new ArrayList<>();
            if (checkPlenty.isChecked()) selectedAvailabilities.add("@string/plenty_of_space");
            if (checkLimited.isChecked()) selectedAvailabilities.add("@string/limited_space");
            if (checkNoSpace.isChecked()) selectedAvailabilities.add("@string/no_space");
            if (checkUnknown.isChecked()) selectedAvailabilities.add("@string/unknown_availability");

            // Now you can use selectedAvailabilities list for your filtering logic
        });

    }

    // refresh maps locations
    private void setupRefreshButton(Button button) {
        button.setOnClickListener(v -> {
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
        ExistingParkingBottomSheet bottomSheet = new ExistingParkingBottomSheet(locations, mapView);
        bottomSheet.show(requireFragmentManager(), bottomSheet.getTag());
        }

    // locate nearest parking
    private void locateNearestParking() {
        try {
            GeoPoint userLocation = locationHelper.getUserLocation();
            Location nearest = NearestParkingFinder.findNearestParking(userLocation.getLatitude(), userLocation.getLongitude(), locations);

            Log.d("NearestLocation", "Nearest Location: " + nearest.getName() + ", Lat: " + nearest.getLatitude() + ", Lon: " + nearest.getLongitude());

            // show the navigate_to_bottomsheet dialog
            View nearest_location_bottomsheet = LayoutInflater.from(requireContext()).inflate(R.layout.navigat_to_bottomsheet, null);
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
            bottomSheetDialog.setContentView(nearest_location_bottomsheet);

            TextView nearestLocation = nearest_location_bottomsheet.findViewById(R.id.nearestLocation);
            String nearestLocationName = "'"+nearest.getName()+"'";
            nearestLocation.setText(nearestLocationName);

            Button navigateBtn = nearest_location_bottomsheet.findViewById(R.id.buttonSave2);
            navigateBtn.setOnClickListener(v -> {
                try {
                    RouteDrawer.fetchRoute(userLocation, new GeoPoint(nearest.getLatitude(), nearest.getLongitude()), mapView);
                    //GeoPoint userLocation = locationHelper.getUserLocation();
                    bottomSheetDialog.dismiss();
                } catch (Exception e) {
                    Log.d("locateNearestParking", "Error fetching route: " + e.getMessage());
                }
            });

            Button cancelBtn = nearest_location_bottomsheet.findViewById(R.id.buttonSave3);
            cancelBtn.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.show();

        } catch (Exception e) {
            Log.d("locateNearestParking", "Error finding nearest location: " + e.getMessage());
        }
    }

    // add new parking
    private void addNewParking() {
        parkingLocationHelper.startAddingParking(false);
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
                                boolean type = Boolean.TRUE.equals(document.getBoolean("type"));
                                String user = document.getString("user");
                                String nickName = document.getString("userName");

                                if (description == null) {
                                    description = "No description available";
                                }

                                Log.d("FirestoreData", "Name: " + name + ", Lat: " + latitude + ", Lon: " + longitude);

                                Location location = new Location(name, latitude, longitude, availability, rating, price, document.getId(), description, type, user, nickName);
                                // add location names to list
                                locations.add(location);

                                // 👉 Add Geofence for this location
                                addGeofence(location);
                            }

                            //initializeLocations(locations);
                            //locationOverlayManager.addLocationMarkers(locations);

                            // load privet parking
                            try {
                                AppDatabase localDb = AppDatabase.getInstance(requireContext());

                                List<ParkingLocationEntity> localList = localDb.parkingLocationDao().getAllLocations();

                                for (ParkingLocationEntity entity : localList) {
                                    String name = entity.name;
                                    Double latitude = entity.latitude;
                                    Double longitude = entity.longitude;
                                    String availability = entity.availability;
                                    Integer rating = entity.rating;
                                    Double price = entity.price;
                                    String description = entity.description;
                                    boolean type = true; // or use `entity.type` if you saved this as a field
                                    String user = entity.user;
                                    String nickName = entity.user;

                                    if (description == null) {
                                        description = "No description available";
                                    }

                                    Log.d("LocalData", "Name: " + name + ", Lat: " + latitude + ", Lon: " + longitude);

                                    // binding privet parking details to locations adapter
                                    Location location = new Location(name, latitude, longitude, availability, rating, price, null, description, type, user, nickName);
                                    locations.add(location);

                                    // 👉 Add Geofence for this location
                                    addGeofence(location);
                                }
                            } catch (Exception e) {
                                Log.d("fetch locations local", "error " + e.getMessage());
                            }

                            locationOverlayManager.addLocationMarkers(locations);
                            initializeLocations(locations);

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

    private void addGeofence(Location location) {
        try {
            if (!hasLocationPermissions()) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                }, 1001);
                Log.d("Geofence", "Permission not granted");
                return;
            }

            if (location.getLatitude() == 0.0 || location.getLongitude() == 0.0) return;

            Geofence geofence = new Geofence.Builder()
                    .setRequestId(location.getName())
                    .setCircularRegion(location.getLatitude(), location.getLongitude(), 10)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build();

            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build();

            Intent intent = new Intent(requireContext(), GeofenceBroadcastReceiver.class);
            PendingIntent pendingIntent =PendingIntent.getBroadcast(
                    requireContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            int apiAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext());
            Log.d("Geofence", "Google Play Services status: " + apiAvailable);

//            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
//                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(requireActivity(),
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
//                        1001);
//                return;
//            }

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Geofence", "Permission not granted");
                return;
            }
            else{
                Log.d("Geofence", "Permission granted");
            }

            if (pendingIntent == null){
                Log.d("Geofence", "pendingIntent is null");
            }
            assert pendingIntent != null;

            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnSuccessListener(aVoid -> Log.d("Geofence", "Geofence added for: " + location.getName()))
                    .addOnFailureListener(e -> Log.d("Geofence", "Failed to add geofence", e));
        } catch (Exception e) {
            Log.d("addGeoFence", "error : " + e.getMessage());
        }
    }

    private boolean hasLocationPermissions() {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
        // Create and add the rotation gesture overlay
        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(mapView);
        rotationGestureOverlay.setEnabled(true);
        mapView.getOverlays().add(rotationGestureOverlay);

            layerChanger.setOnClickListener(v -> {
                // Cycle between tile sources
                if (currentLayer == 0) {
                    mapView.setTileSource(TileSourceFactory.USGS_SAT);
                    currentLayer = 1;
                } else if (currentLayer == 1) {
                    mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                    currentLayer = 2;
                } else {
                    mapView.setTileSource(TileSourceFactory.MAPNIK);
                    currentLayer = 0;
                }
                mapView.invalidate(); // Refresh map
            });

        if (locationHelper.checkLocationPermission()) {
            locateUser();
        } else {
            locationHelper.requestLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE);
        }

        mapView.setMinZoomLevel(5.0);
        mapView.setMaxZoomLevel(23.0);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(mapEventsOverlay); // Add MapEventsOverlay

        /// update user location when changed
        locationHelper.startContinuousLocationUpdates(location -> {
            GeoPoint userPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            locationHelper.setAccuracy(location.getAccuracy());
            updateUserMarkerPosition(userPoint);
        });

        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN ||
                        event.getAction() == MotionEvent.ACTION_MOVE) {
                    stopAutoRotation();
                }

//                    // Stop rotating the map
//                    //mapView.setMapOrientation(0);
//                    mapView.invalidate();
//
//                    // Continue rotating the marker to match azimuth (relative to North)
//                    locationHelper.setHeadingListener(azimuth -> {
//                        if (userMarker != null) {
//                            userMarker.setRotation(-azimuth);
//                            mapView.invalidate();
//                        }
//                    });

                return false; // Let MapView also handle the event normally
            }
        });

        }
            catch (Exception e) {
                Toast.makeText(requireContext(), "Error setting up map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAutoRotation() {
        locationHelper.stopCompass();
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

    @SuppressLint("MissingPermission")
    private void locateUser() {
        locationHelper.startCompass();
        if (!locationHelper.checkLocationPermission()) {
            locationHelper.requestLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        locationHelper.getFusedLocationClient().getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        GeoPoint userPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        locationHelper.setAccuracy(location.getAccuracy()); // store accuracy inside LocationHelper
                        updateUserMarkerPosition(userPoint);
                        mapView.getController().animateTo(userPoint);
                        mapView.getController().setZoom(20.0);
                    } else {
                        // set a dialog to remind user to enable location
                        showLocationEnableDialog();
                        //Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLocationEnableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enable Location")
                .setMessage("This app needs location services to work properly. Please turn on location.")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open location settings
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    private void updateUserMarkerPosition(GeoPoint point) {
        try {
            if (userMarker == null) {
                userMarker = new Marker(mapView);
//                userMarker.setIcon(getResources().getDrawable(R.drawable.userlocation));
                userMarker.setIcon(getResources().getDrawable(R.drawable.north_arrow));
                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                double accuracy = locationHelper.getAccuracy();
                userMarker.setTitle("You're Here on accuracy to " + String.format("%.2f", accuracy) + "m!");
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
                userAccuracyCircle.setStrokeWidth(0f);
                mapView.getOverlays().add(userAccuracyCircle); // Add only once
            }

            double accuracy = locationHelper.getAccuracy();
            userAccuracyCircle.setPoints(Polygon.pointsAsCircle(userLocation, accuracy)); // Just update points
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
        locationHelper.startCompass();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        //locationHelper.stopLocationUpdates();
        locationHelper.stopCompass();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        parkingLocationHelper.handleMapTap(p);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        //Toast.makeText(requireContext(), "Long Press at: " + p.getLatitude() + ", " + p.getLongitude(), Toast.LENGTH_SHORT).show();
        parkingLocationHelper.startAddingParking(true);
        parkingLocationHelper.handleMapTap(p);

        return true; // Return true to indicate the event was handled
    }
}
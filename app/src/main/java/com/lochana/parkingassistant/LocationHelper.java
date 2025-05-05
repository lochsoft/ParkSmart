package com.lochana.parkingassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.util.GeoPoint;

public class LocationHelper {

    private final Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private float accuracy = 50f; // default 50m if no data yet

    public LocationHelper(Context context) {
        this.context = context;
        initFusedLocation();
    }

    public void initFusedLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    // Request one accurate fresh location update like Google Maps
    @SuppressLint("MissingPermission")
    public void getAccurateLocation(OnSuccessListener<Location> onSuccessListener) {
        if (!checkLocationPermission()) {
            Toast.makeText(context, "Location permission not granted.", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) {
                    Toast.makeText(context, "Unable to get location.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Location location = locationResult.getLastLocation();
                Log.d("FreshUserLocation", "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                onSuccessListener.onSuccess(location);

                // Stop updates after receiving one location
                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
        };

        // Start location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, context.getMainLooper());
    }

    public boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission(Fragment fragment, int requestCode) {
        ActivityCompat.requestPermissions(fragment.requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }

    // Optional: to get last known location if needed as a fallback
    @SuppressLint("MissingPermission")
    public void getLastKnownLocation(OnSuccessListener<Location> onSuccessListener) {
        if (!checkLocationPermission()) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(e -> Toast.makeText(context, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Optional: to fetch old location synchronously (not recommended)
    public GeoPoint getUserLocation() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) return null;

        if (!checkLocationPermission()) return null;

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation == null) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (lastKnownLocation != null) {
            Log.d("UserLocation", "Lat: " + lastKnownLocation.getLatitude() + ", Lon: " + lastKnownLocation.getLongitude());
            return new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        } else {
            Toast.makeText(context, "Could not get user location", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // Stop any ongoing location updates
    public void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public FusedLocationProviderClient getFusedLocationClient() {
        return fusedLocationClient;
    }

}

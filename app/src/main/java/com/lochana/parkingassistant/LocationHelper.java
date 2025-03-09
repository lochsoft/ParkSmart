package com.lochana.parkingassistant;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.util.GeoPoint;

public class LocationHelper {

    private Context context; // Store the context

    public LocationHelper(Context context) {
        this.context = context;
    }

    public boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission(Fragment fragment, int requestCode) {
        ActivityCompat.requestPermissions(fragment.requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }

    public GeoPoint getUserLocation() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) return null;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null; // Permission already handled in checkLocationPermission()
        }

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
}
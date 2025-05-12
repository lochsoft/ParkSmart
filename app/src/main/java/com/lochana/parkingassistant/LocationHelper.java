package com.lochana.parkingassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
    private LocationCallback singleLocationCallback;
    private LocationCallback continuousLocationCallback;
    private float accuracy = 50f; // default 50m if no data yet

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity;
    private float[] geomagnetic;
    private HeadingListener headingListener;
    private float currentAzimuth = 0f;
    private static final float ALPHA = 0.25f;
    private static final float MIN_DIFF_TO_UPDATE = 5.0f;

    public interface HeadingListener {
        void onHeadingChanged(float azimuth);
    }
    public LocationHelper(Context context) {
        this.context = context;
        initFusedLocation();
        initCompassSensor();
    }

    public void initFusedLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    private void initCompassSensor() {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
    }

    // ✅ One-time fresh accurate location
    @SuppressLint("MissingPermission")
    public void getAccurateLocation(OnSuccessListener<Location> onSuccessListener) {
        if (!checkLocationPermission()) {
            Toast.makeText(context, "Location permission not granted.", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMaxUpdates(1)
                .build();

        singleLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) {
                    Toast.makeText(context, "Unable to get location.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Location location = locationResult.getLastLocation();
                Log.d("FreshUserLocation", "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                accuracy = location.getAccuracy();
                onSuccessListener.onSuccess(location);

                // stop after getting one location
                fusedLocationClient.removeLocationUpdates(singleLocationCallback);
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, singleLocationCallback, context.getMainLooper());
    }

    // ✅ Continuous location updates like Google Maps
    @SuppressLint("MissingPermission")
    public void startContinuousLocationUpdates(OnSuccessListener<Location> onLocationChanged) {
        if (!checkLocationPermission()) {
            Toast.makeText(context, "Location permission not granted.", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000)
                .setMinUpdateIntervalMillis(2000)
                .setMinUpdateDistanceMeters(5f)
                .build();

        continuousLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Log.d("LiveLocation", "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                    accuracy = location.getAccuracy();
                    onLocationChanged.onSuccess(location);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, continuousLocationCallback, context.getMainLooper());
    }

    // ✅ Stop continuous updates (called in onPause/onDestroy)
    public void stopLocationUpdates() {
        if (fusedLocationClient != null) {
            if (singleLocationCallback != null)
                fusedLocationClient.removeLocationUpdates(singleLocationCallback);
            if (continuousLocationCallback != null)
                fusedLocationClient.removeLocationUpdates(continuousLocationCallback);
        }
    }

    // Permission checker
    public boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Request permission dialog
    public void requestLocationPermission(Fragment fragment, int requestCode) {
        ActivityCompat.requestPermissions(fragment.requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }
    public void requestLocationPermission(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }

    // Fallback: last known location (optional)
    @SuppressLint("MissingPermission")
    public void getLastKnownLocation(OnSuccessListener<Location> onSuccessListener) {
        if (!checkLocationPermission()) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(e -> Toast.makeText(context, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Synchronous location fetch via LocationManager (not recommended)
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

    public void startCompass() {
        if (sensorManager != null && accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stopCompass() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    public void setHeadingListener(HeadingListener listener) {
        this.headingListener = listener;
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

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                gravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                geomagnetic = event.values;

            if (gravity != null && geomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    float rawAzimuth = (float) Math.toDegrees(orientation[0]);
                    if (rawAzimuth < 0) rawAzimuth += 360;

                    // Apply low-pass filter
                    float filteredAzimuth = currentAzimuth + ALPHA * (rawAzimuth - currentAzimuth);

                    // Only notify listener if heading changes more than MIN_DIFF_TO_UPDATE
                    if (Math.abs(filteredAzimuth - currentAzimuth) >= MIN_DIFF_TO_UPDATE) {
                        currentAzimuth = filteredAzimuth;
                        if (headingListener != null) {
                            headingListener.onHeadingChanged(currentAzimuth);
                        }
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };


}

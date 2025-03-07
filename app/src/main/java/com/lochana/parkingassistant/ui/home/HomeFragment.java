package com.lochana.parkingassistant.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import com.lochana.parkingassistant.R;

public class HomeFragment extends Fragment {

    private MapView mapView;
    private static final String KEY_ZOOM_LEVEL = "zoomLevel";
    private static final String KEY_CENTER_LATITUDE = "centerLatitude";
    private static final String KEY_CENTER_LONGITUDE = "centerLongitude";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize MapView
        mapView = root.findViewById(R.id.openStreetMap);
        mapView.setTileSource(TileSourceFactory.MAPNIK); // Set the default tile source
        mapView.setBuiltInZoomControls(true); // Enable zoom controls
        mapView.setMultiTouchControls(true); // Enable multi-touch controls

        return root;
    }

    @Override
    public void onViewStateRestored(@NonNull Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        // Restore the map state if savedInstanceState is not null
        if (savedInstanceState != null) {
            double centerLatitude = savedInstanceState.getDouble(KEY_CENTER_LATITUDE);
            double centerLongitude = savedInstanceState.getDouble(KEY_CENTER_LONGITUDE);
            int zoomLevel = savedInstanceState.getInt(KEY_ZOOM_LEVEL);

            // Restore the map center and zoom level
            mapView.getController().setCenter(new GeoPoint(centerLatitude, centerLongitude));
            mapView.getController().setZoom(zoomLevel);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the map state
        if (mapView != null) {
            outState.putInt(KEY_ZOOM_LEVEL, mapView.getZoomLevel());
            GeoPoint center = (GeoPoint) mapView.getMapCenter();
            outState.putDouble(KEY_CENTER_LATITUDE, center.getLatitude());
            outState.putDouble(KEY_CENTER_LONGITUDE, center.getLongitude());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume(); // Necessary for OSMDroid
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause(); // Necessary for OSMDroid
        }
    }
}
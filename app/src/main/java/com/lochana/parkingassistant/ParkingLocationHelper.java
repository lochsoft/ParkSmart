// add new parking

package com.lochana.parkingassistant;

import static android.app.ProgressDialog.show;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lochana.parkingassistant.addNewLocation;
import com.lochana.parkingassistant.ui.home.HomeFragment;

public class ParkingLocationHelper{

    private HomeFragment homeFragment;
    private Context context;
    private MapView mapView;
    private boolean isAddingParking = false;
    private Marker newParkingMarker;
    private addNewLocation addNewLocation;

    public ParkingLocationHelper(Context context, MapView mapView, addNewLocation addNewLocation, HomeFragment homeFragment) {
        this.context = context;
        this.mapView = mapView;
        this.addNewLocation = addNewLocation;
        this.homeFragment = homeFragment;
    }

    public void startAddingParking() {
        Log.d("stratAddingParking", "starting adding parking");
        try {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Add New Parking Location")
                    .setMessage("Tap on the map to add a new parking location")
                    .setPositiveButton("OK", (dialog, which) -> {
                        isAddingParking = true;
                        newParkingMarker = new Marker(mapView);
                        newParkingMarker.setIcon(context.getResources().getDrawable(R.drawable.parking_sign));
                        newParkingMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        mapView.getOverlays().add(newParkingMarker);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
            //Toast.makeText(context, "Tap on the map to add a parking location", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.d("startAddingParking", "Error starting adding parking" + e.getMessage());
        }
    }

    public boolean isAddingParking() {
        return isAddingParking;
    }

    public void setAddingParking(boolean addingParking) {
        isAddingParking = addingParking;
    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }

    public void handleMapTap(GeoPoint p) {
        Log.d("handleMapTap", "handleMapTap called");
        if (isAddingParking) {
            newParkingMarker.setPosition(p);
            mapView.invalidate();
            showParkingDetailsDialog(p);
        }
    }

    private void showParkingDetailsDialog(GeoPoint point) {
        NewParkingDetailsBottomSheet bottomSheet = new NewParkingDetailsBottomSheet(
                context, point, addNewLocation, mapView, newParkingMarker, this, null);
        bottomSheet.show(((HomeFragment) homeFragment).getChildFragmentManager(), "ParkingDetailsBottomSheet");
    }

    public void cancelAddingParking() {
        if (isAddingParking) {
            isAddingParking = false;
            if (newParkingMarker != null) {
                newParkingMarker.remove(mapView);
                mapView.invalidate();
            }
        }
    }
}
// add new parking

package com.lochana.parkingassistant;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

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
        isAddingParking = true;
        Toast.makeText(context, "Tap on the map to add a parking location", Toast.LENGTH_SHORT).show();

        newParkingMarker = new Marker(mapView);
        newParkingMarker.setIcon(context.getResources().getDrawable(R.drawable.parking_sign));
        newParkingMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(newParkingMarker);
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
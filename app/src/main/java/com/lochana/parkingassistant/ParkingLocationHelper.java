package com.lochana.parkingassistant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.lochana.parkingassistant.addNewLocation; // Assuming this is your Firebase upload class
import com.lochana.parkingassistant.ui.home.HomeFragment;

public class ParkingLocationHelper{

    private HomeFragment homeFragment; // Reference to HomeFragment
    private Context context;
    private MapView mapView;
    private boolean isAddingParking = false;
    private Marker newParkingMarker;
    private addNewLocation addNewLocation; // Firebase upload class instance

    public ParkingLocationHelper(Context context, MapView mapView, addNewLocation addNewLocation, HomeFragment homeFragment) {
        this.context = context;
        this.mapView = mapView;
        this.addNewLocation = addNewLocation;
        this.homeFragment = homeFragment; // Receive HomeFragment instance
    }

    public void startAddingParking() {
        isAddingParking = true;
        Toast.makeText(context, "Tap on the map to add a parking location", Toast.LENGTH_SHORT).show();

        newParkingMarker = new Marker(mapView);
        newParkingMarker.setIcon(context.getResources().getDrawable(R.drawable.parking_sign));
        newParkingMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(newParkingMarker);
    }

    public boolean isAddingParking() {        return isAddingParking;
    }

    public void handleMapTap(GeoPoint p) {
        if (isAddingParking) {
            newParkingMarker.setPosition(p);
            mapView.invalidate();
            showParkingDetailsDialog(p);
        }
    }

    private void showParkingDetailsDialog(GeoPoint point) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Parking Details");

        final EditText input = new EditText(context);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                addNewLocation.addNewLocation(name, point.getLatitude(), point.getLongitude());
                isAddingParking = false;
                newParkingMarker.remove(mapView);
                mapView.invalidate();
                Toast.makeText(context, "Parking location added", Toast.LENGTH_SHORT).show();
                if (homeFragment != null) {
                    homeFragment.fetchLocations(); // Call fetchLocations on HomeFragment
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                isAddingParking = false;
                newParkingMarker.remove(mapView);
                mapView.invalidate();
            }
        });

        builder.show();
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
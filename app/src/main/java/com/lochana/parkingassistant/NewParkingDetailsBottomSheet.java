package com.lochana.parkingassistant;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.lochana.parkingassistant.addNewLocation;
import com.lochana.parkingassistant.ui.home.HomeFragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class NewParkingDetailsBottomSheet extends BottomSheetDialogFragment {

    private Context context;
    private GeoPoint selectedPoint;
    private addNewLocation addNewLocation;
    private MapView mapView;
    private Marker newParkingMarker;
    private ParkingLocationHelper parkingLocationHelper; // To access cancelAddingParking and HomeFragment

    private EditText editTextParkingName;
    private Button buttonSave;
    private Button buttonCancel;

    public NewParkingDetailsBottomSheet(Context context, GeoPoint point, addNewLocation addNewLocation, MapView mapView, Marker newParkingMarker, ParkingLocationHelper parkingLocationHelper) {
        this.context = context;
        this.selectedPoint = point;
        this.addNewLocation = addNewLocation;
        this.mapView = mapView;
        this.newParkingMarker = newParkingMarker;
        this.parkingLocationHelper = parkingLocationHelper;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_new_parking_bottom_sheet, container, false);
        editTextParkingName = view.findViewById(R.id.editTextParkingName);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonCancel = view.findViewById(R.id.buttonCancel);

        buttonSave.setOnClickListener(v -> {
            String name = editTextParkingName.getText().toString();
            if (!name.isEmpty()) {
                addNewLocation.addNewLocation(name, selectedPoint.getLatitude(), selectedPoint.getLongitude());
                parkingLocationHelper.setAddingParking(false); // Update the flag in the helper
                newParkingMarker.remove(mapView);
                mapView.invalidate();
                Toast.makeText(context, "Parking location added", Toast.LENGTH_SHORT).show();
                HomeFragment homeFragment = parkingLocationHelper.getHomeFragment();
                if (homeFragment != null) {
                    homeFragment.fetchLocations();
                }
                dismiss(); // Dismiss the bottom sheet
            } else {
                Toast.makeText(context, "Please enter a parking name", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancel.setOnClickListener(v -> {
            parkingLocationHelper.setAddingParking(false); // Update the flag in the helper
            newParkingMarker.remove(mapView);
            mapView.invalidate();
            dismiss(); // Dismiss the bottom sheet
        });

        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // Handle the event when the bottom sheet is dismissed (including tapping outside)
        parkingLocationHelper.setAddingParking(false);
        if (newParkingMarker != null) {
            newParkingMarker.remove(mapView);
            mapView.invalidate();
            Toast.makeText(context, "Adding parking cancelled", Toast.LENGTH_SHORT).show();
        }
    }

}
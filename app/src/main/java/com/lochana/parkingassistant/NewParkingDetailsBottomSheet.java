// show the bottom sheet for adding new parking location
package com.lochana.parkingassistant;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
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

    private EditText editTextParkingName, editTextAvailability, price;
    private Button buttonSave;
    private Button buttonCancel;
    private RatingBar ratingBar;

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
        editTextAvailability = view.findViewById(R.id.editTextParkingName2);
        price = view.findViewById(R.id.editTextParkingName3);
        ratingBar = view.findViewById(R.id.ratingBar);

        buttonSave.setOnClickListener(v -> {
            String name = editTextParkingName.getText().toString();
            String availability = editTextAvailability.getText().toString();
            Integer price = Integer.parseInt(this.price.getText().toString());
            Integer rating = (int) ratingBar.getRating();

            if (!name.isEmpty()) {
                addNewLocation.addNewLocation(name, selectedPoint.getLatitude(), selectedPoint.getLongitude(), availability, price, rating);
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

}
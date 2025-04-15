// show the bottom sheet for adding new parking location
package com.lochana.parkingassistant;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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
    private ExistingParkingData existingPoint;

    EditText editTextParkingName;
    EditText editTextAvailability;
    EditText price;
    private Button buttonSave;
    private Button buttonCancel;
    RatingBar ratingBar;
    private EditText description;

    public NewParkingDetailsBottomSheet(Context context, GeoPoint point, addNewLocation addNewLocation,
                                        MapView mapView, Marker newParkingMarker, ParkingLocationHelper parkingLocationHelper, ExistingParkingData existingPoint) {
        this.context = context;
        this.selectedPoint = point;
        this.addNewLocation = addNewLocation;
        this.mapView = mapView;
        this.newParkingMarker = newParkingMarker;
        this.parkingLocationHelper = parkingLocationHelper;
        this.existingPoint = existingPoint;
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
        description = view.findViewById(R.id.parkingDescription);
        String documentId = existingPoint != null ? existingPoint.getDocumentId() : null;

        // If editing, populate existing data
        if (existingPoint != null) {
            editTextParkingName.setText(existingPoint.getName());
            editTextAvailability.setText(existingPoint.getAvailability());
            price.setText(String.valueOf(existingPoint.getPrice()));
            ratingBar.setRating(existingPoint.getRating());
            description.setText(existingPoint.getDescription());
            buttonSave.setText("Update");
            selectedPoint = new GeoPoint(existingPoint.getLatitude(), existingPoint.getLongitude());
        }

        buttonSave.setOnClickListener(v -> {
            try {
                Log.d("savefunc", "passes save func");
                String name = editTextParkingName.getText().toString().trim();
                String availability = editTextAvailability.getText().toString().trim();
                String priceText = this.price.getText().toString().trim();
                float ratingValue = ratingBar.getRating();
                String descriptionText = description.getText().toString().trim();

                Double price = null;
                Integer rating = null;

                try {
                    if (!priceText.isEmpty()) {
                        price = Double.parseDouble(priceText);
                    }
                } catch (NumberFormatException e) {
                    price = null;
                    Toast.makeText(context, "Price should be a Number!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ratingValue > 0) {
                    rating = (int) ratingValue;
                }

                if (!name.isEmpty() && !availability.isEmpty() && price != null && rating != null) {
                    // All inputs are valid, proceed
                    Log.d("updateLocationTest", name + " " + selectedPoint.getLatitude() + " " + selectedPoint.getLongitude() + " " + availability + " " + price + " " + rating + " " + descriptionText + " " + documentId);
                    addNewLocation.addNewLocation(name, selectedPoint.getLatitude(), selectedPoint.getLongitude(), availability, price, rating, descriptionText, documentId);
                    if (existingPoint == null) {
                        parkingLocationHelper.setAddingParking(false); // Update the flag in the helper
                        newParkingMarker.remove(mapView);
                        mapView.invalidate();

                        Toast.makeText(context, "Parking location added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Parking location updated", Toast.LENGTH_SHORT).show();
                    }
                    HomeFragment homeFragment = parkingLocationHelper.getHomeFragment();
                    if (homeFragment != null) {
                        homeFragment.fetchLocations();
                    }
                    dismiss(); // Dismiss the bottom sheet
                } else {
                    Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("UpdateError","error " + e.getMessage());
            }});

        buttonCancel.setOnClickListener(v -> {
            try {
                if (existingPoint == null) {
                    parkingLocationHelper.setAddingParking(false); // Update the flag in the helper
                    newParkingMarker.remove(mapView);
                }
                mapView.invalidate();
                dismiss(); // Dismiss the bottom sheet
            } catch (Exception e) {
                Log.d("cancelBtn","error" + e.getMessage());
            }
        });

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(null); // Remove default background
            }
        });

        return dialog;
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        try {
            // Only remove marker if it's a new parking location (i.e., not editing existing one)
            if (existingPoint == null && newParkingMarker != null && mapView != null) {
                newParkingMarker.remove(mapView);
                mapView.invalidate();
                parkingLocationHelper.setAddingParking(false); // Reset flag
            }
        } catch (Exception e) {
            Log.d("onDismissError", "Error removing marker on dismiss: " + e.getMessage());
        }
    }


}
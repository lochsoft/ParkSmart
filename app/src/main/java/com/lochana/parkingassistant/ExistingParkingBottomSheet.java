package com.lochana.parkingassistant;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

//public class ExistingParkingBottomSheet extends BottomSheetDialogFragment {
//
//    private List<Location> parkingList;
//    private MapView mapView;
//    private Chip plenty, limited, noSpace, unknown;
//
//    public ExistingParkingBottomSheet(List<Location> parkingList, MapView mapView) {
//        this.parkingList = parkingList;
//        this.mapView = mapView;
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.show_all_parkings, container, false);
//        RecyclerView recyclerView = view.findViewById(R.id.parkingRecyclerView);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.setAdapter(new AllParkings(getContext(), parkingList, mapView, this));
//
//        return view;
//    }
//
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
//
//        dialog.setOnShowListener(dialogInterface -> {
//            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
//            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//            if (bottomSheet != null) {
//                bottomSheet.setBackground(null); // Remove default background
//            }
//        });
//
//        return dialog;
//    }
public class ExistingParkingBottomSheet extends BottomSheetDialogFragment {

    private List<Location> parkingList;
    private MapView mapView;
    private Chip plenty, limited, noSpace, unknown;
    private AllParkings adapter; // Keep reference
    private RecyclerView recyclerView;
    private ChipGroup availabilityChipGroup;
    private TextView emptyInfo;

    public ExistingParkingBottomSheet(List<Location> parkingList, MapView mapView) {
        this.parkingList = parkingList;
        this.mapView = mapView;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_all_parkings, container, false);
        recyclerView = view.findViewById(R.id.parkingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyInfo = view.findViewById(R.id.emptyInfo);

        adapter = new AllParkings(getContext(), parkingList, mapView, this);
        recyclerView.setAdapter(adapter);
        emptyInfo.setText("Total : " + parkingList.size());

        // Initialize chip group
        availabilityChipGroup = view.findViewById(R.id.availabilityChipGroup);
        plenty = view.findViewById(R.id.checkPlenty);
        limited = view.findViewById(R.id.checkLimited);
        noSpace = view.findViewById(R.id.checkNoSpace);
        unknown = view.findViewById(R.id.checkUnknown);

        // Chip listeners
//        plenty.setOnClickListener(v -> filterList("Plenty of Space"));
//        limited.setOnClickListener(v -> filterList("Limited Space"));
//        noSpace.setOnClickListener(v -> filterList("No Space"));
//        unknown.setOnClickListener(v -> filterList("Unknown"));

        /// when no chip is checked
        availabilityChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // No chip selected — reset list
                adapter = new AllParkings(getContext(), parkingList, mapView, this);
                recyclerView.setAdapter(adapter);
                emptyInfo.setText("Total : " + parkingList.size());
            } else {
                int selectedChipId = checkedIds.get(0);  // since single selection
                Chip selectedChip = view.findViewById(selectedChipId);
                filterList(selectedChip.getText().toString());
            }
        });

        return view;
    }

    private void filterList(String availability) {
        List<Location> filteredList = new ArrayList<>();
        for (Location loc : parkingList) {
            if (loc.getAvailability().equals(availability)) {
                filteredList.add(loc);
            }
        }

        adapter = new AllParkings(getContext(), filteredList, mapView, this);
        recyclerView.setAdapter(adapter);

        if (filteredList.isEmpty()) {
            emptyInfo.setText("no matching parking");
        }
        else {
            emptyInfo.setText("Total : " + filteredList.size());
            emptyInfo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(null);
            }
        });

        return dialog;
    }
}



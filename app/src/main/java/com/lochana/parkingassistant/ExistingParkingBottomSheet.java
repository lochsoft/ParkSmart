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

import java.util.List;

public class ExistingParkingBottomSheet extends BottomSheetDialogFragment {

    private List<Location> parkingList;

    public ExistingParkingBottomSheet(List<Location> parkingList) {
        this.parkingList = parkingList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_all_parkings, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.parkingRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new AllParkings(getContext(), parkingList));

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
}

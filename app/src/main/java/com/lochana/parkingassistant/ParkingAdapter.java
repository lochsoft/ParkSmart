package com.lochana.parkingassistant;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {

    private Context context;
    private List<ParkingData> parkingList;

    public ParkingAdapter(Context context, List<ParkingData> parkingList) {
        this.context = context;
        this.parkingList = parkingList;
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.parking_item_layout, parent, false);
        return new ParkingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        try {
            ParkingData data = parkingList.get(position);

            holder.titleText.setText(data.getName());
            holder.priceText.setText(data.getPrice());
            holder.ratingBar.setRating((float) data.getRating());
            holder.removeBtn.setVisibility(View.VISIBLE);

            holder.navBtn.setOnClickListener(v -> {
                NavigationHelper.navigateToSelectedLocation(context, null, new GeoPoint(data.getLatitude(), data.getLongitude()));
            });

            holder.removeBtn.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Remove Parking")
                        .setMessage("Are you sure you want to remove this saved location?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            AppDatabase.getInstance(context).parkingDataDao().delete(data);
                            parkingList.remove(data);
                            notifyDataSetChanged();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

            });
        } catch (Exception e) {
            Log.d("ParkingAdapter", "Error binding view holder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return parkingList.size();
    }

    public static class ParkingViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, priceText;
        RatingBar ratingBar;
        Button navBtn, removeBtn;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.parkingName);
            priceText = itemView.findViewById(R.id.parking_price);
            ratingBar = itemView.findViewById(R.id.parkingRating);
            navBtn = itemView.findViewById(R.id.navigateBtn);
            removeBtn = itemView.findViewById(R.id.remove_btn);
        }
    }
}


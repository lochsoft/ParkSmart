package com.lochana.parkingassistant;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.osmdroid.util.GeoPoint;

import java.util.List;
import java.util.Objects;

public class PrivetParkingAdapter extends RecyclerView.Adapter<PrivetParkingAdapter.ParkingViewHolder> {

    private Context context;
    private List<ParkingLocationEntity> parkingList;

    public PrivetParkingAdapter(Context context, List<ParkingLocationEntity> parkingList) {
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
            ParkingLocationEntity data = parkingList.get(position);

            holder.titleText.setText(data.getName());
            holder.priceText.setText(Double.toString(data.getPrice()));
            holder.ratingBar.setRating((float) data.getRating());
            holder.removeBtn.setVisibility(View.VISIBLE);
            holder.removeBtn.setText("Delete");

            holder.availabilityView.setVisibility(View.VISIBLE);
            if (Objects.equals(data.getAvailability(), "Plenty of Space")) {
                holder.availabilityView.setImageResource(R.drawable.plenty_of_space);
            } else if (Objects.equals(data.getAvailability(), "Limited Space")) {
                holder.availabilityView.setImageResource(R.drawable.low_space);
            } else if (Objects.equals(data.getAvailability(), "No Space")) {
                holder.availabilityView.setImageResource(R.drawable.no_spaces_available);
            } else if (Objects.equals(data.getAvailability(), "Unknown Availability")) {
                holder.availabilityView.setImageResource(R.drawable.unknown_availability);
            }

            holder.navBtn.setOnClickListener(v -> {
                NavigationHelper.navigateToSelectedLocation(context, null, new GeoPoint(data.getLatitude(), data.getLongitude()));
            });

            holder.removeBtn.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Delete Privet Parking")
                        .setMessage("Are you sure you want to delete this Privet location?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            AppDatabase.getInstance(context).parkingLocationDao().delete(data);
                            parkingList.remove(data);
                            notifyDataSetChanged();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

            });
        } catch (Exception e) {
            Log.d("PrivetParkingAdapter", "Error binding view holder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return parkingList.size();
    }

    public static class ParkingViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, priceText, privetIndicator;
        RatingBar ratingBar;
        Button navBtn, removeBtn;
        ImageView availabilityView;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.parkingName);
            priceText = itemView.findViewById(R.id.parking_price);
            ratingBar = itemView.findViewById(R.id.parkingRating);
            navBtn = itemView.findViewById(R.id.navigateBtn);
            removeBtn = itemView.findViewById(R.id.remove_btn);
            availabilityView = itemView.findViewById(R.id.availabilitySignImgView);
        }
    }
}


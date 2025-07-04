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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.List;
import java.util.Objects;

public class AllParkings extends RecyclerView.Adapter<AllParkings.ViewHolder> {

    private List<Location> parkingList;
    private Context context;
    private boolean isPrivateParking;
    private MapView mapView;
    private BottomSheetDialogFragment bottomSheetDialogFragment;

    public AllParkings(Context context, List<Location> parkingList, MapView mapView, BottomSheetDialogFragment bottomSheetDialogFragment) {
        this.context = context;
        this.parkingList = parkingList;
        this.mapView = mapView;
        this.bottomSheetDialogFragment = bottomSheetDialogFragment;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        RatingBar rating;
        Button navigateBtn, locateBtn;
        TextView privateParkingIndicator;
        ImageView availabilityView;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.parkingName);
            price = view.findViewById(R.id.parking_price);
            rating = view.findViewById(R.id.parkingRating);
            navigateBtn = view.findViewById(R.id.navigateBtn);
            privateParkingIndicator = view.findViewById(R.id.private_parking_indicator2);
            locateBtn = view.findViewById(R.id.parkingLocateBtn);
            availabilityView = view.findViewById(R.id.availabilitySignImgView);
        }
    }

    @NonNull
    @Override
    public AllParkings.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.parking_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllParkings.ViewHolder holder, int position) {
        try {
            Location parking = parkingList.get(position);
            holder.name.setText(parking.getName());
            holder.price.setText(parking.getPrice());
            holder.rating.setRating(parking.getRating());
            holder.locateBtn.setVisibility(View.VISIBLE);

            isPrivateParking = parking.getType();
            if (isPrivateParking) {
                holder.privateParkingIndicator.setVisibility(View.VISIBLE);
            } else {
                holder.privateParkingIndicator.setVisibility(View.GONE);
            }

            holder.navigateBtn.setOnClickListener(v -> {
                NavigationHelper.navigateToSelectedLocation(context, null, new GeoPoint(parking.getLatitude(), parking.getLongitude()));
            });
            holder.locateBtn.setOnClickListener(v -> {
                // dismiss the bottom sheet dialog
                try {
                    if (bottomSheetDialogFragment != null) {
                        bottomSheetDialogFragment.dismiss();
                    }
                    mapView.getController().setZoom(25.0);
                    mapView.getController().animateTo(new GeoPoint(parking.getLatitude(), parking.getLongitude()));
                } catch (Exception e) {
                    Log.d("AllParkings", "Error navigating to location: " + e.getMessage());
                }
            });

            holder.availabilityView.setVisibility(View.VISIBLE);
            if (Objects.equals(parking.getAvailability(), "Plenty of Space")) {
                holder.availabilityView.setImageResource(R.drawable.plenty_of_space);
            } else if (Objects.equals(parking.getAvailability(), "Limited Space")) {
                holder.availabilityView.setImageResource(R.drawable.low_space);
            } else if (Objects.equals(parking.getAvailability(), "No Space")) {
                holder.availabilityView.setImageResource(R.drawable.no_spaces_available);
            } else if (Objects.equals(parking.getAvailability(), "Unknown Availability")) {
                holder.availabilityView.setImageResource(R.drawable.unknown_availability);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Error binding view holder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("AllParkings", "Error binding view holder:" + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return parkingList.size();
    }
}


package com.lochana.parkingassistant;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public class AllParkings extends RecyclerView.Adapter<AllParkings.ViewHolder> {

    private List<Location> parkingList;
    private Context context;

    public AllParkings(Context context, List<Location> parkingList) {
        this.context = context;
        this.parkingList = parkingList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        RatingBar rating;
        Button navigateBtn;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.parkingName);
            price = view.findViewById(R.id.parking_price);
            rating = view.findViewById(R.id.parkingRating);
            navigateBtn = view.findViewById(R.id.navigateBtn);
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

            holder.navigateBtn.setOnClickListener(v -> {
                NavigationHelper.navigateToSelectedLocation(context, null, new GeoPoint(parking.getLatitude(), parking.getLongitude()));
            });
        } catch (Exception e) {
            Toast.makeText(context, "Error binding view holder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return parkingList.size();
    }
}


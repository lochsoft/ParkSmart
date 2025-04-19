package com.lochana.parkingassistant;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "parking_data")
public class ParkingData {

    @PrimaryKey
    @NonNull
    private String documentId;

    private String name;
    private double latitude;
    private double longitude;
    private String availability;
    private String price;
    private double rating;
    private String description;

    public ParkingData(@NonNull String documentId, String name, double latitude, double longitude,
                       String availability, String price, double rating, String description) {
        this.documentId = documentId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availability = availability;
        this.price = price;
        this.rating = rating;
        this.description = description;
    }

    @NonNull
    public String getDocumentId() {
        return documentId;

    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;

    }
    public double getLongitude() {
        return longitude;
    }

    public String getAvailability() {
        return availability;
    }

    public String getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }
}

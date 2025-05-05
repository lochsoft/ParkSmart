package com.lochana.parkingassistant;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "parking_locations")
public class ParkingLocationEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double latitude;
    public double longitude;
    public String availability;
    public double price;
    public Integer rating;
    public String description;
    public boolean type;

    public ParkingLocationEntity(String name, double latitude, double longitude,
                       String availability, double price, Integer rating, String description) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availability = availability;
        this.price = price;
        this.rating = rating;
        this.description = description;
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

    public double getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }
}

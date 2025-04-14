package com.lochana.parkingassistant;

import android.util.Log;

public class ExistingParkingData {
    private String name;
    private double latitude;
    private double longitude;
    private String availability;
    private String price;
    private int rating;
    private String description;
    private String documentId;

    public ExistingParkingData(String name, double latitude, double longitude, String availability,
                               String price, int rating, String description, String documentId) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availability = availability;
        this.price = price;
        this.rating = rating;
        this.description = description;
        this.documentId = documentId;

        Log.d("ExistingParkingData", name + " " + latitude + " " + longitude + " " + availability + " " + price + " " + rating + " " + description);
    }

    // Getters
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getAvailability() { return availability; }
    public String getPrice() { return price; }
    public int getRating() { return rating; }
    public String getDescription() { return description;}
    public String getDocumentId() { return documentId;}

    // Optional: Setters if you want it to be mutable
}


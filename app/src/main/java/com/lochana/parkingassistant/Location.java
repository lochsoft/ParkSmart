package com.lochana.parkingassistant;
public class Location {
    private final String description;
    private String name;
    private double latitude;
    private double longitude;
    private String availability;
    private Integer rating;
    private double price;
    private String documentid;
    private boolean type;

    public Location(String name, double latitude, double longitude, String availability, Integer rating, double price, String documentid, String description, boolean type) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availability = availability;
        this.rating = rating;
        this.price = price;
        this.documentid = documentid;
        this.description = description;
        this.type = type;
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

    public String getAvailability(){
        return availability;
    }

    public Integer getRating(){
        return rating;
    }

    public String getPrice(){
        return String.valueOf(price);
    }

    public String getDocumentid(){
        return documentid;
    }

    public String getDescription() {
        return description;
    }

    public boolean getType() {
        return type;
    }
}
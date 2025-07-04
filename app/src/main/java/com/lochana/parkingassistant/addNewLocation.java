package com.lochana.parkingassistant;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class addNewLocation {

    private FirebaseFirestore db;
    private AppDatabase localDb;
    private FirebaseAuth mAuth;

    public addNewLocation(Context context) {
        db = FirebaseFirestore.getInstance();
        localDb = AppDatabase.getInstance(context);
        mAuth = FirebaseAuth.getInstance();
    }

    public void addNewLocation(String name, double latitude, double longitude, String availability, double price, Integer rating, String description, @Nullable String documentId, boolean type) {
        // if a private parking
        if (type){
//            ParkingLocationEntity location = new ParkingLocationEntity();
//            location.name = name;
//            location.latitude = latitude;
//            location.longitude = longitude;
//            location.availability = availability;
//            location.price = price;
//            location.rating = rating;
//            location.description = description;
//            location.type = type;

            ParkingLocationEntity location = new ParkingLocationEntity(
                    name,
                    latitude,
                    longitude,
                    availability,
                    price,
                    rating,
                    description
            );

            localDb.parkingLocationDao().insert(location);
            Log.d("LocalSave", "Saved location locally: " + name);
        }
        else {
            try {
                // Create a new document with a generated ID
                Log.d("FirebaseHelper", "Adding new location with name: " + name);
                Map<String, Object> location = new HashMap<>();
                location.put("name", name);
                location.put("latitude", latitude);
                location.put("longitude", longitude);
                location.put("availability", availability);
                location.put("price", price);
                location.put("rating", rating);
                location.put("description", description);
                location.put("user", mAuth.getCurrentUser().getUid());
                location.put("userName", mAuth.getCurrentUser().getDisplayName());

                if (documentId == null) {
                    // Add a new document with a generated ID
                    db.collection("locations")
                            .add(location)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("FirebaseHelper", "DocumentSnapshot added with ID: " + documentReference.getId());
                                // Optionally handle successful addition (e.g., show a toast)
                                // add points to user
                                db.collection("users")
                                        .document(mAuth.getCurrentUser().getUid())
                                        .update("points", FieldValue.increment(10));
                            })
                            .addOnFailureListener(e -> {
                                Log.w("FirebaseHelper", "Error adding document", e);
                                // Optionally handle failure (e.g., show an error message)
                            });
                } else { // updating a location
                    Log.d("FirebaseHelper", "Updating document with ID: " + documentId);
                    db.collection("locations")
                            .document(documentId)
                            .set(location)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("FirebaseHelper", "Document updated successfully");
                                // add points to user
                                db.collection("users")
                                        .document(mAuth.getCurrentUser().getUid())
                                        .update("points", FieldValue.increment(5));
                            })
                            .addOnFailureListener(e -> {
                                Log.w("FirebaseHelper", "Error updating document", e);
                            });
                }
            } catch (Exception e) {
                Log.e("addNewLocation", "Error adding new location", e);
            }
        }
    }

}

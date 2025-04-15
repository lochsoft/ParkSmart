package com.lochana.parkingassistant;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class addNewLocation {

    private FirebaseFirestore db;

    public addNewLocation() {
        db = FirebaseFirestore.getInstance();
    }

    public void addNewLocation(String name, double latitude, double longitude, String availability, double price, Integer rating, String description, @Nullable String documentId) {
        // Create a new document with a generated ID
        Log.d("FirebaseHelper", "Adding new location with name: " + name);
        Map<String, Object> location = new HashMap<>();
        location.put("name", name);
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        location.put("availability",availability );
        location.put("price", price);
        location.put("rating", rating);
        location.put("description", description);

        if (documentId == null){
        // Add a new document with a generated ID
        db.collection("locations")
                .add(location)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirebaseHelper", "DocumentSnapshot added with ID: " + documentReference.getId());
                    // Optionally handle successful addition (e.g., show a toast)
                })
                .addOnFailureListener(e -> {
                    Log.w("FirebaseHelper", "Error adding document", e);
                    // Optionally handle failure (e.g., show an error message)
                });
        }
        else{
            Log.d("FirebaseHelper", "Updating document with ID: " + documentId);
            db.collection("locations")
                    .document(documentId)
                    .set(location)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseHelper", "Document updated successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.w("FirebaseHelper", "Error updating document", e);
                    });
        }
    }

}
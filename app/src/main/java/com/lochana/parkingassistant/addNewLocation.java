package com.lochana.parkingassistant;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class addNewLocation { // Or put this method in your Activity

    private FirebaseFirestore db;

    public addNewLocation() {
        db = FirebaseFirestore.getInstance();
    }

    public void addNewLocation(String name, double latitude, double longitude, String availability, Integer price, Integer rating) {
        // Create a new document with a generated ID
        Map<String, Object> location = new HashMap<>();
        location.put("name", name);
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        location.put("availability",availability );
        location.put("price", price);
        location.put("rating", rating);

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

    // Example Usage (from an Activity or Fragment):
    // FirebaseHelper firebaseHelper = new FirebaseHelper();
    // firebaseHelper.addNewLocation("New Location Name", 12.3456, 78.9012);
}
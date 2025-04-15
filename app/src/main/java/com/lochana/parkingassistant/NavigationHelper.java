package com.lochana.parkingassistant;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.osmdroid.util.GeoPoint;

public class NavigationHelper {

    /**
     * Launches navigation to a selected destination using Google Maps.
     * If Google Maps is not installed, opens the directions in a web browser.
     *
     * @param context          The context for launching intents and showing toasts.
     * @param userLocation     The current location of the user.
     * @param destination      The destination to navigate to.
     */
    public static void navigateToSelectedLocation(Context context, GeoPoint userLocation, GeoPoint destination) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Navigate")
                .setMessage("Do you want to navigate to the selected location?")
                .setPositiveButton("Yes", (dialog, which) -> {
        try {
            if (destination != null) {
                    double latitude = destination.getLatitude();
                    double longitude = destination.getLongitude();

                    // Intent to open in Google Maps app
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=d");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");

                    // If Google Maps app is installed, open it
                    if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(mapIntent);
                    } else {
                        // If not installed, open in browser
                        Uri gmmWebUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" +
                                "&destination=" + latitude + "," + longitude +
                                "&travelmode=driving");
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, gmmWebUri);
                        context.startActivity(webIntent);
                    }
            } else {
                Toast.makeText(context, "Please select a location to navigate to", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d("NavigationHelper", "Navigation error: " + e.getMessage());
        }
    }
    ).setNegativeButton("No", null)
    .show();
}
}

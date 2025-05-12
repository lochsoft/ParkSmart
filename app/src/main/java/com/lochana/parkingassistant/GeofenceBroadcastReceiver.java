package com.lochana.parkingassistant;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "parking_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.d("GeofenceReceiver", "Received intent: " + intent.getAction());

//            if (!"com.lochana.parkingassistant.ACTION_GEOFENCE_EVENT".equals(intent.getAction())) {
//                Log.d("GeofenceReceiver", "Received unrelated intent, ignoring.");
//                return;
//            }

            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    Log.d("GeofenceReceiver", String.format("Key: %s Value: %s", key, value));
                }
            }

            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent == null) {
                Log.e("GeofenceReceiver", "Received null GeofencingEvent — invalid intent.");
                return;
            }
            if (geofencingEvent.hasError()) {
                Log.e("GeofenceReceiver", "Error code: " + geofencingEvent.getErrorCode());
                return;
            }

            int transitionType = geofencingEvent.getGeofenceTransition();
            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                sendNotification(context, "You arrived at a parking spot!");
                Toast.makeText(context, "You arrived at a parking spot!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d("GeofenceReceiver", "Error: " + e.getMessage());
        }
    }

    private void sendNotification(Context context, String message) {

        try {
            // Create notification channel if necessary (for Android 8+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Parking Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }

            // Build the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentTitle("ParkSmart")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            // Send the notification (no need to check POST_NOTIFICATIONS for minSdk 29–32)
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.notify(1, builder.build());
        } catch (Exception e) {
            Log.d("GeofenceReceiver", "Error sending notification: " + e.getMessage());
        }
    }
}

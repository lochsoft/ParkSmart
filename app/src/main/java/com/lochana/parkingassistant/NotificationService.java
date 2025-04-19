package com.lochana.parkingassistant;

import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("notification_item.xml", "Test");

        String title = null;
        String message = null;

        if (remoteMessage.getNotification() != null) {
            // For notification payload
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();
        }

        if (remoteMessage.getData().size() > 0) {
            // For data payload
            title = remoteMessage.getData().get("title");
            message = remoteMessage.getData().get("message");
        }

        if (title != null && message != null) {
            showNotification(title, message);
        }

    }

    private void showNotification(String title, String message) {
        // Save to Room DB
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        db.notificationDao().insert(new NotificationEntity(title, message));

        // Show system notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.notify(101, builder.build());
    }


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Upload the token to your server or Firestore
        Log.d("FCM", "New token: " + token);
    }
}


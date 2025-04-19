package com.lochana.parkingassistant;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.lochana.parkingassistant.databinding.ActivityMainBinding;
import com.lochana.parkingassistant.ui.dashboard.DashboardFragment;
import com.lochana.parkingassistant.ui.home.HomeFragment;
import com.lochana.parkingassistant.ui.notifications.NotificationsFragment;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Fragment homeFragment;
    private Fragment notificationsFragment;
    private Fragment dashboardFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // request notification_item.xml permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Check first launch
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean firstLaunch = prefs.getBoolean("firstLaunch", true);

        if (firstLaunch) {
            // Show notification
            showWelcomeNotification();

            // Update preference so it doesn't show again
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstLaunch", false);
            editor.apply();
        }

//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(task -> {
//                    if (!task.isSuccessful()) {
//                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
//                        return;
//                    }
//
//                    // Get new FCM registration token
//                    String token = task.getResult();
//                    Log.d("FCM", "Token: " + token);
//                });

        BottomNavigationView bottomNavigationView = binding.navView;

        // Initialize fragments
        homeFragment = new HomeFragment();
        notificationsFragment = new NotificationsFragment();
        dashboardFragment = new DashboardFragment();
        activeFragment = homeFragment;

        // Add fragments once and show the initial one
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main, dashboardFragment, "3").hide(dashboardFragment).commit();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main, notificationsFragment, "2").hide(notificationsFragment).commit();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main, homeFragment, "1").commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                switchFragment(homeFragment);
            }
            else if (id == R.id.navigation_notifications) {
                switchFragment(notificationsFragment);
            }
            else if (id == R.id.navigation_dashboard) {
                switchFragment(dashboardFragment);

            }

            return true;
        });

        // Internet check
        checkInternetAndShowRetry();

        // Set status bar color
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.purple_500));
        }

        // Make status bar icons dark
        ViewCompat.getWindowInsetsController(getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

        // notification_item.xml handler
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id",  // ID
                    "Default Channel",     // Name
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Default Channel for Firebase Notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

    }

    private void showWelcomeNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Welcome!")
                .setContentText("Thanks for installing Parking Assistant.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(1001, builder.build());
        }
    }


    // notification_item.xml handling
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Show explanation or disable notifications
            }
        }
    }

    // switch according to bottom navigation bar
    private void switchFragment(Fragment targetFragment) {
        if (targetFragment != activeFragment) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(activeFragment)
                    .show(targetFragment)
                    .commit();
            activeFragment = targetFragment;
        }
    }

    public void checkInternetAndShowRetry() {
        if (!isInternetAvailable()) {
            showRetryDialog();
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    private void showRetryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setCancelable(false)
                .setPositiveButton("Retry", (dialog, which) -> checkInternetAndShowRetry())
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .show();
    }
}

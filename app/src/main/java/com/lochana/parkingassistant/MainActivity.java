package com.lochana.parkingassistant;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lochana.parkingassistant.databinding.ActivityMainBinding;
import com.lochana.parkingassistant.ui.dashboard.DashboardFragment;
import com.lochana.parkingassistant.ui.home.HomeFragment;
import com.lochana.parkingassistant.ui.notifications.NotificationsFragment;

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

            return false;
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
    }

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

package com.lochana.parkingassistant.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.lochana.parkingassistant.AppDatabase;
import com.lochana.parkingassistant.NotificationAdapter;
import com.lochana.parkingassistant.NotificationEntity;
import com.lochana.parkingassistant.R;
import com.lochana.parkingassistant.databinding.FragmentNotificationsBinding;

import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = root.findViewById(R.id.notificationRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadNotifications);
        swipeRefreshLayout.setOnRefreshListener(()->{
            loadNotifications();
            Toast.makeText(requireContext(), "Refreshed Notifications", Toast.LENGTH_SHORT).show();
        });

        Button clearButton = root.findViewById(R.id.clrNoticationsBtn);
        clearButton.setOnClickListener(v -> clearAllNotifications());


        loadNotifications();

        return root;
    }

    private void loadNotifications() {
        AppDatabase db = AppDatabase.getInstance(getContext());
        List<NotificationEntity> notifications = db.notificationDao().getAllNotifications();

        adapter = new NotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);

        // Stop the refresh animation
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    // clear all notifications
    private void clearAllNotifications() {
        // Clear from Room
        AppDatabase db = AppDatabase.getInstance(getContext());
        db.notificationDao().deleteAll();

        // Clear from system notification tray
        NotificationManagerCompat.from(getContext()).cancelAll();

        // Refresh RecyclerView
        loadNotifications();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
package com.lochana.parkingassistant.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lochana.parkingassistant.AppDatabase;
import com.lochana.parkingassistant.NotificationAdapter;
import com.lochana.parkingassistant.NotificationEntity;
import com.lochana.parkingassistant.R;
import com.lochana.parkingassistant.databinding.FragmentNotificationsBinding;

import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = root.findViewById(R.id.notificationRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadNotifications();

        return root;
    }

    private void loadNotifications() {
        AppDatabase db = AppDatabase.getInstance(getContext());
        List<NotificationEntity> notifications = db.notificationDao().getAllNotifications();

        adapter = new NotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        //recyclerView.setAdapter(new NotificationAdapter(NotificationStorage.notifications));
    }

}
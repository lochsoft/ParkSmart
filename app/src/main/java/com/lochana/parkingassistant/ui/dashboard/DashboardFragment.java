package com.lochana.parkingassistant.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.lochana.parkingassistant.AppDatabase;
import com.lochana.parkingassistant.ParkingAdapter;
import com.lochana.parkingassistant.ParkingData;
import com.lochana.parkingassistant.R;
import com.lochana.parkingassistant.databinding.FragmentDashboardBinding;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private RecyclerView savedParkingRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ParkingAdapter adapter;
    private List<ParkingData> parkingList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        try {
            DashboardViewModel dashboardViewModel =
                    new ViewModelProvider(this).get(DashboardViewModel.class);

            binding = FragmentDashboardBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);
            savedParkingRecycler = root.findViewById(R.id.saved_parking_recyclerview);
            savedParkingRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

            adapter = new ParkingAdapter(getContext(), parkingList);
            savedParkingRecycler.setAdapter(adapter);

            loadData(); // load once when activity starts

            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadData(); // refresh when swiped down
                Toast.makeText(requireContext(), "Refreshed Saved Parking", Toast.LENGTH_SHORT).show();
            });

            return root;
        } catch (Exception e) {
            Log.d("DashboardFragment", "Error creating view", e);
            return null;
        }
    }

    public void loadData() {
        try {
            // Reload from database
            List<ParkingData> newList = AppDatabase.getInstance(requireContext()).parkingDataDao().getAll();

            // Clear old data and add new
            parkingList.clear();
            parkingList.addAll(newList);

            adapter.notifyDataSetChanged(); // tell adapter to refresh UI
            swipeRefreshLayout.setRefreshing(false); // stop the loading animation
        } catch (Exception e) {
            Log.d("DashboardFragment", "Error loading data", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(); // refresh data when returning to this screen
    }

}
package com.lochana.parkingassistant.ui.contributions;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.lochana.parkingassistant.LoginActivity;
import com.lochana.parkingassistant.R;
import com.lochana.parkingassistant.User;
import com.lochana.parkingassistant.UserAdapter;

import java.util.ArrayList;
import java.util.List;

public class ContributionsFragment extends Fragment {

    private ContributionsViewModel mViewModel;
    private TextView rankTextView, nicknameTextView;
    private Chip pointsChip;
    private Button logoutBtn;
    FirebaseAuth mAuth;
    SwipeRefreshLayout swipeLayout;
    FirebaseFirestore db;
    List<User> userList;
    UserAdapter adapter;
    public static ContributionsFragment newInstance() {
        return new ContributionsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contributions, container, false);
        nicknameTextView = view.findViewById(R.id.userName);
        rankTextView = view.findViewById(R.id.rankTxt);
        pointsChip = view.findViewById(R.id.pointsChip);
        logoutBtn = view.findViewById(R.id.logoutBtn);
        swipeLayout = view.findViewById(R.id.swipe_refresh_layout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        logoutBtn.setOnClickListener(v -> {
            logout();
        });

        RecyclerView recyclerView = view.findViewById(R.id.rankRecyclerView);
        userList = new ArrayList<>();
        adapter = new UserAdapter(userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // update user data in fragment
        updateUserData();

        // fetch and sort users
        fetchUsers();

        swipeLayout.setOnRefreshListener(() -> {
            fetchUsers();
            updateUserData();
        });
        return view;
    }

    private void updateUserData(){
        try {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nickname = documentSnapshot.getString("nickname");
                            Long points = documentSnapshot.getLong("points");
                            Long rank = documentSnapshot.getLong("rank");

                            Log.d("Contributions", "Nickname: " + nickname);
                            Log.d("Contributions", "Points: " + points);
                            Log.d("Contributions", "Rank: " + rank);

                            nicknameTextView.setText(nickname);
                            pointsChip.setText(String.format(String.valueOf(points)));
                            rankTextView.setText("Rank : " + String.format(String.valueOf(rank)));
                        }
                    })
                    .addOnFailureListener(e -> Log.w("Contributions", "Failed to fetch user data", e));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void fetchUsers() {
        try {
            db.collection("users")
                    .orderBy("points", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        userList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            User user = doc.toObject(User.class);
                            userList.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching users", e);
                    });
            swipeLayout.setRefreshing(false);
        } catch (Exception e) {
            Log.d("Contributions", "Error fetching users");
        }
    }
    private void logout() {
        try {
            mAuth.signOut();
            // Now redirect user back to Login screen
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();  // If called from Fragment, otherwise use `finish()` directly if in Activity
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ContributionsViewModel.class);
        // TODO: Use the ViewModel
    }

}
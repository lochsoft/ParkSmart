package com.lochana.parkingassistant.ui.contributions;

import static android.content.Context.MODE_PRIVATE;

import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.lochana.parkingassistant.AvatarAdapter;
import com.lochana.parkingassistant.FeedbackDialogFragment;
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
    private int selectedAvatarResId;
    FirebaseAuth mAuth;
    SwipeRefreshLayout swipeLayout;
    FirebaseFirestore db;
    List<User> userList;
    UserAdapter adapter;
    ImageView avatarImageView;
    ImageButton editAvatarBtn;
    DatabaseReference userRef;
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
        avatarImageView = view.findViewById(R.id.circleImageView);
        editAvatarBtn = view.findViewById(R.id.imageButton);

        userRef = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        logoutBtn.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        logout();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
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

        // edit avatar button
        editAvatarBtn.setOnClickListener(v -> {
            showAvatarSelectionDialog();
        });

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long avatarResId = documentSnapshot.getLong("avatar");
                        if (avatarResId != null) {
                            avatarImageView.setImageResource(avatarResId.intValue());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Contributions", "Failed to load avatar", e));

        ImageButton sendFeedbackButton = view.findViewById(R.id.button_send_feedback);
        sendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFeedbackDialog();
            }
        });

        return view;
    }

    // feedback dialog
    private void showFeedbackDialog() {
        FeedbackDialogFragment feedbackDialog = new FeedbackDialogFragment();
        feedbackDialog.show(requireFragmentManager(), "FeedbackDialog");
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
                        long rank = 1;

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                user.setRank(rank);  // Set local rank for display
                                user.setAvatar(doc.getLong("avatar"));
                                userList.add(user);

                                // Update rank field in Firestore for this user
                                db.collection("users").document(doc.getId())
                                        .update("rank", rank)
                                        .addOnFailureListener(e -> Log.e("Firestore", "Failed to update rank", e));

                                rank++; // Increment rank for next user
                            }
                        }
                        adapter.notifyDataSetChanged();
                        swipeLayout.setRefreshing(false);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching users", e);
                        swipeLayout.setRefreshing(false);
                    });

        } catch (Exception e) {
            Log.d("Contributions", "Error fetching users", e);
            swipeLayout.setRefreshing(false);
        }
    }

    private void logout() {
        try {
            mAuth.signOut();

            SharedPreferences preferences = requireContext().getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("rememberMe", false);
            editor.apply();
            //  redirect user back to Login screen
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();  // If called from Fragment, otherwise use `finish()` directly if in Activity
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showAvatarSelectionDialog() {
        try {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
            // Assuming R.layout.avatar_bottomsheet contains your GridView for avatars AND an EditText with ID nicknameText
            bottomSheetDialog.setContentView(R.layout.avatar_bottomsheet);

            GridView gridView = bottomSheetDialog.findViewById(R.id.gridViewAvatars);
            final EditText nicknameEditText = bottomSheetDialog.findViewById(R.id.nicknameText); // Make final to access in lambda

            int[] avatars = {
                    R.drawable.male, R.drawable.female, R.drawable.male2, R.drawable.female2,
                    R.drawable.male3, R.drawable.female3, R.drawable.male4, R.drawable.female4
            };

            AvatarAdapter adapter = new AvatarAdapter(requireContext(), avatars);
            if (gridView != null) {
                gridView.setAdapter(adapter);

                gridView.setOnItemClickListener((parent, view, position, id) -> {
                    selectedAvatarResId = avatars[position];
                    avatarImageView.setImageResource(selectedAvatarResId);

                    // Update avatar in Firestore immediately on selection
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        db.collection("users").document(userId)
                                .update("avatar", selectedAvatarResId)
                                .addOnSuccessListener(aVoid -> Log.d("Contributions", "Avatar updated successfully"))
                                .addOnFailureListener(e -> Log.e("Contributions", "Failed to update avatar", e));
                    }
                    // We don't dismiss here; the user might want to edit nickname too,
                    // or they can explicitly dismiss. The dismiss listener handles final actions.
                });
            }

            // --- PART 1: Populate nickname EditText when the dialog opens ---
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists() && nicknameEditText != null) {
                                String currentNickname = documentSnapshot.getString("nickname");
                                if (currentNickname != null && !currentNickname.isEmpty()) {
                                    nicknameEditText.setText(currentNickname);
                                } else {
                                    nicknameEditText.setHint("Enter your nickname"); // Hint for empty nickname
                                }
                            }
                        })
                        .addOnFailureListener(e -> Log.e("Contributions", "Failed to load current nickname: " + e.getMessage()));
            }

            // --- PART 2: Handle dismissal - Update nickname in Firestore & Firebase Auth displayName ---
            bottomSheetDialog.setOnDismissListener(dialogInterface -> {
                Log.d("Contributions", "Avatar/Nickname Bottom Sheet Dialog dismissed.");

                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(requireContext(), "No user logged in.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String newNicknameInput = nicknameEditText.getText().toString().trim();

                // Only proceed with update if the EditText is NOT empty
                if (!newNicknameInput.isEmpty()) {
                    String userId = user.getUid();

                    // Update nickname in Firestore
                    db.collection("users").document(userId)
                            .update("nickname", newNicknameInput)
                            .addOnSuccessListener(aVoid -> Log.d("Contributions", "Firestore nickname updated to: " + newNicknameInput))
                            .addOnFailureListener(e -> Log.e("Contributions", "Failed to update Firestore nickname: " + e.getMessage()));

                    // Update Firebase Authentication displayName
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(newNicknameInput)
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("Contributions", "Firebase Auth display name updated to: " + newNicknameInput);
                                    //Toast.makeText(requireContext(), "Nickname updated!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("Contributions", "Failed to update Firebase Auth display name: " + task.getException().getMessage());
                                    Toast.makeText(requireContext(), "Failed to update display name.", Toast.LENGTH_SHORT).show();
                                }
                                // After potential update, refresh the UI in the ContributionsFragment
                                updateUserData(); // This method already refreshes the nicknameTextView
                            });
                } else {
                    // If EditText is empty, no change is required.
                    // However, we still want to ensure the UI is consistent with the *current*
                    // Firebase Auth display name and Firestore nickname, just in case.
                    // So, we'll still call updateUserData() to refresh.
                    Log.d("Contributions", "Nickname EditText was empty. No change applied to nickname.");
                    Toast.makeText(requireContext(), "Nickname not changed (input was empty).", Toast.LENGTH_SHORT).show();
                    updateUserData();
                }
            });

            bottomSheetDialog.show();
        } catch (Exception e) {
            Log.e("Contributions", "Error showing avatar selection bottom sheet", e);
        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ContributionsViewModel.class);
    }

}
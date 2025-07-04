package com.lochana.parkingassistant;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue; // Correct import for serverTimestamp()

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FeedbackDialogFragment extends DialogFragment {

    private EditText feedbackEditText;
    private Button submitButton;
    private ProgressBar progressBar;
    private TextView statusTextView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // This APP_ID will be stored as a field in each feedback document
    private static final String APP_ID = "parksmart-university-parking-app"; // Using a more specific ID

    public FeedbackDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Using a custom dialog theme for styling (like a BottomSheetDialog theme)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback_dialog, container, false);

        feedbackEditText = view.findViewById(R.id.edit_text_feedback);
        submitButton = view.findViewById(R.id.button_submit_feedback);
        progressBar = view.findViewById(R.id.progress_bar_feedback);
        statusTextView = view.findViewById(R.id.text_view_status);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            // Apply a transparent background to allow your custom drawable (e.g., rounded_dialog_background) to show
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // You might want to remove this line if CustomBottomSheetDialogTheme already sets layout params
            // If you want it to fill width, uncomment dialog.getWindow().setLayout(...)
            // dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void sendFeedback() {
        String feedbackText = feedbackEditText.getText().toString().trim();

        if (feedbackText.isEmpty()) {
            statusTextView.setText("Feedback cannot be empty.");
            statusTextView.setTextColor(Color.RED);
            statusTextView.setVisibility(View.VISIBLE);
            return;
        }

        setLoadingState(true);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId;

        if (currentUser != null) {
            userId = currentUser.getUid();
            saveFeedbackToFirestore(userId, feedbackText);
        } else {
            // If no user is logged in, sign in anonymously
            mAuth.signInAnonymously()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser anonymousUser = mAuth.getCurrentUser();
                            if (anonymousUser != null) {
                                String anonymousUserId = anonymousUser.getUid();
                                saveFeedbackToFirestore(anonymousUserId, feedbackText);
                            } else {
                                // Fallback if anonymous sign-in somehow doesn't provide a user
                                String generatedId = UUID.randomUUID().toString();
                                saveFeedbackToFirestore(generatedId, feedbackText);
                            }
                        } else {
                            // Handle anonymous sign-in failure
                            setLoadingState(false);
                            statusTextView.setText("Failed to authenticate. Please try again.");
                            statusTextView.setTextColor(Color.RED);
                            statusTextView.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveFeedbackToFirestore(String userId, String feedbackText) {
        // --- THIS IS THE KEY CHANGE ---
        // We are now targeting a top-level collection called "feedbacks"
        CollectionReference feedbackCollection = db.collection("feedbacks");

        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("feedback", feedbackText);
        feedbackData.put("timestamp", FieldValue.serverTimestamp()); // Use FieldValue.serverTimestamp()
        feedbackData.put("user", userId); // Renamed from "userId" to "user" as per your request
        feedbackData.put("appId", APP_ID); // Still include the app ID if you manage multiple apps

        feedbackCollection.add(feedbackData)
                .addOnSuccessListener(documentReference -> {
                    setLoadingState(false);
                    statusTextView.setText("Thank you for your feedback!");
                    statusTextView.setTextColor(Color.GREEN);
                    statusTextView.setVisibility(View.VISIBLE);
                    feedbackEditText.setText(""); // Clear input
                    new android.os.Handler().postDelayed(this::dismiss, 2000); // Dismiss after 2 seconds
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    statusTextView.setText("Failed to send feedback. Please try again.");
                    statusTextView.setTextColor(Color.RED);
                    statusTextView.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);
            statusTextView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
        }
    }
}
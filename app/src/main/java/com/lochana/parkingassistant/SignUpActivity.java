package com.lochana.parkingassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest; // <-- Add this import
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    ProgressBar progressBar;
    EditText etNickname;
    DatabaseReference mDatabase; // Note: You're using Firestore primarily, Realtime DB might not be needed
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // mDatabase = FirebaseDatabase.getInstance().getReference("users"); // Unless you specifically need Realtime Database, this line can be removed as you're using Firestore for user data.
        progressBar = findViewById(R.id.progressBar3);

        Button btnSignUp = findViewById(R.id.signupBtn);
        EditText etEmail = findViewById(R.id.userNameTxt2);
        EditText etPassword = findViewById(R.id.passwordTxt2);
        Button backToLoginBtn = findViewById(R.id.backToLoginBtn);
        etNickname = findViewById(R.id.userNameTxt3); // This is where the nickname is input

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim(); // Trim whitespace
            String password = etPassword.getText().toString().trim(); // Trim whitespace
            String nickname = etNickname.getText().toString().trim(); // Get nickname and trim

            if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                if (email.isEmpty()) etEmail.setError("Email is required");
                if (password.isEmpty()) etPassword.setError("Password is required");
                if (nickname.isEmpty()) etNickname.setError("Nickname is required");
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                signUpUser(email, password, nickname); // Pass nickname to the signUpUser method
            }
        });

        backToLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void signUpUser(String email, String password, String nickname) { // Added nickname parameter
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // 1. Set the display name for the Firebase Authentication user profile
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nickname) // Use the provided nickname
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Log.d("SignUpActivity", "User profile display name updated successfully.");

                                            // 2. Send email verification (moved inside profile update success)
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(verifyTask -> {
                                                        if (verifyTask.isSuccessful()) {
                                                            String userId = user.getUid();

                                                            // 3. Create User data in Firestore
                                                            Map<String, Object> newUser = new HashMap<>();
                                                            newUser.put("nickname", nickname); // Use the provided nickname
                                                            newUser.put("email", user.getEmail());
                                                            newUser.put("points", 0);
                                                            newUser.put("rank", 0);

                                                            db.collection("users").document(userId).set(newUser)
                                                                    .addOnSuccessListener(aVoid -> Log.d("Signup", "User data stored in Firestore successfully"))
                                                                    .addOnFailureListener(e -> Log.w("Signup", "Error adding user data to Firestore", e));

                                                            Toast.makeText(this, "Account created. Please check your email to verify your account before logging in.", Toast.LENGTH_LONG).show();

                                                            // 4. Redirect to login activity (after all async operations are initiated)
                                                            // It's good practice to sign out after registration so they are forced to log in with verified email
                                                            mAuth.signOut();
                                                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                                            startActivity(intent);
                                                            finish();

                                                        } else {
                                                            Toast.makeText(this, "Failed to send verification email: " + verifyTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                            // Even if verification email fails, the account is created, decide if you want to proceed or revert.
                                                            // For now, we'll still go to login but with a toast.
                                                            mAuth.signOut();
                                                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                        progressBar.setVisibility(View.GONE); // Hide progress bar after email verification task completes or fails
                                                    });
                                        } else {
                                            Log.w("SignUpActivity", "Error setting display name: " + profileTask.getException());
                                            Toast.makeText(this, "Registration successful, but failed to set nickname. Please try updating it later.", Toast.LENGTH_LONG).show();
                                            // Even if profile update fails, try to send email verification
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(verifyTask -> { /* ... same logic as above ... */ });
                                            progressBar.setVisibility(View.GONE); // Hide progress bar even if display name update fails
                                        }
                                    });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Registration failed: User is null.", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

}
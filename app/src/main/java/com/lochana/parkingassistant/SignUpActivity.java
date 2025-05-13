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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    ProgressBar progressBar;
    EditText etNickname;
    DatabaseReference mDatabase;
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
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        progressBar = findViewById(R.id.progressBar3);

        Button btnSignUp = findViewById(R.id.signupBtn);
        EditText etEmail = findViewById(R.id.userNameTxt2);
        EditText etPassword = findViewById(R.id.passwordTxt2);
        Button backToLoginBtn = findViewById(R.id.backToLoginBtn);
        etNickname = findViewById(R.id.userNameTxt3);

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            if (email.isEmpty() || password.isEmpty() || etNickname.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter both email and password and a nickname", Toast.LENGTH_SHORT).show();
            }
            else {
                progressBar.setVisibility(View.VISIBLE);
                signUpUser(email, password);
            }
        });

        backToLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }
    private void signUpUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Send email verification
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            String userId = user.getUid();

                                            // Create User object
                                            Map<String, Object> newUser = new HashMap<>();
                                            newUser.put("nickname", etNickname.getText().toString());
                                            newUser.put("email", user.getEmail());
                                            newUser.put("points", 0);
                                            newUser.put("rank", 0);

                                            db.collection("users").document(userId).set(newUser)
                                                    .addOnSuccessListener(aVoid -> Log.d("Signup", "User profile created successfully"))
                                                    .addOnFailureListener(e -> Log.w("Signup", "Error adding user", e));

                                            Toast.makeText(this, "Account created. Please check your email to verify your account before logging in.", Toast.LENGTH_LONG).show();

                                            // Redirect to login activity
                                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Failed to send verification email: " + verifyTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }

}
package com.example.monitorapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvSignup;

    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔥 Check in Firebase
            usersRef.orderByChild("email")
                    .equalTo(email)
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (!snapshot.exists()) {
                                Toast.makeText(LoginActivity.this,
                                        "User not registered. Please Sign Up.",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            // User found
                            for (DataSnapshot userSnap : snapshot.getChildren()) {

                                String role = userSnap.child("role")
                                        .getValue(String.class);

                                String name = userSnap.child("name")
                                        .getValue(String.class);

                                SharedPrefHelper.setLoggedIn(LoginActivity.this, true);
                                SharedPrefHelper.saveUser(LoginActivity.this,
                                        name, email, role);

                                if ("ADMIN".equals(role)) {
                                    startActivity(new Intent(LoginActivity.this,
                                            AdminActivity.class));
                                } else {
                                    startActivity(new Intent(LoginActivity.this,
                                            UserActivity.class));
                                }

                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(LoginActivity.this,
                                    "Database error",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvSignup.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }
}
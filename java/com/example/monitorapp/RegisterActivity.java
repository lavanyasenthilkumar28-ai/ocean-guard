package com.example.monitorapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etConfirm;
    Button btnSignup;
    TextView tvLogin;

    private static final String ADMIN_EMAIL = "admin1@gmail.com";

    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);

        // 🔥 Firebase reference
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnSignup.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Fill all details", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Password mismatch", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = email.equalsIgnoreCase(ADMIN_EMAIL) ? "ADMIN" : "USER";

            // 🔥 Create user object
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("name", name);
            userMap.put("email", email);
            userMap.put("role", role);
            userMap.put("deviceId", "wearable_001");
            userMap.put("timestamp", System.currentTimeMillis());

            // 🔥 Push to Firebase
            String userId = usersRef.push().getKey();
            usersRef.child(userId).setValue(userMap);

            // Local Save
            SharedPrefHelper.setRegistered(this, true);
            SharedPrefHelper.setLoggedIn(this, true);
            SharedPrefHelper.saveUser(this, name, email, role);

            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();

            if (role.equals("ADMIN")) {
                startActivity(new Intent(this, AdminActivity.class));
            } else {
                startActivity(new Intent(this, UserActivity.class));
            }

            finish();
        });

        tvLogin.setOnClickListener(v -> finish());
    }
}
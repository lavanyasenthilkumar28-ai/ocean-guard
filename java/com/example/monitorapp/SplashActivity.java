package com.example.monitorapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            // 🔑 DECISION LOGIC
            if (!SharedPrefHelper.isRegistered(this)) {
                // First time → Register
                startActivity(new Intent(this, LoginActivity.class));

            } else if (!SharedPrefHelper.isLoggedIn(this)) {
                // Registered but not logged in
                startActivity(new Intent(this, RegisterActivity.class));

            }

            finish();
        }, 2000);
    }

}
package com.example.monitorapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class UserActivity extends AppCompatActivity {

    TextView tvDevice, tvRisk, tvActivity, tvLocation;
    Button btnSOS;

    ImageView navHome, navPeople, navLocation, navLogout;

    DatabaseReference liveRef;

    double lat = 0, lng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // UI
        tvDevice   = findViewById(R.id.tvDevice);
        tvRisk     = findViewById(R.id.tvRisk);
        tvActivity = findViewById(R.id.tvActivity);
        tvLocation = findViewById(R.id.tvLocation);
        btnSOS     = findViewById(R.id.btnSOS);

        navHome     = findViewById(R.id.navHome);
        navPeople   = findViewById(R.id.navPeople);
        navLocation = findViewById(R.id.navLocation);
        navLogout   = findViewById(R.id.navLogout);

        String deviceId = "wearable_001";
        tvDevice.setText("Monitoring : " + deviceId);

        // Firebase
        liveRef = FirebaseDatabase.getInstance()
                .getReference(deviceId)
                .child("live");

        liveRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {

                // ---------- ALERT / RISK ----------
                String alert = s.child("alert").getValue(String.class);
                if (alert == null) alert = "SAFE";

                tvRisk.setText(alert);

                if (alert.equalsIgnoreCase("SAFE")) {
                    tvRisk.setTextColor(Color.parseColor("#4CAF50"));
                    tvRisk.clearAnimation();
                } else if (alert.contains("FALL")) {
                    tvRisk.setTextColor(Color.parseColor("#FFA000"));
                } else {
                    tvRisk.setTextColor(Color.parseColor("#E53935"));

                    // Blink animation
                    AlphaAnimation blink = new AlphaAnimation(0.2f, 1.0f);
                    blink.setDuration(600);
                    blink.setRepeatMode(AlphaAnimation.REVERSE);
                    blink.setRepeatCount(AlphaAnimation.INFINITE);
                    tvRisk.startAnimation(blink);
                }

                // ---------- ACTIVITY ----------
                String water = s.child("water").child("status").getValue(String.class);
                String fall  = s.child("fall").child("level").getValue(String.class);

                tvActivity.setText(
                        "Water : " + (water != null ? water : "-") +
                                "\nFall : "  + (fall  != null ? fall  : "-")
                );

                // ---------- GPS ----------
                Double la = s.child("gps").child("lat").getValue(Double.class);
                Double ln = s.child("gps").child("lng").getValue(Double.class);

                if (la != null && ln != null) {
                    lat = la;
                    lng = ln;
                    tvLocation.setText("Lat : " + la + "\nLng : " + ln);
                } else {
                    tvLocation.setText("Fetching GPS...");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvActivity.setText("Firebase error");
            }
        });

        // ---------- SOS ----------
        btnSOS.setOnClickListener(v ->

                // LIVE STATUS (USER + ADMIN பார்க்க)
                liveRef.child("alert").setValue("MAN OVERBOARD"));
        liveRef.child("timestamp").setValue(System.currentTimeMillis());

// EMERGENCY HISTORY (ADMIN + HISTORY)
        HashMap<String, Object> emergency = new HashMap<>();
        emergency.put("deviceId", "wearable_001");
        emergency.put("userEmail", SharedPrefHelper.getEmail(this));
        emergency.put("status", "ACTIVE");
        emergency.put("createdTime", System.currentTimeMillis());



        // ---------- NAV ----------
        navHome.setOnClickListener(v -> {});

        navPeople.setOnClickListener(v ->
                startActivity(new Intent(this, PeopleActivity.class)));

        navLocation.setOnClickListener(v -> {

            Intent i = new Intent(this, LocationActivity.class);
            i.putExtra("lat", lat);
            i.putExtra("lng", lng);
            startActivity(i);
        });

        navLogout.setOnClickListener(v -> {
            SharedPrefHelper.logout(this);
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}
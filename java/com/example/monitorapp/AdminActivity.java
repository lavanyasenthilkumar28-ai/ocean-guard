package com.example.monitorapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    TextView tvAdminEmails, tvUserCount, tvEmergency, tvLastLocation;
    ImageView navHome, navPeople, navLogout;
    Button btnLiveMonitor, btnSOSHistory, btnResolve;

    DatabaseReference usersRef, liveRef, emergencyRef;

    // 🔴 VERY IMPORTANT – current emergency tracker
    String currentEmergencyId = null;

    private final List<String> adminEmails = Arrays.asList(
            "admin1@gmail.com"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard);

        // UI
        tvAdminEmails  = findViewById(R.id.tvAdminEmail);
        tvUserCount    = findViewById(R.id.tvUserCount);
        tvEmergency    = findViewById(R.id.tvEmergency);
        tvLastLocation = findViewById(R.id.tvLastLocation);

        btnLiveMonitor = findViewById(R.id.btnLiveMonitor);
        btnSOSHistory  = findViewById(R.id.btnSOSHistory);
        btnResolve     = findViewById(R.id.btnResolve);

        navHome   = findViewById(R.id.navHome);
        navPeople = findViewById(R.id.navPeople);
        navLogout = findViewById(R.id.navLogout);

        btnResolve.setEnabled(false);

        // ADMIN EMAIL LIST
        StringBuilder emailBuilder = new StringBuilder();
        for (String email : adminEmails) {
            emailBuilder.append("• ").append(email).append("\n");
        }
        tvAdminEmails.setText(emailBuilder.toString());

        // FIREBASE REFERENCES
        usersRef = FirebaseDatabase.getInstance()
                .getReference("users");

        liveRef = FirebaseDatabase.getInstance()
                .getReference("devices")
                .child("wearable_001")
                .child("live");

        emergencyRef = FirebaseDatabase.getInstance()
                .getReference("emergencyRequests");

        // USER COUNT
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvUserCount.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // LIVE STATUS (DASHBOARD)
        liveRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    tvEmergency.setText("NO DATA");
                    return;
                }

                String alert = snapshot.child("alert").getValue(String.class);
                if (alert == null) alert = "SAFE";
                tvEmergency.setText(alert);

                Double lat = snapshot.child("gps").child("lat").getValue(Double.class);
                Double lng = snapshot.child("gps").child("lng").getValue(Double.class);

                if (lat != null && lng != null) {
                    tvLastLocation.setText(lat + ", " + lng);
                } else {
                    tvLastLocation.setText("Waiting for GPS");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvEmergency.setText("ERROR");
            }
        });

        // 🔥 DETECT ACTIVE EMERGENCY (IMPORTANT)
        emergencyRef.limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            String status = snap.child("status").getValue(String.class);

                            if ("ACTIVE".equals(status)) {
                                currentEmergencyId = snap.getKey();
                                tvEmergency.setText("MAN OVERBOARD");
                                btnResolve.setEnabled(true);
                            }
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        // 🔥 RESOLVE BUTTON – FULLY WORKING
        btnResolve.setOnClickListener(v -> {
            if (currentEmergencyId == null) {
                Toast.makeText(this, "No active emergency", Toast.LENGTH_SHORT).show();
                return;
            }

            long now = System.currentTimeMillis();


// 1️⃣ emergencyRequests → RESOLVED
            emergencyRef.child(currentEmergencyId).updateChildren(
                    new HashMap<String, Object>() {{
                        put("status", "RESOLVED");
                        put("resolvedBy", SharedPrefHelper.getEmail(AdminActivity.this));
                        put("resolvedTime", now);
                    }}
            );

// 2️⃣ LIVE STATUS → SAFE (MOST IMPORTANT)
            liveRef.updateChildren(
                    new HashMap<String, Object>() {{
                        put("alert", "SAFE");
                        put("timestamp", now);
                    }}
            );

// 3️⃣ UI feedback
            tvEmergency.setText("SAFE");
            btnResolve.setEnabled(false);
            currentEmergencyId = null;

            Toast.makeText(
                    this,
                    "✅ Emergency resolved successfully",
                    Toast.LENGTH_LONG
            ).show();

            if (currentEmergencyId == null) {
                Toast.makeText(this,
                        "No active emergency",
                        Toast.LENGTH_SHORT).show();
                return;
            }



            // 1️⃣ UPDATE EMERGENCY HISTORY
            DatabaseReference emergencyNode =
                    emergencyRef.child(currentEmergencyId);

            HashMap<String, Object> emergencyUpdate = new HashMap<>();
            emergencyUpdate.put("status", "RESOLVED");
            emergencyUpdate.put("resolvedBy",
                    SharedPrefHelper.getEmail(this));
            emergencyUpdate.put("resolvedTime", now);

            emergencyNode.updateChildren(emergencyUpdate);
            emergencyNode.updateChildren(emergencyUpdate);

            // 2️⃣ UPDATE LIVE STATUS → SAFE
            HashMap<String, Object> liveUpdate = new HashMap<>();
            liveUpdate.put("alert", "SAFE");
            liveUpdate.put("timestamp", now);

            liveRef.updateChildren(liveUpdate);

            // 3️⃣ UI RESET
            tvEmergency.setText("SAFE");
            btnResolve.setEnabled(false);
            currentEmergencyId = null;

            Toast.makeText(this,
                    "✅ Emergency resolved successfully",
                    Toast.LENGTH_LONG).show();
        });

        // NAVIGATION
        btnLiveMonitor.setOnClickListener(v ->
                startActivity(new Intent(this, AdminLiveActivity.class)));

        btnSOSHistory.setOnClickListener(v ->
                startActivity(new Intent(this, SOSHistory.class)));

        navPeople.setOnClickListener(v ->
                startActivity(new Intent(this, PeopleActivity.class)));

        navLogout.setOnClickListener(v -> {
            SharedPrefHelper.logout(this);
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}
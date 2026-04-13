package com.example.monitorapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SOSActivity extends AppCompatActivity {

    Button btnSOS;
    DatabaseReference liveRef, emergencyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        btnSOS = findViewById(R.id.btnSOS);

        liveRef = FirebaseDatabase.getInstance()
                .getReference("devices")
                .child("wearable_001")
                .child("live");

        emergencyRef = FirebaseDatabase.getInstance()
                .getReference("emergencyRequests");

        btnSOS.setOnClickListener(v -> {

            long now = System.currentTimeMillis();

            // 1️⃣ LIVE STATUS UPDATE
            HashMap<String, Object> liveUpdate = new HashMap<>();
            liveUpdate.put("alert", "MAN OVERBOARD");
            liveUpdate.put("timestamp", now);

            liveRef.updateChildren(liveUpdate);

            // 2️⃣ EMERGENCY HISTORY SAVE (🔥 THIS IS WHERE MODEL IS USED)
            double lat=10.8106;
            double lng = 78.6845;
            EmergencyModel model = new EmergencyModel(
                    "wearable_001",
                    (String) SharedPrefHelper.getEmail(this),   // userEmail
                    (String) SharedPrefHelper.getEmail(this),   // userName (temporary)
                    lat,
                    lng,
                    now
            );

            emergencyRef.push().setValue(model);

            Toast.makeText(this,
                    "🚨 SOS SENT TO ADMIN",
                    Toast.LENGTH_LONG).show();
        });
    }

    private void sendSOS() {

        double lat = 10.814713;   // Replace with real GPS later
        double lng = 78.673078;

        long time = System.currentTimeMillis();

        // 🔴 LIVE UPDATE (Admin Live Screen)
        HashMap<String, Object> live = new HashMap<>();
        live.put("alert", "SOS");
        live.put("timestamp", time);

        HashMap<String, Object> gps = new HashMap<>();
        gps.put("lat", lat);
        gps.put("lng", lng);

        live.put("gps", gps);

        liveRef.setValue(live);

        // 🚨 EMERGENCY HISTORY RECORD
        HashMap<String, Object> emergency = new HashMap<>();
        emergency.put("deviceId", "wearable_001");
        Object userEmail = emergency.put("userEmail", SharedPrefHelper.getUsername(this));
        Object userName = emergency.put("userName", SharedPrefHelper.getUsername(this));
        emergency.put("lat", lat);
        emergency.put("lng", lng);
        emergency.put("createdTime", time);
        emergency.put("status", "ACTIVE");
        emergency.put("resolvedBy", null);
        emergency.put("resolvedTime", null);

        emergencyRef.push().setValue(emergency);

        Toast.makeText(this,
                "🚨 SOS SENT TO ADMIN",
                Toast.LENGTH_LONG).show();
    }
}
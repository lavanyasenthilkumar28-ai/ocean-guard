package com.example.monitorapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;

public class AdminLiveActivity extends AppCompatActivity {

    TextView tvAlert, tvLocation;


    MapView map;
    Marker marker;

    DatabaseReference liveRef, emergencyRef;
    String currentEmergencyId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_admin_live);

        tvAlert = findViewById(R.id.tvAlert);
        tvLocation = findViewById(R.id.tvLocation);
        map = findViewById(R.id.map);

        map.setMultiTouchControls(true);
        map.getController().setZoom(16.0);

        marker = new Marker(map);
        map.getOverlays().add(marker);



        liveRef = FirebaseDatabase.getInstance()
                .getReference("devices")
                .child("wearable_001")
                .child("live");

        emergencyRef = FirebaseDatabase.getInstance()
                .getReference("emergencyRequests");

        // 🔴 LIVE LOCATION
        liveRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String alert = snapshot.child("alert").getValue(String.class);
                if (alert == null) alert = "SAFE";

                Double lat = snapshot.child("gps").child("lat").getValue(Double.class);
                Double lng = snapshot.child("gps").child("lng").getValue(Double.class);

                tvAlert.setText(alert);

                if (lat != null && lng != null) {
                    tvLocation.setText("Lat: " + lat + "\nLng: " + lng);

                    GeoPoint point = new GeoPoint(lat, lng);
                    map.getController().setCenter(point);
                    marker.setPosition(point);
                    map.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });






    }
    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}

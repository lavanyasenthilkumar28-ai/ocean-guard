package com.example.monitorapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class LocationActivity extends AppCompatActivity {

    MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 REQUIRED – USER AGENT
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_location);

        map = findViewById(R.id.map);

        // MAP SETTINGS
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // DEFAULT LOCATION (TEST)
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);

        GeoPoint point = new GeoPoint(lat, lng);
        map.getController().setZoom(18.0);
        map.getController().setCenter(point);

        // MARKER
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setTitle("Wearable Location");
        map.getOverlays().add(marker);
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
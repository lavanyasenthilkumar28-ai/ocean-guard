package com.example.monitorapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SOSHistory extends AppCompatActivity {

    ListView listSOS;
    ArrayList<String> sosList;
    ArrayAdapter<String> adapter;
    DatabaseReference emergencyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminsos_history);

        listSOS = findViewById(R.id.listSOS);
        sosList = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                sosList
        );

        listSOS.setAdapter(adapter);

        emergencyRef = FirebaseDatabase.getInstance()
                .getReference("emergencyRequests");

        emergencyRef.orderByChild("createdTime")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        sosList.clear();
                        int total = 0;

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            total++;

                            String name = snap.child("userName").getValue(String.class);
                            String email = snap.child("userEmail").getValue(String.class);
                            String status = snap.child("status").getValue(String.class);

                            Double lat = snap.child("lat").getValue(Double.class);
                            Double lng = snap.child("lng").getValue(Double.class);

                            Long created = snap.child("createdTime").getValue(Long.class);
                            Long resolved = snap.child("resolvedTime").getValue(Long.class);
                            String resolvedBy = snap.child("resolvedBy").getValue(String.class);

                            // 🔒 Null safety
                            if (name == null) name = "Unknown";
                            if (email == null) email = "-";
                            if (status == null) status = "UNKNOWN";
                            if (lat == null) lat = 0.0;
                            if (lng == null) lng = 0.0;
                            if (resolvedBy == null) resolvedBy = "-";

                            String createdTime = (created == null)
                                    ? "Unknown"
                                    : DateFormat.getDateTimeInstance()
                                    .format(new Date(created));

                            String resolvedTime = (resolved == null)
                                    ? "Not Resolved"
                                    : DateFormat.getDateTimeInstance()
                                    .format(new Date(resolved));

                            String item =
                                    "User: " + name +
                                            "\nEmail: " + email +
                                            "\nStatus: " + status +
                                            "\nLocation: " + lat + ", " + lng +
                                            "\nTriggered: " + createdTime +
                                            "\nResolved By: " + resolvedBy +
                                            "\nResolved At: " + resolvedTime +
                                            "\n---------------------------";

                            // 🔥 Latest first
                            sosList.add(0, item);
                        }

                        sosList.add(0,
                                "TOTAL SOS COUNT: " + total +
                                        "\n============================\n");

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
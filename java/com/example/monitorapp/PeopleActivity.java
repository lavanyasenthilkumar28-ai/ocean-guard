package com.example.monitorapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PeopleActivity extends AppCompatActivity {

    RecyclerView recyclerPeople;
    ArrayList<Person> personList;
    PersonAdapter adapter;

    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        recyclerPeople = findViewById(R.id.recyclerPeople);
        recyclerPeople.setLayoutManager(new LinearLayoutManager(this));

        personList = new ArrayList<>();
        adapter = new PersonAdapter(personList);
        recyclerPeople.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                personList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Person p = data.getValue(Person.class);
                    personList.add(p);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }
}
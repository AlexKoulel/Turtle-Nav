package com.example.agriculturenavigation.Database;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agriculturenavigation.R;

import java.util.ArrayList;

public class DisplayPatterns extends AppCompatActivity {
    private ArrayList<PatternModal> patternModalArrayList;
    private DBManager mydb;
    private PatternRVAdapter patternRVAdapter;
    private RecyclerView patternRV;
    private String fieldName,fieldLocation;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displaypatterns);

        patternModalArrayList = new ArrayList<>();
        mydb = new DBManager(DisplayPatterns.this);

        Bundle bundle = getIntent().getExtras();

        patternModalArrayList = mydb.readPatternsOfField(bundle.getString("Field Name"));

        patternRVAdapter = new PatternRVAdapter(patternModalArrayList,DisplayPatterns.this);
        patternRV = findViewById(R.id.idRVPatterns);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DisplayPatterns.this,RecyclerView.VERTICAL,false);
        patternRV.setLayoutManager(linearLayoutManager);

        patternRV.setAdapter(patternRVAdapter);

    }

    @Override
    public void onBackPressed()
    {
        finish();
        Intent i = new Intent(DisplayPatterns.this, DisplayFields.class);
        startActivity(i);
    }
}


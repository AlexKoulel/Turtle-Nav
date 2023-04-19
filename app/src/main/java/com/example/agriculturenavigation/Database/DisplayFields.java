package com.example.agriculturenavigation.Database;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agriculturenavigation.MainClass;
import com.example.agriculturenavigation.R;

import java.util.ArrayList;


public class DisplayFields extends AppCompatActivity {
    private ArrayList<FieldModal> fieldModalArrayList;
    private DBManager mydb;
    private FieldRVAdapter fieldRVAdapter;
    private RecyclerView fieldRV;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displayfields);

        fieldModalArrayList = new ArrayList<>();
        mydb = new DBManager(DisplayFields.this);

        fieldModalArrayList = mydb.readFields();

        fieldRVAdapter = new FieldRVAdapter(fieldModalArrayList, DisplayFields.this);
        fieldRV = findViewById(R.id.idRVFields);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DisplayFields.this,RecyclerView.VERTICAL,false);
        fieldRV.setLayoutManager(linearLayoutManager);

        fieldRV.setAdapter(fieldRVAdapter);
    }

    @Override
    public void onBackPressed()
    {
        finish();
        Intent i = new Intent(DisplayFields.this, MainClass.class);
        startActivity(i);
    }

}

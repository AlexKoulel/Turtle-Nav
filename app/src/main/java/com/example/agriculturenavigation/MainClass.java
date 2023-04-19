package com.example.agriculturenavigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.agriculturenavigation.Database.DBManager;
import com.example.agriculturenavigation.Database.DisplayFields;
import com.example.agriculturenavigation.Maps.MapsActivity;
import com.example.agriculturenavigation.Maps.MapsActivityViewAll;
import com.example.agriculturenavigation.databinding.ActivityMainBinding;
import com.google.android.gms.maps.GoogleMap;

public class MainClass extends AppCompatActivity {
    private ImageView imgCreateField,imgMap,imgFields;
    private TextView appVersionTV;
    private Button infoBtn;
    private DBManager dbManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbManager = new DBManager(MainClass.this);
        imgCreateField = (ImageView) findViewById(R.id.createImage);
        imgFields = (ImageView) findViewById(R.id.fieldsImage);
        imgMap = (ImageView) findViewById(R.id.mapImage);
        infoBtn = (Button) findViewById(R.id.infobtn);
        appVersionTV = (TextView) findViewById(R.id.versionTV);

        appVersionTV.setText(getResources().getString(R.string.app_version));


        imgCreateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainClass.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        imgFields.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Number Of Fields: ",String.valueOf(dbManager.getNumberOfFields()));
                if(dbManager.getNumberOfFields())
                {
                    Toast.makeText(MainClass.this,"You need to create a field first.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent intent = new Intent(MainClass.this, DisplayFields.class);
                    startActivity(intent);
                }
            }
        });

        imgMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbManager.getNumberOfFields())
                {
                    Toast.makeText(MainClass.this,"You need to create a field first.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent intent = new Intent(MainClass.this, MapsActivityViewAll.class);
                    startActivity(intent);
                }
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainClass.this,InfoClass.class);
                startActivity(intent);
            }
        });
    }

}

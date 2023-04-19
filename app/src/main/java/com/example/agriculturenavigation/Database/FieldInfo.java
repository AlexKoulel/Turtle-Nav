package com.example.agriculturenavigation.Database;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agriculturenavigation.Maps.MapsActivityViewSingleField;
import com.example.agriculturenavigation.Maps.PatternActivity;
import com.example.agriculturenavigation.R;

public class FieldInfo extends AppCompatActivity
{
    private EditText fieldNameEdt;
    private Button updateFieldBtn,deleteFieldBtn,showFieldBtn, createPatternBtn, patternsBtn;
    private DBManager dbManager;
    String fieldName, fieldLocation,fieldPattern;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_info);

        fieldNameEdt = findViewById(R.id.idEdtLandName);
        updateFieldBtn = findViewById(R.id.idBtnUpdateLand);
        deleteFieldBtn = findViewById(R.id.idBtnDeleteLand);
        showFieldBtn = findViewById(R.id.idBtnShowField);
        createPatternBtn = findViewById(R.id.idBtnCreatePattern);
        patternsBtn = findViewById(R.id.idBtnPatterns);
        dbManager = new DBManager(FieldInfo.this);

        fieldName = getIntent().getStringExtra("name");
        fieldLocation = getIntent().getStringExtra("location");
        fieldPattern = getIntent().getStringExtra("pattern");

        fieldNameEdt.setText(fieldName);

        updateFieldBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                dbManager.updateField(fieldName, fieldNameEdt.getText().toString());
                Toast.makeText(FieldInfo.this, "Field name changed to " + fieldNameEdt.getText().toString() + ".", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(FieldInfo.this, DisplayFields.class);
                startActivity(i);
            }
        });

        deleteFieldBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbManager.deleteField(fieldName);
                Toast.makeText(FieldInfo.this,"Field " + fieldNameEdt.getText().toString() + " deleted.",Toast.LENGTH_SHORT).show();
                Intent i = new Intent(FieldInfo.this, DisplayFields.class);
                startActivity(i);
            }
        });

        showFieldBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dbManager.retrievePolygon(fieldLocation);
                Intent i = new Intent(FieldInfo.this, MapsActivityViewSingleField.class);
                Bundle bundle = new Bundle();
                bundle.putString("Field location",fieldLocation);
                bundle.putString("Field Name",fieldName);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        createPatternBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dbManager.retrievePolygon(fieldLocation);
                Intent i = new Intent(FieldInfo.this, PatternActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Field location",fieldLocation);
                bundle.putString("Field Name",fieldName);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        patternsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view){
                //Intent i = new Intent(FieldInfo.this, MapsDirections.class);
                Intent i = new Intent(FieldInfo.this,DisplayPatterns.class);
                Bundle bundle = new Bundle();
                bundle.putString("Field location",fieldLocation);
                bundle.putString("Field Name",fieldName);
                bundle.putString("Field Pattern",fieldPattern);
                i.putExtras(bundle);
                startActivity(i);
            }
        });
    }
}

package com.example.agriculturenavigation.Database;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agriculturenavigation.Maps.MapsDirections;
import com.example.agriculturenavigation.R;

public class PatternInfo extends AppCompatActivity
{
    private EditText patternNameEdt;
    private Button btnUpdatePattern,btnDeletePattern,btnNavigation;
    private DBManager dbManager;
    private String patternName,pattern,belongto;

    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_info);

        patternNameEdt = findViewById(R.id.idEdtPatternName);
        btnUpdatePattern = (Button) findViewById(R.id.idBtnUpdatePattern);
        btnDeletePattern = (Button) findViewById(R.id.idBtnDeletePattern);
        btnNavigation = (Button) findViewById(R.id.idBtnCreatePattern);

        dbManager = new DBManager(PatternInfo.this);

        patternName = getIntent().getStringExtra("patternname");
        pattern = getIntent().getStringExtra("pattern");
        belongto = getIntent().getStringExtra("belongsto");

        patternNameEdt.setText(patternName);

        btnUpdatePattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbManager.updatePattern(patternName,patternNameEdt.getText().toString());
                Toast.makeText(PatternInfo.this,"Pattern name changed to " +patternNameEdt.getText().toString() + "." ,Toast.LENGTH_SHORT).show();
                Intent i = new Intent(PatternInfo.this, DisplayFields.class);
                startActivity(i);
            }
        });

        btnDeletePattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbManager.deletePattern(patternName);
                Toast.makeText(PatternInfo.this,"Pattern " + patternName.toString() + " deleted.",Toast.LENGTH_SHORT).show();
                Intent i = new Intent(PatternInfo.this, DisplayFields.class);
                startActivity(i);
            }
        });

        btnNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PatternInfo.this, MapsDirections.class);
                Bundle bundle = new Bundle();
                bundle.putString("patternname",patternName);
                bundle.putString("pattern",pattern);
                bundle.putString("belongsto",belongto);
                i.putExtras(bundle);
                startActivity(i);

            }
        });
    }

    @Override
    public void onBackPressed()
    {
        finish();
        Intent i = new Intent(PatternInfo.this, DisplayFields.class);
        startActivity(i);
    }
}

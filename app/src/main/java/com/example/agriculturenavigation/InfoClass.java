package com.example.agriculturenavigation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class InfoClass extends AppCompatActivity
{
    TextView appInfoTv;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_layout);

        appInfoTv = (TextView) findViewById(R.id.tvAppName);

        appInfoTv.setText(getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.app_version));
    }
}

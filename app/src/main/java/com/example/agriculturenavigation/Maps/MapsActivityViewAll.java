package com.example.agriculturenavigation.Maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

//mport com.example.agriculturenavigation.Maps.databinding.ActivityMapsViewAllBinding;
import com.example.agriculturenavigation.Database.DBManager;
import com.example.agriculturenavigation.Database.FieldInfo;
import com.example.agriculturenavigation.Database.FieldModal;
import com.example.agriculturenavigation.NavUtil;
import com.example.agriculturenavigation.PermissionUtils;
import com.example.agriculturenavigation.R;
import com.example.agriculturenavigation.databinding.ActivityMapsViewAllBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivityViewAll extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap mMap;
    private ActivityMapsViewAllBinding binding;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;

    private ArrayList<FieldModal> fieldModalArrayList = new ArrayList<>();
    private DBManager mydb;
    private UiSettings mUI;
    private NavUtil navobj = new NavUtil();
    private Button btnShowInfo;
    private String fieldName,fieldLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsViewAllBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view_all);
        mapFragment.getMapAsync(this);

        btnShowInfo = (Button) findViewById(R.id.btnShowInfo);
        mydb = new DBManager(MapsActivityViewAll.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        btnShowInfo.setVisibility(View.INVISIBLE);
        enableMyLocation(mMap);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnPolygonClickListener(this);
        showAllFields();
        mUI = mMap.getUiSettings();
        mUI.setZoomControlsEnabled(true);

        btnShowInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MapsActivityViewAll.this, FieldInfo.class);
                Bundle bundle = new Bundle();
                bundle.putString("name",fieldName);
                bundle.putString("location",fieldLocation);
                i.putExtras(bundle);
                startActivity(i);
            }
        });
    }

    public void showAllFields()
    {
        fieldModalArrayList = new ArrayList<>();
        Polygon polygono = null;
        fieldModalArrayList = mydb.readFields();
        for(int i =0;i<fieldModalArrayList.size();i++)
        {
            List<LatLng> list = mydb.retrievePolygon(fieldModalArrayList.get(i).getFieldLocation());
            polygono = mMap.addPolygon(new PolygonOptions().clickable(true).add(new LatLng(0,0)));//,new LatLng(list.get(1).longitude,list.get(1).longitude),new LatLng(list.get(2).longitude,list.get(2).longitude)));
            polygono.setPoints(list);
            polygono.setTag(fieldModalArrayList.get(i).getFieldName());
            polygono.setFillColor(0xffffffff);
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }
    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {
        Toast.makeText(this,polygon.getTag().toString(),Toast.LENGTH_SHORT).show();
        navobj.zoomOnField(polygon.getPoints(),mMap);
        btnShowInfo.setVisibility(View.VISIBLE);
        fieldName = polygon.getTag().toString();
        fieldLocation = polygon.getPoints().toString();
    }
    @SuppressLint("MissingPermission")
    private void enableMyLocation(@NonNull GoogleMap map) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            return;
        }

        Toast.makeText(this,"NO PERMISSIONS",Toast.LENGTH_LONG).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            enableMyLocation(mMap);
        } else {
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

}
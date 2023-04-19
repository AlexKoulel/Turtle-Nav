package com.example.agriculturenavigation.Maps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.agriculturenavigation.Database.DBManager;
import com.example.agriculturenavigation.Database.DisplayFields;
import com.example.agriculturenavigation.NavUtil;
import com.example.agriculturenavigation.PermissionUtils;
import com.example.agriculturenavigation.R;
import com.example.agriculturenavigation.databinding.ActivityMapsViewSingleFieldBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;
//import com.example.agriculturenavigation.Maps.databinding.ActivityMapsViewSingleFieldBinding;

public class MapsActivityViewSingleField extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private Button btnEditField, btnCancelEdit, btnSaveEdit;

    private List<LatLng> pointsList = new ArrayList<>();
    private List<String> pointTagsList = new ArrayList<>();

    private Marker currentmarker = null;
    private int markernumber = 0;
    private ActivityMapsViewSingleFieldBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    private List<LatLng> list = new ArrayList<>();
    private DBManager mydb;
    private UiSettings mUI;
    private NavUtil navobj = new NavUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsViewSingleFieldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view_single_field);
        mapFragment.getMapAsync(this);

        btnEditField = (Button) findViewById(R.id.btnEditField);
        btnCancelEdit = (Button) findViewById(R.id.btnCancelEdit);
        btnSaveEdit = (Button) findViewById(R.id.btnSaveEdit);

        mydb = new DBManager(MapsActivityViewSingleField.this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation(mMap);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnMarkerClickListener(this);
        mUI = mMap.getUiSettings();
        mUI.setZoomControlsEnabled(true);

        Bundle bundle = getIntent().getExtras();
        String fieldlocation = bundle.getString("Field location");
        String fieldName = bundle.getString("Field Name");
        Polygon polygono = null;
        list = mydb.retrievePolygon(fieldlocation);
        polygono = mMap.addPolygon(new PolygonOptions().clickable(true).add(new LatLng(0, 0)));
        polygono.setPoints(list);
        polygono.setTag(fieldName);
        polygono.setFillColor(0xD9EAD3);
        navobj.zoomOnField(list,mMap);

        Polygon finalPolygono = polygono;
        btnEditField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalPolygono.setVisible(false);
                for (LatLng i : list) {
                    LatLng point = i;
                    currentmarker = mMap.addMarker(new MarkerOptions().position(point).title(point.toString()).snippet("Marker " + markernumber).draggable(true));
                    currentmarker.setTag("Marker " + markernumber);
                    pointsList.add(currentmarker.getPosition());
                    pointTagsList.add(currentmarker.getTag().toString());
                    markernumber++;
                }
                btnCancelEdit.setVisibility(View.VISIBLE);
                btnSaveEdit.setVisibility(View.VISIBLE);
                btnEditField.setVisibility(View.INVISIBLE);
            }
        });

        Polygon finalPolygono1 = polygono;
        btnSaveEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Polygon neopolygono = null;
                neopolygono = mMap.addPolygon(new PolygonOptions().clickable(true).add(new LatLng(0,0)));
                neopolygono.setPoints(pointsList);
                mydb.updateFIeldCoordinates(finalPolygono1.getPoints().toString(),neopolygono.getPoints().toString());
                Intent intent = new Intent(MapsActivityViewSingleField.this, DisplayFields.class);
                startActivity(intent);
            }
        });

        btnCancelEdit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(getIntent());
            }
        });


        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                int pos = 0;
                for (int i = 0; i < pointTagsList.size(); i++)
                {
                    if(pointTagsList.get(i).equals(marker.getTag()))
                    {
                        pos = i;
                    }
                }
                pointsList.set(pos,marker.getPosition());
            }

            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {

            }

        });
    }


    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Toast.makeText(this,marker.getPosition().toString(),Toast.LENGTH_LONG).show();
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
        navobj.zoomOnField(list,mMap);
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
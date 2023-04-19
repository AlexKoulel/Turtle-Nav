package com.example.agriculturenavigation.Maps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.example.agriculturenavigation.Database.DBManager;
import com.example.agriculturenavigation.Database.DisplayFields;
import com.example.agriculturenavigation.PermissionUtils;
import com.example.agriculturenavigation.R;
import com.example.agriculturenavigation.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap aMap;
    private ActivityMapsBinding binding;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private int markernumber;
    private double area = 0;
    private boolean permissionDenied = false;

    private List<LatLng> pointsList = new ArrayList<>();
    private List<String> pointTagsList = new ArrayList<>();

    private Button btnCreateField,btnDiscardMarkers,btnSaveField,btnCancelField;
    private EditText fieldNameEdt;
    private TextView titleTxt;

    private Marker currentmarker = null;
    private Polygon polygon1 = null;
    private PolygonOptions plg = new PolygonOptions();

    private UiSettings mUI;

    private DBManager mydb;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnCreateField =(Button) findViewById(R.id.btnCreateField);
        btnDiscardMarkers = (Button) findViewById(R.id.btnDiscardMarkersCreate);
        markernumber = 0;
        mydb = new DBManager(MapsActivity.this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap aMap) {
        enableMyLocation(aMap);
        aMap.setOnMyLocationButtonClickListener(this);
        aMap.setOnMyLocationClickListener(this);
        aMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        btnCreateField.setVisibility(View.VISIBLE);
        btnDiscardMarkers.setVisibility(View.INVISIBLE);


        aMap.setOnMarkerClickListener(this);
        aMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()

        {@Override public void onMapLongClick(@NonNull LatLng point)
        {
            currentmarker = aMap.addMarker(new MarkerOptions().position(point).title(point.toString()).snippet("Marker "+markernumber).draggable(true));    //ΠΡΕΠΕΙ ΝΑ ΓΙΝΕΤΑΙ UPDATE ΤΟ SNIPPET
            assert currentmarker != null;
            currentmarker.setTag("Marker " + markernumber);
            Log.d("MARKER TAG:", Objects.requireNonNull(currentmarker.getTag()).toString());
            pointsList.add(currentmarker.getPosition());
            pointTagsList.add(currentmarker.getTag().toString());
            markernumber++;
            btnDiscardMarkers.setVisibility(View.VISIBLE);
        }});

        aMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
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
                //Log.d("MARKER TAG",String.valueOf(pos));
                pointsList.set(pos,marker.getPosition());
                //Log.d("POSITION",pointsList.get(pos).toString());
            }

            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {

            }
        });

        btnCreateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(markernumber>=3)
                {
                    for(int i =0;i<pointsList.size();i++)
                    {
                        plg.add(new LatLng(pointsList.get(i).latitude,pointsList.get(i).longitude));
                    }
                    double area = 0;
                    polygon1 = aMap.addPolygon(plg);
                    area = calculateDistance();
                    if(area > 100000)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        builder.setCancelable(false);
                        builder.setTitle("Field area limit.");
                        builder.setMessage("Field cannot be more than 100,000 squared meters.");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                                startActivity(getIntent());
                            }
                        });
                        builder.show();
                    }
                    else
                    {
                        createNewFieldDialog();
                    }
                }
                else
                {
                    Toast.makeText(MapsActivity.this,"Add at least 3 points on the map.",Toast.LENGTH_LONG).show();
                }
            }
        });

        btnDiscardMarkers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markernumber =0;
                pointsList.clear();
                pointTagsList.clear();
                aMap.clear();
                btnDiscardMarkers.setVisibility(View.INVISIBLE);
            }
        });

        mUI = aMap.getUiSettings();
        mUI.setZoomControlsEnabled(true);
        mUI.setAllGesturesEnabled(true);

    }

    private double calculateDistance()
    {
        List<LatLng> latlngs = new ArrayList<>();
        latlngs = polygon1.getPoints();
        area = SphericalUtil.computeArea(latlngs);
        BigDecimal barea = new BigDecimal(area);
        BigDecimal finalarea = barea.setScale(2, RoundingMode.HALF_UP);
        Log.d("Area:", String.valueOf(finalarea));
        area = finalarea.doubleValue();
        return area;
    }

    @SuppressLint("SetTextI18n")
    private void createNewFieldDialog()
    {
        dialogBuilder = new AlertDialog.Builder(this);
        final View fieldPopUpView = getLayoutInflater().inflate(R.layout.popup_window,null);
        fieldNameEdt = (EditText) fieldPopUpView.findViewById(R.id.idEdtPopUpTxtField);
        titleTxt = (TextView) fieldPopUpView.findViewById(R.id.popupTitle);

        btnSaveField = (Button) fieldPopUpView.findViewById(R.id.btnSaveFieldPopUp);
        btnCancelField = (Button) fieldPopUpView.findViewById(R.id.btnCancelFieldPopUp);

        dialogBuilder.setView(fieldPopUpView);
        dialog = dialogBuilder.create();
        dialog.show();
        fieldNameEdt.setHint("e.g. Field 1");
        titleTxt.setText("Enter field name.");

        btnSaveField.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String strfieldNameEdt = fieldNameEdt.getText().toString();
                if(TextUtils.isEmpty(strfieldNameEdt))
                {
                    fieldNameEdt.setError("Field name cannot be empty.");
                }
                else
                {
                    if(mydb.addField(fieldNameEdt.getText().toString(),polygon1.getPoints().toString(), String.valueOf(area),null)==1)
                    {
                        Toast.makeText(MapsActivity.this,"Field added.",Toast.LENGTH_SHORT).show();
                        mydb.addPattern("TEST","1230","Field1");
                        Intent intent = new Intent(MapsActivity.this, DisplayFields.class);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(MapsActivity.this,"Field name already exists.",Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        btnCancelField.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
                dialog.dismiss();
                startActivity(getIntent());
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                {
                    finish();
                    dialog.dismiss();
                    startActivity(getIntent());
                }
                return true;
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
        PermissionUtils.requestLocationPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, true);
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
            enableMyLocation(aMap);
        } else {
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}
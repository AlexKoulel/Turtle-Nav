package com.example.agriculturenavigation.Maps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agriculturenavigation.Database.DBManager;
import com.example.agriculturenavigation.Database.DisplayFields;
import com.example.agriculturenavigation.PermissionUtils;
import com.example.agriculturenavigation.R;
import com.example.agriculturenavigation.databinding.ActivityPatternBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class PatternActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback  {

    private GoogleMap mMap;
    protected ActivityPatternBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;

    private DBManager mydb;
    protected UiSettings mUI;
    private Button btnSetABLine,btnDiscardMarkers,btnSetPointA,btnSetPointB,btnSavePattern,btnDiscardPattern;

    protected Button btnSaveDistance,btnCancelDistance;
    private EditText fieldDistanceEdt,patternNameEdt;
    protected TextView titleTxt;

    private Marker markerA,markerB;

    protected List<LatLng> list = new ArrayList<>();

    private Polygon polygono = null;

    protected LatLng currentLatLng = null;

    private Boolean noMoreLeft = false;
    private Boolean MarkerOutsideOfPolygonLeft = false;
    private Boolean MarkerOutsideOfPolygonRight = false;

    protected LatLng BehindALatLng = null;
    private Double distance = (double) 0;

    protected AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private String fieldName = null;
    protected List<List<LatLng>> polylines = new ArrayList<>();

    int markercount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPatternBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapPattern);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        mydb = new DBManager(PatternActivity.this);

        btnSetABLine = (Button) findViewById(R.id.btnSetABLine);
        btnDiscardMarkers = (Button) findViewById(R.id.btnDiscardMarkers);
        btnSetPointA = (Button) findViewById(R.id.btnSetPointA);
        btnSetPointB = (Button) findViewById(R.id.btnSetPointB);
        btnSavePattern = (Button) findViewById(R.id.btnSavePattern);
        btnDiscardPattern = (Button) findViewById(R.id.btnDiscardPattern);

        Bundle bundle = getIntent().getExtras();
        fieldName = bundle.getString("Field Name");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation(mMap);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        btnSetABLine.setVisibility(View.VISIBLE);
        btnDiscardMarkers.setVisibility(View.INVISIBLE);
        btnSetPointA.setVisibility(View.INVISIBLE);
        btnSetPointB.setVisibility(View.INVISIBLE);
        btnSavePattern.setVisibility(View.INVISIBLE);
        btnDiscardPattern.setVisibility(View.INVISIBLE);

        mUI = mMap.getUiSettings();
        mUI.setZoomControlsEnabled(true);
        mMap.setOnPolygonClickListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.setOnMarkerClickListener(this);
        mUI = mMap.getUiSettings();
        mUI.setZoomControlsEnabled(true);

        setField();

        mMap.setOnMapLongClickListener(point -> {
            if(markercount == 0)
            {
                if(PolyUtil.containsLocation(point, polygono.getPoints(), true))
                {
                    markerA = mMap.addMarker(new MarkerOptions()
                            .position(point)
                            .title("Point A"));
                    markercount++;
                    btnDiscardMarkers.setVisibility(View.VISIBLE);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Outside of field.",Toast.LENGTH_SHORT).show();
                }

            }
            else if(markercount == 1) {
                if (PolyUtil.containsLocation(point, polygono.getPoints(), true)) {
                    markerB = mMap.addMarker(new MarkerOptions()
                            .position(point)
                            .title("Point B"));
                    markercount++;
                } else {
                    Toast.makeText(getApplicationContext(), "Outside of field.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnSetABLine.setOnClickListener(view -> {
            if(markercount == 2)
            {
                markerA.setVisible(false);
                markerB.setVisible(false);
                createNewFieldDialogMeters();
            }
            else
            {
                Toast.makeText(PatternActivity.this,"Add 2 points on the map.",Toast.LENGTH_LONG).show();
            }
        });

        btnDiscardMarkers.setOnClickListener(view -> {
            markercount = 0;
            markerA.remove();
            try {
                markerB.remove();
            }
            catch(Exception e)
            {
                Log.e("B marker does not exist!",e.getMessage(),e);
            }
            btnDiscardMarkers.setVisibility(View.INVISIBLE);
        });

        btnSavePattern.setOnClickListener(view -> {
            createNewFieldDialogPattern();
        });

        btnDiscardPattern.setOnClickListener(view -> {
            finish();
            startActivity(getIntent());
        });
    }


    //Παράθυρο popup για εισαγωγή απόστασης
    @SuppressLint("SetTextI18n")
    private void createNewFieldDialogMeters()
    {
        dialogBuilder = new AlertDialog.Builder(this);
        final View fieldPopUpView = getLayoutInflater().inflate(R.layout.popup_window,null);
        fieldDistanceEdt = (EditText) fieldPopUpView.findViewById(R.id.idEdtPopUpTxtField);
        titleTxt = (TextView) fieldPopUpView.findViewById(R.id.popupTitle);

        btnSaveDistance = (Button) fieldPopUpView.findViewById(R.id.btnSaveFieldPopUp);
        btnCancelDistance = (Button) fieldPopUpView.findViewById(R.id.btnCancelFieldPopUp);
        dialogBuilder.setView(fieldPopUpView);
        dialog = dialogBuilder.create();
        dialog.show();

        titleTxt.setText("Enter distance between lines.");
        fieldDistanceEdt.setHint("e.g. 10");
        fieldDistanceEdt.setInputType(InputType.TYPE_CLASS_NUMBER);

        //Αποθήκευση pattern
        btnSaveDistance.setOnClickListener(v -> {
            String strdistance = fieldDistanceEdt.getText().toString();
            if(TextUtils.isEmpty(strdistance))
            {
                fieldDistanceEdt.setError("Distance cannot be empty.");
            }
            else if(Integer.parseInt(strdistance) == 0)
            {
                fieldDistanceEdt.setError("Distance cannot be 0.");
            }
            else
            {
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                fieldDistanceEdt.setVisibility(View.INVISIBLE);
                btnSaveDistance.setVisibility(View.INVISIBLE);
                btnCancelDistance.setVisibility(View.INVISIBLE);
                titleTxt.setVisibility(View.INVISIBLE);
                dialog.dismiss();

                final View loadingPopUpView = getLayoutInflater().inflate(R.layout.popup_loading,null);
                dialogBuilder.setView(loadingPopUpView);
                dialog = dialogBuilder.create();
                dialog.show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        double headingOfAandB = SphericalUtil.computeHeading(markerA.getPosition(),markerB.getPosition());
                        createParallelLines(headingOfAandB, Double.parseDouble(strdistance));
                        dialog.dismiss();
                    }
                }, 2000);
                btnSetABLine.setVisibility(View.INVISIBLE);
                btnDiscardMarkers.setVisibility(View.INVISIBLE);
                btnSavePattern.setVisibility(View.VISIBLE);
                btnDiscardPattern.setVisibility(View.VISIBLE);
            }

        });

        btnCancelDistance.setOnClickListener(v -> {
            finish();
            startActivity(getIntent());
        });

    }

    //Παράθυρο popup για αποθήκευση pattern
    @SuppressLint("SetTextI18n")
    private void createNewFieldDialogPattern()
    {
        dialogBuilder = new AlertDialog.Builder(this);
        final View fieldPopUpView = getLayoutInflater().inflate(R.layout.popup_window,null);
        patternNameEdt = (EditText) fieldPopUpView.findViewById(R.id.idEdtPopUpTxtField);
        titleTxt = (TextView) fieldPopUpView.findViewById(R.id.popupTitle);

        btnSaveDistance = (Button) fieldPopUpView.findViewById(R.id.btnSaveFieldPopUp);
        btnCancelDistance = (Button) fieldPopUpView.findViewById(R.id.btnCancelFieldPopUp);

        dialogBuilder.setView(fieldPopUpView);
        dialog = dialogBuilder.create();
        dialog.show();

        titleTxt.setText("Enter pattern name.");
        patternNameEdt.setHint("e.g. Fertilizing");

        btnSaveDistance.setOnClickListener(v -> {
            String strPatternName = patternNameEdt.getText().toString();
            if(TextUtils.isEmpty(strPatternName))
            {
                fieldDistanceEdt.setError("Name cannot be empty.");
            }
            else
            {
                if(mydb.addPattern(strPatternName, polylines.toString(), fieldName) == 1)
                {
                    Intent intent = new Intent(PatternActivity.this, DisplayFields.class);
                    startActivity(intent);
                    dialog.dismiss();
                }
                else
                {
                    Toast.makeText(PatternActivity.this,"Pattern Name already exists.",Toast.LENGTH_LONG).show();
                }

            }

        });

        //Ακύρωση αποθήκευσης
        btnCancelDistance.setOnClickListener(v -> {
            finish();
            startActivity(getIntent());
        });
    }

    private void createParallelLines(double heading,double distanceBetween)
    {
        double headingOfBehindA = heading +180;
        double metersBehindMarkerA = findMeters(markerA.getPosition(),headingOfBehindA);

        while(!MarkerOutsideOfPolygonLeft)
        {
            currentLatLng = SphericalUtil.computeOffset(markerA.getPosition(),distance,heading - 90);
            BehindALatLng = SphericalUtil.computeOffset(currentLatLng,metersBehindMarkerA,headingOfBehindA);
            LatLng pointA = findPoint(BehindALatLng,heading,metersBehindMarkerA);
            if(PolyUtil.isLocationOnEdge(pointA, polygono.getPoints(), true, 0.5)) //Αν είναι μέσα στο πολύγωνο τα points απο αριστέρα/δεξία και το αρχικό
            {
                LatLng pointB = findPoint(pointA,heading,metersBehindMarkerA);
                createABLine(pointA,pointB);

                LatLng furtherPointA;
                LatLng furtherPointB = pointB;
                while(!noMoreLeft)
                {
                    furtherPointA = findPoint(furtherPointB,heading,metersBehindMarkerA);
                    furtherPointB = findPoint(furtherPointA,heading,metersBehindMarkerA);
                    createABLine(furtherPointA,furtherPointB);
                }
                distance += distanceBetween;
            }
            else
            {
                MarkerOutsideOfPolygonLeft =true;
            }
            noMoreLeft = false;
        }

        distance = distanceBetween;
        while(!MarkerOutsideOfPolygonRight)
        {
            currentLatLng = SphericalUtil.computeOffset(markerA.getPosition(),distance,heading + 90);
            BehindALatLng = SphericalUtil.computeOffset(currentLatLng,metersBehindMarkerA,headingOfBehindA);
            LatLng pointA = findPoint(BehindALatLng,heading,metersBehindMarkerA);
            if(PolyUtil.isLocationOnEdge(pointA, polygono.getPoints(), true, 0.5)) //Αν είναι μέσα στο πολύγωνο τα points απο αριστέρα/δεξία και το αρχικό
            {
                LatLng pointB = findPoint(pointA,heading,metersBehindMarkerA);
                createABLine(pointA,pointB);

                LatLng furtherPointA;
                LatLng furtherPointB = pointB;
                while(!noMoreLeft)
                {
                    furtherPointA = findPoint(furtherPointB,heading,metersBehindMarkerA);
                    furtherPointB = findPoint(furtherPointA,heading,metersBehindMarkerA);
                    createABLine(furtherPointA,furtherPointB);
                }
                distance += distanceBetween;
            }
            else
            {
                MarkerOutsideOfPolygonRight =true;
            }
            noMoreLeft = false;
        }
    }

    private LatLng findPoint(LatLng startingPoint,double heading,double meterLimit)
    {
        boolean firstCheck = false;
        boolean found = false;
        double distance = 0;
        LatLng point = null;
        int lineOfStartingPoint = PolyUtil.locationIndexOnPath(startingPoint,polygono.getPoints(),true,1);
        if(meterLimit>1000)
        {
            meterLimit += meterLimit;
        }
        else
        {
            meterLimit=1000;
        }
        while(!found)
        {
            point = SphericalUtil.computeOffset(startingPoint,distance,heading);
            if(PolyUtil.isLocationOnEdge(point,polygono.getPoints(),true,0.5)&& firstCheck && PolyUtil.locationIndexOnPath(point,polygono.getPoints(),true,1) != lineOfStartingPoint)
            {
                found = true;
            }
            else
            {
                if(!firstCheck)
                {
                    distance += 2;
                    firstCheck = true;
                }
                else
                {
                    if(distance <= meterLimit)
                    {
                        distance +=1;
                    }
                    else
                    {
                        found = true;
                    }
                }

            }
        }
        return point;
    }

    private double findMeters(LatLng pointA,double heading)
    {
        boolean foundBack = false;
        boolean foundFront = false;
        LatLng pointBack = null;
        LatLng pointFront = null;
        double currentDistance = 1;
        double behindDistance=0,frontDistance=0,finalDistance =0;
        double headingOfBehindA = heading;
        while(!foundBack && !foundFront)
        {
            pointBack = SphericalUtil.computeOffset(markerA.getPosition(),currentDistance,headingOfBehindA);
            if(PolyUtil.containsLocation(pointBack.latitude,pointBack.longitude,polygono.getPoints(),true))
            {
                currentDistance++;
            }
            else
            {
                foundBack = true;
                behindDistance = currentDistance;
            }
        }
        currentDistance = 1;

        while(!foundFront)
        {
            pointFront = SphericalUtil.computeOffset(markerA.getPosition(),currentDistance,headingOfBehindA + 180);
            if(PolyUtil.containsLocation(pointFront.latitude,pointFront.longitude,polygono.getPoints(),true))
            {
                currentDistance++;
            }
            else
            {
                foundFront = true;
                frontDistance = currentDistance;
                frontDistance += currentDistance;
            }
        }
        finalDistance = frontDistance + behindDistance;
        return finalDistance;
    }

    public void createABLine(LatLng pointA,LatLng pointB)
    {
        if(PolyUtil.isLocationOnEdge(pointA,polygono.getPoints(),true,0.5))
        {
            if(PolyUtil.isLocationOnEdge(pointB,polygono.getPoints(),true,0.5))
            {
                Polyline abParallelLine = mMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .add((pointA),(pointB))
                        .color(0xff388E3C));
                polylines.add(abParallelLine.getPoints());
            }
            else
            {
                noMoreLeft = false;
            }
        }
        else
        {
            noMoreLeft = true;
        }
    }

    private void setField()
    {
        Bundle bundle = getIntent().getExtras();
        String fieldlocation = bundle.getString("Field location");
        list = mydb.retrievePolygon(fieldlocation);
        polygono = mMap.addPolygon(new PolygonOptions().clickable(true).add(new LatLng(0, 0)));
        polygono.setPoints(list);
        polygono.setTag(fieldName);
        polygono.setStrokeWidth(10);
        zoomOnField(list);
    }



    public void zoomOnField(List<LatLng> coordinateslist)
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i=0;i<coordinateslist.size();i++)
        {
            builder.include(coordinateslist.get(i));
        }
        int padding = 300;
        LatLngBounds bounds = builder.build();
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,padding);
        mMap.animateCamera(cu);
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

    }
}
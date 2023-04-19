package com.example.agriculturenavigation.Maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.agriculturenavigation.Database.DBManager;
import com.example.agriculturenavigation.NavUtil;
import com.example.agriculturenavigation.PermissionUtils;
import com.example.agriculturenavigation.R;
import com.example.agriculturenavigation.databinding.ActivityMapsDirectionsBinding;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StampStyle;
import com.google.android.gms.maps.model.TextureStyle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MapsDirections extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private ActivityMapsDirectionsBinding binding;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    private DBManager mydb;
    private UiSettings mUI;
    private Button btnStartDirections,btnStopDirections;
    private TextView tvDistance;


    private FusedLocationProviderClient fusedLocationClient;

    private List<LatLng> list = new ArrayList<>();
    private List<LatLng> pointList = new ArrayList<>();

    private LocationRequest locationRequest;

    private Location currentlocationvar = null;
    private Location previouslocationvar = null;

    private String currentlocationlatitude = null;
    private String currentlocationlongitude = null;
    private Polygon polygono = null;

    private List<Polyline> polylines = new ArrayList<>();


    private boolean clickedCameraButton = false;
    private boolean firstTimeLocation = false;
    private boolean onABLine = false;

    private NavUtil navobj = new NavUtil();

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if(locationResult == null)
            {
                return;
            }
            for(Location location : locationResult.getLocations())
            {
                currentlocationlatitude = String.valueOf(location.getLatitude());
                currentlocationlongitude = String.valueOf(location.getLongitude());
                currentlocationvar = location;
                try {
                    liveDirections(currentlocationlatitude,currentlocationlongitude,tvDistance);
                } catch (Exception e) {
                    Log.d("Something went wrong with the navigation system",e.getMessage(),e);
                    throw new RuntimeException(e);
                }

                if(clickedCameraButton)
                {
                    setCameraToUser();
                    routeDraw();
                    firstTimeLocation = true;
                    previouslocationvar = currentlocationvar;
                }
                else
                {
                    firstTimeLocation = false;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsDirectionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapdirections);
        mapFragment.getMapAsync(this);

        mydb = new DBManager(MapsDirections.this);

        btnStartDirections = (Button) findViewById(R.id.btnStartDirections);
        btnStopDirections = (Button) findViewById(R.id.btnStopDirections);
        tvDistance = (TextView)(findViewById(R.id.tvDistance));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        tvDistance.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            getLastLocation();
            checkSettingsAndStartLocationUpdates();
        }
        else
        {
            //Do nothing
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        stopLocationUpdates();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation(mMap);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        btnStartDirections.setVisibility(View.VISIBLE);

        mMap.setOnMarkerClickListener(this);

        mUI = mMap.getUiSettings();
        mUI.setZoomControlsEnabled(true);
        mMap.setOnPolygonClickListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.setOnMarkerClickListener(this);
        mUI = mMap.getUiSettings();
        mUI.setZoomControlsEnabled(true);

        btnStartDirections.setVisibility(View.VISIBLE);
        btnStopDirections.setVisibility(View.INVISIBLE);

        setField();

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                try {
                    navobj.zoomOnField(polygono.getPoints(),mMap);
                }
                catch(Exception e)
                {
                    Log.d("Something went wrong with map zoom",e.getMessage(),e);
                }
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()

        {@Override public void onMapLongClick(LatLng point)
        {

        }});

        btnStartDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvDistance.setVisibility(View.VISIBLE);
                clickedCameraButton = true;
                btnStartDirections.setVisibility(View.INVISIBLE);
                btnStopDirections.setVisibility(View.VISIBLE);
            }
        });

        btnStopDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvDistance.setVisibility(View.INVISIBLE);
                clickedCameraButton = false;
                btnStartDirections.setVisibility(View.VISIBLE);
                btnStopDirections.setVisibility(View.INVISIBLE);
                navobj.zoomOnField(polygono.getPoints(),mMap);
            }
        });

    }


    @SuppressLint("SetTextI18n")
    private void liveDirections(String Latitude, String Longitude, TextView tv)
    {
        LatLng currentLocationOfTheUser = new LatLng(currentlocationvar.getLatitude(),currentlocationvar.getLongitude());
        int whichPoint = PolyUtil.locationIndexOnEdgeOrPath(currentLocationOfTheUser,pointList,false,true,3);
        Log.d("Which Point: ",String.valueOf(whichPoint));

        if(whichPoint % 2 ==0)
        {
            resetLineColors();
            onABLine = true;
            Double distanceToLine = PolyUtil.distanceToLine(currentLocationOfTheUser,polylines.get(whichPoint).getPoints().get(0),polylines.get(whichPoint).getPoints().get(1));
            BigDecimal bdDistance = new BigDecimal(distanceToLine);
            BigDecimal finalDistance = bdDistance.setScale(2, RoundingMode.HALF_UP);
            tvDistance.setText(String.valueOf(finalDistance));
            Polyline polyline = polylines.get(whichPoint);
            polyline.setColor(0xff388E3C);
        }
        else if (whichPoint % 2 == 1)
        {
            resetLineColors();
            onABLine = true;
            Double distanceToLine = PolyUtil.distanceToLine(currentLocationOfTheUser,polylines.get(whichPoint+1).getPoints().get(0),polylines.get(whichPoint+1).getPoints().get(1));
            BigDecimal bdDistance = new BigDecimal(distanceToLine);
            BigDecimal finalDistance = bdDistance.setScale(2, RoundingMode.HALF_UP);
            tvDistance.setText(String.valueOf(finalDistance));
            Polyline polyline = polylines.get(whichPoint+1);
            polyline.setColor(0xff388E3C);
        }
        else if(whichPoint == -1)
        {
            onABLine = false;
            tvDistance.setText("-");
            resetLineColors();
        }
    }

    private void routeDraw()
    {
        StampStyle stampStyle = TextureStyle.newBuilder(BitmapDescriptorFactory.fromResource(com.google.maps.android.R.drawable.abc_ab_share_pack_mtrl_alpha)).build();
        if(!firstTimeLocation)
        {
            //Do not draw line.
        }
        else
        {
            Polyline route = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(new LatLng(previouslocationvar.getLatitude(),previouslocationvar.getLongitude()),new LatLng(currentlocationvar.getLatitude(),currentlocationvar.getLongitude()))
                .color(Color.RED));
        }
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Settings of device are satisfied and we can start location updates
                startLocationUpdates();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Settings of device are not satisfied
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(MapsDirections.this, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

    }

    private void stopLocationUpdates()
    {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null)
                {
                    /**Έχουμε τοποθεσία **/
                    Log.d("LOCATION","onSuccess: " + location.toString());
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
    private void setField()
    {

        Bundle bundle = getIntent().getExtras();
        String belongsTo = bundle.getString("belongsto");
        String fieldlocation = mydb.getFieldCoordinates(belongsTo);
        String fieldPattern = bundle.getString("pattern");
        list = mydb.retrievePolygon(fieldlocation);
        showABLines(fieldPattern);
        polygono = mMap.addPolygon(new PolygonOptions().clickable(true).add(new LatLng(0, 0)));
        polygono.setPoints(list);
        polygono.setTag(belongsTo);
        polygono.setStrokeWidth(15);

    }

    private void showABLines(String pattern)
    {
        pointList = mydb.retrievePolylines(pattern);
        for(int i = 0; i< pointList.size()-2; i+=2)
        {
            Polyline abLine = mMap.addPolyline(new PolylineOptions().clickable(true).add(pointList.get(i), pointList.get(i+1)).color(0xffffffff));
            polylines.add(abLine);
            Polyline dummyLine = mMap.addPolyline(new PolylineOptions().clickable(false).add(new LatLng(0,0)).visible(false));
            polylines.add(dummyLine);
        }
        Polyline abLineLast = mMap.addPolyline(new PolylineOptions().clickable(true).add(pointList.get(pointList.size()-1), pointList.get(pointList.size()-2)).color(0xffffffff));
        polylines.add(abLineLast);
    }

    private void resetLineColors() //Ξανακάνει το χρώμα των γραμμών άσπρο
    {
        for(Polyline line : polylines)
        {
            line.setColor(0xffffffff);
        }
    }

    private void setCameraToUser()
    {
        LatLng mylocation = new LatLng(Double.parseDouble(currentlocationlatitude),Double.parseDouble(currentlocationlongitude));

        CameraPosition cp = new CameraPosition.Builder().
                target(mylocation)
                .zoom(200)
                .bearing(currentlocationvar.getBearing())
                .tilt(50)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Integer clickCount = (Integer) marker.getTag();

        Toast.makeText(this,marker.getPosition().toString(),Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public boolean onMyLocationButtonClick()
    {

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


package com.example.agriculturenavigation;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

public class NavUtil
{
    public void zoomOnField(List<LatLng> coordinateslist, GoogleMap theMap)
    {
        LatLng firstpoint = coordinateslist.get(0);
        LatLng lastpoint = coordinateslist.get(coordinateslist.size()-1);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i=0;i<coordinateslist.size();i++)
        {
            builder.include(coordinateslist.get(i));
        }
        int padding = 300;
        LatLngBounds bounds = builder.build();
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,padding);
        theMap.animateCamera(cu);
    }
}

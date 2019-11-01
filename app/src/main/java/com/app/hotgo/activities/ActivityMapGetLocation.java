package com.app.hotgo.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.config.Constant;
import com.app.hotgo.service.GPSTracker;

public class ActivityMapGetLocation extends BaseActivity {

    private View view;
    private GoogleMap mMap;
    private Activity self;
    private ImageView btnBack;
    private GPSTracker gps;
    Handler handler;
    private double lat;
    private double lnt;
    private LatLng latLongMap;
    private TextView btnSaveLatlong;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        self = this;
        gps = new GPSTracker(self);
        handler = new Handler();
        btnBack =  findViewById(R.id.btnBack);
        btnSaveLatlong = findViewById(R.id.btnSaveLatlong);
        btnSaveLatlong.setVisibility(View.GONE);
        btnBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });

        btnSaveLatlong.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(Constant.LOCATION, latLongMap);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        Maps();
    }


    private void Maps() {
        setUpMap();
    }


    private void setUpMap() {
        // TODO Auto-generated method stub

        if (mMap == null) {
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragMaps);
            fm.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    mMap.setMyLocationEnabled(true);

                    mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            // TODO Auto-generated method stub
                            marker.showInfoWindow();
                            return true;
                        }
                    });
                    mMap.setOnMapClickListener(new OnMapClickListener() {

                        @Override
                        public void onMapClick(LatLng latLng) {
                            // TODO Auto-generated method stub
                            MarkerOptions markerOptions = new MarkerOptions();
                            // Setting the position for the marker
                            markerOptions.position(latLng);
                            // Setting the title for the marker.
                            // This will be displayed on taping the marker
                            markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                            // Clears the previously touched position
                            mMap.clear();
                            // Animating to the touched position
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                            // Placing a marker on the touched position
                            mMap.addMarker(markerOptions);
                            //plotMarkers(mCustomMarkerArray);
                            latLongMap = latLng;
                            if (btnSaveLatlong.getVisibility() == View.GONE) {
                                btnSaveLatlong.setVisibility(View.VISIBLE);
                            }

                        }
                    });
                }
            });

//            if (lat >= 0 && lnt >= 0) {
//                mMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(lat, lnt))
//                        .title("Perth"));
//            }


        }
        if (gps.canGetLocation()) {
            handler.post(runGoogleUpdateLocation);
        } else {
            gps.showSettingsAlert();
        }


    }

    private void setLocationLatLong(double longitude, double latitude) {
        // set filter
        lat = latitude;
        lnt = longitude;
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);// zoom :
        // 2-21
    }

    private void refreshMyLocation() {
        try {
            Location location = null;
            if (mMap != null) {
                location = mMap.getMyLocation();
            }
            if (location == null) {
                if (gps.canGetLocation()) {
                    location = gps.getLocation();
                }
                handler.postDelayed(runGoogleUpdateLocation, 2 * 1000);
            } else {
                setLocationLatLong(location.getLongitude(), location.getLatitude());
            }
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    Runnable runGoogleUpdateLocation = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            refreshMyLocation();
        }
    };
}

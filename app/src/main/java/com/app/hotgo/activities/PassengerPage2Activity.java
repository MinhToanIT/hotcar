package com.app.hotgo.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.service.GPSTracker;

public class PassengerPage2Activity extends BaseActivity {
    RelativeLayout btnLink1, btnLink2, btnLink3;

    private GoogleMap map;
    private GPSTracker gps;
    Handler handler;
    TextView btnBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_page2);
        handler = new Handler();
        gps = new GPSTracker(self);
        setUpMap();
        init();
        (findViewById(R.id.btnBook))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoActivity(WaitDriverConfirmActivity.class);
                        finish();
                    }
                });

        initUIInThis();
    }

    public void initUIInThis() {

        btnBack =  findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void init() {
        btnLink1 =  findViewById(R.id.btnlink1);
        btnLink1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoActivity(WaitDriverConfirmActivity.class);
                finish();
            }
        });
        btnLink2 =  findViewById(R.id.btnlink2);
        btnLink2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoActivity(WaitDriverConfirmActivity.class);
                finish();
            }
        });
        btnLink3 =  findViewById(R.id.btnlink3);
        btnLink3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoActivity(WaitDriverConfirmActivity.class);
                finish();
            }
        });

    }

    // --------------------------------------------
    // --- Set up for map
    // --------------------------------------------
    private void setUpMap() {
        if (map == null) {
            ((MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
                        }
                        return;
                    }
                    map.setMyLocationEnabled(true);
                    map.getUiSettings().setMyLocationButtonEnabled(false);
                }
            });
        }
        if (gps.canGetLocation()) {
            handler.post(runGoogleUpdateLocation);
        } else {
            gps.showSettingsAlert();
        }
    }

    Runnable runGoogleUpdateLocation = new Runnable() {

        @Override
        public void run() {
            refreshMyLocation();
        }
    };

    private void refreshMyLocation() {
        Location location = null;
        if (map != null) {
            location = map.getMyLocation();
        }
        if (location == null) {
            if (gps.canGetLocation()) {
                location = gps.getLocation();
            }
            handler.postDelayed(runGoogleUpdateLocation, 2 * 1000);
        }
        setLocationLatLong(location.getLongitude(), location.getLatitude());
    }

    private void setLocationLatLong(double longitude, double latitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
        // zoom : 2-21
    }
}

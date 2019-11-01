package com.app.hotgo;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.object.UserUpdate;
import com.app.hotgo.service.GPSTracker;


public class ServiceUpdateLocation extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    public static String ACTION = "SEND_LOCATION_FROM_SERVICE";
    public static String LOCATION_LAT_LAST = "LOCATION_LAT_LAST";
    public static String LOCATION_LONG_LAST = "LOCATION_LONG_LAST";
    GPSTracker gpsTracker;
    private DatabaseReference ref;
    Handler handler = new Handler();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            gpsTracker = new GPSTracker(getApplicationContext());
            if (gpsTracker.canGetLocation()) {
                if (PreferencesManager.getInstance(getApplicationContext()).isDriver()) {
                    if (PreferencesManager.getInstance(getApplicationContext()).driverIsOnline()) {
                        String status = PreferencesManager.getInstance(getApplicationContext()).getDriverStatus();
                        if (status.isEmpty()) status = "1";
                        if (mLastLocation.getLatitude() != 0 && mLastLocation.getLongitude() != 0) {
                            ref.child("user").child(PreferencesManager.getInstance(getApplicationContext()).getUserID()).setValue(new UserUpdate(mLastLocation.getLatitude() + "", mLastLocation.getLongitude() + "", status, GlobalValue.getInstance().user.getCarObj().getTypeCar()));
                            updateCoornidate(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        }

                    }
                }
            }
            handler.postDelayed(runnable, 30000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference();


        handler.postDelayed(runnable, 30000);
//        gpsTracker = new GPSTracker(getApplicationContext());
        return START_NOT_STICKY;
    }

    public void updateCoornidate(final double latitude, final double longtitude) {
        ModelManager.updateCoordinate(PreferencesManager.getInstance(this).getToken(), latitude + "", longtitude + "", this, false, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {
                if (PreferencesManager.getInstance(getApplicationContext()).driverIsOnline()) {
                    updateCoornidate(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                }
            }
        });

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        gpsTracker.stopUsingGPS();
        this.stopSelf();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroy() {
        if (gpsTracker != null)
            gpsTracker.stopUsingGPS();
        this.stopSelf();
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }
}

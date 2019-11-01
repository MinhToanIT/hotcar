package com.app.hotgo.fragment;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.app.hotgo.BaseFragment;
import com.app.hotgo.R;
import com.app.hotgo.ServiceUpdateLocation;
import com.app.hotgo.activities.Ac_ConfirmPayByCash;
import com.app.hotgo.activities.RequestPassengerActivity;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.network.ProgressDialog;
import com.app.hotgo.object.UserUpdate;
import com.app.hotgo.service.GPSTracker;
import com.app.hotgo.utility.PermissionUtil;
import com.app.hotgo.widget.MyLocation;

public class BeforeOnlineFragment extends BaseFragment implements
        OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    Button btnOnline;
    private TextView lblTitle;
    TextView lbl_Online;
    private GPSTracker tracker;
    private TextView lblRequest, lblWaiting;
    private DatabaseReference ref;


    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_before_online,
                container, false);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        tracker = new GPSTracker(getActivity());
        buildGoogleApiClient();
        initUI(view);
        initControl();
        initMenuButton(view);
        return view;
    }

    public void changeLanguage() {
        btnOnline.setText(R.string.lbl_online);
        lblTitle.setText(getResources().getString(R.string.lbl_online));
        lbl_Online.setText(R.string.lbl_guide);
        lblWaiting.setText(R.string.lblClearGps);
    }

    public void initUI(View view) {
        lblTitle = view.findViewById(R.id.lblTitle);
        lblTitle.setText(getResources().getString(R.string.lbl_online));
        btnOnline = view.findViewById(R.id.btnOnline);
        lblRequest = view.findViewById(R.id.lblRequest);
        lbl_Online = view.findViewById(R.id.lbl_Online);
        lblWaiting = view.findViewById(R.id.lblWaiting);
        lblTitle.setVisibility(View.GONE);
    }

    public void initControl() {
        btnOnline.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnOnline:
//                new DownloadTask(self).execute();
                if (PermissionUtil.checkLocationPermission(getActivity())) {
                    online(tracker.getLocation());
                }

                break;
        }
    }

    @Override
    public void onDestroy() {
        tracker.stopSelf();
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {

        if (!hidden) {
            getDriverConfirm();
        }
    }

    public void getDriverConfirm() {
        ModelManager.showTripHistory(preferencesManager.getToken(), "1", getActivity(), true, new ModelManagerListener() {
            @Override
            public void onError() {
                showToast(getString(R.string.message_have_some_error));
            }

            @Override
            public void onSuccess(String json) {
                if (ParseJsonUtil.isSuccess(json)) {
                    String isWaitDriverConfirm = ParseJsonUtil.pareWaitDriverConfirm(json);
                    if (isWaitDriverConfirm.equals(Constant.TRIP_WAIT_DRIVER_NOTCONFIRM)) {
                        mainActivity.gotoActivity(Ac_ConfirmPayByCash.class);
                    }
                } else {
                    showToast(ParseJsonUtil.getMessage(json));
                }
            }
        });
    }

    public void online(final Location location) {
        final String latitude, longitude;
        if (preferencesManager.getLocation() != null) {
            latitude = preferencesManager.getLocation().latitude + "";
            longitude = preferencesManager.getLocation().longitude + "";
        } else {
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude() + "";
                longitude = mLastLocation.getLongitude() + "";
            } else {
                if (tracker.getLatitude() != 0 && tracker.getLongitude() != 0) {
                    latitude = tracker.getLatitude() + "";
                    longitude = tracker.getLongitude() + "";
                } else {
                    latitude = "0";
                    longitude = "0";
                }
            }

        }
        ModelManager.online(PreferencesManager.getInstance(self)
                .getToken(), self, true, new ModelManagerListener() {
            @Override
            public void onSuccess(String json) {

                if (ParseJsonUtil.isSuccess(json)) {
                    preferencesManager.setDriverStatus("1");
                    ref.child("user").child(preferencesManager.getUserID()).setValue(new UserUpdate(latitude, longitude, "1", GlobalValue.getInstance().user.getCarObj().getTypeCar()));
                    Intent intent1 = new Intent(getActivity(), ServiceUpdateLocation.class);
                    getActivity().startService(intent1);
                    preferencesManager.setDriverOnline();
                    preferencesManager.isFromBeforeOnline(true);
//                    mainActivity.gotoActivity(OnlineActivity.class);
                    mainActivity.gotoActivity(RequestPassengerActivity.class);
//                    if (pDialog != null) {
//                        pDialog.dismiss();
//                    }
//                        updateCoordinate(location.getLatitude() + "", location.getLongitude()
//                                + "");
                } else {
//                    pDialog.dismiss();
                    showToast(ParseJsonUtil.getMessage(json));
                }
            }

            @Override
            public void onError() {
//                pDialog.dismiss();
                showToast(R.string.message_have_some_error);
            }
        });
    }

    class DownloadTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pDialog;
        private Context context;

        public DownloadTask(Context context) {
            this.context = context;
            pDialog = new ProgressDialog(context);
            pDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (pDialog != null) {
                pDialog.show();
                self.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lblWaiting.setText(getString(R.string.lblWaiting));
                    }
                });

            } else {
                self.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lblWaiting.setText(getString(R.string.lblCanNotgetGps));
                    }
                });

            }


        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
                    @Override
                    public void gotLocation(Location location) {
                        if (location != null) {
                            online(location);
                        } else {
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            self.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    lblWaiting.setText(getString(R.string.lblCanNotgetGps));
                                }
                            });

                        }
                    }
                };
                MyLocation myLocation = new MyLocation();
                myLocation.getLocation(context, locationResult);
            } else {
                if (pDialog != null) {
                    pDialog.dismiss();
                }
                self.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lblWaiting.setText(getString(R.string.lblCanNotgetGps));
                    }
                });
            }
            super.onPostExecute(aVoid);
        }


    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(self)
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
        if (PermissionUtil.checkLocationPermission(self)) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
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

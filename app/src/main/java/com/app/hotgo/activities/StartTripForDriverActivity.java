package com.app.hotgo.activities;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.googledirections.Route;
import com.app.hotgo.googledirections.Routing;
import com.app.hotgo.googledirections.RoutingListener;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.CurrentOrder;
import com.app.hotgo.object.UserUpdate;
import com.app.hotgo.service.FusedLocationService;
import com.app.hotgo.service.FusedLocationService.MyBinder;
import com.app.hotgo.service.GPSTracker;
import com.app.hotgo.util.ImageUtil;
import com.app.hotgo.utility.AppUtil;
import com.app.hotgo.utility.DateUtil;
import com.app.hotgo.utility.NetworkUtil;
import com.app.hotgo.utility.NumberUtil;
import com.app.hotgo.utility.PermissionUtil;
import com.joooonho.SelectableRoundedImageView;

import org.joda.time.DateTime;
import org.joda.time.Duration;


public class StartTripForDriverActivity extends BaseActivity implements RoutingListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private TextView lblName;
    private TextView lblPhone;
    private RatingBar ratingBar;
    private TextView tvSeat;
    private TextView lblStartLocation, lblDistance, txtCountTime;
    private TextView lblEndLocation, lblTimes;
    TextView txtStar;
    private TextView btnStartTrip, btnBeginTask, btnEndTask;
    private TextView btnEndTrip;
    private SelectableRoundedImageView imgPassenger;
    AQuery aq;

    private GoogleMap mMap;
    private GPSTracker gps;
    LatLng shipperLocation, endLocation, shopLocation;
    Bitmap iconMarker;
    private CardView imgBack;
    // For timer
    private Routing routing;
    private Marker mMarkerEndLocation, mMarkerShipperLocation, mMarkerShop;
    Runnable runnable;
    private CardView llHelp;
    private String phoneAdmin = "";
    private ImageView imgCall, imgSms;

    private long startTime = 0L;

    private LinearLayout llCountTime, llToB;
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    private GoogleApiClient mGoogleApiClient;
    private TextView tvPrice, tvService;
    private boolean isArrived;

    /*
    todo heith and width
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_for_driver_trip);
        aq = new AQuery(this);
        gps = new GPSTracker(this);
        initUI();
        initView();
        Maps();
        initControl();


        startLocationService();
        initLocalBroadcastManager();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (preferencesManager.getDriverCurrentScreen().equals("")) {
            showToastMessage(R.string.message_you_trip_cancel_by_passenger);
            preferencesManager.setDriverStatus("1");
            gotoActivity(OnlineActivity.class);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (gps != null) {
            gps.stopUsingGPS();
        }
        super.onDestroy();
    }

    public void initView() {
        lblName = findViewById(R.id.lblName);
        tvSeat = findViewById(R.id.tvSeat);
        lblPhone = findViewById(R.id.lblPhone);
        ratingBar = findViewById(R.id.ratingBar);
        lblStartLocation = findViewById(R.id.lblStartLocation);
        lblStartLocation.setSelected(true);
        lblDistance = findViewById(R.id.lblDistance);
        lblTimes = findViewById(R.id.lblTimes);
        txtCountTime = findViewById(R.id.txtCountTime);
        lblEndLocation = findViewById(R.id.lblEndLocation);
        txtStar = findViewById(R.id.txtStar);
        imgPassenger = findViewById(R.id.imgPassenger);

        btnStartTrip = findViewById(R.id.btnStartTrip);
        btnEndTrip = findViewById(R.id.btnEndTrip);
        btnBeginTask = findViewById(R.id.btnBeginTask);
        llCountTime = findViewById(R.id.llCountTime);
        llToB = findViewById(R.id.llToB);
        btnEndTask = findViewById(R.id.btnEndTask);
        llHelp = findViewById(R.id.cv_help);
        btnEndTrip.setVisibility(View.GONE);
        imgBack = findViewById(R.id.cv_back);
        imgCall = findViewById(R.id.imgCall);
        imgSms = findViewById(R.id.imgSms);
        imgBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showCancelTripDialog();
            }
        });

        tvPrice = findViewById(R.id.tv_price);
        tvService = findViewById(R.id.tv_product_name);

    }

    public void initControl() {

        btnStartTrip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatusEndTrip();
            }
        });
        btnEndTrip.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                endTripAndGetDistance();
            }
        });
        lblPhone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (globalValue.getCurrentOrder().getPassenger_phone(false).length() > 0) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:"
                            + globalValue.getCurrentOrder()
                            .getPassenger_phone(false)));
                    startActivity(callIntent);
                } else {
                    showToastMessage(R.string.msg_call_phone);
                }
            }
        });
        llHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtil.checkNetworkAvailable(StartTripForDriverActivity.this)) {
                    ModelManager.sendNeedHelp(preferencesManager.getToken(), preferencesManager.getCurrentOrderId(), StartTripForDriverActivity.this, false, new ModelManagerListener() {
                        @Override
                        public void onError() {

                        }

                        @Override
                        public void onSuccess(String json) {
                            Log.e("Success", "Success");
                        }
                    });
                }
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"
                        + phoneAdmin));
                startActivity(callIntent);
            }
        });
        btnBeginTask.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                preferencesManager.setDriverArrivedB("0");
                preferencesManager.setDriverBeginTask("1");
                updateStatusStartTask();
            }
        });
        btnEndTask.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                preferencesManager.setDriverBeginTask("0");
                endTripAndGetDistanceTask();
            }
        });
        imgCall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + globalValue.getCurrentOrder()
                        .getPassenger_phone(false)));
                startActivity(intent);
            }
        });
        imgSms.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", globalValue.getCurrentOrder().getPassenger_phone(false));
                smsIntent.putExtra("sms_body", "message");
                startActivity(smsIntent);
            }
        });
    }

    public void updateStatusStartTask() {
        ModelManager.changeStatus(preferencesManager.getToken(), globalValue.getCurrentOrder().getId(), Constant.STATUS_START_TASK, StartTripForDriverActivity.this, true, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {
                CurrentOrder currentOrder = ParseJsonUtil.parseCurrentOrder(json);
                startTime = Long.parseLong(currentOrder.getStartTimeWorking());
                //
                txtCountTime.setText(DateUtil.convertTimeStampToDate(startTime + "", "dd MMM h:m a"));

                llToB.setVisibility(View.GONE);
                final String distance = NumberUtil
                        .getNumberFomatDistance(fusedLocationService
                                .getCurentDistance());
                preferencesManager.putStringValue("DISTANCE_NUMBER", distance);
                stopLocationService();
                llCountTime.setVisibility(View.VISIBLE);
                btnBeginTask.setVisibility(View.GONE);
                btnEndTask.setVisibility(View.VISIBLE);
            }
        });
    }

    public void changeStatusEndTrip() {
        ModelManager.changeStatus(preferencesManager.getToken(), globalValue.getCurrentOrder().getId(), Constant.STATUS_ARRIVED_B, StartTripForDriverActivity.this, true, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {
                if (globalValue.getCurrentOrder().getWorkAtB() != null && globalValue.getCurrentOrder().getWorkAtB().equals("1")) {
                    btnEndTrip.setVisibility(View.GONE);
                    btnBeginTask.setVisibility(View.VISIBLE);
                    txtCountTime.setVisibility(View.VISIBLE);
                    shipperLocation = endLocation;
                    isArrived = true;
                    mMap.clear();
                    setLocationLatLong(shipperLocation);
                    setEndMarkerNoUpdate();
                } else {
                    btnEndTrip.setVisibility(View.VISIBLE);
                    btnBeginTask.setVisibility(View.GONE);
                    txtCountTime.setVisibility(View.GONE);
                }

                btnStartTrip.setVisibility(View.GONE);
                btnEndTask.setVisibility(View.GONE);

            }
        });
    }

    private void initData() {
        ModelManager.getGeneralSettings(preferencesManager.getToken(),
                self, false, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            phoneAdmin = ParseJsonUtil
                                    .getPhoneAdmin(json);
                        }
                    }

                    @Override
                    public void onError() {
                    }
                });
        if (getPreviousActivityName().equals(
                SplashActivity.class.getSimpleName())) {
            ModelManager.showTripDetail(preferencesManager.getToken(),
                    preferencesManager.getCurrentOrderId(), StartTripForDriverActivity.this, false,
                    new ModelManagerListener() {
                        @Override
                        public void onSuccess(String json) {

                            if (ParseJsonUtil.isSuccess(json)) {
                                globalValue.setCurrentOrder(ParseJsonUtil
                                        .parseCurrentOrder(json));
                                endLocation = new LatLng(Double.parseDouble(ParseJsonUtil
                                        .parseCurrentOrder(json).getEndLat()), Double.parseDouble(ParseJsonUtil
                                        .parseCurrentOrder(json).getEndLong()));
                                lblName.setText(globalValue.getCurrentOrder()
                                        .getPassengerName());
                                tvSeat.setText(GlobalValue.convertLinkToString(StartTripForDriverActivity.this, globalValue.convertToInt(globalValue.getCurrentOrder().getLink()) + ""));
                                lblPhone.setText(globalValue.getCurrentOrder()
                                        .getPassenger_phone(false));
                                lblStartLocation.setText(globalValue
                                        .getCurrentOrder().getEndLocation());
                                tvService.setText(globalValue.getCurrentOrder().getProductName());
                                tvPrice.setText(getResources().getString(R.string.lblCurrency) + globalValue.getCurrentOrder().getPrice());

                                if (globalValue.getCurrentOrder()
                                        .getPassenger_rate().isEmpty()) {
                                    txtStar.setText("0");
                                } else {
                                    txtStar.setText("" + Float
                                            .parseFloat(globalValue
                                                    .getCurrentOrder()
                                                    .getPassenger_rate()) / 2 + "(" + globalValue.getCurrentOrder().getPassengerRateCount() + ")");
                                }

                                aq.id(R.id.imgPassenger).image(
                                        globalValue.getCurrentOrder().getImagePassenger());

                                if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_START_TASK)) {
                                    llToB.setVisibility(View.GONE);
                                    llCountTime.setVisibility(View.VISIBLE);
                                    startTime = Long.parseLong(globalValue.getCurrentOrder().getStartTimeWorking());
                                    //
                                    txtCountTime.setText(DateUtil.convertTimeStampToDate(startTime + "", "dd MMM h:m a"));
//                                    customHandler.postDelayed(updateTimerThread, 0);
                                    btnStartTrip.setVisibility(View.GONE);
                                    btnEndTrip.setVisibility(View.GONE);
                                    btnBeginTask.setVisibility(View.GONE);
                                    btnEndTask.setVisibility(View.VISIBLE);
                                    shipperLocation = endLocation;
                                } else {
                                    llToB.setVisibility(View.VISIBLE);
                                    llCountTime.setVisibility(View.GONE);
                                    if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_ARRIVED_B)) {
                                        btnStartTrip.setVisibility(View.GONE);
                                        btnEndTrip.setVisibility(View.GONE);
                                        btnBeginTask.setVisibility(View.VISIBLE);
                                        btnEndTask.setVisibility(View.GONE);
                                        shipperLocation = endLocation;
                                    } else if (globalValue.getCurrentOrder().getStatus().equals(Constant.TRIP_STATUS_IN_PROGRESS)) {
                                        btnStartTrip.setVisibility(View.VISIBLE);
                                        btnEndTrip.setVisibility(View.GONE);
                                        btnBeginTask.setVisibility(View.GONE);
                                        btnEndTask.setVisibility(View.GONE);
                                    }
                                }
                                getDistance();
                                setLocationLatLong(shipperLocation);
//                                setShopMarker();
                                if (globalValue.getCurrentOrder().getStatus().equals(Constant.TRIP_STATUS_IN_PROGRESS)) {
                                    setEndMarker();
                                } else {
                                    setEndMarkerNoUpdate();
                                }

                                if (shipperLocation != null) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shipperLocation, 14.0f));

                                }
                            } else {
                                showToastMessage(ParseJsonUtil.getMessage(json));
                            }
                        }

                        @Override
                        public void onError() {

                            showToastMessage(R.string.message_have_some_error);
                        }
                    });

        } else {
            lblName.setText(globalValue.getCurrentOrder().getPassengerName());
            tvSeat.setText(GlobalValue.convertLinkToString(StartTripForDriverActivity.this, globalValue.convertToInt(globalValue.getCurrentOrder().getLink()) + ""));
            lblPhone.setText(globalValue.getCurrentOrder().getPassenger_phone(false));
            lblStartLocation.setText(globalValue.getCurrentOrder()
                    .getEndLocation());
            lblEndLocation.setText(globalValue.getCurrentOrder()
                    .getEndLocation());
            aq.id(R.id.imgPassenger).image(
                    globalValue.getCurrentOrder().getImagePassenger());
            tvService.setText(globalValue.getCurrentOrder().getProductName());
            tvPrice.setText(getResources().getString(R.string.lblCurrency) + globalValue.getCurrentOrder().getPrice());

            if (globalValue.getCurrentOrder()
                    .getPassenger_rate().isEmpty()) {
                txtStar.setText("0");
            } else {
                txtStar.setText("" + Float
                        .parseFloat(globalValue
                                .getCurrentOrder()
                                .getPassenger_rate()) / 2 + "(" + globalValue.getCurrentOrder().getPassengerRateCount() + ")");
            }
            if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_START_TASK)) {
                llToB.setVisibility(View.GONE);
                llCountTime.setVisibility(View.VISIBLE);
                startTime = Long.parseLong(globalValue.getCurrentOrder().getStartTimeWorking());
                //
                txtCountTime.setText(DateUtil.convertTimeStampToDate(startTime + "", "dd MMM h:m a"));
                btnStartTrip.setVisibility(View.GONE);
                btnEndTrip.setVisibility(View.GONE);
                btnBeginTask.setVisibility(View.VISIBLE);
                btnEndTask.setVisibility(View.GONE);
                shipperLocation = endLocation;
            } else {
                llToB.setVisibility(View.VISIBLE);
                llCountTime.setVisibility(View.GONE);
                if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_ARRIVED_B)) {
                    btnStartTrip.setVisibility(View.GONE);
                    btnEndTrip.setVisibility(View.GONE);
                    btnBeginTask.setVisibility(View.VISIBLE);
                    btnEndTask.setVisibility(View.GONE);
                    shipperLocation = endLocation;
                } else if (globalValue.getCurrentOrder().getStatus().equals(Constant.TRIP_STATUS_IN_PROGRESS)) {
                    btnStartTrip.setVisibility(View.VISIBLE);
                    btnEndTrip.setVisibility(View.GONE);
                    btnBeginTask.setVisibility(View.GONE);
                    btnEndTask.setVisibility(View.GONE);
                }
            }
            getDistance();
            endLocation = new LatLng(Double.parseDouble(globalValue.getCurrentOrder().getEndLat()), Double.parseDouble(globalValue.getCurrentOrder().getEndLong()));
            setLocationLatLong(shipperLocation);
//            setShopMarker();
            if (globalValue.getCurrentOrder().getStatus().equals(Constant.TRIP_STATUS_IN_PROGRESS)) {
                setEndMarker();
            } else {
                setEndMarkerNoUpdate();
            }
            if (shipperLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shipperLocation, 14.0f));
            }
        }
    }

    @Override
    public void onBackPressed() {

    }

    public void startTrip(String tripId) {

        ModelManager.startTrip(preferencesManager.getToken(), tripId, StartTripForDriverActivity.this,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            fusedLocationService.connectionApi();
                            btnStartTrip.setVisibility(View.GONE);
                            btnEndTrip.setVisibility(View.VISIBLE);
                            llHelp.setVisibility(View.VISIBLE);
                            imgBack.setVisibility(View.GONE);
                            preferencesManager.setDriverStartTrip(true);
                        } else {
                            showToastMessage(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToastMessage(getResources().getString(
                                R.string.message_have_some_error));
                    }
                });
    }

    public void endTrip(String tripId, String distance) {
        ModelManager.endTrip(preferencesManager.getToken(), tripId, distance,
                StartTripForDriverActivity.this, true, new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {

                            preferencesManager.setDriverStartTrip(false);
                            gotoActivity(RatingPassengerActivity.class);
                            preferencesManager
                                    .setDriverCurrentScreen(RatingPassengerActivity.class
                                            .getSimpleName());
                            finish();
                        } else {
                            showToastMessage(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        // enterRealDistance();
                    }
                });
    }

    private void endTripAndGetDistance() {
        final String distance = NumberUtil
                .getNumberFomatDistance(fusedLocationService
                        .getCurentDistance());
        String distanceNew = "";
        if (Double.parseDouble(distance) < 1) {
            double distanceMeter = Double.parseDouble(distance) * 1000;
            distanceNew = distanceMeter + " m";
        } else {
            distanceNew = distance + " km";
        }

        final Builder dialog = new AlertDialog.Builder(StartTripForDriverActivity.this);
        dialog.setTitle(R.string.lbl_message_real_distance);
        dialog.setMessage(getResources().getString(R.string.the_distance_is) + " " + distanceNew);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopLocationService();
                endTrip(globalValue.getCurrentOrder().getId(), "");
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void endTripAndGetDistanceTask() {
        final String distance = preferencesManager.getStringValue("DISTANCE_NUMBER");
        String distanceNew = "";
        if (Double.parseDouble(distance) < 1) {
            double distanceMeter = Double.parseDouble(distance) * 1000;
            distanceNew = distanceMeter + " m";
        } else {
            distanceNew = distance + " km";
        }
        DateTime today = new DateTime();
        DateTime yesterday = new DateTime(startTime * 1000L);
        Duration duration = new Duration(yesterday, today);
        String timeTotalData = "";
        long timeMinutes = 0;
        long timeHours = 0;
        long timeTotal = duration.getStandardSeconds();
        if (timeTotal > 60) {
            timeMinutes = timeTotal / 60;
            if (timeMinutes > 60) {
                timeHours = timeMinutes / 60;
                timeMinutes = timeMinutes % 60;
            }
            timeTotalData = timeHours + "h" + timeMinutes;
        } else {
            timeTotalData = timeTotal + "s";
        }

        final Builder dialog = new AlertDialog.Builder(StartTripForDriverActivity.this);
        dialog.setTitle(R.string.lbl_message_real_distance);
        dialog.setMessage(getResources().getString(R.string.the_distance_is) + " " + distanceNew + " " + getResources().getString(R.string.time_working) + " " + timeTotalData);
        dialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                endTrip(globalValue.getCurrentOrder().getId(), "");
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void initLocalBroadcastManager() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_CANCEL_TRIP);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver,
                intentFilter);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constant.KEY_ACTION);
            if (action.equals(Constant.ACTION_CANCEL_TRIP)) {
                showToastMessage(R.string.message_you_trip_cancel_by_passenger);
                preferencesManager.setDriverStatus("1");
                ref.child("user").child(preferencesManager.getUserID()).child("status").setValue("1");
                if (preferencesManager.IsStartWithOutMain()) {
                    gotoActivity(OnlineActivity.class);
                    finish();
                } else {
                    finish();
                }
            }
        }
    };

    private void showCancelTripDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.msg_do_you_cancel)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                cancelTrip();
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    private void cancelTrip() {
        ModelManager.cancelTrip(preferencesManager.getToken(), globalValue
                        .getCurrentOrder().getId(), StartTripForDriverActivity.this, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            preferencesManager.setDriverStatus("1");
                            ref.child("user").child(preferencesManager.getUserID()).child("status").setValue("1");
                            if (preferencesManager.IsStartWithOutMain()) {
                                gotoActivity(OnlineActivity.class);
                                finish();
                            } else {
                                finish();
                            }
                        } else {
                            showToastMessage(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToastMessage(getResources().getString(
                                R.string.message_have_some_error));
                    }
                });
    }

    FusedLocationService fusedLocationService;
    boolean mBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected ..............");
            mBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBinder binder = (MyBinder) service;
            fusedLocationService = binder.getService();
            mBound = true;

        }
    };

    private void startLocationService() {
        Intent intent = new Intent(StartTripForDriverActivity.this, FusedLocationService.class);
        startService(intent);
        bindService(intent, serviceConnection, StartTripForDriverActivity.this.BIND_AUTO_CREATE);
    }

    private void stopLocationService() {
        // Unbind from the service
        if (mBound) {
            unbindService(serviceConnection);
            mBound = false;
        }
    }


    @Override
    public void onRoutingFailure() {
//        showDirection();
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        if (shopLocation != null || endLocation != null) {
//            mMap.clear();
//            setLocationLatLong(shipperLocation);
//            setShopMarkerAgain();
//            setEndMarker();
//            if (globalValue.getCurrentOrder().getPickUpAtA().equals("1")) {
//                setShopMarkerAgain();
//            }
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(R.color.second_primary);
            polyOptions.width(10);
            polyOptions.addAll(mPolyOptions.getPoints());
            mMap.addPolyline(polyOptions);
            lblTimes.setText(route.getDurationText());
        }
    }

    private void Maps() {
        //initData();
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
                    if (PermissionUtil.checkLocationPermission(StartTripForDriverActivity.this)) {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        buildGoogleApiClient();
                    }
                }
            });
        }
        if (gps.canGetLocation()) {
            shipperLocation = new LatLng(gps.getLatitude(), gps.getLongitude());
            ModelManager.showTripDetail(preferencesManager.getToken(),
                    preferencesManager.getCurrentOrderId(), StartTripForDriverActivity.this, false,
                    new ModelManagerListener() {

                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                globalValue.setCurrentOrder(ParseJsonUtil
                                        .parseCurrentOrder(json));
                                shopLocation = new LatLng(Double.parseDouble(globalValue.getCurrentOrder().getStartLat()), Double.parseDouble(globalValue.getCurrentOrder().getStartLong()));
                                initData();
                            } else {
                                showToastMessage(ParseJsonUtil.getMessage(json));
                            }
                        }

                        @Override
                        public void onError() {
                            showToastMessage(R.string.message_have_some_error);
                        }
                    });
        } else {
            gps.showSettingsAlert();
        }

    }

    protected void showDirection() {
        if (shopLocation != null && endLocation != null) {
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(shopLocation, endLocation);
        }
    }

    private void setLocationLatLong(LatLng location) {
        // set filter
        LatLng latLng = new LatLng(location.latitude, location.longitude);

        if (mMarkerShipperLocation != null) {
            mMarkerShipperLocation.remove();
        }
        iconMarker = BitmapFactory.decodeResource(
                getResources(), R.drawable.ic_driver);
        iconMarker = Bitmap.createScaledBitmap(iconMarker,
                AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 38),
                false);
        mMarkerShipperLocation = mMap.addMarker(new MarkerOptions().position(
                latLng).icon(
                BitmapDescriptorFactory.fromBitmap(iconMarker)));
        Log.e(TAG, "setLocationLatLong: ");
    }

    public void setShopMarker() {
        if (shopLocation != null) {
            if (mMarkerShop != null) {
                mMarkerShop.remove();
            }
            iconMarker = ImageUtil.createBitmapFromUrl(globalValue.getCurrentOrder().getMarkerImage());
            // resize
            int size = ImageUtil.getSizeBaseOnDensity(self, 50);
            iconMarker = ImageUtil.getResizedBitmap(iconMarker, AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30));
//            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            mMarkerShop = mMap.addMarker(new MarkerOptions().position(
                    shopLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, globalValue.getCurrentOrder().getShopName(), getResources().getColor(R.color.primary), 0, iconMarker))));
//            showDirection();
        }
    }

    public void setShopMarkerAgain() {
        if (shopLocation != null) {
            if (mMarkerShop != null) {
                mMarkerShop.remove();
            }
            iconMarker = ImageUtil.createBitmapFromUrl(globalValue.getCurrentOrder().getMarkerImage());
            // resize
            int size = ImageUtil.getSizeBaseOnDensity(self, 50);
            iconMarker = ImageUtil.getResizedBitmap(iconMarker, AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30));
//            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            mMarkerShop = mMap.addMarker(new MarkerOptions().position(
                    shopLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, globalValue.getCurrentOrder().getShopName(), getResources().getColor(R.color.primary), 0, iconMarker))));
        }
    }

    public void setEndMarker() {
        if (endLocation != null) {
            if (mMarkerEndLocation != null) {
                mMarkerEndLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_position_b);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
            mMarkerEndLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, getResources().getString(R.string.lblDestination), getResources().getColor(R.color.to), 0, iconMarker))));
            showDirection();
        }
    }

    public void setEndMarkerNoUpdate() {
        if (endLocation != null) {
            if (mMarkerEndLocation != null) {
                mMarkerEndLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_position_b);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
            mMarkerEndLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, getResources().getString(R.string.lblDestination), getResources().getColor(R.color.to), 0, iconMarker))));
        }
    }


    private void getDistance() {
        if (globalValue.getCurrentOrder() != null) {
            ModelManager.showDistance(preferencesManager.getToken(),
                    globalValue.getCurrentOrder().getId(), StartTripForDriverActivity.this, false,
                    new ModelManagerListener() {

                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                try {
                                    Float temp = Float.parseFloat(ParseJsonUtil
                                            .getDistance(json));
                                    if (temp.toString().length() > 6) {
                                        lblDistance.setText(round(Double.parseDouble(temp.toString().substring(
                                                0, 6)), 1)
                                                + " " + getString(R.string.unit_measure));
                                    } else {
                                        lblDistance.setText(round(Double.parseDouble(temp.toString()), 1)
                                                + " " + getString(R.string.unit_measure));
                                    }
                                } catch (NumberFormatException e) {
                                    lblDistance.setText("0"
                                            + " " + getString(R.string.unit_measure));
                                }

                            }
                        }

                        @Override
                        public void onError() {

                        }
                    });
        }

    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
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
        LocationRequest mLocationRequest = new LocationRequest();
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
//        updateCoordinate(location.getLatitude() + "", location.getLongitude()
//                + "");
        if (globalValue.getCurrentOrder() != null && globalValue.getCurrentOrder().getStatus().equals(Constant.TRIP_STATUS_IN_PROGRESS) && !isArrived) {
            shipperLocation = new LatLng(location.getLatitude(), location.getLongitude());
            String status = preferencesManager.getDriverStatus();
            ref.child("user").child(preferencesManager.getUserID()).setValue(new UserUpdate(location.getLatitude() + "", location.getLongitude() + "", status, GlobalValue.getInstance().user.getCarObj().getTypeCar()));
            updateCoordinate(location.getLatitude() + "", location.getLongitude() + "");
            Log.e(TAG, "onLocationChanged: ");
            setLocationLatLong(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }
}

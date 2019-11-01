package com.app.hotgo.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.config.Constant;
import com.app.hotgo.googledirections.Route;
import com.app.hotgo.googledirections.Routing;
import com.app.hotgo.googledirections.RoutingListener;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.util.ImageUtil;
import com.app.hotgo.utility.AppUtil;
import com.joooonho.SelectableRoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class ConfirmActivity extends BaseActivity implements RoutingListener {
    private TextView lblCarPlate;
    private TextView lblName, tvSeat;
    private TextView lblPhone;
    private RatingBar ratingBar;
    private ImageView imgDriver;
    private SelectableRoundedImageView imgPassenger;
    private ImageView imgCar;
    private TextView btnCancel;
    private TextView lblDistance;
    private TextView lblStartLocation;
    private TextView lblEndlocation, lblTimes, lblDistanceTime;
    TextView txtStar;
    AQuery aq;

    // For timer
    private int mInterval = 1; // 5 seconds by default, can be changed later
    private Timer mTimer;
    private GoogleMap mMap;
    LatLng shipperLocation, startLocation, endLocation;
    Bitmap iconMarker;
    Runnable runnable;

    private CardView imgBack;
    // For timer
    private Routing routing;
    private Marker mMarkerStartLocation, mMarkerShipperLocation, mMarkerEndLocation;
    private TextView lblTitlePassenger, lblTitleDriver, txtStatus;
    private ImageView imgCall, imgSms;
    private boolean checkDataDistance = true;
    private DatabaseReference ref;
    private boolean checkFirst = true, checkPath = true;
    private String dataPath = "";
    Handler handler = new Handler();
    Runnable updateMarker;

    private TextView tvProductName, tvQuantity, tvPrice;

    private void autoRefreshEvents() {
        if (mTimer == null) {
            mTimer = new Timer();
            RefreshEvents refresh = new RefreshEvents();
            try {
                mTimer.scheduleAtFixedRate(refresh, mInterval * 10 * 1000,
                        mInterval * 10 * 1000);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onRoutingFailure() {

    }

    @Override
    public void onRoutingStart() {
        lblDistanceTime.setText(dataPath);
    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        if (shipperLocation != null || startLocation != null) {
            if (checkDataDistance) {
                mMap.clear();
                setStartMarkerAgain();
                setEndMarkerNoUpdateDirection();
                setLocationLatLong(shipperLocation);

                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.color(R.color.second_primary);
                polyOptions.width(10);
                polyOptions.addAll(mPolyOptions.getPoints());
                mMap.addPolyline(polyOptions);
                String msg = getString(R.string.msgDrivingComingDistance);
                msg = msg.replace("[a]", route.getDistanceText());
                msg = msg.replace("[b]", route.getDurationText());
                lblDistanceTime.setText(msg);
                checkPath = false;
                dataPath = msg;
            } else {
                String msg = getString(R.string.msgDrivingComingDistance);
                msg = msg.replace("[a]", route.getDistanceText());
                msg = msg.replace("[b]", route.getDurationText());
                lblDistanceTime.setText(msg);
                dataPath = msg;
            }


        }
    }

    private class RefreshEvents extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Do some thing here
                    getDistance();
                }
            });
        }

    }

    protected void showDirection() {
        if (shipperLocation != null && startLocation != null) {
            checkDataDistance = true;
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(shipperLocation, startLocation);

        }
    }

    protected void showDistanceAndTime() {
        if (shipperLocation != null && startLocation != null) {
            checkDataDistance = false;
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(shipperLocation, startLocation);

        }

    }

    public void setStartMarker() {
        if (startLocation != null) {
            if (mMarkerStartLocation != null) {
                mMarkerStartLocation.remove();
            }
            iconMarker = ImageUtil.createBitmapFromUrl(globalValue.getCurrentOrder().getMarkerImage());
            // resize
            int size = ImageUtil.getSizeBaseOnDensity(self, AppUtil.convertDpToPixel(self, 50));
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
            mMarkerStartLocation = mMap.addMarker(new MarkerOptions().position(
                    startLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, globalValue.getCurrentOrder().getShopName(),getResources().getColor(R.color.primary), 0, iconMarker))));
            showDirection();
        }
    }

    public void setStartMarkerAgain() {
        if (startLocation != null) {
            if (mMarkerStartLocation != null) {
                mMarkerStartLocation.remove();
            }
            iconMarker = ImageUtil.createBitmapFromUrl(globalValue.getCurrentOrder().getMarkerImage());
            // resize
            int size = ImageUtil.getSizeBaseOnDensity(self, AppUtil.convertDpToPixel(self, 50));
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
            mMarkerStartLocation = mMap.addMarker(new MarkerOptions().position(
                    startLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, globalValue.getCurrentOrder().getShopName(),getResources().getColor(R.color.primary), 0, iconMarker))));
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
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, getResources().getString(R.string.lblDestination),getResources().getColor(R.color.to), 0, iconMarker))));
            showDirectionFromShopToDestination();
        }
    }

    public void setEndMarkerNoUpdateDirection() {
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
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, getResources().getString(R.string.lblDestination),getResources().getColor(R.color.to), 0, iconMarker))));
        }
    }

    protected void showDirectionFromShopToDestination() {
        if (startLocation != null && endLocation != null) {
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(startLocation, endLocation);
        }
    }

    private void getDistance() {
        if (globalValue.getCurrentOrder() != null) {
            ModelManager.showDistance(preferencesManager.getToken(),
                    globalValue.getCurrentOrder().getId(), context, false,
                    new ModelManagerListener() {

                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                try {
                                    Float temp = Float.parseFloat(ParseJsonUtil
                                            .getDistance(json));
                                    if (temp.toString().length() > 6) {
                                        lblDistance.setText(temp.toString().substring(
                                                0, 6)
                                                + " " + getString(R.string.unit_measure));
                                    } else {
                                        lblDistance.setText(temp.toString()
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

    /* OVERRIDE */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        aq = new AQuery(self);
        initUI();
        initView();
        Maps();
        initData();
        initControl();
        initLocalBroadcastManager();
        autoRefreshEvents();
        preferencesManager.setPassengerWaitConfirm(false);
        preferencesManager.putStringValue("countDriver", "0");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }


    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        handler.removeCallbacks(updateMarker);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onResume() {
        if (preferencesManager.getPassengerCurrentScreen().equals(
                "StartTripForPassengerActivity")) {
            Log.e("arrived", "onResume");
            gotoActivity(StartTripForPassengerActivity.class);
            finish();
        } else if (preferencesManager.getPassengerCurrentScreen().equals("")) {
            gotoActivity(MainActivity.class);
            finish();
        } else {
            Log.e("arrived", "onResume 1");
            preferencesManager.setPassengerWaitConfirm(false);
            preferencesManager.setPassengerCurrentScreen(ConfirmActivity.class
                    .getSimpleName());
            preferencesManager.setPassengerIsInTrip(true);
            initData();
        }
        super.onResume();
    }

    ;

    private void Maps() {
        //initData();
        setUpMap();
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
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));b
        // 2-21
    }

    private void setUpMap() {
        // TODO Auto-generated method stub

        if (mMap == null) {
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragMaps);
            fm.getMapAsync(new OnMapReadyCallback() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    if (ActivityCompat.checkSelfPermission(ConfirmActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                    }
                    mMap.setMyLocationEnabled(false);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        cancelTrip();
    }

    /* FUNCTION */
    private void initView() {
        tvSeat = findViewById(R.id.tvSeat);
        lblName = findViewById(R.id.lblName);
        lblCarPlate = findViewById(R.id.lblCarPlate);
        lblPhone = findViewById(R.id.lblPhone);
        ratingBar = findViewById(R.id.ratingBar);
        imgPassenger = findViewById(R.id.imgPassenger);
        imgCar = findViewById(R.id.imgCar);
        btnCancel = findViewById(R.id.btnCancel);
        lblDistance = findViewById(R.id.lblDistance);
        lblStartLocation = findViewById(R.id.lblStartLocation);
        lblEndlocation = findViewById(R.id.lblEndlocation);
        imgDriver = findViewById(R.id.imgDriver);
        lblTimes = findViewById(R.id.lblTimes);
        lblTitleDriver = findViewById(R.id.lblTitleDriver);
        txtStatus = findViewById(R.id.txtStatus);
        lblTitlePassenger = findViewById(R.id.lblTitlePassenger);
        txtStar = findViewById(R.id.txtStar);
        imgBack = findViewById(R.id.cv_back);
        lblDistanceTime = findViewById(R.id.lblDistanceTime);
        findViewById(R.id.btnBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        imgBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        imgCall = findViewById(R.id.imgCall);
        imgSms = findViewById(R.id.imgSms);
        tvProductName = findViewById(R.id.tv_product_name);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvPrice = findViewById(R.id.tv_price);

    }

    private void initData() {
//        if (!getPreviousActivityName().equals(
//                WaitDriverConfirmActivity.class.getName())) {
        ModelManager.showTripDetail(preferencesManager.getToken(),
                preferencesManager.getCurrentOrderId(), context, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            globalValue.setCurrentOrder(ParseJsonUtil
                                    .parseCurrentOrder(json));

                            endLocation = new LatLng(Double.parseDouble(ParseJsonUtil
                                    .parseCurrentOrder(json).getEndLat()), Double.parseDouble(ParseJsonUtil
                                    .parseCurrentOrder(json).getEndLong()));
                            startLocation = new LatLng(Double.parseDouble(ParseJsonUtil
                                    .parseCurrentOrder(json).getStartLat()), Double.parseDouble(ParseJsonUtil
                                    .parseCurrentOrder(json).getStartLong()));

                            lblName.setText(globalValue.getCurrentOrder()
                                    .getDriverName());
                            lblCarPlate.setText(globalValue
                                    .getCurrentOrder().getCarPlate());
                            lblPhone.setText(globalValue.getCurrentOrder()
                                    .getDriver_phone(true));
                            lblStartLocation.setText(globalValue
                                    .getCurrentOrder().getStartLocation());
                            lblEndlocation.setText(globalValue
                                    .getCurrentOrder().getEndLocation());
                            lblTitlePassenger.setText(globalValue
                                    .getCurrentOrder().getDriverName());
                            lblTitleDriver.setText(globalValue
                                    .getCurrentOrder().getPassengerName());

                            tvProductName.setText(globalValue.getCurrentOrder().getProductName());
                            tvQuantity.setText("x" + globalValue.getCurrentOrder().getQuantity());
                            tvPrice.setText(getResources().getString(R.string.lblCurrency) + globalValue.getCurrentOrder().getPrice());

                            if (globalValue.getCurrentOrder()
                                    .getDriverRate().isEmpty()) {
                                txtStar.setText("0");
                            } else {
                                txtStar.setText("" + Float
                                        .parseFloat(globalValue
                                                .getCurrentOrder()
                                                .getDriverRate()) / 2 + "(" + globalValue.getCurrentOrder().getDriverRateCount() + ")");
                            }
                            tvSeat.setText(convertLinkToString(globalValue.convertToInt(globalValue.getCurrentOrder().getLink()) + ""));
                            aq.id(imgCar).image(
                                    globalValue.getCurrentOrder()
                                            .getCarImage());
                            Log.e("eeeeeeeeee", "image: " + globalValue.getCurrentOrder()
                                    .getActualFare());
                            Glide.with(self).load(globalValue.getCurrentOrder().getImageDriver()).into(imgPassenger);
                            aq.id(imgDriver).image(
                                    globalValue.getCurrentOrder()
                                            .getProductImage());
                            if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_ARRIVED_A)) {
                                txtStatus.setText(getString(R.string.lbl_arrived));
                                txtStatus.setTextColor(getResources().getColor(R.color.to));
                            }
                            getDistance();
                            updatePositionForPassenger(globalValue.getCurrentOrder().getDriverId());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 14.0f));


                        } else {
                            showToastMessage(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToastMessage(R.string.message_have_some_error);
                    }
                });
//        } else {
//
//            tvSeat.setText(convertLinkToString(globalValue.convertToInt(globalValue.getCurrentOrder().getLink()) + ""));
//            lblName.setText(globalValue.getCurrentOrder().getDriverName());
//            lblCarPlate.setText(globalValue.getCurrentOrder().getCarPlate());
//            lblPhone.setText(globalValue.getCurrentOrder().getDriver_phone(true));
//            lblStartLocation.setText(globalValue
//                    .getCurrentOrder().getStartLocation());
//            lblEndlocation.setText(globalValue
//                    .getCurrentOrder().getEndLocation());
//
//            tvProductName.setText(globalValue.getCurrentOrder().getProductName());
//            tvQuantity.setText("x" + globalValue.getCurrentOrder().getQuantity());
//            tvPrice.setText(getResources().getString(R.string.lblCurrency) + globalValue.getCurrentOrder().getPrice());
//
//            if (globalValue.getCurrentOrder()
//                    .getDriverRate().isEmpty()) {
//                txtStar.setText("0");
////                                    ratingBar.setRating(0);
//            } else {
//                txtStar.setText("" + Float
//                        .parseFloat(globalValue
//                                .getCurrentOrder()
//                                .getDriver_rate()) / 2+ "(" + globalValue.getCurrentOrder().getDriverRateCount() + ")");
////                                    ratingBar.setRating(Float
////                                            .parseFloat(globalValue
////                                                    .getCurrentOrder()
////                                                    .getPassenger_rate()) / 2);
//            }
//            aq.id(imgCar).image(globalValue.getCurrentOrder().getCarImage());
//            // TODO: 12/12/2015 need to update image for car. currently url image die
//            Log.e("eeeeeeeeee", "image: " + globalValue.getCurrentOrder()
//                    .getCarImage());
//            aq.id(imgPassenger).image(
//                    globalValue.getCurrentOrder().getImageDriver());
//            if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_ARRIVED_A)) {
//                txtStatus.setText(getString(R.string.lbl_arrived));
//            }
//            getDistance();
//            if (globalValue.getCurrentOrder().getPickUpAtA() != null && globalValue.getCurrentOrder().getPickUpAtA().equals("0")) {
//                shipperLocation = new LatLng(Double.parseDouble(globalValue.getCurrentOrder().getEndLat()), Double.parseDouble(globalValue.getCurrentOrder().getEndLong()));
//            } else {
//                shipperLocation = new LatLng(Double.parseDouble(globalValue.getCurrentOrder().getStartLat()), Double.parseDouble(globalValue.getCurrentOrder().getStartLong()));
//            }
//            updatePositionForPassenger(globalValue.getCurrentOrder().getDriverId());
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shipperLocation, 14.0f));
//        }
    }

    public String convertLinkToString(String link) {
        switch (link) {
            case "I":
                return getString(R.string.sedan4);

            case "II":
                return getString(R.string.suv6);

            case "III":
                return getString(R.string.lux);
        }
        return link;
    }

    private void initControl() {
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTrip();
            }
        });
        lblPhone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (globalValue.getCurrentOrder().getDriver_phone(false).length() > 0) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:"
                            + globalValue.getCurrentOrder().getDriver_phone(false)));
                    startActivity(callIntent);
                } else {
                    showToastMessage(R.string.msg_call_phone);
                }
            }
        });
        imgCall.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + globalValue.getCurrentOrder()
                        .getDriver_phone(false)));
                startActivity(intent);
            }
        });
        imgSms.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", globalValue.getCurrentOrder()
                        .getDriver_phone(false));
                smsIntent.putExtra("sms_body", "message");
                startActivity(smsIntent);
            }
        });
    }

    private void cancelTrip() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.msg_do_you_cancel)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                cancelTripAPI();
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    private void cancelTripAPI() {
        ModelManager.cancelTrip(preferencesManager.getToken(), globalValue
                        .getCurrentOrder().getId(), context, true,
                new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            preferencesManager
                                    .setPassengerCurrentScreen("MainActivity");
                            if (preferencesManager.IsStartWithOutMain()) {
                                gotoActivity(MainActivity.class);
                                finish();
                                overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_right);
                            } else {
                                finish();
                                overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_right);
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

    private void initLocalBroadcastManager() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_DRIVER_START_TRIP);
        intentFilter.addAction(Constant.ACTION_CANCEL_TRIP);
        intentFilter.addAction(Constant.ACTION_DRIVER_ARRIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver,
                intentFilter);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constant.KEY_ACTION);
            if (action.equals(Constant.ACTION_CANCEL_TRIP)) {
                showToastMessage(R.string.message_you_trip_cancel_by_driver);
                preferencesManager.setPassengerCurrentScreen("");
                preferencesManager.setPassengerIsInTrip(false);
                preferencesManager.setPassengerHavePush(false);
                if (preferencesManager.IsStartWithOutMain()) {
                    gotoActivity(MainActivity.class);
                    finish();
                } else {
                    finish();
                }
            } else {
                if (action.equals(Constant.ACTION_DRIVER_START_TRIP)) {
                    gotoActivity(StartTripForPassengerActivity.class);
                    Log.e("arrived", "mMessageReceiver");
                    finish();
                } else if (action.equals(Constant.ACTION_DRIVER_ARRIVED)) {
                    txtStatus.setText(getString(R.string.lbl_arrived));
                    txtStatus.setTextColor(getResources().getColor(R.color.to));
                    preferencesManager.getPassengerCurrentScreen().equals(
                            "ConfirmActivity");
                    setStartMarkerAgain();
                    setEndMarker();
//                    checkFake = false;
                }
            }
        }
    };


    public void updatePositionForPassenger(final String driverId) {
        updateMarker = new Runnable() {
            @Override
            public void run() {
                ref.child("user").child(driverId).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Log.e("chay vao day", "chay vao day");
                        try {
                            JSONObject object = new JSONObject(snapshot.getValue().toString());
                            String latitude = object.getString("lat");
                            String longitude = object.getString("lng");
                            shipperLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                            setLocationLatLong(shipperLocation);
                            if (checkFirst) {
                                if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_ARRIVED_A)) {
                                    setStartMarkerAgain();
                                    setEndMarker();
                                } else {
                                    setStartMarker();
                                    setEndMarkerNoUpdateDirection();
                                }

                                checkFirst = false;
                            } else {
                                if (!checkPath) {
                                    showDistanceAndTime();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                });
            }
        };
        handler.postDelayed(updateMarker, 2000);
    }

    public Bitmap getMarkerIconWithLabel(Context mContext, String label, float angle, Bitmap bitmap) {
        IconGenerator iconGenerator = new IconGenerator(mContext);
        View markerView = LayoutInflater.from(mContext).inflate(R.layout.lay_marker, null);
        ImageView imgMarker = markerView.findViewById(R.id.img_marker);
        TextView tvLabel = markerView.findViewById(R.id.tv_label);
        imgMarker.getLayoutParams().width = AppUtil.convertDpToPixel(self, 28);
        imgMarker.getLayoutParams().height = AppUtil.convertDpToPixel(self, 30);
        imgMarker.setImageBitmap(bitmap);
        imgMarker.setRotation(angle);
        tvLabel.setText(label);
        iconGenerator.setContentView(markerView);
        iconGenerator.setBackground(null);
        return iconGenerator.makeIcon(label);
    }
}

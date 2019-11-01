package com.app.hotgo.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import com.app.hotgo.util.ImageUtil;
import com.app.hotgo.utility.AppUtil;
import com.app.hotgo.utility.DateUtil;
import com.app.hotgo.utility.NetworkUtil;
import com.app.hotgo.utility.PermissionUtil;
import com.joooonho.SelectableRoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

public class StartTripForPassengerActivity extends BaseActivity implements RoutingListener {

    private CardView llHelp, llCancel;
    private TextView lblName, tvSeat, lblCarPlate;
    private TextView lblPhone;
    private RatingBar ratingBar;
    private TextView lblStartLocation, lblDistance, lblTimes;
    private TextView lblEndlocation, lblDistanceTime, txtStatus, lblDuration;
    TextView txtStar;
    private ImageView imgCarDriver;
    private SelectableRoundedImageView imgPassenger;
    private ImageView imgCar;
    AQuery aq;

    private GoogleMap mMap;
    LatLng shipperLocation, endLocation, shopLocation;
    Bitmap iconMarker;
    Runnable runnable;

    // For timer
    private Routing routing;
    private Marker mMarkerEndLocation, mMarkerShipperLocation, mMarkerShop;
    private String phoneAdmin = "";
    private ImageView imgCall, imgSms;
    private boolean checkDataDistance = true;
    private boolean checkFirst = true;
    private DatabaseReference ref;
    private LinearLayout llDuration, llDistance;
    private long startTime = 0L;

    boolean checkPath = true;
    private String dataPath = "";
    Handler handler = new Handler();
    Runnable updateMarker;

    private TextView tvProductName, tvQuantity, tvPrice, tvDescription;
    private boolean isArrived;

    @Override
    public void onResume() {
        if (preferencesManager.getPassengerCurrentScreen().equals(
                "RateDriverActivity")) {
            gotoActivity(RateDriverActivity.class);
            finish();
        } else {
            preferencesManager
                    .setPassengerCurrentScreen(StartTripForPassengerActivity.class
                            .getSimpleName());
            initData();

        }
        super.onResume();
    }

    ;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        aq = new AQuery(self);
        setContentView(R.layout.activity_start_trip);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        initUI();
        initView();
        Maps();
        initData();
        initControl();
        initLocalBroadcastManager();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    public void onBackPressed() {
    }

    private void initData() {
        ModelManager.getGeneralSettings(preferencesManager.getToken(),
                self, true, new ModelManagerListener() {
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
        preferencesManager.setArrived("0");
        if (!getPreviousActivityName().equals(
                WaitDriverConfirmActivity.class.getName())) {
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
                                tvSeat.setText(GlobalValue.convertLinkToString(StartTripForPassengerActivity.this, globalValue.convertToInt(globalValue.getCurrentOrder().getLink()) + ""));
                                lblPhone.setText(globalValue.getCurrentOrder()
                                        .getDriver_phone(false));
                                lblName.setText(globalValue.getCurrentOrder()
                                        .getDriverName());
                                lblCarPlate.setText(globalValue.getCurrentOrder().getCarPlate());

                                lblStartLocation.setText(globalValue
                                        .getCurrentOrder().getEndLocation());
                                lblEndlocation.setText(globalValue
                                        .getCurrentOrder().getEndLocation());

                                tvProductName.setText(globalValue.getCurrentOrder().getProductName());
                                tvQuantity.setText("x" + globalValue.getCurrentOrder().getQuantity());
                                tvPrice.setText(getResources().getString(R.string.lblCurrency) + globalValue.getCurrentOrder().getPrice());
                                tvDescription.setText(globalValue.getCurrentOrder().getProductDesciption());

                                if (globalValue.getCurrentOrder()
                                        .getDriverRate() == null || globalValue.getCurrentOrder()
                                        .getDriverRate().isEmpty()) {
                                    txtStar.setText("0");
                                } else {
                                    txtStar.setText("" + Float
                                            .parseFloat(globalValue
                                                    .getCurrentOrder()
                                                    .getDriverRate()) / 2 + "(" + globalValue.getCurrentOrder().getDriverRateCount() + ")");
                                }

                                aq.id(imgCarDriver).image(
                                        globalValue.getCurrentOrder()
                                                .getProductImage());
                                aq.id(imgPassenger).image(
                                        globalValue.getCurrentOrder()
                                                .getImageDriver());
                                if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_ARRIVED_B)) {
                                    txtStatus.setText(getString(R.string.lbl_arrived));
                                    txtStatus.setTextColor(getResources().getColor(R.color.colorViolet));
                                    llDistance.setVisibility(View.VISIBLE);
                                    llDuration.setVisibility(View.GONE);
                                    llCancel.setVisibility(View.GONE);
                                    llHelp.setVisibility(View.VISIBLE);
                                    shipperLocation = endLocation;
                                } else if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_START_TASK)) {
                                    txtStatus.setText(getString(R.string.on_task));
                                    txtStatus.setTextColor(getResources().getColor(R.color.colorViolet));
                                    llDistance.setVisibility(View.GONE);
                                    llDuration.setVisibility(View.VISIBLE);
                                    llCancel.setVisibility(View.GONE);
                                    llHelp.setVisibility(View.VISIBLE);
                                    Log.e("data", "data:" + globalValue.getCurrentOrder().getStartTimeWorking());
                                    startTime = Long.parseLong(globalValue.getCurrentOrder().getStartTimeWorking());
                                    //
                                    lblDuration.setText(DateUtil.convertTimeStampToDate(startTime + "", "dd MMM h:m a"));
                                    shipperLocation = endLocation;
                                }
                                getDistance();
                                if (globalValue.getCurrentOrder().getStatus().equals(Constant.TRIP_STATUS_IN_PROGRESS)) {
                                    updatePositionForPassenger(globalValue.getCurrentOrder().getDriverId());
                                } else {
                                    lblDistanceTime.setText("0 Km (0 min)");
                                    setLocationLatLong(shipperLocation);
                                    setEndtMarkerNoUpdateDirection();
                                }

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endLocation, 14.0f));
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
            tvSeat.setText(GlobalValue.convertLinkToString(StartTripForPassengerActivity.this, globalValue.getCurrentOrder().getLink()));
            lblPhone.setText(globalValue.getCurrentOrder().getDriver_phone(false));
            lblName.setText(globalValue.getCurrentOrder()
                    .getDriverName());
            lblCarPlate.setText(globalValue.getCurrentOrder().getCarPlate());
            lblStartLocation.setText(globalValue.getCurrentOrder()
                    .getEndLocation());
            lblEndlocation.setText(globalValue.getCurrentOrder()
                    .getEndLocation());

            tvProductName.setText(globalValue.getCurrentOrder().getProductName());
            tvQuantity.setText("x" + globalValue.getCurrentOrder().getQuantity());
            tvPrice.setText(getResources().getString(R.string.lblCurrency) + globalValue.getCurrentOrder().getPrice());
            tvDescription.setText(globalValue.getCurrentOrder().getProductDesciption());

            if (globalValue.getCurrentOrder()
                    .getDriverRate() == null || globalValue.getCurrentOrder()
                    .getDriverRate().isEmpty()) {
                txtStar.setText("0");
            } else {
                txtStar.setText("" + Float
                        .parseFloat(globalValue
                                .getCurrentOrder()
                                .getDriverRate()) / 2 + "(" + globalValue.getCurrentOrder().getDriverRateCount() + ")");
            }
            aq.id(imgCarDriver).image(
                    globalValue.getCurrentOrder()
                            .getProductImage());
            aq.id(imgPassenger).image(
                    globalValue.getCurrentOrder()
                            .getImageDriver());
            if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_ARRIVED_B)) {
                txtStatus.setText(getString(R.string.lbl_arrived));
                llDistance.setVisibility(View.VISIBLE);
                llDuration.setVisibility(View.GONE);
            } else if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_START_TASK)) {
                txtStatus.setText(getString(R.string.on_task));
                llDistance.setVisibility(View.GONE);
                llDuration.setVisibility(View.VISIBLE);
                Log.e("data", "data:" + globalValue.getCurrentOrder().getStartTimeWorking());
                startTime = Long.parseLong(globalValue.getCurrentOrder().getStartTimeWorking());
                lblDuration.setText(DateUtil.convertTimeStampToDate(startTime + "", "dd MMM h:m a"));
            }
            getDistance();
            endLocation = new LatLng(Double.parseDouble(globalValue.getCurrentOrder().getStartLong()), Double.parseDouble(globalValue.getCurrentOrder().getEndLong()));
            updatePositionForPassenger(globalValue.getCurrentOrder().getDriverId());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endLocation, 14.0f));
        }
    }

    protected void showDistanceAndTime() {
        if (shipperLocation != null && endLocation != null) {
//            findDirections(shipperLocation.latitude, shipperLocation.longitude, shipperLocation.latitude, shipperLocation.longitude, GMapV2Direction.MODE_DRIVING);
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(shipperLocation, endLocation);
            checkDataDistance = false;
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

    protected void showDirection() {
        if (shopLocation != null && endLocation != null) {
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(shipperLocation, endLocation);
            checkDataDistance = true;
        }
    }

    public void setEndMarker() {
        if (endLocation != null) {
            if (mMarkerEndLocation != null) {
                mMarkerEndLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_end);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
            mMarkerEndLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, getResources().getString(R.string.lblDestination), getResources().getColor(R.color.colorViolet), 0, iconMarker))));
            showDirection();
        }
    }

    public void setEndtMarkerNoUpdateDirection() {
        if (endLocation != null) {
            if (mMarkerEndLocation != null) {
                mMarkerEndLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_end);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
            mMarkerEndLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, getResources().getString(R.string.lblDestination), getResources().getColor(R.color.colorViolet), 0, iconMarker))));
        }
    }

    public void updatePositionForPassenger(final String driverId) {
        updateMarker = new Runnable() {
            @Override
            public void run() {
                ref.child("user").child(driverId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        try {
                            JSONObject object = new JSONObject(snapshot.getValue().toString());
                            String latitude = object.getString("lat");
                            String longitude = object.getString("lng");
                            shipperLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));


                            if (checkFirst) {
                                checkFirst = false;
                                setLocationLatLong(shipperLocation);
//                                setShopMarker();
                                setEndMarker();
                            } else {
                                if (!checkPath) {
//                                    showDistanceAndTime();
                                    setEndMarker();
                                    setLocationLatLongNoCamera(shipperLocation);
//                                    setEndtMarkerNoUpdateDirection();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });
            }
        };
        handler.postDelayed(updateMarker, 2000);
    }

    private void initView() {
        llHelp = findViewById(R.id.cv_help);
        llCancel = findViewById(R.id.cv_back);
        tvSeat = findViewById(R.id.tvSeat);
        lblName = findViewById(R.id.lblName);
        lblCarPlate = findViewById(R.id.lblCarPlate);
        lblPhone = findViewById(R.id.lblPhone);
        ratingBar = findViewById(R.id.ratingBar);
        lblStartLocation = findViewById(R.id.lblStartLocation);
        lblEndlocation = findViewById(R.id.lblEndlocation);
        txtStatus = findViewById(R.id.txtStatus);
        lblDistance = findViewById(R.id.lblDistance);
        txtStar = findViewById(R.id.txtStar);
        lblTimes = findViewById(R.id.lblTimes);
        lblDistanceTime = findViewById(R.id.lblDistanceTime);
        lblDuration = findViewById(R.id.lblDuration);

        imgPassenger = findViewById(R.id.imgPassenger);
        imgCar = findViewById(R.id.imgCar);
        imgCarDriver = findViewById(R.id.imgCarDriver);
        imgCall = findViewById(R.id.imgCall);
        imgSms = findViewById(R.id.imgSms);
        llDuration = findViewById(R.id.llDuration);
        llDistance = findViewById(R.id.llDistance);
        tvProductName = findViewById(R.id.tv_product_name);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvPrice = findViewById(R.id.tv_price);
        tvDescription = findViewById(R.id.tv_description);
    }

    private void initControl() {
        llHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtil.checkNetworkAvailable(StartTripForPassengerActivity.this)) {
                    ModelManager.sendNeedHelp(preferencesManager.getToken(), preferencesManager.getCurrentOrderId(), context, true, new ModelManagerListener() {
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
        llCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelTripDialog();
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

    private void initLocalBroadcastManager() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_TASKER_ARRIVED_B);
        intentFilter.addAction(Constant.ACTION_TASKER_START_TASK);
        intentFilter.addAction(Constant.ACTION_DRIVER_END_TRIP);
        intentFilter.addAction(Constant.ACTION_CANCEL_TRIP);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver,
                intentFilter);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra(Constant.KEY_DATA);
            String action = intent.getStringExtra(Constant.KEY_ACTION);
            switch (action) {
                case Constant.ACTION_CANCEL_TRIP:
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
                    break;
                case Constant.ACTION_DRIVER_END_TRIP:
                    preferencesManager.setBeginTask("0");
//                customHandler.removeCallbacks(updateTimerThread);
                    globalValue.getCurrentOrder().setActualFare(
                            ParseJsonUtil.getActualFare(json));
                    gotoActivity(RateDriverActivity.class);
                    finish();
                    break;
                case Constant.ACTION_TASKER_ARRIVED_B:
                    txtStatus.setText(getString(R.string.lbl_arrived));
                    txtStatus.setTextColor(getResources().getColor(R.color.to));
                    llDistance.setVisibility(View.VISIBLE);
                    llDuration.setVisibility(View.GONE);
                    llCancel.setVisibility(View.GONE);
                    llHelp.setVisibility(View.VISIBLE);

                    shipperLocation = endLocation;
                    isArrived = true;
                    mMap.clear();
                    setLocationLatLong(shipperLocation);
                    setEndtMarkerNoUpdateDirection();
                    lblDistanceTime.setText("0 Km (0 min)");
                    break;
                case Constant.ACTION_TASKER_START_TASK:
                    preferencesManager.setArrivedB("0");
                    ModelManager.showTripDetail(preferencesManager.getToken(),
                            preferencesManager.getCurrentOrderId(), context, false,
                            new ModelManagerListener() {

                                @Override
                                public void onSuccess(String json) {
                                    if (ParseJsonUtil.isSuccess(json)) {
                                        globalValue.setCurrentOrder(ParseJsonUtil
                                                .parseCurrentOrder(json));
                                        Log.e("data", "data:" + globalValue.getCurrentOrder().getStartTimeWorking());
                                        startTime = Long.parseLong(globalValue.getCurrentOrder().getStartTimeWorking());
                                        //
                                        lblDuration.setText(DateUtil.convertTimeStampToDate(startTime + "", "dd MMM h:m a"));
//                                    customHandler.postDelayed(updateTimerThread, 0);
                                        txtStatus.setText(getString(R.string.on_task));
                                        llDistance.setVisibility(View.GONE);
                                        llDuration.setVisibility(View.VISIBLE);
                                        llCancel.setVisibility(View.GONE);
                                        llHelp.setVisibility(View.VISIBLE);
                                    } else {
                                        showToastMessage(ParseJsonUtil.getMessage(json));
                                    }
                                }

                                @Override
                                public void onError() {
                                    showToastMessage(R.string.message_have_some_error);
                                }
                            });

                    break;
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
                        .getCurrentOrder().getId(), self, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            if (preferencesManager.IsStartWithOutMain()) {
                                gotoActivity(MainActivity.class);
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
        // 2-21
    }

    public void setShopMarker() {
        if (shopLocation != null) {
            if (mMarkerShop != null) {
                mMarkerShop.remove();
            }
            iconMarker = ImageUtil.createBitmapFromUrl(globalValue.getCurrentOrder().getMarkerImage());
            // resize
            int size = ImageUtil.getSizeBaseOnDensity(self, 50);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
//            iconMarker = ImageUtil.getResizedBitmap(iconMarker, AppUtil.convertDpToPixel(self,28), AppUtil.convertDpToPixel(self,32));
//            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            mMarkerShop = mMap.addMarker(new MarkerOptions().position(
                    shopLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, globalValue.getCurrentOrder().getShopName(), getResources().getColor(R.color.orange), 0, iconMarker))));
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
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
//            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            mMarkerShop = mMap.addMarker(new MarkerOptions().position(
                    shopLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, globalValue.getCurrentOrder().getShopName(), getResources().getColor(R.color.orange), 0, iconMarker))));
        }
    }

    private void setLocationLatLongNoCamera(LatLng location) {
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
        // 2-21
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
                    if (PermissionUtil.checkLocationPermission(StartTripForPassengerActivity.this)) {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    }
                }
            });
        }
        ModelManager.showTripDetail(preferencesManager.getToken(),
                preferencesManager.getCurrentOrderId(), context, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            globalValue.setCurrentOrder(ParseJsonUtil
                                    .parseCurrentOrder(json));
                            shopLocation = new LatLng(Double.parseDouble(globalValue.getCurrentOrder().getStartLat()), Double.parseDouble(globalValue.getCurrentOrder().getStartLong()));
                        } else {
                            showToastMessage(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToastMessage(R.string.message_have_some_error);
                    }
                });


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
    public void onRoutingFailure() {
        lblDistanceTime.setText(dataPath);
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        if (shipperLocation != null || endLocation != null) {
//            mMap.clear();
//            setLocationLatLong(shipperLocation);
//            setEndMarker();
            if (checkDataDistance) {
                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.color(R.color.second_primary);
                polyOptions.width(10);
                polyOptions.addAll(mPolyOptions.getPoints());
                mMap.addPolyline(polyOptions);
                lblTimes.setText(route.getDurationText());
                String msg = getString(R.string.msgBeginTripDistance);
                msg = msg.replace("[a]", route.getDistanceText());
                msg = msg.replace("[b]", route.getDurationText());
                lblDistanceTime.setText(msg);
                checkPath = false;
                dataPath = msg;
            } else {
                String msg = getString(R.string.msgBeginTripDistance);
                msg = msg.replace("[a]", route.getDistanceText());
                msg = msg.replace("[b]", route.getDurationText());
                lblDistanceTime.setText(msg);
                dataPath = msg;
            }

        }
    }
}

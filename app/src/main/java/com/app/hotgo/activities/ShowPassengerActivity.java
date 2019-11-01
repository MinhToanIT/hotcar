package com.app.hotgo.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import com.app.hotgo.object.UserUpdate;
import com.app.hotgo.service.GPSTracker;
import com.app.hotgo.util.ImageUtil;
import com.app.hotgo.utility.AppUtil;
import com.app.hotgo.utility.PermissionUtil;
import com.joooonho.SelectableRoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/***
 * todo //
 */
public class ShowPassengerActivity extends BaseActivity implements
        OnClickListener, RoutingListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private TextView tvSeat;
    private TextView lblName;
    private TextView lblPhone;
    private RatingBar ratingBar;
    private TextView lblStart;
    private TextView lblEnd;
    private TextView lblDistance, lblTimes;
    TextView txtStar;
    private SelectableRoundedImageView imgPassenger;
    private LinearLayout btnArrived, btnGoToDestination;
    private TextView btnCancelTrip;
    private AQuery aq;

    private GoogleMap mMap;
    private GPSTracker gps;
    LatLng startLocation, shopLocation, endLocation;
    Bitmap iconMarker;

    // For timer
    private int mInterval = 1; // 5 seconds by default, can be changed later
    private Timer mTimer;
    private Routing routing;
    private Marker mMarkerShopLocation, mMarkerShipperLocation, mMakerEndLocation;
    Runnable runnable;
    private CardView imgBack;
    private ImageView imgCall, imgSms;
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    private TextView tvProductName, tvQuantity, tvPrice;

    private GoogleApiClient mGoogleApiClient;

    public ShowPassengerActivity() {
    }

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
    public void onStop() {
        super.onStop();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onRoutingFailure() {
        showDirection();
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        if (startLocation != null || shopLocation != null) {
            mMap.clear();
            setLocationLatLong(startLocation);
            setStartMarkerNoUpdateDirection();
            setEndMarkerNoUpdateDirection();

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(R.color.second_primary);
            polyOptions.width(10);
            polyOptions.addAll(mPolyOptions.getPoints());
            mMap.addPolyline(polyOptions);
            lblTimes.setText(route.getDurationText());
        }
    }


    protected void showDirection() {
        if (startLocation != null && shopLocation != null) {
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(startLocation, shopLocation);
        }
    }

    protected void showDirectionFromShopToDestination() {
        if (shopLocation != null && endLocation != null) {
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(shopLocation, endLocation);
        }
    }

    public void setStartMarker() {
        if (shopLocation != null) {
            if (mMarkerShopLocation != null) {
                mMarkerShopLocation.remove();
            }
            iconMarker = ImageUtil.createBitmapFromUrl(globalValue.getCurrentOrder().getMarkerImage());
            // resize
            int size = ImageUtil.getSizeBaseOnDensity(self, AppUtil.convertDpToPixel(self, 50));
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
//            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            mMarkerShopLocation = mMap.addMarker(new MarkerOptions().position(
                    shopLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, globalValue.getCurrentOrder().getShopName(), getResources().getColor(R.color.primary), 0, iconMarker))));
            showDirection();
        }
    }

    public void setStartMarkerNoUpdateDirection() {
        if (shopLocation != null) {
            if (mMarkerShopLocation != null) {
                mMarkerShopLocation.remove();
            }
            iconMarker = ImageUtil.createBitmapFromUrl(globalValue.getCurrentOrder().getMarkerImage());
            // resize
            int size = ImageUtil.getSizeBaseOnDensity(self, AppUtil.convertDpToPixel(self, 50));
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
//            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            mMarkerShopLocation = mMap.addMarker(new MarkerOptions().position(
                    shopLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, globalValue.getCurrentOrder().getShopName(), getResources().getColor(R.color.primary), 0, iconMarker))));
        }
    }

    public void setEndMarker() {
        if (endLocation != null) {
            if (mMakerEndLocation != null) {
                mMakerEndLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_position_b);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
            mMakerEndLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, getResources().getString(R.string.lblDestination), getResources().getColor(R.color.to), 0, iconMarker))));
        }
        showDirectionFromShopToDestination();
    }

    public void setEndMarkerNoUpdateDirection() {
        if (endLocation != null) {
            if (mMakerEndLocation != null) {
                mMakerEndLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_position_b);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
            mMakerEndLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, getResources().getString(R.string.lblDestination), getResources().getColor(R.color.to), 0, iconMarker))));
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_passenger);
        aq = new AQuery(this);
        gps = new GPSTracker(this);
        initUI();
        initView();
        Maps();

        initControl();
        initLocalBroadcastManager();
        autoRefreshEvents();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
                    if (ActivityCompat.checkSelfPermission(ShowPassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 100);
                        }
                    }
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    buildGoogleApiClient();
                    initData();

                }
            });

        }
        if (gps.canGetLocation()) {
            startLocation = new LatLng(gps.getLatitude(), gps.getLongitude());
        } else {
            gps.showSettingsAlert();
        }

    }

    @Override
    public void onBackPressed() {
        showCancelTripDialog();
    }

    ;

    private void initData() {

        ModelManager.showTripDetail(preferencesManager.getToken(),
                preferencesManager.getCurrentOrderId(), context, true,
                new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        Log.e("tripId", "tripId:" + json);
                        if (ParseJsonUtil.isSuccess(json)) {
                            globalValue.setCurrentOrder(ParseJsonUtil
                                    .parseCurrentOrder(json));
                            Log.e("phone", "phone:" + globalValue.getCurrentOrder()
                                    .getPassenger_phone(true));
                            if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_ARRIVED_A)) {
                                startLocation = new LatLng(Double.parseDouble(ParseJsonUtil
                                        .parseCurrentOrder(json).getStartLat()), Double.parseDouble(ParseJsonUtil
                                        .parseCurrentOrder(json).getStartLong()));
                                btnArrived.setVisibility(View.GONE);
                                btnGoToDestination.setVisibility(View.VISIBLE);
                            } else {
                                btnArrived.setVisibility(View.VISIBLE);
                                btnGoToDestination.setVisibility(View.GONE);
                            }
                            endLocation = new LatLng(Double.parseDouble(ParseJsonUtil
                                    .parseCurrentOrder(json).getEndLat()), Double.parseDouble(ParseJsonUtil
                                    .parseCurrentOrder(json).getEndLong()));
                            shopLocation = new LatLng(Double.parseDouble(ParseJsonUtil
                                    .parseCurrentOrder(json).getStartLat()), Double.parseDouble(ParseJsonUtil
                                    .parseCurrentOrder(json).getStartLong()));
                            lblName.setText(globalValue.getCurrentOrder()
                                    .getPassengerName());
                            tvSeat.setText(GlobalValue.convertLinkToString(ShowPassengerActivity.this, globalValue.getCurrentOrder().getLink() + ""));
                            lblPhone.setText(globalValue.getCurrentOrder()
                                    .getPassenger_phone(true));
                            lblStart.setText(globalValue.getCurrentOrder()
                                    .getStartLocation());
                            lblEnd.setText(globalValue.getCurrentOrder()
                                    .getEndLocation());

                            tvProductName.setText(globalValue.getCurrentOrder().getProductName());
                            tvQuantity.setText("x" + globalValue.getCurrentOrder().getQuantity());
                            tvPrice.setText(getResources().getString(R.string.lblCurrency) + globalValue.getCurrentOrder().getPrice());

                            if (globalValue.getCurrentOrder()
                                    .getPassenger_rate() == null || globalValue.getCurrentOrder()
                                    .getPassenger_rate().isEmpty()) {
                                txtStar.setText("0");
//                                    ratingBar.setRating(0);
                            } else {
                                txtStar.setText("" + Float
                                        .parseFloat(globalValue
                                                .getCurrentOrder()
                                                .getPassenger_rate()) / 2 + "(" + globalValue.getCurrentOrder().getDriverRateCount() + ")");
                            }
                            Log.e("image", "image:" + globalValue.getCurrentOrder()
                                    .getImagePassenger());
                            aq.id(R.id.imgPassenger).image(
                                    globalValue.getCurrentOrder()
                                            .getShopImage());
                            getDistance();
                            setLocationLatLong(startLocation);

                            if (globalValue.getCurrentOrder().getStatus().equals(Constant.STATUS_ARRIVED_A)) {
                                setEndMarker();
                                setStartMarkerNoUpdateDirection();
                            } else {
                                setStartMarker();
                                setEndMarkerNoUpdateDirection();
                            }

//                                setDriverMarker();
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

    }

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

    private void initView() {
        lblName = findViewById(R.id.lblName);
        tvSeat = findViewById(R.id.tvSeat);
        lblPhone = findViewById(R.id.lblPhone);
        ratingBar = findViewById(R.id.ratingBar);

        lblStart = findViewById(R.id.lblStart);
        lblStart.setSelected(true);
        lblEnd = findViewById(R.id.lblEnd);
        lblDistance = findViewById(R.id.lblDistance);
        lblTimes = findViewById(R.id.lblTimes);
        imgPassenger = findViewById(R.id.imgPassenger);
        btnArrived = findViewById(R.id.btnArrived);
        btnGoToDestination = findViewById(R.id.btn_go_to_destination);
        btnCancelTrip = findViewById(R.id.btnCancelTrip);
        txtStar = findViewById(R.id.txtStar);
        imgBack = findViewById(R.id.cv_back);
        imgCall = findViewById(R.id.imgCall);
        imgSms = findViewById(R.id.imgSms);
        tvProductName = findViewById(R.id.tv_product_name);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvPrice = findViewById(R.id.tv_price);

        imgBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void initControl() {
        btnArrived.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("param", "param:" + preferencesManager.getToken() + "-" + globalValue
                        .getCurrentOrder().getId());
                ModelManager.changeStatus(preferencesManager.getToken(), globalValue
                        .getCurrentOrder().getId(), Constant.STATUS_ARRIVED_A, context, true, new ModelManagerListener() {
                    @Override
                    public void onError() {

                    }

                    @Override
                    public void onSuccess(String json) {
                        btnArrived.setVisibility(View.GONE);
                        btnGoToDestination.setVisibility(View.VISIBLE);
                        setEndMarker();
                    }
                });

            }
        });
        btnGoToDestination.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrip(globalValue.getCurrentOrder().getId());
            }
        });

        btnCancelTrip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelTripDialog();
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
        imgCall.setOnClickListener(new OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
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
                smsIntent.putExtra("address", globalValue.getCurrentOrder()
                        .getPassenger_phone(false));
                smsIntent.putExtra("sms_body", "message");
                startActivity(smsIntent);
            }
        });
    }

    private void setLocationLatLong(LatLng location) {
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
    }

    private void cancelTrip() {
        ModelManager.cancelTrip(preferencesManager.getToken(), globalValue
                        .getCurrentOrder().getId(), context, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
//                            gotoActivity(OnlineActivity.class);
                            preferencesManager.setDriverStatus("1");
                            ref.child("user").child(preferencesManager.getUserID()).child("status").setValue("1");
                            finish();
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

    private void getDistance() {
        if (globalValue
                .getCurrentOrder() != null && globalValue
                .getCurrentOrder().getId() != null) {
            ModelManager.showDistance(preferencesManager.getToken(), globalValue
                            .getCurrentOrder().getId(), context, false,
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

    private void initLocalBroadcastManager() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver,
                        new IntentFilter(Constant.ACTION_CANCEL_TRIP));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constant.KEY_ACTION);
            if (action.equals(Constant.ACTION_CANCEL_TRIP)) {
                showToastMessage(R.string.message_you_trip_cancel_by_passenger);
                preferencesManager.setDriverStatus("1");
                ref.child("user").child(preferencesManager.getUserID()).child("status").setValue("1");
//                gotoActivity(OnlineActivity.class);
                finish();
            }
        }
    };

    /* Request updates at startup */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        if (preferencesManager.getDriverCurrentScreen().equals("")) {
            showToastMessage(R.string.message_you_trip_cancel_by_passenger);
            gotoActivity(OnlineActivity.class);
            finish();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
//        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onDestroy() {
        if (gps != null) {
            gps.stopUsingGPS();
        }
        super.onDestroy();
    }

    public void updatePositionForPassenger(String driverId) {
        ModelManager.getLocationDriver(this, driverId, false, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                    String latitude = jsonObject1.getString("driverLat");
                    String longitude = jsonObject1.getString("driverLon");
                    startLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
//                    setDriverMarkerNoUpdateDirection();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void startTrip(String tripId) {

        ModelManager.startTrip(preferencesManager.getToken(), tripId, self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            preferencesManager.setDriverStartTrip(true);
                            gotoActivity(StartTripForDriverActivity.class);
                            preferencesManager
                                    .setDriverCurrentScreen(StartTripForDriverActivity.class
                                            .getSimpleName());
                            finish();
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
        String status = preferencesManager.getDriverStatus();
        ref.child("user").child(preferencesManager.getUserID()).setValue(new UserUpdate(location.getLatitude() + "", location.getLongitude() + "", status, GlobalValue.getInstance().user.getCarObj().getTypeCar()));
        updateCoordinate(location.getLatitude() + "", location.getLongitude() + "");
        getDistance();
        startLocation = new LatLng(location.getLatitude(), location.getLongitude());
        setLocationLatLong(startLocation);
        Log.e(TAG, "onLocationChanged: ");
    }
}

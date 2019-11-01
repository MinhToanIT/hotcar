package com.app.hotgo.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.hotgo.activities.PaymentPointActivity;
import com.app.hotgo.activities.WaitDriverConfirmActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.app.hotgo.BaseFragment;
import com.app.hotgo.R;
import com.app.hotgo.RequestService;
import com.app.hotgo.activities.SelectProductActivity;
import com.app.hotgo.adapters.CarTypeAdapter;
import com.app.hotgo.autocompleteaddress.PlacesAutoCompleteAdapter;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.googledirections.Route;
import com.app.hotgo.googledirections.Routing;
import com.app.hotgo.googledirections.RoutingListener;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.CarType;
import com.app.hotgo.object.DriverUpdate;
import com.app.hotgo.object.SettingObj;
import com.app.hotgo.object.ShopObj;
import com.app.hotgo.service.GPSTracker;
import com.app.hotgo.util.ImageUtil;
import com.app.hotgo.utility.AppUtil;
import com.app.hotgo.utility.MapsUtil;
import com.app.hotgo.utility.PermissionUtil;
import com.app.hotgo.widget.PicassoMarker;
import com.app.hotgo.widget.TextViewPixeden;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import me.drakeet.materialdialog.MaterialDialog;

public class PassengerPage1Fragment extends BaseFragment implements
        OnMyLocationChangeListener, RoutingListener, OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    Handler handler;
    protected Double lat_start, lat_end, lng_start, lng_end;
    boolean processClick = true;

    // ===================== VARIABLE FOR LOG =====================
    private final String TAG = BaseFragment.class.getSimpleName();

    // ===================== VARIABLE FOR UI =====================
    private TextViewPixeden btnIcGPS, btnRefresh;
    private GoogleMap mMap;
    //    private TextView lblAvailableVehicle/*, ivFrom, ivTo*/;
    private ImageView ivFrom, ivTo;
    private ImageView btnBook;
    //    private Button btnBack;
    private AutoCompleteTextView txtFrom;
    private ImageView btnStart;
    private AutoCompleteTextView txtTo;
    private ImageView btnEnd;


    // ======== VARIABLE FOR LOGIC START LOCATION AND END LOCATION ========
    private HandlerThread mHandlerThread;
    private Handler mThreadHandler;
    private PlacesAutoCompleteAdapter mAdapter;
    private boolean selectFromMap = false;
    LatLng startLocation, endLocation;
    Bitmap iconMarker;
    Bitmap iconMarker2;
    private boolean txtFromIsSelected = false, txtToIsSelected = false;
    private Marker mMarkerStartLocation, mMarkerEndLocation;

    // ======== VARIABLE FOR DRAW DIRECTION ========
    private Routing routing;
    public PreferencesManager preferencesManager;
    protected GlobalValue globalValue;
    private IntentFilter mIntentFilter;
    Marker markerName;
    LatLngBounds.Builder builder;
    Circle circle;
    private HashMap<Integer, Marker> visibleMarkers = new HashMap<Integer, Marker>();
    ArrayList<DriverUpdate> listMarkers = new ArrayList<>();
    private boolean checkData = true;
    String estimateDistance = "";
    double price = 0;
    MaterialDialog dialog, dialogPayment;
    private DatabaseReference ref;
    private ArrayList<ShopObj> listShops;
    private ArrayList<CarType> labelerDates = new ArrayList<>();
    String carType = "";
    private RecyclerView rcvTypeCars;
    private CarTypeAdapter adapter;
    private PicassoMarker marker;
    private ArrayList<CarType> listCarTypes = new ArrayList<>();
    String link = "";
    Bitmap bitmapDescriptor = null;
    private String imageMaker;
    private BitmapDescriptor OldBitmapDescriptor = null;
    private String idMap = "";
    private View view;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;


    // ===================== @OVERRIDE =====================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        self.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        view = inflater.inflate(R.layout.fragment_passenger_page1,
                container, false);
        preferencesManager = PreferencesManager.getInstance(getActivity());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        if (GlobalValue.getInstance().getListCarTypes() != null) {
            Log.e("aaaaaa", "aaaaaa");
            listCarTypes.addAll(GlobalValue.getInstance().getListCarTypes());
            intiDataContent(view);
        } else {
            ModelManager.getGeneralSettings(preferencesManager.getToken(),
                    self, true, new ModelManagerListener() {
                        @Override
                        public void onSuccess(String json) {
                            Log.e("aaaaaa", "bbbbbbbbb");
                            if (ParseJsonUtil.isSuccess(json)) {
                                SettingObj settingObj = ParseJsonUtil.getGeneralSettings(json);
                                listCarTypes.addAll(ParseJsonUtil.parseListCarTypes(json));
                                GlobalValue.getInstance().setListCarTypes(listCarTypes);
                                preferencesManager.setDataSetting(settingObj);
                                intiDataContent(view);
                            }

                        }

                        @Override
                        public void onError() {

                        }
                    });
        }

//        mIntentFilter = new IntentFilter();
//        mIntentFilter.addAction(ServiceUpdateLocation.ACTION);
//        getActivity().registerReceiver(mReceiver, mIntentFilter);


        return view;
    }

    public void intiDataContent(View view) {
        handler = new Handler();
        initUI(view);
        initView(view);
        initControl(view);
        initMenuButton(view);
        initCarTypes();
        setUpMap();
        globalValue = GlobalValue.getInstance();
    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
//                link = listCarTypes.get(adapter.getPositionCheck()).getId();
                initDataMap(link);
            }
        };
    }

    public void initDataMap(final String catId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                ModelManager.showShopByCategory(self, startLocation.latitude, startLocation.longitude,
                        catId, false, new ModelManagerListener() {
                            @Override
                            public void onError() {
//                                lblAvailableVehicle.setText(0 + "");
                            }

                            @Override
                            public void onSuccess(String json) {
                                listShops = new ArrayList<>();
                                listShops.addAll(ParseJsonUtil.parseShopByCategory(json));

                                for (int i = 0; i < listMarkers.size(); i++) {
                                    Log.e(TAG, "remove: ");
                                    listMarkers.get(i).getMarker().remove();
                                }
                                listMarkers.clear();

                                try {
                                    for (ShopObj shopObj : listShops) {

                                        iconMarker =ImageUtil.createBitmapFromUrl(shopObj.getImgSelected());

                                        iconMarker = Bitmap.createScaledBitmap(iconMarker,AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 38),false);


                                        Marker marker = mMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self,"", 0, 0, iconMarker)))
                                                .position(new LatLng(Double.parseDouble(shopObj.getLatitude()), Double.parseDouble(shopObj.getLongitude()))));
                                        DriverUpdate driverUpdate = new DriverUpdate();
                                        driverUpdate.setId(shopObj.getId());
                                        //driverUpdate.setShopName(shopObj.getName());
                                        driverUpdate.setMarker(marker);
                                        listMarkers.add(driverUpdate);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Log.e(TAG, "marker size 1: " + listMarkers.size());

                            }
                        }

                );
            }
        };

        handler.post(runnable);

    }

    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }


    @Override
    public void onResume() {
        super.onResume();
        initDataMap(link);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = Double.parseDouble(intent.getStringExtra("latitude"));
            double longitude = Double.parseDouble(intent.getStringExtra("longitude"));
            if (markerName != null) {
                markerName.remove();
            }
            markerName = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Driver"));
        }
    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {

        } else {
            self.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    }


    @Override
    public void onMyLocationChange(Location lastKnownLocation) {
        mMap.clear();
        CameraUpdate myLoc = CameraUpdateFactory
                .newCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude())).zoom(12)
                        .build());
        mMap.moveCamera(myLoc);
        mMap.setOnMyLocationChangeListener(null);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        getActivity().unregisterReceiver(mReceiver);
    }
// ===================== @OVERRIDE FOR DRAW ROUTING=====================

    @Override
    public void onRoutingFailure() {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        if (startLocation != null || endLocation != null) {
            checkData = true;
            mMap.clear();

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(R.color.second_primary);
            polyOptions.width(10);
            polyOptions.addAll(mPolyOptions.getPoints());
            mMap.addPolyline(polyOptions);
            setStartMarkerAgain();
            setEndMarkerAgain();
            setMarker(/*listMarkers*/);
            if (mPolyOptions.getPoints().size() > 0) {
                checkData = false;
            }

        }
    }

    protected void showDirection() {
        if (startLocation != null && endLocation != null) {
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(startLocation, endLocation);
        }
    }

    // ===================== FUNCTION BASE FOR FRAGMENT =====================
    public void initView(View view) {
        btnIcGPS = view.findViewById(R.id.btnIcGPS);
        btnRefresh = view.findViewById(R.id.btnRefresh);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                int currentApiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentApiVersion >= Build.VERSION_CODES.M) {
                    requestAcessFineLocationPermission();
                } else {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    } else {
                        mMap.setMyLocationEnabled(true);
                    }
                }
            }
        });
        btnBook = view.findViewById(R.id.btnBook);
        txtFrom = view.findViewById(R.id.txtFrom);
        btnStart = view.findViewById(R.id.btnStart);
        txtTo = view.findViewById(R.id.txtTo);
        btnEnd = view.findViewById(R.id.btnEnd);
        ivFrom = view.findViewById(R.id.iv_from);
        ivTo = view.findViewById(R.id.iv_to);
        rcvTypeCars = view.findViewById(R.id.rcvTypeCars);
    }

    public void createServiceRequest() {
        getMainActivity().startService(new Intent(getMainActivity(), RequestService.class));
    }

    public void initCarTypes() {
        adapter = new CarTypeAdapter(self, GlobalValue.getInstance().getListCarTypes());
        rcvTypeCars.setLayoutManager(new LinearLayoutManager(self, LinearLayoutManager.HORIZONTAL, false));
        rcvTypeCars.setAdapter(adapter);

        Picasso.with(getActivity()).load(listCarTypes.get(0).getImageTypeActive()).into(btnBook);
        imageMaker = listCarTypes.get(0).getImageMarker();
        Bitmap bitmap = ImageUtil.createBitmapFromUrl(listCarTypes.get(0).getImageMarker());
        // resize
        int size = ImageUtil.getSizeBaseOnDensity(self, 100);
//        bitmap = ImageUtil.getResizedBitmap(bitmap, size, size);
        bitmap = Bitmap.createScaledBitmap(bitmap,
                AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                false);
        bitmapDescriptor = bitmap;
        adapter.setOnItemClickListener(new CarTypeAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                adapter.setCurrentPosition(position);
                imageMaker = listCarTypes.get(position).getImageMarker();
                Bitmap bitmap = ImageUtil.createBitmapFromUrl(listCarTypes.get(position).getImageMarker());
                // resize
                int size = ImageUtil.getSizeBaseOnDensity(self, 50);
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                        false);
                bitmapDescriptor = bitmap;
                Picasso.with(getActivity()).load(listCarTypes.get(position).getImageTypeActive()).into(btnBook);

                link = listCarTypes.get(position).getId();
//                if (!link.equals(preferencesManager.getDataCarType().getId())) {
//                for (int i = 0; i < listMarkers.size(); i++) {
//                    listMarkers.get(i).getMarker().remove();
//                }
//                listMarkers.clear();
//                }

                initDataMap(link);
                preferencesManager.setCarTypeData(listCarTypes.get(position));
            }
        });
    }

    public void initControl(View view) {
        btnBook.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startLocation == null || endLocation == null) {
                    showToast(R.string.message_please_select_start_and_end);
                } else {
//                    Intent intent = new Intent(self, SelectProductActivity.class);
//                    intent.putExtra(Constant.KEY_STARTLOCATION_LATITUDE, startLocation.latitude);
//                    intent.putExtra(Constant.KEY_STARTLOCATION_LONGITUDE, startLocation.longitude);
//                    intent.putExtra(Constant.KEY_ENDLOCATION_LATITUDE, endLocation.latitude);
//                    intent.putExtra(Constant.KEY_ENDLOCATION_LONGITUDE, endLocation.longitude);
//                    intent.putExtra(Constant.KEY_CATEGORY_ID, link);
//                    intent.putExtra(Constant.KEY_ADDRESS_START, txtFrom.getText().toString().trim());
//                    intent.putExtra(Constant.KEY_ADDRESS_TO, txtTo.getText().toString().trim());
//                    self.startActivity(intent);
//                    self.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left2);
//                    link = "";
//                    int position = adapter.getPositionCheck();
//                    for (int i = 0; i < position; i++) {
//                        link = link + "I";
//                    }
                    showDialogCreateRequest();
                }
            }
        });

        btnRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (PermissionUtil.checkLocationPermission(getMainActivity())) {
                    refreshDriver();
                }
            }
        });
        btnStart.setOnClickListener(new

                                            OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    final GPSTracker tracker = new GPSTracker(mainActivity);
                                                    if (tracker.canGetLocation() == false) {
                                                        tracker.showSettingsAlert();
                                                        showToast(R.string.message_wait_for_location);
                                                    } else {
                                                        if (mLastLocation != null) {
                                                            startLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                                        } else {
                                                            startLocation = new LatLng(tracker.getLatitude(), tracker.getLongitude());
                                                        }
                                                        mMap.animateCamera(CameraUpdateFactory
                                                                .newLatLngZoom(startLocation, 12));
                                                        setStartMarker();
                                                        String address = MapsUtil.getCompleteAddressString(getContext(), startLocation.latitude, startLocation.longitude);
                                                        if (!address.isEmpty()) {
                                                            // Set marker's title
                                                            selectFromMap = true;
                                                            txtFrom.setText(address);
                                                        }
                                                        initDataMap(link);
                                                    }
                                                }
                                            }

        );

        btnEnd.setOnClickListener(new

                                          OnClickListener() {

                                              @Override
                                              public void onClick(View v) {

                                                  final GPSTracker tracker = new GPSTracker(mainActivity);
                                                  if (tracker.canGetLocation() == false) {
                                                      tracker.showSettingsAlert();
                                                      showToast(R.string.message_wait_for_location);
                                                  } else {
                                                      if (mLastLocation != null) {
                                                          endLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                                      } else {
                                                          endLocation = new LatLng(tracker.getLatitude(), tracker.getLongitude());
                                                      }
                                                      mMap.animateCamera(CameraUpdateFactory
                                                              .newLatLngZoom(endLocation, 12));
                                                      setEndMarker();

                                                      String address = MapsUtil.getCompleteAddressString(getContext(), endLocation.latitude, endLocation.longitude);
                                                      if (!address.isEmpty()) {
                                                          // Set marker's title
                                                          selectFromMap = true;
                                                          txtTo.setText(address);
                                                      }

                                                  }
                                              }
                                          }

        );
        ivFrom.setOnClickListener(new

                                          OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  txtFrom.requestFocus();
                                                  txtFromIsSelected = true;
                                                  txtToIsSelected = false;
                                              }
                                          }

        );
        ivTo.setOnClickListener(new

                                        OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                txtTo.requestFocus();
                                                txtFromIsSelected = false;
                                                txtToIsSelected = true;
                                            }
                                        }

        );

    }

    public void showDialogCreateRequest() {
        String msg = getString(R.string.msgEstimatedFare);

        dialog = new MaterialDialog(self);
        dialog.setPositiveButton(getResources().getString(R.string.book_now), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createRequest();
                /*if (globalValue.getUser().getBalance() != null) {
                    if (globalValue.getUser().getBalance() >= price) {
                        createRequest();
                    } else {
                        String msg = getString(R.string.msgValidateBalance);
                        msg = msg.replace("[a]", globalValue.getUser().getBalance() + "");
                        msg = msg.replace("[b]", "$" + price + "");
                        showDialogAddPaymentForRequest(msg);
                    }
                }*/


            }
        }).setNegativeButton(getResources().getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //msg = msg.replace("%.2f", price + "");
        dialog.setMessage(msg);
        dialog.show();
    }

    public void showDialogAddPaymentForRequest(String message) {
        dialogPayment = new MaterialDialog(self);
        dialogPayment.setMessage(message);
        dialogPayment.setPositiveButton(getResources().getString(R.string.add_point), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPayment.dismiss();
                startActivity(PaymentPointActivity.class);
            }
        });
        dialogPayment.setNegativeButton(getResources().getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPayment.dismiss();
            }
        });
        dialogPayment.show();
    }

    private void createRequest() {
        ModelManager.createRequest(preferencesManager.getToken(), link,startLocation.latitude + "", startLocation.longitude + "", txtFrom.getText().toString().trim(),
                endLocation.latitude + "", endLocation.longitude + "", txtTo.getText().toString().trim(), self, true, new ModelManagerListener() {
                    @Override
                    public void onError() {
                        showToast(getResources().getString(R.string.message_have_some_error));
                    }

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            setDataCreateRequest();
                            createServiceRequest();
                            globalValue.setEstimate_fare(ParseJsonUtil.getEstimateFare(json));
                            preferencesManager.putStringValue("countDriver", ParseJsonUtil.getCountDriver(json));
                            startActivity(WaitDriverConfirmActivity.class);
                            dialog.dismiss();
                        } else {
                            showToast(ParseJsonUtil.getMessage(json));
                        }
                    }
                });
    }

    public void setDataCreateRequest() {
        preferencesManager.putStringValue(Constant.KEY_STARTLOCATION_LATITUDE, startLocation.latitude + "");
        preferencesManager.putStringValue(Constant.KEY_STARTLOCATION_LONGITUDE, startLocation.longitude + "");
        preferencesManager.putStringValue(Constant.KEY_ENDLOCATION_LATITUDE, endLocation.latitude + "");
        preferencesManager.putStringValue(Constant.KEY_ENDLOCATION_LONGITUDE, endLocation.longitude + "");
        preferencesManager.putStringValue(Constant.KEY_ADDRESS_START, txtFrom.getText().toString().trim());
        preferencesManager.putStringValue(Constant.KEY_ADDRESS_TO, txtTo.getText().toString().trim());
        preferencesManager.putStringValue(Constant.KEY_LINK,link);
    }

    public void refreshDriver() {
        final GPSTracker tracker = new GPSTracker(mainActivity);
        if (tracker.canGetLocation() == false) {
            tracker.showSettingsAlert();
            showToast("Wait for location service");
        } else {
            LatLng currentLatLng = new LatLng(tracker.getLatitude(), tracker.getLongitude());
            CameraUpdate myLoc = CameraUpdateFactory
                    .newCameraPosition(new CameraPosition.Builder()
                            .target(currentLatLng).zoom(12).build());
            mMap.moveCamera(myLoc);
//            link = listCarTypes.get(adapter.getPositionCheck()).getId();
            initDataMap(link);
        }

    }


//    private void refreshData(String carType) {
//        final GPSTracker tracker = new GPSTracker(mainActivity);
//        if (tracker.canGetLocation() == false) {
//            lblAvailableVehicle.setText(0 + "");
//        } else {
//            ModelManager.getTotalDriversAroundLocation(self, tracker.getLatitude(), tracker.getLongitude(), 3, carType, true, new ModelManagerListener() {
//                @Override
//                public void onError() {
//                    lblAvailableVehicle.setText(0 + "");
//                }
//
//                @Override
//                public void onSuccess(String json) {
//                    if (ParseJsonUtil.isSuccess(json)) {
//                        lblAvailableVehicle.setText(ParseJsonUtil.getDriverCount(json));
//                    } else {
//                        lblAvailableVehicle.setText(0 + "");
//                    }
//                }
//            });
//
//        }
//    }


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private void setUpMap() {

        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    if (PermissionUtil.checkLocationPermission(getActivity())) {


                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        buildGoogleApiClient();

                        if (mLastLocation != null) {
                            startLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        } else {
                            GPSTracker tracker = new GPSTracker(self);
                            startLocation = new LatLng(tracker.getLatitude(), tracker.getLongitude());
                        }

                        link = listCarTypes.get(0).getId();
                        initDataMap(link);
                        setupAutoComplete(view);
                        setUpMapOnClick();
                        mMap.setOnCameraChangeListener(getCameraChangeListener());
                        Location myLocation = mMap.getMyLocation();
                        if (myLocation != null) {
                            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                            preferencesManager.setLocation(latLng);
                        }

                        mMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
                            @Override
                            public void onMyLocationChange(Location location) {
                                mMap.clear();
                                CameraUpdate myLoc = CameraUpdateFactory
                                        .newCameraPosition(new CameraPosition.Builder()
                                                .target(new LatLng(location.getLatitude(),
                                                        location.getLongitude())).zoom(12)
                                                .build());
                                mMap.moveCamera(myLoc);
                                mMap.setOnMyLocationChangeListener(null);
                            }
                        });
                    } else {
                    }
                }
            });
        }

    }

    // ===================== SETUP AUTO COMPLETE ADDRESS =====================
    public void setStartMarker() {
        if (startLocation != null) {
            PreferencesManager.getInstance(getActivity()).putStringValue("STARTLATITUDE", startLocation.latitude + "");
            PreferencesManager.getInstance(getActivity()).putStringValue("STARTLONGITUDE", startLocation.longitude + "");
            if (mMarkerStartLocation != null) {
                mMarkerStartLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    mainActivity.getResources(), R.drawable.ic_start);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
            mMarkerStartLocation = mMap.addMarker(new MarkerOptions().position(
                    startLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));

            if (circle != null) {
                circle.remove();
            }
            Log.e(TAG, "LUAN_COMMENT: " + startLocation.latitude + "-" + startLocation.longitude);
            /*circle = mMap.addCircle(new CircleOptions()
                    .center(startLocation)
                    .radius(6000)
                    .strokeWidth(0.5f)
                    .strokeColor(Color.rgb(0, 136, 255))
                    .fillColor(Color.argb(20, 0, 136, 255)));

            Log.e(TAG, "setEndMarker: " + startLocation.latitude + "-" + startLocation.longitude);
            showDirection();*/
        }
    }

    public static final int REQUEST_ACCESS_FINE_LOCATION = 99;

    public void setStartMarkerAgain() {
        if (startLocation != null) {
            if (mMarkerStartLocation != null) {
                mMarkerStartLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    mainActivity.getResources(), R.drawable.ic_start);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
            mMarkerStartLocation = mMap.addMarker(new MarkerOptions().position(
                    startLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));

            if (circle != null) {
                circle.remove();
            }
            /*circle = mMap.addCircle(new CircleOptions()
                    .center(startLocation)
                    .radius(6000)
                    .strokeWidth(0.5f)
                    .strokeColor(Color.rgb(0, 136, 255))
                    .fillColor(Color.argb(20, 0, 136, 255)));*/
        }
    }

    public void setEndMarker() {
        if (endLocation != null) {
            PreferencesManager.getInstance(getActivity()).putStringValue("ENDLATITUDE", endLocation.latitude + "");
            PreferencesManager.getInstance(getActivity()).putStringValue("ENDLONGITUDE", endLocation.longitude + "");
            if (mMarkerEndLocation != null) {
                mMarkerEndLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    mainActivity.getResources(), R.drawable.ic_end);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
            mMarkerEndLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
            showDirection();
        }
    }

    public void setEndMarkerAgain() {
        if (endLocation != null) {
            if (mMarkerEndLocation != null) {
                mMarkerEndLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    mainActivity.getResources(), R.drawable.ic_end);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 30),
                    false);
            mMarkerEndLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap( iconMarker)));
        }
    }

    public void setUpMapOnClick() {
        // Click on map to get latitude and longitude.
        if (mMap != null)
            mMap.setOnMapClickListener(new OnMapClickListener() {
                @Override
                public void onMapClick(LatLng loc) {
                    if (txtFromIsSelected) {
                        txtFrom.setText(getCompleteAddressString(loc.latitude, loc.longitude));
                    } else {
                        if (txtToIsSelected) {
                            txtTo.setText(getCompleteAddressString(loc.latitude, loc.longitude));
                        }
                    }
                    closeKeyboard();
                    selectFromMap = true;
                    if (txtFromIsSelected) {
                        if (circle != null) {
                            circle.remove();
                        }
                        initDataMap(link);
                        startLocation = loc;
                        setStartMarker();
                        txtFrom.setDropDownHeight(0);
                    } else {
                        if (txtToIsSelected) {
                            endLocation = loc;
                            setEndMarker();
                            txtTo.setDropDownHeight(0);
                        }
                    }
                    // Get address by latlng async

                }
            });
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                if (returnedAddress.getMaxAddressLineIndex() > 0) {
                    for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                        if (i < returnedAddress.getMaxAddressLineIndex() - 1) {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(", ");
                        } else {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("");
                        }

                    }
                } else {
                    strReturnedAddress.append(returnedAddress.getAddressLine(0));
                }
                strAdd = strReturnedAddress.toString();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }

    public void setMarker(/*ArrayList<DriverUpdate> listMarkers*/) {
//        ArrayList<DriverUpdate> listMarkerDatas = new ArrayList<>();
//        listMarkerDatas.addAll(listMarkers);
//        for (int i = 0; i < listMarkers.size(); i++) {
//            listMarkers.get(i).getMarker().remove();
//        }
//        listMarkers.clear();
        Log.e(TAG, "marker size 2: " + listMarkers.size());
        for (int i = 0; i < listMarkers.size(); i++) {
            Log.e(TAG, "setMarker: ");
            String shopName = listMarkers.get(i).getShopName();
            iconMarker = BitmapFactory.decodeResource(
                    mainActivity.getResources(), R.drawable.ic_driver);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    AppUtil.convertDpToPixel(self, 28), AppUtil.convertDpToPixel(self, 38),
                    false);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(ImageUtil.getMarkerIconWithLabel(self, shopName, getResources().getColor(R.color.orange), 0, iconMarker)))
                    .position(listMarkers.get(i).getMarker().getPosition()));
//            BitmapDescriptor bitmapDescriptor = null;
//            Bitmap bitmap = ImageUtil.createBitmapFromUrl(imageMaker);
//            // resize
//            int size = ImageUtil.getSizeBaseOnDensity(self, 50);
//            bitmap = ImageUtil.getResizedBitmap(bitmap, size, size);
//            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
//            marker.setIcon(bitmapDescriptor);
//            DriverUpdate driverUpdate = new DriverUpdate();
//            driverUpdate.setId(listMarkerDatas.get(i).getId());
//            driverUpdate.setMarker(listMarkerDatas.get(i).getMarker());
//            listMarkers.add(driverUpdate);
        }
        for (int i = 0; i < listMarkers.size(); i++) {
            Log.e(TAG, "remove 2: ");
            listMarkers.get(i).getMarker().remove();
        }
    }

    public void setupAutoComplete(View view) {
        if (mThreadHandler == null) {
            mHandlerThread = new HandlerThread(TAG,
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();

            // Initialize the Handler
            mThreadHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        ArrayList<String> results = mAdapter.resultList;

                        if (results != null && results.size() > 0) {
                            mAdapter.notifyDataSetChanged();
                        } else {
                            mAdapter.notifyDataSetInvalidated();
                        }
                    }
                }
            };
        }
        txtFrom = view.findViewById(R.id.txtFrom);
        txtFrom.setAdapter(new PlacesAutoCompleteAdapter(self,
                R.layout.item_auto_place));
        txtFrom.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    txtFromIsSelected = true;
                    txtToIsSelected = false;
                } else {
                    txtFromIsSelected = false;
                    txtToIsSelected = true;
                }
            }
        });
        txtFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                closeKeyboard();
// Get data associated with the specified position
// in the list (AdapterView)
                final String description = (String) parent
                        .getItemAtPosition(position);

                LatLng latLng = MapsUtil.getLocationFromAddress(getContext(), description);
                if (latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(latLng, 12));

                    // Add marker
                    initDataMap(link);
                    startLocation = latLng;
                    setStartMarker();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.can_not_find_location), Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                final String value = s.toString();
                if (value.length() > 0) {
                    // Remove all callbacks and messages
                    mThreadHandler.removeCallbacksAndMessages(null);

                    // Now add a new one
                    mThreadHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mAdapter == null) {
                                mAdapter = new PlacesAutoCompleteAdapter(self,
                                        R.layout.item_auto_place);
                            }
                            // Background thread
                            mAdapter.resultList = mAdapter.mPlaceAPI
                                    .autocomplete(value);

                            // Footer
                            if (mAdapter.resultList.size() > 0) {
                                mAdapter.resultList.add("footer");
                            }

                            // Post to Main Thread
                            mThreadHandler.sendEmptyMessage(1);
                        }
                    }, 500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (selectFromMap) {
                    selectFromMap = false;
                } else {
                    txtFrom.setDropDownHeight(LayoutParams.WRAP_CONTENT);
                }
            }
        });

        txtTo = (AutoCompleteTextView) view.findViewById(R.id.txtTo);
        txtTo.setAdapter(new PlacesAutoCompleteAdapter(self,
                R.layout.item_auto_place));
        txtTo.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    txtToIsSelected = true;
                } else {
                    txtToIsSelected = false;
                }
            }
        });
        txtTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                closeKeyboard();
                // Get data associated with the specified position
                // in the list (AdapterView)
                final String description = (String) parent
                        .getItemAtPosition(position);

                // Move camera to new address.
                LatLng latLng = MapsUtil.getLocationFromAddress(getContext(), description);
                if (latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(latLng, 12));

                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(latLng, 11));

                    // Add marker
                    endLocation = latLng;
                    setEndMarker();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.can_not_find_location), Toast.LENGTH_SHORT).show();
                }
//                new MapsUtil.GetLatLngByAddress(new IMaps() {
            }
        });

        txtTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                final String value = s.toString();
                if (value.length() > 0) {
                    // Remove all callbacks and messages
                    mThreadHandler.removeCallbacksAndMessages(null);

                    // Now add a new one
                    mThreadHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mAdapter == null) {
                                mAdapter = new PlacesAutoCompleteAdapter(self,
                                        R.layout.item_auto_place);
                            }
                            // Background thread
                            mAdapter.resultList = mAdapter.mPlaceAPI
                                    .autocomplete(value);

                            // Footer
                            if (mAdapter.resultList.size() > 0) {
                                mAdapter.resultList.add("footer");
                            }

                            // Post to Main Thread
                            mThreadHandler.sendEmptyMessage(1);
                        }
                    }, 500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (selectFromMap) {
                    selectFromMap = false;
                } else {
                    txtTo.setDropDownHeight(LayoutParams.WRAP_CONTENT);
                }
            }
        });
    }


// ===================== DOMAIN =====================

    private void closeKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (txtFrom.hasFocus()) {
                imm.hideSoftInputFromWindow(txtFrom.getWindowToken(), 0);
            } else {
                imm.hideSoftInputFromWindow(txtFrom.getWindowToken(), 0);
            }

            if (txtTo.hasFocus()) {
                imm.hideSoftInputFromWindow(txtTo.getWindowToken(), 0);
            } else {
                imm.hideSoftInputFromWindow(txtTo.getWindowToken(), 0);
            }
        } catch (Exception ex) {

        }
    }

    public void back() {
        clear();
    }

    public void clear() {
        if (mMap != null)
            mMap.clear();
        startLocation = null;
        endLocation = null;
        txtFrom.setText("");
        txtTo.setText("");
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Location myLocation = mMap.getMyLocation();
                    if (myLocation != null) {
                        LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                        preferencesManager.setLocation(latLng);
                    }
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    } else {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    requestAcessFineLocationPermission();
                    Toast.makeText(getActivity(), "This app needs permission to use this feature. You can grant them in app settings.", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' statements for other permssions
        }
    }

    /*================================*/
    private void requestAcessFineLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true);
            Location myLocation = mMap.getMyLocation();
            if (myLocation != null) {
                LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                preferencesManager.setLocation(latLng);
            }
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
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        preferencesManager.setLocation(latLng);
    }
}

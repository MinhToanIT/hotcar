package com.app.hotgo.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.autocompleteaddress.PlacesAutoCompleteAdapter;
import com.app.hotgo.config.Constant;
import com.app.hotgo.service.GPSTracker;
import com.app.hotgo.utility.MapsUtil;
import com.app.hotgo.utility.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChooseExtraLocationActivity extends BaseActivity {

    private GoogleMap mMap;
    private AutoCompleteTextView txtFrom;
    private ImageView btnStart;
    private ImageButton btnBack;

    private HandlerThread mHandlerThread;
    private Handler mThreadHandler;
    private PlacesAutoCompleteAdapter mAdapter;
    LatLng extraLocation;
    Bitmap iconMarker;
    private Marker mMarkerStartLocation;
    private Circle circle;
    private TextView tvSave;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_extra_location);
        initViews();
        setUpMap();
        initControl();
    }

    private void initViews() {
        txtFrom = findViewById(R.id.txtFrom);
        tvSave = findViewById(R.id.tv_save);
        btnStart = findViewById(R.id.btnStart);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initData() {
        Intent intent = getIntent();
        extraLocation = intent.getParcelableExtra(Constant.LOCATION);
        if (extraLocation != null) {
            txtFrom.setText(getCompleteAddressString(extraLocation.latitude, extraLocation.longitude));
            setStartMarker();
        }
    }

    private void setUpMap() {

        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    if (PermissionUtil.checkLocationPermission(ChooseExtraLocationActivity.this)) {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);

                        setupAutoComplete();
                        setUpMapOnClick();

                        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                            @Override
                            public void onMyLocationChange(Location location) {
                                mMap.clear();
                                initData();
                                CameraUpdate myLoc = CameraUpdateFactory
                                        .newCameraPosition(new CameraPosition.Builder()
                                                .target(new LatLng(location.getLatitude(),
                                                        location.getLongitude())).zoom(12)
                                                .build());
                                mMap.moveCamera(myLoc);
                                mMap.setOnMyLocationChangeListener(null);
                            }
                        });
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    } else {
//                        showSettingsDialog();
                    }
                }
            });
        }

    }

    public void setUpMapOnClick() {
        // Click on map to get latitude and longitude.
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng loc) {
                txtFrom.setText(getCompleteAddressString(loc.latitude, loc.longitude));

                closeKeyboard();
                if (circle != null)
                    circle.remove();
//                        mMap.clear();
                extraLocation = loc;
                setStartMarker();
                circle = mMap.addCircle(new CircleOptions()
                        .center(extraLocation)
                        .radius(3000)
                        .strokeWidth(0.5f)
                        .strokeColor(Color.rgb(0, 136, 255))
                        .fillColor(Color.argb(20, 0, 136, 255)));
                txtFrom.setDropDownHeight(0);

            }
        });

    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(ChooseExtraLocationActivity.this, Locale.getDefault());
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

    private void initControl() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GPSTracker tracker = new GPSTracker(ChooseExtraLocationActivity.this);
                if (tracker.canGetLocation() == false) {
                    tracker.showSettingsAlert();
                    showToastMessage(R.string.message_wait_for_location);
                } else {
                    extraLocation = new LatLng(tracker.getLatitude(), tracker.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(extraLocation, 12));
                    setStartMarker();
                    String address = MapsUtil.getCompleteAddressString(ChooseExtraLocationActivity.this, extraLocation.latitude, extraLocation.longitude);
                    if (!address.isEmpty()) {
                        // Set marker's title
                        txtFrom.setText(address);
                    }
                }
            }
        });
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(Constant.LOCATION, extraLocation);
                intent.putExtra(Constant.ADDRESS,txtFrom.getText().toString().trim());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    public void setupAutoComplete() {
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
        txtFrom = findViewById(R.id.txtFrom);
        txtFrom.setAdapter(new PlacesAutoCompleteAdapter(self,
                R.layout.item_auto_place));
        txtFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                closeKeyboard();
// Get data associated with the specified position
// in the list (AdapterView)
                final String description = (String) parent
                        .getItemAtPosition(position);

                LatLng latLng = MapsUtil.getLocationFromAddress(ChooseExtraLocationActivity.this, description);
                if (latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(latLng, 12));

                    // Add marker
                    extraLocation = latLng;
                    setStartMarker();
                } else {
                    Toast.makeText(ChooseExtraLocationActivity.this, getResources().getString(R.string.can_not_find_location), Toast.LENGTH_SHORT).show();
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
//                if (selectFromMap) {
//                    selectFromMap = false;
//                } else {
//                    txtFrom.setDropDownHeight(DrawerLayout.LayoutParams.WRAP_CONTENT);
//                }
            }
        });

    }

    public void setStartMarker() {
        if (extraLocation != null) {
            if (mMarkerStartLocation != null) {
                mMarkerStartLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    ChooseExtraLocationActivity.this.getResources(), R.drawable.ic_marker);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    iconMarker.getWidth(), iconMarker.getHeight(),
                    false);
            mMarkerStartLocation = mMap.addMarker(new MarkerOptions().position(
                    extraLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
        }
    }

    private void closeKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (txtFrom.hasFocus()) {
                imm.hideSoftInputFromWindow(txtFrom.getWindowToken(), 0);
            } else {
                imm.hideSoftInputFromWindow(txtFrom.getWindowToken(), 0);
            }

        } catch (Exception ex) {

        }
    }

}

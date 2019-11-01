package com.app.hotgo.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.ServiceUpdateLocation;
import com.app.hotgo.adapters.RequestPassengerAdapter;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.RequestObj;
import com.app.hotgo.service.GPSTracker;
import com.app.hotgo.utility.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RequestPassengerActivity extends BaseActivity implements
        LocationListener, RequestPassengerAdapter.IListennerAdapter, RequestPassengerAdapter.OnClickDeleteButtonListener {

    private ListView lvRequestPassenger;
    private RequestPassengerAdapter adapter;
    private ArrayList<RequestObj> array;
    public PreferencesManager preferencesManager;
    private LocationManager locationManager;
    private String provider;
    private GPSTracker gps;
    private DatabaseReference ref;
    private Button btnOffline;
    private ImageButton btnBack;
    private SwipeRefreshLayout refreshLayout;
    private TextView tvNumberRequest;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_request_passenger);
        initWithoutHeader();
        preferencesManager = PreferencesManager.getInstance(context);
        gps = new GPSTracker(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        initView();
        initControl();

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_PASSENGER_CREATE_REQUEST);
        intentFilter.addAction(Constant.ACTION_PASSENGER_CANCEL_REQUEST);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        /* DESTROY RECEIVER MESSAGE */
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        super.onDestroy();
    }

    /* FOR RECEIVER MESSAGE */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constant.KEY_ACTION);
            if (action.equals(Constant.ACTION_PASSENGER_CREATE_REQUEST)) {
                showMyRequest();
            } else {
                if (action.equals(Constant.ACTION_PASSENGER_CANCEL_REQUEST)) {
                    showToastMessage(R.string.message_you_request_cancel_by_driver);
                    supdateRequest();
                }
            }
        }
    };

    /* Request updates at startup */
    @Override
    public void onResume() {
        super.onResume();
        showMyRequest();
        preferencesManager.setDriverCurrentScreen("RequestPassengerActivity");
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}*/
        if (PermissionUtil.checkLocationPermission(RequestPassengerActivity.this))
            locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the location listener updates when Activity is paused */
    @Override
    public void onPause() {
        super.onPause();
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}*/
        locationManager.removeUpdates(this);
    }

    private void initView() {
        lvRequestPassenger = findViewById(R.id.lvRequestPassenger);
        btnOffline = findViewById(R.id.btnOffline);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setVisibility(View.GONE);
        refreshLayout = findViewById(R.id.refresh_layout);
        tvNumberRequest = findViewById(R.id.tv_number_request);
    }

    private void initControl() {
        array = new ArrayList<RequestObj>();
        adapter = new RequestPassengerAdapter(this, array, this, this);
        lvRequestPassenger.setAdapter(adapter);
        lvRequestPassenger.setEmptyView(findViewById(R.id.tv_no_data));
        btnOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuitDialog();
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showMyRequest();
            }
        });
    /*	lvRequestPassenger.setOnItemClickListener(new OnItemClickListener() {
            @Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				driverConfirm(array.get(position).getId());
			}
		});*/
    }

    private void showMyRequest() {
        ModelManager.showMyRequest(preferencesManager.getToken(), self, true,
                new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (refreshLayout.isRefreshing())
                            refreshLayout.setRefreshing(false);
                        if (ParseJsonUtil.isSuccess(json)) {
                            array.clear();
                            array.addAll(ParseJsonUtil.parseMyRequest(json));
                            if (array.size() == 0) {
//                                showToastMessage(R.string.message_you_request_cancel_by_driver);
                                tvNumberRequest.setText(getResources().getString(R.string.lbl_waiting_for_request));
                            } else {
                                tvNumberRequest.setText(String.format(getResources().getString(R.string.you_have_buyer_request), array.size() + ""));
                            }
                            adapter.setArrViews(array);
                            adapter.notifyDataSetChanged();
                            lvRequestPassenger.invalidateViews();

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

    private void refreshMyRequest() {
        ModelManager.showMyRequest(preferencesManager.getToken(), self, true,
                new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            array.clear();
                            array.addAll(ParseJsonUtil.parseMyRequest(json));
                            if (array.size() == 0) {
//                                finish();
                                tvNumberRequest.setText(getResources().getString(R.string.lbl_waiting_for_request));
                            } else {
                                tvNumberRequest.setText(String.format(getResources().getString(R.string.you_have_buyer_request), array.size() + ""));
                            }
                            adapter.setArrViews(array);
                            adapter.notifyDataSetChanged();
                            lvRequestPassenger.invalidateViews();
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

    private void supdateRequest() {
        ModelManager.showMyRequest(preferencesManager.getToken(), self, true,
                new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            array.clear();
                            array.addAll(ParseJsonUtil.parseMyRequest(json));
                            if (array.size() == 0) {
//                                finish();
                                tvNumberRequest.setText(getResources().getString(R.string.lbl_waiting_for_request));
                            } else {
                                tvNumberRequest.setText(String.format(getResources().getString(R.string.you_have_buyer_request), array.size() + ""));
                            }
//                            if (array != null && array.size() > 0) {
                            adapter.setArrViews(array);
                            adapter.notifyDataSetChanged();
                            lvRequestPassenger.invalidateViews();
//                            }\

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

    @Override
    public void onBackPressed() {
//        taskerDeleteRequest();
        preferencesManager.setDriverCurrentScreen(OnlineActivity.class.getSimpleName());
        showQuitDialog();
//        gotoActivity(OnlineActivity.class);
//        finish();
    }

    private void taskerDeleteRequest() {
        ModelManager.taskerDeleteRequest(preferencesManager.getToken(), RequestPassengerActivity.this, false, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {

            }
        });
    }

    public void driverConfirm(String requestId) {
        if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
        } else {
            String address = getCompleteAddressString(gps.getLatitude(), gps.getLongitude());
            ModelManager.driverConfirm(preferencesManager.getToken(), requestId, gps.getLatitude() + "", gps.getLongitude() + "", address,
                    this, true, new ModelManagerListener() {
                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                preferencesManager.setDriverStatus("0");
                                ref.child("user").child(preferencesManager.getUserID()).child("status").setValue("0");
                                globalValue.setCurrentOrder(ParseJsonUtil
                                        .parseCurrentOrder(json));
                                if (globalValue.getCurrentOrder().getWorkAtB() != null && globalValue.getCurrentOrder().getWorkAtB().equals("1")) {
                                    gotoActivity(StartTripForDriverActivity.class);
                                    preferencesManager
                                            .setDriverCurrentScreen(StartTripForDriverActivity.class
                                                    .getSimpleName());
                                } else {
                                    gotoActivity(ShowPassengerActivity.class);
                                    preferencesManager
                                            .setDriverCurrentScreen(ShowPassengerActivity.class
                                                    .getSimpleName());
                                }
                                preferencesManager.setDriverIsInTrip();
                                preferencesManager.setCurrentOrderId(globalValue
                                        .getCurrentOrder().getId());
//                                finish();
                            } else {
                                showToastMessage(ParseJsonUtil.getMessage(json));
                                refreshMyRequest();
                            }
                        }

                        @Override
                        public void onError() {
                            showToastMessage(R.string.message_have_some_error);
                        }
                    });
        }

    }

    /* LOGICAL METHOD */
    private void showQuitDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.msg_offline)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                offline();
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    /* CALL API */
    private void offline() {
        preferencesManager.putStringValue("OFFLINE_DRIVER", "0");
        ModelManager.offline(preferencesManager.getToken(), this, true,
                new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            preferencesManager.setDriverOffline();
                            stopService(new Intent(RequestPassengerActivity.this, ServiceUpdateLocation.class));
                            ref.child("user").child(preferencesManager.getUserID()).child("status").setValue("0");
                            preferencesManager.setDriverStatus("0");
                            if (getPreviousActivityName().equals(
                                    MainActivity.class.getSimpleName())) {
                                finish();
                                overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_right);
                            } else {
                                if (preferencesManager.IsStartWithOutMain()) {
                                    gotoActivityWithClearTop(MainActivity.class);
                                    finish();
                                } else {
                                    finish();
                                }
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
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w(TAG, "" + strReturnedAddress.toString());
            } else {
                Log.w(TAG, "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Canont get Address!");
        }
        return strAdd;
    }

    @Override
    public void onLocationChanged(Location location) {

        updateCoordinate(location.getLatitude() + "", location.getLongitude()
                + "");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {


    }

    @Override
    public void onProviderEnabled(String provider) {


    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClickItemAdapter(int position) {
        driverConfirm(array.get(position).getId());
    }

    @Override
    public void onClickDelete(int position) {
        showDialogConfirm(position, array.get(position).getId());
    }

    private Dialog mDialog;

    private void showDialogConfirm(final int position, final String requestId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RequestPassengerActivity.this);
        builder.setTitle(getResources().getString(R.string.alert));
        builder.setMessage(getResources().getString(R.string.confirm));
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismissRequest(position, requestId);
                mDialog.dismiss();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDialog.dismiss();
            }
        });
        mDialog = builder.create();
        mDialog.setCancelable(false);
        mDialog.show();

    }

    private void dismissRequest(final int position, String requestId) {
        //Call server to delete request by requestId
        ModelManager.taskerDismissRequest(preferencesManager.getToken(), requestId, this, true, new ModelManagerListener() {
            @Override
            public void onError() {
                showToastMessage(getResources().getString(R.string.message_have_some_error));
            }

            @Override
            public void onSuccess(String json) {
                if (ParseJsonUtil.isSuccess(json)) {
                    array.remove(position);
                    adapter.notifyDataSetChanged();
                } else {
                    showToastMessage(ParseJsonUtil.getMessage(json));
                }
            }
        });
    }
}

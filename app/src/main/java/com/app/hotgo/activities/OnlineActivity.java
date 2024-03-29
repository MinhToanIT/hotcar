package com.app.hotgo.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.ServiceUpdateLocation;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.User;

public class OnlineActivity extends BaseActivity implements LocationListener {
    private Button btnOffline;
    private DatabaseReference ref;
    private User user;
    private TextView tvNumberRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        // get reference to 'users' node
        init();
//        initWithoutHeader();
        /* REGISTER RECEIVER MESSAGE */
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver,
                new IntentFilter(Constant.ACTION_PASSENGER_CREATE_REQUEST));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void init() {
        btnOffline = (Button) findViewById(R.id.btnOffline);
        tvNumberRequest = findViewById(R.id.tv_number_request);
        initControl();
    }

    private void initControl() {
        btnOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offline();
            }
        });
    }

    @Override
    public void onResume() {
        if (preferencesManager.driverIsHavePush()) {
            gotoActivity(RequestPassengerActivity.class);
            preferencesManager.setDriverHaveNoPush();
            finish();
        }
        if (!preferencesManager.getFromBeforeOnline()) {
            ModelManager.showInfoProfile(PreferencesManager.getInstance(context).getToken(), context, true, new ModelManagerListener() {
                @Override
                public void onError() {
                    showToastMessage(getString(R.string.message_have_some_error));
                }

                @Override
                public void onSuccess(String json) {
                    if (ParseJsonUtil.isSuccess(json)) {
                        user = ParseJsonUtil.parseInfoProfile(json);
                        if (user.getBalance() <= 0) {
                            offline();
                            showToastMessage(getString(R.string.message_balance));
                        }
                    } else {
                        showToastMessage(ParseJsonUtil.getMessage(json));
                    }
                }
            });

        }
        super.onResume();
    }

    ;

    @Override
    public void onBackPressed() {
        showQuitDialog();
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
                gotoActivity(RequestPassengerActivity.class);
                finish();
            }
        }
    };

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
                            stopService(new Intent(OnlineActivity.this, ServiceUpdateLocation.class));
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

    @Override
    public void onLocationChanged(Location location) {

//        updateCoordinate(location.getLatitude() + "", location.getLongitude()
//                + "");
    }
}

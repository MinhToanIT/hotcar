package com.app.hotgo.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;

import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.ServiceUpdateLocation;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.CarType;
import com.app.hotgo.object.SettingObj;
import com.app.hotgo.service.GPSTracker;
import com.app.hotgo.utility.NetworkUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;

public class SplashActivity extends BaseActivity {

    private final int SPLASH_DISPLAY_LENGHT = 1000;
    public static final int REQUEST_LOCATION = 101;
    Locale myLocale;
    private GPSTracker gps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();

        } else {
            getAppKeyHash();
            if (preferencesManager.driverIsOnline()) {
                Intent intent1 = new Intent(this, ServiceUpdateLocation.class);
                startService(intent1);
            }

            NetworkUtil.enableStrictMode();
            //getLanguage();
            /* CLEAR PUSH NOTIFICATION */
            NotificationManager notifManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.cancelAll();
            /*Check network & GPS*/
            gps = new GPSTracker(this);
            checkBaseCondition();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    getAppKeyHash();

                    if (preferencesManager.driverIsOnline()) {
                        Intent intent1 = new Intent(this, ServiceUpdateLocation.class);
                        startService(intent1);
                    }

                    NetworkUtil.enableStrictMode();
                    //getLanguage();
                    NotificationManager notifManager = (NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    notifManager.cancelAll();
                    /*Check network & GPS*/
                    gps = new GPSTracker(this);
                    checkBaseCondition();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    requestLocationPermission();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(SplashActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);
    }



    @Override
    public void onResume() {
        super.onResume();
    }

    private void checkBaseCondition() {
        if (NetworkUtil.checkNetworkAvailable(this)) {

            if (!gps.canGetLocation()) {
                gps.showSettingsAlert();
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        processNextScreen();
                    }
                }, SPLASH_DISPLAY_LENGHT);
            }
        } else {
            showWifiSetting(this);
        }
    }

    public void showWifiSetting(final Context act) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(act);

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.wifi_settings));

        // Setting Dialog Message
        alertDialog
                .setMessage(getString(R.string.wifi_not_enable));

        // On pressing Settings button
        alertDialog.setPositiveButton(getResources().getString(R.string.settings),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_SETTINGS);
                        act.startActivity(intent);
                        dialog.dismiss();
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }


    private void processNextScreen() {
        // Check user login or not
        if ((preferencesManager.isAlreadyLogin())) {
            firstLogin();
        } else {
            gotoActivity(LoginActivity.class);
            finish();
        }
    }

    // Start code check for user
    public void firstLogin() {
        generalSettings();
    }

    public void generalSettings() {
        ModelManager.getGeneralSettings(preferencesManager.getToken(),
                self, true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            SettingObj settingObj = ParseJsonUtil.getGeneralSettings(json);
                            ArrayList<CarType> listCarTypes = new ArrayList<CarType>();
                            listCarTypes.addAll(ParseJsonUtil.parseListCarTypes(json));
                            GlobalValue.getInstance().setListCarTypes(listCarTypes);
                            preferencesManager.setDataSetting(settingObj);
                            ModelManager.showInfoProfile(PreferencesManager.getInstance(self)
                                    .getToken(), self, true, new ModelManagerListener() {
                                @Override
                                public void onSuccess(String json) {
                                    Log.e("data", "data image:" + ParseJsonUtil.parseInfoProfile(json).getLinkImage());
                                    globalValue.setUser(ParseJsonUtil.parseInfoProfile(json));
                                    if (globalValue.getUser() != null && globalValue.getUser().getIsActive() != null && globalValue.getUser().getIsActive().equals("1")) {
                                        if (ParseJsonUtil.isSuccess(json)) {
                                            if (globalValue.getUser().getTypeTasker().equals(Constant.TYPE_TASKER_ROLE_SELLER)) {
                                                if (ParseJsonUtil.isShopFromSplash(json)) {
                                                    preferencesManager.setIsShop(true);
                                                    preferencesManager.setIsUser(false);
                                                    preferencesManager.setIsDriver(false);
                                                    // If is driver check active or not
                                                    if (ParseJsonUtil.shopIsActiveFromSplash(json)) {
                                                        preferencesManager.setShopIsActive(true);
                                                        preferencesManager.setDriverIsUnActive();
                                                    } else {
                                                        preferencesManager.setShopIsActive(false);
                                                    }
                                                }
                                            } else if (globalValue.getUser().getTypeTasker().equals(Constant.TYPE_TASKER_ROLE_SHIPER)) {
                                                if (ParseJsonUtil.isDriverFromSplash(json)) {
                                                    preferencesManager.setIsDriver(true);
                                                    preferencesManager.setIsUser(false);
                                                    preferencesManager.setIsShop(false);
                                                    // If is driver check active or not
                                                    if (ParseJsonUtil.driverIsActiveFromSplash(json)) {
                                                        preferencesManager.setDriverIsActive();
                                                        preferencesManager.setShopIsActive(false);
                                                    } else {
                                                        preferencesManager.setDriverIsUnActive();
                                                    }
                                                }
                                            } else {
                                                preferencesManager.setIsUser(true);
                                                preferencesManager.setIsShop(false);
                                                preferencesManager.setIsDriver(false);
                                            }

                                            // Check is normal user or driver user
                                            if (preferencesManager.isShop()) {
                                                checkDriverOnline();
                                            }/* else if (preferencesManager.isShop()) {
                                                gotoActivity(MainActivity.class);
                                                finish();
                                            }*/ else {
                                                checkUserWithOutApi();
                                            }
                                        } else {
                                            PreferencesManager.getInstance(context).setLogout();
                                            gotoActivity(LoginActivity.class);
                                            finish();
                                        }
                                    } else {
                                        showToastMessage(getString(R.string.msgAccountInActive));
                                        PreferencesManager.getInstance(context).setLogout();
                                        gotoActivity(LoginActivity.class);
                                        finish();
                                    }

                                }

                                @Override
                                public void onError() {
                                    PreferencesManager.getInstance(context).setLogout();
                                    gotoActivity(LoginActivity.class);
                                    finish();
                                }
                            });
                        } else {
                            PreferencesManager.getInstance(context).setLogout();
                            gotoActivity(LoginActivity.class);
                            finish();
                        }
                    }

                    @Override
                    public void onError() {
                        PreferencesManager.getInstance(context).setLogout();
                        gotoActivity(LoginActivity.class);
                        finish();
                    }
                });
    }

    // Start code check for user
    public void checkUserWithOutApi() {
        Log.e("token check", "token check:" + preferencesManager.getToken());
        ModelManager.showMyRequestForUser(preferencesManager.getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        Log.e("json data", "json data checkUserWithOutApi:" + json);
                        if (ParseJsonUtil.isSuccess(json)) {
                            if (ParseJsonUtil.parseMyRequest(json).size() > 0) {
                                preferencesManager.setStartWithOutMain(true);
                                gotoActivity(WaitDriverConfirmActivity.class);
                                finish();
                            } else {
                                checkUserInTrip();
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

    // Start code check for Driver
    public void checkDriverOnline() {
        if (globalValue.getUser().getShopObj().getIsOnline()
                .equals(Constant.STATUS_IDLE)) {
            if (globalValue.getUser().getShopObj().getStatus()
                    .equals(Constant.STATUS_IDLE)) {
                ModelManager.showTripHistory(PreferencesManager.getInstance(self)
                        .getToken(), "1", self, true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            String status = ParseJsonUtil.parseTripStatus(json);
                            String passengerRate = ParseJsonUtil.parseTripPassengerRate(json);
                            String isWaitDriverConfirm = ParseJsonUtil.pareWaitDriverConfirm(json);
                            String tripId = ParseJsonUtil.parseTripId(json);
                            preferencesManager.setTripId(tripId);
                            preferencesManager.setCurrentOrderId(tripId);
                            if (isWaitDriverConfirm.equals(Constant.TRIP_WAIT_DRIVER_NOTCONFIRM)) {
                                preferencesManager.setPreviousScreen(SplashActivity.class.getSimpleName());
                                gotoActivity(Ac_ConfirmPayByCash.class);
                            } else {
                                if (globalValue.getUser().getShopObj().getStatus()
                                        .equals(Constant.STATUS_IDLE)) {
                                    countMyRequest();
                                } else {
                                    switch (status) {
                                        case Constant.TRIP_STATUS_PENDING_PAYMENT:
                                            Log.e("driverRate", "driverRate:" + passengerRate);
                                            if (passengerRate != null && !passengerRate.equals("")) {
                                                countMyRequest();
                                            } else {
                                                preferencesManager.setStartWithOutMain(true);
                                                gotoActivity(RatingPassengerActivity.class);
                                                finish();
                                            }
                                            break;
                                        case Constant.TRIP_STATUS_FINISH:
                                            Log.e("driverRate", "driverRate:" + passengerRate);
                                            if (passengerRate != null && !passengerRate.equals("")) {
                                                countMyRequest();
                                            } else {
                                                preferencesManager.setStartWithOutMain(true);
                                                gotoActivity(RatingPassengerActivity.class);
                                                finish();
                                            }
                                            break;
                                        default:
                                            countMyRequest();
                                            break;
                                    }
                                }
                            }
                        } else {
                            showToastMessage(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        countMyRequest();
                    }
                });
            } else {
                if (globalValue.getUser().getShopObj().getStatus()
                        .equals(Constant.STATUS_BUSY)) {
                    Log.e("driverRate", "driverRate busy");
                    checkDriverInTrip();
                }
            }
        } else {
//            if (preferencesManager.isDriver()) {
//                checkDriverInTrip();
//            } else {
            checkUserWithOutApi();
//            }

        }

    }

    private void checkDriverInTrip() {
        ModelManager.showTripHistory(PreferencesManager.getInstance(self)
                .getToken(), "1", self, true, new ModelManagerListener() {
            @Override
            public void onSuccess(String json) {
                Log.e("json data", "json data in trip:" + json);
                if (ParseJsonUtil.isSuccess(json)) {
                    String status = ParseJsonUtil.parseTripStatus(json);
                    String passengerRate = ParseJsonUtil.parseTripPassengerRate(json);
                    preferencesManager.setCurrentOrderId(ParseJsonUtil
                            .parseTripId(json));
                    switch (status) {
                        case Constant.TRIP_STATUS_APPROACHING:
                            preferencesManager.setStartWithOutMain(true);
                            gotoActivity(ShowPassengerActivity.class);
                            finish();
                            break;
                        case Constant.TRIP_STATUS_IN_PROGRESS:
                            preferencesManager.setDriverStartTrip(true);
                            preferencesManager.setStartWithOutMain(true);
                            gotoActivity(StartTripForDriverActivity.class);
                            finish();
                            break;
                        case Constant.STATUS_ARRIVED_A:
//                            preferencesManager.setStartWithOutMain(true);
//                            gotoActivity(StartTripForDriverActivity.class);
//                            finish();
                            preferencesManager.setStartWithOutMain(true);
                            gotoActivity(ShowPassengerActivity.class);
                            finish();
                            break;
                        case Constant.STATUS_ARRIVED_B:
                            preferencesManager.setStartWithOutMain(true);
                            gotoActivity(StartTripForDriverActivity.class);
                            finish();
                            break;
                        case Constant.STATUS_START_TASK:
                            preferencesManager.setStartWithOutMain(true);
                            gotoActivity(StartTripForDriverActivity.class);
                            finish();
                            break;
                        case Constant.TRIP_STATUS_PENDING_PAYMENT:
                            Log.e("passengerRate", "passengerRate:" + passengerRate);
                            if (passengerRate != null && !passengerRate.equals("")) {
                                if (preferencesManager.driverIsOnline()) {
//                                    gotoActivity(OnlineActivity.class);
                                    gotoActivity(RequestPassengerActivity.class);
                                    finish();
                                } else {
                                    gotoActivity(MainActivity.class);
                                    finish();
                                }

                            } else {
                                preferencesManager.setStartWithOutMain(true);
                                gotoActivity(RatingPassengerActivity.class);
                                finish();
                            }
                            break;
                        case Constant.TRIP_STATUS_FINISH:
                            Log.e("passengerRate", "passengerRate finish:" + passengerRate);
                            if (passengerRate != null && !passengerRate.equals("")) {
                                if (preferencesManager.driverIsOnline()) {
//                                    gotoActivity(OnlineActivity.class);
                                    gotoActivity(RequestPassengerActivity.class);
                                    finish();
                                } else {
                                    gotoActivity(MainActivity.class);
                                    finish();
                                }
                            } else {
                                preferencesManager.setStartWithOutMain(true);
                                gotoActivity(RatingPassengerActivity.class);
                                finish();
                            }
                            break;
                        default:
                            if (globalValue.getUser() != null && globalValue.getUser().getPhone() != null && !globalValue.getUser().getPhone().equals("")) {
                                gotoActivity(MainActivity.class);
                                finish();
                            } else {
                                gotoActivity(EditProfileFirstActivity.class);
                                finish();
                            }
                            break;
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

    private void checkUserInTrip() {
        ModelManager.showTripHistory(preferencesManager
                .getToken(), "1", self, true, new ModelManagerListener() {
            @Override
            public void onSuccess(String json) {

                if (ParseJsonUtil.isSuccess(json)) {
                    String status = ParseJsonUtil.parseTripStatus(json);
                    String driverRate = ParseJsonUtil.parseTripDriverRate(json);
                    Log.e("status", "status:" + status);
                    String isWaitDriverConfirm = ParseJsonUtil.pareWaitDriverConfirm(json);
                    preferencesManager.setCurrentOrderId(ParseJsonUtil
                            .parseTripId(json));
                    if (isWaitDriverConfirm.equals(Constant.TRIP_WAIT_DRIVER_NOTCONFIRM)) {
                        preferencesManager.setPassengerCurrentScreen("");
                        gotoActivity(MainActivity.class);
                        finish();
                    } else {
                        switch (status) {
                            case Constant.TRIP_STATUS_APPROACHING:
                                preferencesManager.setStartWithOutMain(true);
                                gotoActivity(ConfirmActivity.class);
                                finish();
                                break;
                            case Constant.STATUS_ARRIVED_A:
                                preferencesManager.setStartWithOutMain(true);
                                Log.e("arrived", "arrived a");
                                preferencesManager.getPassengerCurrentScreen().equals(
                                        "ConfirmActivity");
                                gotoActivity(ConfirmActivity.class);
                                finish();
                                break;
                            case Constant.TRIP_STATUS_IN_PROGRESS:
                                preferencesManager.setStartWithOutMain(true);
                                Log.e("status", "status 2");
                                gotoActivity(StartTripForPassengerActivity.class);
                                finish();
                                break;
                            case Constant.STATUS_ARRIVED_B:
                                preferencesManager.setStartWithOutMain(true);
                                gotoActivity(StartTripForPassengerActivity.class);
                                finish();
                                break;
                            case Constant.STATUS_START_TASK:
                                preferencesManager.setStartWithOutMain(true);
                                gotoActivity(StartTripForPassengerActivity.class);
                                finish();
                                break;
                            case Constant.TRIP_STATUS_PENDING_PAYMENT:
                                Log.e("driverRate", "driverRate:" + driverRate);
                                if (driverRate != null && !driverRate.equals("")) {
                                    preferencesManager.setPassengerCurrentScreen("");
                                    gotoActivity(MainActivity.class);
                                    finish();
                                } else {
                                    preferencesManager.setStartWithOutMain(true);
                                    gotoActivity(RateDriverActivity.class);
                                    finish();
                                }
                                break;
                            default:
                                preferencesManager.setPassengerCurrentScreen("");
                                gotoActivity(MainActivity.class);
                                finish();
                                break;
                        }
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

    private void countMyRequest() {
        ModelManager.showMyRequest(preferencesManager.getToken(), self, true,
                new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            if (ParseJsonUtil.parseMyRequest(json).size() > 0) {
                                preferencesManager.setStartWithOutMain(true);
                                gotoActivity(RequestPassengerActivity.class);
                                finish();
                            } else {
                                preferencesManager.setStartWithOutMain(true);
//                                gotoActivity(OnlineActivity.class);
                                gotoActivity(RequestPassengerActivity.class);
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

    private void getData() {

    }


    private void showDialogEnableNetwork() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.msg_check_net_work)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                Intent intent = new Intent(
                                        Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(intent);
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                onBackPressed();
                            }
                        }).create().show();
    }

    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;

                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.d("Hash key", "HASH KEY : " + something);
            }
        } catch (NameNotFoundException e1) {
            // TODO Auto-generated catch block
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
        } catch (Exception e) {
        }

    }
}

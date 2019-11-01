package com.app.hotgo.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.android.gms.location.LocationListener;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.ServiceUpdateLocation;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.fragment.BeforeOnlineFragment;
import com.app.hotgo.fragment.HelpFragment;
import com.app.hotgo.fragment.HistoryFragment;
import com.app.hotgo.fragment.PaymentFragment;
import com.app.hotgo.fragment.RegisterAsShipperFragment;
import com.app.hotgo.fragment.RegisterAsShopFragment;
import com.app.hotgo.fragment.RegisterFragment;
import com.app.hotgo.fragment.ShareFragment;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.User;
import com.app.hotgo.service.FusedLocationService;
import com.app.hotgo.service.GPSTracker;
import com.app.hotgo.slidingmenu.SlidingMenu;
import com.app.hotgo.utility.ImageUtil;
import com.app.hotgo.widget.CircleImageView;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;

public class MainActivity extends BaseActivity implements LocationListener {

    // --------------------------------------------
    // --- Menu layout
    // --------------------------------------------
    private CircleImageView imgAvartar;
    private TextView txtPoint;
    private TextView txtUserName;
    private RatingBar ratingBar;
    private LinearLayout btnHome;
    private TextView icHome;
    private TextView lblHome;
    private LinearLayout btnProfile;
    private TextView icProfile;
    private TextView lblProfile;
    private LinearLayout btnPayment;
    private TextView icPayment;
    private TextView lblPayment;
    private LinearLayout btnShare;
    private TextView icShare;
    private TextView lblShare;
    private LinearLayout btnHelp;
    private TextView icHelp;
    private TextView lblHelp;
    private LinearLayout btnTripHistory;
    private TextView icTripHistory;
    private TextView lblTripHistory;
    private LinearLayout btnOnline;
    private TextView icOnline;
    private TextView lblOnline;
    private LinearLayout btnRegisterAsShop;
    private TextView icRegisterAsShop;
    private TextView lblRegisterAsShop;
    private LinearLayout btnRegisterAsShipper;
    private TextView icRegisterAsShipper;
    private TextView lblRegisterAsShipper;
    private LinearLayout btnLanguage;
    private TextView icLanguage;
    private TextView lblLanguage, lbl_logout;
    private LinearLayout btnLogout;

    private ImageView ivHome, ivProfile, ivPayment, ivShare, ivHelp, ivHistory, ivMyProduct, ivRegisterShop;
    private ImageView ivRegisterShipper, ivOnline, ivChangPassword, ivLogout;

    private LinearLayout btnChangePassword;
    private TextView icChangePassword;
    private TextView lblChangePassword;



    // ============== For fragment ==========
    public static final int PROFILE = 0;
    public static final int TRIP_HISTORY = 1;
    public static final int PASSENGER_PAGE1 = 2;
    public static final int HELP = 3;
    public static final int REGISTER_AS_SHOP = 4;
    public static final int REGISTER_AS_SHIPPER = 5;
    public static final int WAIT_DRIVER_CONFIRM = 6;
    public static final int ONLINE = 7;
    public static final int MY_PRODUCT = 8;

    public static final int PAYMENT = 9;
    public static final int SHARE = 10;
    public static final int CHANGE_PASSWORD = 11;
    public static final int TOTAL_FRAGMENT = 12;
    public static final String MY_PREFS_NAME = "LinkApp";

    public final int REQUEST_IMAGE_GALLERY_IMAGE_ONE = 0;
    public final int REQUEST_IMAGE_CAPTURE_IMAGE_ONE = 1;
    public final int REQUEST_IMAGE_GALLERY_IMAGE_TWO = 2;
    public final int REQUEST_IMAGE_CAPTURE_IMAGE_TWO = 3;
    public final int FILE_SELECT_CODE = 4;

    private int currentFragment = PROFILE;
    private Fragment fragments[];
    private FragmentManager fm;
    public SlidingMenu menu;
    public boolean shareFaceBook = false;

    // User user = new User();
    AQuery aq;

    public PreferencesManager preferencesManager;

    LocationManager locationManager;
    public static final String DEFAULT_LAT = "-28.9323405";
    public static final String DEFAULT_LONG = "135.6806593";
    GPSTracker tracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferencesManager = PreferencesManager.getInstance(context);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        initMenu();
        initFragment();
        initMenuControl();
        getInfoMenu();
        updateMyLocation();
        checkGPS();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (globalValue.getUser() == null || globalValue.getUser().getPhone() == null || globalValue.getUser().getPhone().equals("")) {
            gotoActivity(EditProfileFirstActivity.class);
            finish();
        }
        preferencesManager.setPassengerCurrentScreen("");
    }

    private void checkGPS() {
        if (!locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showSettingsAlert();
        }
    }

    public void updateMyLocation() {
        tracker = new GPSTracker(this);
        if (tracker.canGetLocation() == false) {
            tracker.showSettingsAlert();
            showToastMessage(R.string.message_wait_for_location);
        }
    }

    // --------------------------------------------
    // --- Menu Manager
    // --------------------------------------------

    private void initMenu() {
        // Get header menu
        menu = new SlidingMenu(this);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.5f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.layout_menu);

        // Get content menu
        imgAvartar = findViewById(R.id.img_avartar);
        txtPoint = findViewById(R.id.txt_point);
        txtUserName = findViewById(R.id.txt_user_name);
        ratingBar = findViewById(R.id.ratingBar);
        btnHome = findViewById(R.id.btn_home);
        icHome = findViewById(R.id.ic_home);
        lblHome = findViewById(R.id.lbl_home);
        btnProfile = findViewById(R.id.btn_profile);
        icProfile = findViewById(R.id.ic_profile);
        lblProfile = findViewById(R.id.lbl_profile);
        btnPayment = findViewById(R.id.btn_payment);
        icPayment = findViewById(R.id.ic_payment);
        lblPayment = findViewById(R.id.lbl_payment);
        btnShare = findViewById(R.id.btn_share);
        icShare = findViewById(R.id.ic_share);
        lblShare = findViewById(R.id.lbl_share);
        btnHelp = findViewById(R.id.btn_help);
        icHelp = findViewById(R.id.ic_help);
        lblHelp = findViewById(R.id.lbl_help);
        btnTripHistory = findViewById(R.id.btn_trip_history);
        icTripHistory = findViewById(R.id.ic_trip_history);
        lblTripHistory = findViewById(R.id.lbl_trip_history);
        btnOnline = findViewById(R.id.btn_online);
        icOnline = findViewById(R.id.ic_online);
        lblOnline = findViewById(R.id.lbl_online);

        btnRegisterAsShop = findViewById(R.id.btn_register_shop);
        icRegisterAsShop = findViewById(R.id.ic_register_shop);
        lblRegisterAsShop = findViewById(R.id.lbl_register_shop);

        btnRegisterAsShipper = findViewById(R.id.btn_register_shipper);
        icRegisterAsShipper = findViewById(R.id.ic_register_shipper);
        lblRegisterAsShipper = findViewById(R.id.lbl_register_shipper);

        btnLanguage = findViewById(R.id.btn_language);
        icLanguage = findViewById(R.id.ic_language);
        lblLanguage = findViewById(R.id.lbl_language);
        lbl_logout = findViewById(R.id.lbl_logout);
        btnLogout = findViewById(R.id.btn_logout);

        btnChangePassword = findViewById(R.id.btn_change_password);
        icChangePassword = findViewById(R.id.ic_change_password);
        lblChangePassword = findViewById(R.id.lbl_change_password);



        ivHome = findViewById(R.id.iv_home);
        ivProfile = findViewById(R.id.iv_profile);
        ivPayment = findViewById(R.id.iv_payment);
        ivShare = findViewById(R.id.iv_share);
        ivHelp = findViewById(R.id.iv_help);
        ivHistory = findViewById(R.id.iv_trip_history);
        ivRegisterShop = findViewById(R.id.iv_register_shop);
        ivRegisterShipper = findViewById(R.id.iv_register_shipper);
        ivChangPassword = findViewById(R.id.iv_change_password);
        ivOnline = findViewById(R.id.iv_online);
        ivLogout = findViewById(R.id.iv_logout);
        setMenuByUserType();
    }

    public void setMenuByUserType() {
        if (preferencesManager.isUser()) {
            btnOnline.setVisibility(View.GONE);
        } else {
            if (preferencesManager.isDriver()) {
                if (preferencesManager.isActiveDriver()) {
                    btnOnline.setVisibility(View.VISIBLE);
                    btnRegisterAsShipper.setVisibility(View.GONE);
                    btnRegisterAsShop.setVisibility(View.GONE);
                } else {
                    btnOnline.setVisibility(View.GONE);
                    btnRegisterAsShipper.setVisibility(View.GONE);
                    btnRegisterAsShop.setVisibility(View.GONE);
                }
            }

            if (preferencesManager.isShop()) {
                if (preferencesManager.isActiveShop()) {
                    btnOnline.setVisibility(View.VISIBLE);
                    btnRegisterAsShipper.setVisibility(View.GONE);
                    btnRegisterAsShop.setVisibility(View.GONE);
                } else {
                    btnOnline.setVisibility(View.GONE);
                    btnRegisterAsShipper.setVisibility(View.GONE);
                    btnRegisterAsShop.setVisibility(View.GONE);
                }
            }

        }
    }

    public void showMenu() {
        menu.showMenu();

    }

    private void initMenuControl() {
        btnProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(PROFILE);
                menu.showContent(true);
            }
        });

        btnTripHistory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(TRIP_HISTORY);
                menu.showContent(true);
            }
        });

        btnLanguage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // showLanguageDialog();
                updateLanguage();
            }
        });

        btnHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(HELP);
                menu.showContent(true);
            }
        });

        btnRegisterAsShop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(REGISTER_AS_SHOP);
                menu.showContent(true);
            }
        });

        btnRegisterAsShipper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(REGISTER_AS_SHIPPER);
                menu.showContent(true);
            }
        });


        btnHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(PASSENGER_PAGE1);
                menu.showContent(true);
            }
        });

        btnOnline.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(ONLINE);
                menu.showContent(true);
            }
        });

        btnPayment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, R.string.feature_coming_soon, Toast.LENGTH_LONG).show();
                //showFragment(PAYMENT);
                //menu.showContent(true);
            }
        });

        btnShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(SHARE);
                menu.showContent(true);
            }
        });
        btnChangePassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(CHANGE_PASSWORD);
                menu.showContent(true);
            }
        });

        btnLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
    }

    Locale myLocale;

    public void updateLanguage() {
        if (preferencesManager.isChinase()) {
            myLocale = new Locale("en");
            preferencesManager.setIsEnglish();

        } else {
            myLocale = new Locale("zh");
            preferencesManager.setIsChinese();
        }

        Locale.setDefault(myLocale);
        Configuration config = new Configuration();
        config.locale = myLocale;

        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        lblHome.setText(R.string.lbl_home);
        lblLanguage.setText(R.string.lbl_chinese);
        lblProfile.setText(R.string.lbl_profile);
        lblHelp.setText(R.string.lbl_help);
        lblPayment.setText(R.string.lbl_payment);
        lblShare.setText(R.string.lbl_share);
        lblTripHistory.setText(R.string.lbl_trip_history);
        lblOnline.setText(R.string.lbl_online);
        lblRegisterAsShop.setText(R.string.lbl_register_as_driver);
        lbl_logout.setText(R.string.lbl_logout);

        ((PaymentFragment) fragments[PAYMENT]).changeLanguage();
        ((RegisterFragment) fragments[REGISTER_AS_SHOP]).changeLanguage();
        ((ShareFragment) fragments[SHARE]).changeLanguage();
        ((BeforeOnlineFragment) fragments[ONLINE]).changeLanguage();
        ((HelpFragment) fragments[HELP]).changeLanguage();
        ((HistoryFragment) fragments[TRIP_HISTORY]).changeLanguage();

    }

    // logout
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.msg_logout)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                logout();
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    public void getDataUser() {
        ModelManager.showInfoProfile(PreferencesManager.getInstance(self)
                .getToken(), self, true, new ModelManagerListener() {
            @Override
            public void onSuccess(String json) {
                if (ParseJsonUtil.isSuccess(json)) {
                    globalValue.setUser(ParseJsonUtil.parseInfoProfile(json));
                    getInfoMenu();
                }
            }

            @Override
            public void onError() {

            }
        });
    }

    private void logout() {
        ModelManager.logout(PreferencesManager.getInstance(context).getToken(),
                this, true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {

                        if (ParseJsonUtil.isSuccess(json)) {
                            logoutApp();

                        } else {
                            logoutApp();
                        }
                    }

                    @Override
                    public void onError() {

                        showToastMessage(R.string.message_have_some_error);
                    }
                });
    }

    private void logoutApp() {
        PreferencesManager.getInstance(context).setLogout();
        finish();
        gotoActivity(LoginActivity.class);

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    private void updateMenuUI() {
        if (currentFragment == PROFILE) {
            btnProfile.setBackgroundColor(getResources()
                    .getColor(R.color.white));
            icProfile.setTextColor(getResources().getColor(
                    R.color.green));
            lblProfile.setTextColor(getResources().getColor(
                    R.color.green));
            ivProfile.setImageResource(R.drawable.ic_menu_profile_c);
        } else {
            btnProfile.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icProfile.setTextColor(getResources().getColor(R.color.text_color_primary));
            lblProfile.setTextColor(getResources().getColor(R.color.text_color_primary));
            ivProfile.setImageResource(R.drawable.ic_menu_profile);
        }

        if (currentFragment == TRIP_HISTORY) {
            btnTripHistory.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icTripHistory.setTextColor(getResources().getColor(
                    R.color.green));
            lblTripHistory.setTextColor(getResources().getColor(
                    R.color.green));
            ivHistory.setImageResource(R.drawable.ic_menu_histories_c);
        } else {
            btnTripHistory.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icTripHistory.setTextColor(getResources().getColor(R.color.text_color_primary));
            lblTripHistory.setTextColor(getResources().getColor(R.color.text_color_primary));
            ivHistory.setImageResource(R.drawable.ic_menu_histories);
        }

        if (currentFragment == HELP) {
            btnHelp.setBackgroundColor(getResources().getColor(R.color.white));
            icHelp.setTextColor(getResources().getColor(
                    R.color.green));
            lblHelp.setTextColor(getResources().getColor(
                    R.color.green));
            ivHelp.setImageResource(R.drawable.ic_menu_help_c);
        } else {
            btnHelp.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icHelp.setTextColor(getResources().getColor(R.color.text_color_primary));
            lblHelp.setTextColor(getResources().getColor(R.color.text_color_primary));
            ivHelp.setImageResource(R.drawable.ic_menu_help);
        }

        if (currentFragment == ONLINE) {
            btnOnline
                    .setBackgroundColor(getResources().getColor(R.color.white));
            icOnline.setTextColor(getResources().getColor(
                    R.color.green));
            lblOnline.setTextColor(getResources().getColor(
                    R.color.green));
            ivOnline.setImageResource(R.drawable.ic_menu_online_c);
        } else {
            btnOnline.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icOnline.setTextColor(getResources().getColor(R.color.text_color_primary));
            lblOnline.setTextColor(getResources().getColor(R.color.text_color_primary));
            ivOnline.setImageResource(R.drawable.ic_menu_online);
        }

        if (currentFragment == REGISTER_AS_SHOP) {
            btnRegisterAsShop.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icRegisterAsShop.setTextColor(getResources().getColor(
                    R.color.green));
            lblRegisterAsShop.setTextColor(getResources().getColor(
                    R.color.green));
            ivRegisterShop.setImageResource(R.drawable.ic_menu_tasker_c);
        } else {
            btnRegisterAsShop.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icRegisterAsShop.setTextColor(getResources()
                    .getColor(R.color.text_color_primary));
            lblRegisterAsShop.setTextColor(getResources().getColor(
                    R.color.text_color_primary));
            ivRegisterShop.setImageResource(R.drawable.ic_menu_tasker);
        }
        if (currentFragment == REGISTER_AS_SHIPPER) {
            btnRegisterAsShipper.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icRegisterAsShipper.setTextColor(getResources().getColor(
                    R.color.green));
            lblRegisterAsShipper.setTextColor(getResources().getColor(
                    R.color.green));
            ivRegisterShop.setImageResource(R.drawable.ic_menu_tasker_c);
        } else {
            btnRegisterAsShipper.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icRegisterAsShipper.setTextColor(getResources()
                    .getColor(R.color.text_color_primary));
            lblRegisterAsShipper.setTextColor(getResources().getColor(
                    R.color.text_color_primary));
            ivRegisterShop.setImageResource(R.drawable.ic_menu_tasker);
        }


        if (currentFragment == PASSENGER_PAGE1) {
            btnHome.setBackgroundColor(getResources().getColor(R.color.white));
            icHome.setTextColor(getResources().getColor(
                    R.color.green));
            lblHome.setTextColor(getResources().getColor(
                    R.color.green));
            ivHome.setImageResource(R.drawable.ic_menu_home_c);
        } else {
            btnHome.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icHome.setTextColor(getResources().getColor(R.color.text_color_primary));
            lblHome.setTextColor(getResources().getColor(R.color.text_color_primary));
            ivHome.setImageResource(R.drawable.ic_menu_home);
        }

        if (currentFragment == PAYMENT) {
            btnPayment.setBackgroundColor(getResources()
                    .getColor(R.color.white));
            icPayment.setTextColor(getResources().getColor(
                    R.color.green));
            lblPayment.setTextColor(getResources().getColor(
                    R.color.green));
            ivPayment.setImageResource(R.drawable.ic_menu_payment_c);
        } else {
            btnPayment.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icPayment.setTextColor(getResources().getColor(R.color.text_color_primary));
            lblPayment.setTextColor(getResources().getColor(R.color.text_color_primary));
            ivPayment.setImageResource(R.drawable.ic_menu_payment);
        }

        if (currentFragment == SHARE) {
            btnShare.setBackgroundColor(getResources().getColor(R.color.white));
            icShare.setTextColor(getResources().getColor(
                    R.color.green));
            lblShare.setTextColor(getResources().getColor(
                    R.color.green));
            ivShare.setImageResource(R.drawable.ic_menu_share_c);
        } else {
            btnShare.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icShare.setTextColor(getResources().getColor(R.color.text_color_primary));
            lblShare.setTextColor(getResources().getColor(R.color.text_color_primary));
            ivShare.setImageResource(R.drawable.ic_menu_share);
        }
        if (currentFragment == CHANGE_PASSWORD) {
            btnChangePassword.setBackgroundColor(getResources().getColor(R.color.white));
            icChangePassword.setTextColor(getResources().getColor(
                    R.color.green));
            lblChangePassword.setTextColor(getResources().getColor(
                    R.color.green));
            ivChangPassword.setImageResource(R.drawable.ic_menu_changepassword_c);
        } else {
            btnChangePassword.setBackgroundColor(getResources().getColor(
                    R.color.white));
            icChangePassword.setTextColor(getResources().getColor(R.color.text_color_primary));
            lblChangePassword.setTextColor(getResources().getColor(R.color.text_color_primary));
            ivChangPassword.setImageResource(R.drawable.ic_menu_changepassword);
        }
    }

    // --------------------------------------------
    // --- Fragment Manager
    // --------------------------------------------
    private void initFragment() {
        fm = getSupportFragmentManager();
        fragments = new Fragment[TOTAL_FRAGMENT];
        fragments[PROFILE] = fm.findFragmentById(R.id.fragmentProfileFragment);
        fragments[TRIP_HISTORY] = fm
                .findFragmentById(R.id.fragmentHistoryFragment);
        fragments[HELP] = fm.findFragmentById(R.id.fragmentHelpFragment);
        fragments[REGISTER_AS_SHOP] = fm
                .findFragmentById(R.id.fragmentRegisterAsShopFragment);
        fragments[PASSENGER_PAGE1] = fm
                .findFragmentById(R.id.fragmentPassenger1);
        fragments[WAIT_DRIVER_CONFIRM] = fm
                .findFragmentById(R.id.fragmentWaitDriverConfirmFragment);
        fragments[ONLINE] = fm
                .findFragmentById(R.id.fragmentBeforeOnlineFragment);
        fragments[PAYMENT] = fm.findFragmentById(R.id.fragmentPaymentFragment);
        fragments[SHARE] = fm.findFragmentById(R.id.fragmentShareFragment);
        fragments[CHANGE_PASSWORD] = fm.findFragmentById(R.id.fragmentChangePassword);
        fragments[REGISTER_AS_SHIPPER] = fm.findFragmentById(R.id.fragmentRegisterAsShipperFragment);
        fragments[MY_PRODUCT] = fm.findFragmentById(R.id.fragmentMyProduct);
        showFragment(PASSENGER_PAGE1);
    }

    public void showFragment(int fragmentIndex) {
        FragmentTransaction transaction = fm.beginTransaction();
        currentFragment = fragmentIndex;
        updateMenuUI();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        if (tracker != null) {
            tracker.stopUsingGPS();
        }
        stopService(new Intent(MainActivity.this, ServiceUpdateLocation.class));
        super.onDestroy();
    }

    // --------------------------------------------
    // --- Main activity
    // --------------------------------------------
    @Override
    public void onBackPressed() {
        showQuitDialog();
    }

    private void showQuitDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.msg_quit_app)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                stopService(new Intent(MainActivity.this, FusedLocationService.class));
                                finish();
                                overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_right);
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    // get data info user menu
    public void getInfoMenu() {
        aq = new AQuery(self);
        txtUserName.setText(globalValue.getUser().getFullName());
        User user = globalValue.getUser();
        if (preferencesManager.isUser()) {
            if (user.getPassengerRate().length() == 0) {
                ratingBar.setRating(0);
            } else {
                ratingBar.setRating(Float.parseFloat(user.getPassengerRate()) / 2);
            }
        } else {
            if (user.getDriverObj() != null) {
                if (user.getDriverObj().getDriverRate() != null && user.getDriverObj().getDriverRate().length() != 0) {
                    ratingBar.setRating(Float.parseFloat(user.getDriverObj()
                            .getDriverRate()) / 2);
                } else {
                    ratingBar.setRating(0);
                }
            }
            if (user.getShopObj() != null) {
                if (user.getShopObj().getRate() != null && user.getShopObj().getRate().length() != 0) {
                    ratingBar.setRating(Float.parseFloat(user.getShopObj()
                            .getRate()) / 2);
                } else {
                    ratingBar.setRating(0);
                }
            }

        }

        String point = getResources().getString(R.string.message_point);
        txtPoint.setText(point + " " + globalValue.getUser().getBalance());
        aq.id(imgAvartar).image(globalValue.getUser().getLinkImage());
        // Check user permission
        // TODO: 09/01/2019 commented by Kevin
//        if (globalValue.getUser().getDriverObj().getIsActive() == null) {
//            preferencesManager.setIsUser();
//            preferencesManager.setDriverIsUnActive();
//        } else {
//            if (globalValue.getUser().getDriverObj().getIsActive().equals("0")) {
//                preferencesManager.setIsDriver();
//                preferencesManager.setDriverIsUnActive();
//            } else {
//                preferencesManager.setIsDriver();
//                preferencesManager.setDriverIsActive();
//            }
//        }
        setMenuByUserType();
    }

    @Override
    public void onResume() {
        super.onResume();
        getDataUser();
//        ((PassengerPage1Fragment) fragments[PASSENGER_PAGE1]).back();
        preferencesManager.setStartWithOutMain(false);
        preferencesManager.setPassengerWaitConfirm(false);
        txtPoint.setText("Point: "
                + GlobalValue.getInstance().getUser().getBalance());
        showHistoryTripIfNotify();

    }

    public int getSquareCropDimensionForBitmap(Bitmap bitmap) {
        return Math.min(bitmap.getWidth(), bitmap.getHeight());
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(self.getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(self);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            InputStream imageStream = null;
            try {
                imageStream = self.getContentResolver().openInputStream(Crop.getOutput(result));
                Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                ((RegisterAsShopFragment) fragments[REGISTER_AS_SHOP]).setImageProfile(
                        yourSelectedImage);
                ((RegisterAsShipperFragment) fragments[REGISTER_AS_SHIPPER]).setImageProfile(
                        yourSelectedImage);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(self, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        if (shareFaceBook) {
            ((ShareFragment) fragments[SHARE]).shareFaceBook(requestCode,
                    resultCode, imageReturnedIntent);
        }
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(imageReturnedIntent.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, imageReturnedIntent);
        }
        switch (requestCode) {
            case REQUEST_IMAGE_GALLERY_IMAGE_ONE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    Bitmap image = null;
                    Bitmap imageConvert = null;
                    try {
                        image = ImageUtil.decodeUri(self, selectedImage);
                        Log.e("lu", "size 1 decode: " + image.getByteCount());
                        int dimension = getSquareCropDimensionForBitmap(image);
                        imageConvert = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    ((RegisterAsShopFragment) fragments[REGISTER_AS_SHOP]).setImageOne(
                            selectedImage, imageConvert);
                    ((RegisterAsShipperFragment) fragments[REGISTER_AS_SHIPPER]).setImageOne(
                            selectedImage, imageConvert);
                }
                break;
            case REQUEST_IMAGE_CAPTURE_IMAGE_ONE:
                if (resultCode == RESULT_OK) {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    int dimension = getSquareCropDimensionForBitmap(imageBitmap);
                    Bitmap imageConvert = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
                    ((RegisterAsShopFragment) fragments[REGISTER_AS_SHOP])
                            .setImageOne(imageConvert);
                    ((RegisterAsShipperFragment) fragments[REGISTER_AS_SHIPPER])
                            .setImageOne(imageConvert);
                }
                break;
            case REQUEST_IMAGE_GALLERY_IMAGE_TWO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    Bitmap image = null;
                    Bitmap imageConvert = null;
                    try {
                        image = ImageUtil.decodeUri(self, selectedImage);
                        int dimension = getSquareCropDimensionForBitmap(image);
                        imageConvert = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                        Log.d("trangpv", "size 2 decode: " + image.getByteCount());

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    ((RegisterAsShopFragment) fragments[REGISTER_AS_SHOP]).setImageTwo(
                            selectedImage, imageConvert);
                    ((RegisterAsShipperFragment) fragments[REGISTER_AS_SHIPPER]).setImageTwo(
                            selectedImage, imageConvert);
                }
                break;
            case REQUEST_IMAGE_CAPTURE_IMAGE_TWO:
                if (resultCode == RESULT_OK) {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageConvert = null;
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    int dimension = getSquareCropDimensionForBitmap(imageBitmap);
                    imageConvert = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
                    ((RegisterAsShopFragment) fragments[REGISTER_AS_SHOP])
                            .setImageTwo(imageConvert);
                    ((RegisterAsShipperFragment) fragments[REGISTER_AS_SHIPPER])
                            .setImageTwo(imageConvert);
                }
                break;
        }
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());

        return output;
    }

    public void choiseImageOne() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.select_photo_from)
                .setPositiveButton(R.string.gallery,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // one can be replaced with any action code
                                Intent pickPhoto = new Intent(
                                        Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(pickPhoto,
                                        REQUEST_IMAGE_GALLERY_IMAGE_ONE);
                            }
                        })
                .setNegativeButton(R.string.camera,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // zero can be replaced with any action code
                                Intent takePictureIntent = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                if (takePictureIntent
                                        .resolveActivity(getPackageManager()) != null) {
                                    startActivityForResult(takePictureIntent,
                                            REQUEST_IMAGE_CAPTURE_IMAGE_ONE);
                                }
                            }
                        }).create().show();
    }

    public void choiseImageTwo() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.select_photo_from)
                .setPositiveButton(R.string.gallery,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // one can be replaced with any action code
                                Intent pickPhoto = new Intent(
                                        Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(pickPhoto,
                                        REQUEST_IMAGE_GALLERY_IMAGE_TWO);
                            }
                        })
                .setNegativeButton(R.string.camera,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // zero can be replaced with any action code
                                Intent takePictureIntent = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                if (takePictureIntent
                                        .resolveActivity(getPackageManager()) != null) {
                                    startActivityForResult(takePictureIntent,
                                            REQUEST_IMAGE_CAPTURE_IMAGE_TWO);
                                }
                            }
                        }).create().show();
    }

    @Override
    public void onLocationChanged(Location location) {
//        updateCoordinate(location.getLatitude() + "", location.getLongitude() + "");
    }


    public void showHistoryTripIfNotify() {
        if (preferencesManager.IsHistoryPush()) {
            showFragment(TRIP_HISTORY);
            menu.showContent(true);
            preferencesManager.setHistoryPush(false);
        }

    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        // Setting Dialog Message
        alertDialog
                .setMessage(getString(R.string.alert_gps));
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        // Showing Alert Message
        alertDialog.show();
    }

//    private OnRequestPermissionListener onRequestPermissionListener;
//
//    public interface OnRequestPermissionListener {
//        void onResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
//    }
//
//    public void setOnRequestPermissionListener(OnRequestPermissionListener onRequestPermissionListener) {
//        this.onRequestPermissionListener = onRequestPermissionListener;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        onRequestPermissionListener.onResult(requestCode, permissions, grantResults);
//    }
}

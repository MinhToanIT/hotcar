package com.app.hotgo.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.adapters.StateAdapter;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.StateObj;
import com.app.hotgo.service.GPSTracker;
import com.app.hotgo.utility.NetworkUtil;
import com.app.hotgo.utility.PermissionUtil;
import com.app.hotgo.widget.CircleImageView;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Administrator on 10/26/2016.
 */
public class SignUpActivity extends BaseActivity implements View.OnClickListener {
    private EditText txtUpdateNameDrive, txtUpdatePhone, txtUpdateEmail, txtPassword, txtUpdateAddress, sp_city, txtUpdatePostCode, txtAccount;
    private Spinner sp_state;
    private CircleImageView imgProfile;
    public static int SELECT_PHOTO = 1000;
    ArrayList<StateObj> listStates;
    private TextView btnSave;
    private Bitmap yourSelectedImage;
    private ImageButton btnBackUpdate;
    private TextView txtTapAvatar;

    private GPSTracker gps;
    private double latitude = 0, longitude = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        gps = new GPSTracker(this);
        initUI();
        initControl();
        initData();
    }

    public void initUI() {
        txtTapAvatar = findViewById(R.id.txtTapAvatar);
        txtUpdateNameDrive = findViewById(R.id.txtUpdateNameDrive);
        txtUpdatePhone = findViewById(R.id.txtUpdatePhone);
        txtAccount = findViewById(R.id.txtAccount);
        txtUpdateEmail = findViewById(R.id.txtUpdateEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtUpdateAddress = findViewById(R.id.txtUpdateAddress);
        sp_city = findViewById(R.id.sp_city);
        txtUpdatePostCode = findViewById(R.id.txtUpdatePostCode);
        sp_state = findViewById(R.id.sp_state);
        imgProfile = findViewById(R.id.imgProfile);
        btnSave = findViewById(R.id.btnSave);
        btnBackUpdate = findViewById(R.id.btnBackUpdate);

    }

    public void initControl() {
        imgProfile.setOnClickListener(this);
        txtTapAvatar.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnBackUpdate.setOnClickListener(this);
    }

    public void initData() {
        setDateStates();
    }

    public boolean validate() {

        if (yourSelectedImage == null) {
            Toast.makeText(SignUpActivity.this, getString(R.string.lblValidateImage), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtUpdateNameDrive.getText().toString().equals("")) {
            txtUpdateNameDrive.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_name), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtUpdatePhone.getText().toString().equals("")) {
            txtUpdatePhone.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_phone), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtUpdateEmail.getText().toString().equals("")) {
            txtUpdateEmail.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_email), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isValidEmailAddress(txtUpdateEmail.getText().toString())) {
            txtUpdateEmail.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_email2), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtPassword.getText().toString().equals("")) {
            txtPassword.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_password), Toast.LENGTH_SHORT).show();
            return false;

        }
        if (txtUpdateAddress.getText().toString().equals("")) {
            txtUpdateAddress.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_address), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtUpdatePostCode.getText().toString().equals("")) {
            txtUpdatePostCode.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_postcode), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public void sendData() {
        if (validate()) {
            ModelManager.registerAccount(this, txtUpdateNameDrive.getText().toString(), txtUpdatePhone.getText().toString(), txtUpdateEmail.getText().toString(),
                    txtPassword.getText().toString(), txtUpdateAddress.getText().toString(), listStates.get(sp_state.getSelectedItemPosition()).getStateId(), sp_city.getText().toString(), txtUpdatePostCode.getText().toString(), txtAccount.getText().toString(),
                    yourSelectedImage, true, new ModelManagerListener() {
                        @Override
                        public void onError() {

                        }

                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                login();
//                                finish();
                            }
                            Toast.makeText(SignUpActivity.this, ParseJsonUtil.getMessage(json), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }


    public void setDateStates() {
        if (NetworkUtil.checkNetworkAvailable(getBaseContext())) {
            ModelManager.getAllSates(this, true, new ModelManagerListener() {
                @Override
                public void onError() {
                    Toast.makeText(self, getString(R.string.loadding_state_error), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String json) {
                    listStates = new ArrayList<StateObj>();
                    listStates = ParseJsonUtil.parseListStates(json);
                    StateAdapter adapter = new StateAdapter(SignUpActivity.this, listStates);
                    sp_state.setAdapter(adapter);
                }
            });
        } else {
            Toast.makeText(self, getString(R.string.no_intenet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgProfile:
            case R.id.txtTapAvatar:
                if (PermissionUtil.checkReadWriteStoragePermission(SignUpActivity.this)) {
                    Crop.pickImage(SignUpActivity.this);
                }
                break;
            case R.id.btnSave:
                sendData();
                break;
            case R.id.btnBackUpdate:
                finish();
                break;
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(Crop.getOutput(result));
                yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                AQuery aQuery = new AQuery(this);
                aQuery.id(R.id.imgProfile).image(yourSelectedImage);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void login() {
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String gcmId = instanceIdResult.getToken();
                    loginAccount(txtUpdateEmail.getText().toString(), txtPassword.getText().toString(), gcmId, gcmId, "1", latitude, longitude);
                }
            });
            FirebaseInstanceId.getInstance().getInstanceId().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: " + e.toString());
                }
            });
        }
    }

    public void loginAccount(final String email, final String password, final String gcm_id, final String ime, final String type, final double lat, final double longitude) {
        ModelManager.loginAccount(this, email, password, gcm_id, ime, type, lat, longitude, true, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {
                if (ParseJsonUtil.isSuccess(json)) {
                    // Check is user or driver
                    // TODO: 09/01/2019 commented by Kevin
                    // Set Login
                    preferencesManager.setLogin();
                    // Set Token
                    preferencesManager.setToken(ParseJsonUtil
                            .getTokenFromLogin(json));
                    // Set User Id
                    preferencesManager.setUserID(ParseJsonUtil
                            .getIdFromLogin(json));
                    // gotoActivity(MainActivity.class);
                    gotoActivity(SplashActivity.class);
                    finish();
                } else {
                    Toast.makeText(self, ParseJsonUtil.getMessage(json), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(imageReturnedIntent.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, imageReturnedIntent);
        }
//        switch (requestCode) {
//            case 1000:
//                if (resultCode == RESULT_OK) {
//                    Uri selectedImage = imageReturnedIntent.getData();
//                    InputStream imageStream = null;
//                    try {
//                        imageStream = getContentResolver().openInputStream(selectedImage);
//                        yourSelectedImage = BitmapFactory.decodeStream(imageStream);
//                        AQuery aQuery = new AQuery(this);
//                        aQuery.id(R.id.imgProfile).image(yourSelectedImage);
//
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//        }
    }


}

package com.app.hotgo.activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.StateObj;
import com.app.hotgo.object.User;
import com.app.hotgo.utility.ImageUtil;
import com.app.hotgo.utility.PermissionUtil;
import com.app.hotgo.widget.CircleImageView;
import com.app.hotgo.widget.TextViewPixeden;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import static android.provider.Settings.System.AIRPLANE_MODE_ON;

public class EditProfileFirstActivity extends BaseActivity implements
        OnClickListener {
    CircleImageView imgPhoto;
    EditText txtUpdateNameDriver, txtUpdatePhone, txtUpdateEmail,txtUpdateAddress;

    TextView btnSave;
    ImageButton btnBack;
    ImageView imgphoto1, imgphoto2;
    LinearLayout imgCar, llPhone;
    TextView txtTapAvatar;

    User user;
    AQuery lstAq;
    ArrayList<StateObj> listStates;

    private Bitmap imgPhoto1, imgPhoto2;
    private File document;
    private PreferencesManager preferencesManager;

    public final int REQUEST_IMAGE_GALLERY_IMAGE_ONE = 0;
    public final int REQUEST_IMAGE_CAPTURE_IMAGE_ONE = 1;
    public final int REQUEST_IMAGE_GALLERY_IMAGE_TWO = 2;
    public final int REQUEST_IMAGE_CAPTURE_IMAGE_TWO = 3;

    private TextView tvPickOnMap;
    private EditText edtDescription, edtTypeVehicle, edtVehiclePlate;
    private LinearLayout layoutVehicleInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_updateprofile);
        preferencesManager = PreferencesManager.getInstance(context);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getDataFromGlobal();
        init();
        initControl();
        setDataProfile();
    }

    static boolean isAirplaneModeOn(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        return Settings.System.getInt(contentResolver, AIRPLANE_MODE_ON, 0) != 0;
    }

    private void initControl() {

        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        imgphoto1.setOnClickListener(this);
        imgphoto2.setOnClickListener(this);
    }

    private void getDataFromGlobal() {
        user = globalValue.getUser();
    }

//    public void setDateStates() {
//        if (NetworkUtil.checkNetworkAvailable(getBaseContext())) {
//            ModelManager.getAllSates(this, true, new ModelManagerListener() {
//                @Override
//                public void onError() {
//                    Toast.makeText(self, getString(R.string.loadding_state_error), Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onSuccess(String json) {
//                    listStates = new ArrayList<StateObj>();
//                    listStates = ParseJsonUtil.parseListStates(json);
//                    StateAdapter adapter = new StateAdapter(EditProfileFirstActivity.this, listStates);
//                    sp_state.setAdapter(adapter);
//                    if (user.getStateId() != null && !user.getStateId().equals("")) {
//                        for (int i = 0; i < listStates.size(); i++) {
//                            if (user.getStateId().equals(listStates.get(i).getStateId())) {
//                                sp_state.setSelection(i, true);
////                            setDataCityUser(listStates.get(i).getStateId(), user.getCityId());
//                            }
//                        }
//                    }
//
//                }
//            });
//        } else {
//            Toast.makeText(self, getString(R.string.no_intenet), Toast.LENGTH_SHORT).show();
//        }
//    }

    private void init() {

        btnBack = (ImageButton) findViewById(R.id.btnBackUpdate);
        btnBack.setVisibility(View.GONE);
        btnSave = (TextViewPixeden) findViewById(R.id.btnSave);

        txtUpdateNameDriver = findViewById(R.id.txtUpdateNameDrive);
        txtUpdatePhone = findViewById(R.id.txtUpdatePhone);
        txtUpdateEmail = findViewById(R.id.txtUpdateEmail);
        txtUpdateAddress = findViewById(R.id.txtUpdateAddress);
        txtTapAvatar = findViewById(R.id.txtTapAvatar);
        imgphoto1 = findViewById(R.id.imgphoto1);
        imgphoto2 = findViewById(R.id.imgphoto2);
        imgCar = findViewById(R.id.imgCar);
        llPhone = findViewById(R.id.llPhone);

        imgPhoto = findViewById(R.id.imgProfile);

        layoutVehicleInfo = findViewById(R.id.layout_vehicle_info);
        tvPickOnMap = findViewById(R.id.tv_pick_on_map);
        edtDescription = findViewById(R.id.edt_description);
        edtTypeVehicle = findViewById(R.id.edt_type_vehicle);
        edtVehiclePlate = findViewById(R.id.edt_vehicle_plate);
        typeUser();

    }

    public void typeUser() {
        if (user.getTypeTasker().equals(Constant.TYPE_TASKER_ROLE_SHIPER)) {
            if (user.getDriverObj().getIsActive() == null
                    || user.getDriverObj()
                    .getIsActive().equals("0")) {
                imgCar.setVisibility(View.GONE);
            } else {
                preferencesManager.setIsDriver(true);
                preferencesManager.setDriverIsActive();
                preferencesManager.setIsShop(false);
                preferencesManager.setShopIsActive(false);
                imgCar.setVisibility(View.VISIBLE);

            }
        } else if (GlobalValue.getInstance().getUser().getTypeTasker().equals(Constant.TYPE_TASKER_ROLE_SELLER)) {
            if (user.getShopObj().getIsActive() == null
                    || user.getShopObj()
                    .getIsActive().equals("0")) {
                imgCar.setVisibility(View.GONE);
            } else {
                preferencesManager.setIsShop(true);
                preferencesManager.setShopIsActive(true);
                preferencesManager.setIsDriver(false);
                preferencesManager.setDriverIsUnActive();
                imgCar.setVisibility(View.VISIBLE);

            }
        } else {
            imgCar.setVisibility(View.GONE);
            tvPickOnMap.setVisibility(View.GONE);
            layoutVehicleInfo.setVisibility(View.GONE);
        }
    }

    // set info data Profile
    private void setDataProfile() {
        Log.e("user", "user:" + user.getLinkImage());
        if (user.getTypeAccount().equals("0")) {
            txtTapAvatar.setVisibility(View.VISIBLE);
        } else {
            txtTapAvatar.setVisibility(View.GONE);
        }
        lstAq = new AQuery(self);
        Picasso.with(EditProfileFirstActivity.this).load(user.getLinkImage()).into(imgPhoto);
        txtUpdateNameDriver.setText(user.getFullName());
        txtUpdateEmail.setText(user.getEmail());
        txtUpdatePhone.setText(user.getPhone());
        txtUpdateNameDriver.setEnabled(false);
        txtUpdateEmail.setEnabled(false);

        lstAq.id(imgphoto1).image(user.getCarObj().getImageone());
        lstAq.id(imgphoto2).image(user.getCarObj().getImagetwo());

    }

    // ==================Update profile================
    private boolean validteUser() {
        if (txtUpdatePhone.getText().toString().isEmpty()) {
            txtUpdatePhone.requestFocus();
            Toast.makeText(this, getString(R.string.message_phone), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void updateUser() {
        if (validteUser()) {
//            user = new User();
            user.setPhone(txtUpdatePhone.getText().toString());
            user.setAddress(txtUpdateAddress.getText().toString().trim());
            user.setDescription(edtDescription.getText().toString().trim());
            user.setStateName("");
            user.setStateId("");
            ModelManager.updateProfileSocial(PreferencesManager.getInstance(
                    self).getToken(), self, true, user.getPhone(), "",
                    user.getDescription(), user.getAddress(), user.getStateId(), user.getCityName(),
                    new ModelManagerListener() {

                        @Override
                        public void onError() {
                            showToastMessage(getResources().getString(
                                    R.string.message_have_some_error));
                        }

                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                Intent intent = new Intent(EditProfileFirstActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                globalValue.setUser(user);
                            } else {
                                showToastMessage(ParseJsonUtil.getMessage(json));
                            }
                        }
                    });
        }
    }

    // =========================================================================================================
    // OnClick
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBackUpdate:
                onBackPressed();
                break;
            case R.id.btnSave:
                if (preferencesManager.isUser()) {
                    updateUser();
                } else {
                    if (Integer.parseInt(user.getDriverObj().getUpdatePending()) == 1) {
                        showToastMessage(R.string.message_update_driver);
                    } else {
                        updateDriver();
                    }
                }
                break;
            case R.id.imgphoto1:
                choiseImageOne();
                break;
            case R.id.imgphoto2:
                choiseImageTwo();
                break;
            default:
                break;
        }
    }

    /* All Update Profile Driver Car */
    // =======================================================================================================
    public void setImgPhoto1(Uri selectedImage, Bitmap image) {
        imgPhoto1 = image;
        imgphoto1.setImageURI(selectedImage);
    }

    public void setImgPhoto1(Bitmap imageBitmap) {
        imgPhoto1 = imageBitmap;
        imgphoto1.setImageBitmap(imageBitmap);
    }

    public void setImgPhoto2(Uri selectedImage, Bitmap image) {
        imgPhoto2 = image;
        imgphoto2.setImageURI(selectedImage);
    }

    public void setImgPhoto2(Bitmap imageBitmap) {
        imgPhoto2 = imageBitmap;
        imgphoto2.setImageBitmap(imageBitmap);
    }

    public void updateDriver() {
        if (validate()) {
            if (imgPhoto1 == null || imgPhoto2 == null) {
                showToastMessage("Please, choise new images");
            } else {
                user.setPhone(txtUpdatePhone.getText().toString());
                user.setStateName("");
                user.setStateId("");
                ModelManager.updateProfileDriverSocial(PreferencesManager
                                .getInstance(self).getToken(), user.getCarObj()
                                .getCarPlate(), user.getCarObj().getBrand(), user
                                .getCarObj().getModel(), user.getCarObj().getYear(),
                        user.getCarObj().getStatus(), user.getPhone(), user.getStateId(), user.getCityName(),
                        imgPhoto1, imgPhoto2, document, self, true,
                        new ModelManagerListener() {

                            @Override
                            public void onError() {
                                showToastMessage(getResources().getString(
                                        R.string.message_have_some_error));
                            }

                            @Override
                            public void onSuccess(String json) {
                                if (ParseJsonUtil.isSuccess(json)) {
                                    Intent intent = new Intent(EditProfileFirstActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                    globalValue.setUser(user);
                                } else {
                                    showToastMessage(ParseJsonUtil.getMessage(json));
                                }
                            }
                        });
            }
        }
    }

    protected void showToast(String message) {
        Toast.makeText(EditProfileFirstActivity.this, message, Toast.LENGTH_LONG).show();
    }

    // ================ VALIDATE===========================
    public boolean validate() {
        if (listStates.size() <= 0) {
            Toast.makeText(self, getString(R.string.messsage_state_empty), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (txtUpdatePhone.getText().toString().isEmpty()) {
            txtUpdatePhone.requestFocus();
            showToast(getString(R.string.message_phone));
            return false;
        }
        return true;
    }

    // ====================choise image========================================
    public void choiseImageOne() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.select_photo_from)
                .setPositiveButton(R.string.gallery,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // one can be replaced with any action code
                                if (PermissionUtil.checkReadWriteStoragePermission(EditProfileFirstActivity.this)) {
                                    Intent pickPhoto = new Intent(
                                            Intent.ACTION_PICK,
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(pickPhoto,
                                            REQUEST_IMAGE_GALLERY_IMAGE_ONE);
                                }
                            }
                        })
                .setNegativeButton(R.string.camera,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // zero can be replaced with any action code
                                if (PermissionUtil.checkCameraPermission(EditProfileFirstActivity.this)) {
                                    Intent takePictureIntent = new Intent(
                                            MediaStore.ACTION_IMAGE_CAPTURE);
                                    if (takePictureIntent
                                            .resolveActivity(getPackageManager()) != null) {
                                        startActivityForResult(takePictureIntent,
                                                REQUEST_IMAGE_CAPTURE_IMAGE_ONE);
                                    }
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
                                if (PermissionUtil.checkReadWriteStoragePermission(EditProfileFirstActivity.this)) {
                                    Intent pickPhoto = new Intent(
                                            Intent.ACTION_PICK,
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(pickPhoto,
                                            REQUEST_IMAGE_GALLERY_IMAGE_TWO);
                                }
                            }
                        })
                .setNegativeButton(R.string.camera,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // zero can be replaced with any action code
                                if (PermissionUtil.checkCameraPermission(EditProfileFirstActivity.this)) {
                                    Intent takePictureIntent = new Intent(
                                            MediaStore.ACTION_IMAGE_CAPTURE);
                                    if (takePictureIntent
                                            .resolveActivity(getPackageManager()) != null) {
                                        startActivityForResult(takePictureIntent,
                                                REQUEST_IMAGE_CAPTURE_IMAGE_TWO);
                                    }
                                }
                            }
                        }).create().show();
    }


    // ================================================================================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case REQUEST_IMAGE_GALLERY_IMAGE_ONE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    try {
                        setImgPhoto1(ImageUtil.decodeUri(self, selectedImage));
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_IMAGE_CAPTURE_IMAGE_ONE:
                if (resultCode == RESULT_OK) {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    setImgPhoto1(imageBitmap);
                }
                break;
            case REQUEST_IMAGE_GALLERY_IMAGE_TWO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    try {
                        setImgPhoto2(ImageUtil.decodeUri(self, selectedImage));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_IMAGE_CAPTURE_IMAGE_TWO:
                if (resultCode == RESULT_OK) {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    setImgPhoto2(imageBitmap);
                }
                break;
        }
    }
}

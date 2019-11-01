package com.app.hotgo.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.network.ProgressDialog;
import com.app.hotgo.object.User;
import com.app.hotgo.utility.ImageUtil;
import com.app.hotgo.utility.PermissionUtil;
import com.app.hotgo.widget.CircleImageView;
import com.app.hotgo.widget.TextViewPixeden;
import com.joooonho.SelectableRoundedImageView;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateProFileActivity extends BaseActivity implements
        OnClickListener {
    CircleImageView imgPhoto;
    EditText txtUpdateNameDriver, txtUpdatePhone,
            txtUpdateAddress, txtUpdateEmail;
    TextView txtTapAvatar;

    TextView btnSave;
    ImageButton btnBack;
    SelectableRoundedImageView imgphoto1, imgphoto2;
    LinearLayout imgCar, llPhone;

    User user;
    AQuery lstAq;

    private Bitmap imgPhoto1, imgPhoto2;
    private File document;
    private PreferencesManager preferencesManager;

    private static final int REQUEST_CODE_GET_LOCATION = 1001;
    public final int REQUEST_IMAGE_GALLERY_IMAGE_ONE = 0;
    public final int REQUEST_IMAGE_CAPTURE_IMAGE_ONE = 1;
    public final int REQUEST_IMAGE_GALLERY_IMAGE_TWO = 2;
    public final int REQUEST_IMAGE_CAPTURE_IMAGE_TWO = 3;
    private Bitmap yourSelectedImage;
    private String image = "", typeCar = "1";

    private TextView tvPickOnMap;
    private EditText edtDescription, edtTypeVehicle, edtVehiclePlate;
    private LinearLayout layoutVehicleInfo;

    private String lat, longitude;
    private File imageExtra, imageExtra2, avatar;
    private ProgressDialog pDialog;
    private LatLng latLng;
    private String rootFolder;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_updateprofile);
        rootFolder = Environment.getExternalStorageDirectory() + "/"
                + self.getString(R.string.app_name) + "/images/";

        preferencesManager = PreferencesManager.getInstance(context);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        init();
        initControl();
        getDataFromGlobal();
        setDataProfile();
    }


    private void initControl() {
        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        imgphoto1.setOnClickListener(this);
        imgphoto2.setOnClickListener(this);
        imgPhoto.setOnClickListener(this);
        tvPickOnMap.setOnClickListener(this);
        txtTapAvatar.setOnClickListener(this);
    }


    private void getDataFromGlobal() {
        user = GlobalValue.getInstance().user;

        typeUser();
    }


    private void init() {

        btnBack = findViewById(R.id.btnBackUpdate);
        btnSave = (TextViewPixeden) findViewById(R.id.btnSave);

        txtUpdateNameDriver = findViewById(R.id.txtUpdateNameDrive);
        txtUpdatePhone = findViewById(R.id.txtUpdatePhone);
        txtUpdateAddress = findViewById(R.id.txtUpdateAddress);
        txtUpdateEmail = findViewById(R.id.txtUpdateEmail);
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

        if (pDialog == null) {
            pDialog = new ProgressDialog(self);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setCancelable(false);
        }
    }

    public void typeUser() {
        if (preferencesManager.isDriver()) {
            if (user.getDriverObj().getIsActive() == null
                    || user.getDriverObj()
                    .getIsActive().equals("0")) {
                imgCar.setVisibility(View.GONE);
                layoutVehicleInfo.setVisibility(View.GONE);
                tvPickOnMap.setVisibility(View.GONE);
            } else {
                preferencesManager.setIsDriver(true);
                preferencesManager.setDriverIsActive();
                imgCar.setVisibility(View.VISIBLE);
                layoutVehicleInfo.setVisibility(View.VISIBLE);
                tvPickOnMap.setVisibility(View.GONE);

            }
        } else if (preferencesManager.isShop()) {
            if (user.getShopObj().getIsActive() == null
                    || user.getShopObj()
                    .getIsActive().equals("0")) {
                imgCar.setVisibility(View.GONE);
                layoutVehicleInfo.setVisibility(View.GONE);
                tvPickOnMap.setVisibility(View.GONE);
            } else {
                preferencesManager.setIsShop(true);
                preferencesManager.setShopIsActive(true);
                imgCar.setVisibility(View.VISIBLE);
                layoutVehicleInfo.setVisibility(View.VISIBLE);
                tvPickOnMap.setVisibility(View.VISIBLE);

            }
        } else {
            imgCar.setVisibility(View.GONE);
            tvPickOnMap.setVisibility(View.GONE);
            layoutVehicleInfo.setVisibility(View.GONE);
        }
    }

    // set info data Profile
    private void setDataProfile() {
        txtUpdateNameDriver.setText(user.getFullName());
        txtUpdateEmail.setText(user.getEmail());
        txtUpdatePhone.setText(user.getPhone());
        txtUpdateAddress.setText(user.getAddress());
        edtDescription.setText(user.getDescription());

        if (preferencesManager.isShop()) {
            lat = user.getShopObj().getLatitude();
            longitude = user.getShopObj().getLongitude();
            latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(longitude));
        }

        if (user.getTypeAccount().equals(Constant.TYPE_ACCOUNT_NORMAL)) {
            txtTapAvatar.setVisibility(View.VISIBLE);
        } else {
            txtTapAvatar.setVisibility(View.GONE);
        }
        if (user.getTypeAccount().equals(Constant.TYPE_ACCOUNT_NORMAL)) {
            txtUpdateNameDriver.setEnabled(true);
        } else {
            txtUpdateNameDriver.setEnabled(false);
        }
        txtUpdateEmail.setEnabled(false);

        lstAq = new AQuery(self);
        lstAq.id(imgPhoto).image(user.getLinkImage());

        if (preferencesManager.isDriver()) {
            edtTypeVehicle.setText(user.getCarObj().getVehicleType());
            edtVehiclePlate.setText(user.getCarObj().getVehiclePlate());
            lstAq.id(imgphoto1).image(user.getCarObj().getImageone());
            lstAq.id(imgphoto2).image(user.getCarObj().getImagetwo());
        } else if (preferencesManager.isShop()) {
            Glide.with(self).load(user.getShopObj().getImageExtra()).into(imgphoto1);
            Glide.with(self).load(user.getShopObj().getImageExtra2()).into(imgphoto2);
            edtTypeVehicle.setText(user.getShopObj().getCarType());
            edtVehiclePlate.setText(user.getShopObj().getCarPlate());
        }

    }

    // ==================Update profile================
    private boolean validateUser() {
        if (txtUpdatePhone.getText().toString().isEmpty()) {
            txtUpdatePhone.requestFocus();
            showToast(getString(R.string.message_phone));
            return false;
        }
        if (txtUpdateAddress.getText().toString().isEmpty()) {
            txtUpdateAddress.requestFocus();
            showToast(getString(R.string.message_address));
            return false;
        }
        if (txtUpdateNameDriver.getText().toString().isEmpty()) {
            txtUpdateNameDriver.requestFocus();
            showToast(getString(R.string.message_name));
            return false;
        }

        return true;
    }

    private boolean validateShop() {
        if (txtUpdateNameDriver.getText().toString().isEmpty()) {
            txtUpdateNameDriver.requestFocus();
            showToast(getString(R.string.please_enter_name));
            return false;
        }

        if (txtUpdatePhone.getText().toString().isEmpty()) {
            txtUpdatePhone.requestFocus();
            showToast(getString(R.string.message_phone));
            return false;
        }


        if (edtTypeVehicle.getText().toString().trim().isEmpty()) {
            edtTypeVehicle.requestFocus();
            showToast(getResources().getString(R.string.please_enter_type_vehicle));
            return false;
        }
        if (edtVehiclePlate.getText().toString().trim().isEmpty()) {
            edtVehiclePlate.requestFocus();
            showToast(getResources().getString(R.string.please_enter_vehicle_plate));
            return false;
        }

        return true;
    }

    private boolean validateShipper() {
        if (txtUpdateNameDriver.getText().toString().isEmpty()) {
            txtUpdateNameDriver.requestFocus();
            showToast(getString(R.string.please_enter_name));
            return false;
        }

        if (txtUpdatePhone.getText().toString().isEmpty()) {
            txtUpdatePhone.requestFocus();
            showToast(getString(R.string.message_phone));
            return false;
        }
        if (txtUpdateAddress.getText().toString().isEmpty()) {
            txtUpdateAddress.requestFocus();
            showToast(getString(R.string.message_address));
            return false;
        }
        if (edtDescription.getText().toString().isEmpty()) {
            edtDescription.requestFocus();
            showToast(getString(R.string.please_enter_description));
            return false;
        }
        if (edtTypeVehicle.getText().toString().trim().isEmpty()) {
            edtTypeVehicle.requestFocus();
            showToast(getResources().getString(R.string.please_enter_type_vehicle));
            return false;
        }
        if (edtVehiclePlate.getText().toString().trim().isEmpty()) {
            edtVehiclePlate.requestFocus();
            showToast(getResources().getString(R.string.please_enter_vehicle_plate));
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

    public void updateUser() {
        if (validateUser()) {
            user = new User();
            user.setAddress(txtUpdateAddress.getText().toString());
            user.setFullName(txtUpdateNameDriver.getText().toString());
            user.setDescription(edtDescription.getText().toString());
            user.setPhone(txtUpdatePhone.getText().toString());
            user.setAddress(txtUpdateAddress.getText().toString());
            if (edtDescription.getText().toString().equals("")) {
                user.setDescription(" ");
            } else {
                user.setDescription(edtDescription.getText().toString());
            }
            if (yourSelectedImage != null) {
                image = convertBitmapListToArrayJson(yourSelectedImage);
            } else {
                image = "";
            }
            ModelManager.updateProfile(PreferencesManager.getInstance(
                    self).getToken(), self, true, user.getPhone(),
                    user.getDescription(), user.getAddress(), "", "", user.getFullName(), "1", "", image,
                    new ModelManagerListener() {

                        @Override
                        public void onError() {
                            showToastMessage(getResources().getString(
                                    R.string.message_have_some_error));
                        }

                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                onBackPressed();
                            } else {
                                showToastMessage(ParseJsonUtil.getMessage(json));
                            }
                        }
                    });
        }
    }

    private void asynUpdateShop() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                updateShop();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        };
        asyncTask.execute();
    }

    public void updateShop() {

        user = new User();
        user.setDob(txtUpdateEmail.getText().toString());
//        if(user.getTypeTasker().equals(Constant.TYPE_TASKER_ROLE_SHIPER)){
//            user.getCarObj().setTypeCar(typeCar);
//        }
        user.setFullName(txtUpdateNameDriver.getText().toString().trim());
        user.setPhone(txtUpdatePhone.getText().toString().trim());
        user.getShopObj().setLatitude(lat);
        user.getShopObj().setLatitude(longitude);
        user.setDescription(edtDescription.getText().toString().trim());
        user.setAddress(txtUpdateAddress.getText().toString().trim());
        user.getShopObj().setCarType(edtTypeVehicle.getText().toString().trim());
        user.getShopObj().setCarPlate(edtVehiclePlate.getText().toString().trim());
        ModelManager.updateShop(self, preferencesManager.getToken(), user.getShopObj().getCarType(), user.getShopObj().getCarPlate(),user.getFullName(), user.getPhone(),
                imageExtra, imageExtra2, avatar, new ModelManagerListener() {
                    @Override
                    public void onError() {
                        deleteRecursive(new File(rootFolder));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToastMessage(getResources().getString(
                                        R.string.message_have_some_error));
                            }
                        });

                        pDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(final String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(getResources().getString(R.string.update_profile_success));
                                    onBackPressed();
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(ParseJsonUtil.getMessage(json));
                                }
                            });
                        }
                        pDialog.dismiss();
                        deleteRecursive(new File(rootFolder));
                    }
                });

    }

    private void asynUpdateShipper() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                updateShipper();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        };
        asyncTask.execute();
    }

    public void updateShipper() {

        user = new User();
        user.setDob(txtUpdateEmail.getText().toString());
        user.getShopObj().setLatitude(lat);
        user.getShopObj().setLatitude(longitude);
        user.setDescription(edtDescription.getText().toString().trim());
        user.getCarObj().setVehicleType(edtTypeVehicle.getText().toString().trim());
        user.getCarObj().setVehiclePlate(edtVehiclePlate.getText().toString().trim());

        ModelManager.updateShipper(self, preferencesManager.getToken(), user.getCarObj().getVehicleType(), user.getCarObj().getVehiclePlate(), user.getDescription(),
                imageExtra, imageExtra2, avatar, new ModelManagerListener() {
                    @Override
                    public void onError() {
                        deleteRecursive(new File(rootFolder));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToastMessage(getResources().getString(
                                        R.string.message_have_some_error));
                            }
                        });

                        pDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(final String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onBackPressed();
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(ParseJsonUtil.getMessage(json));
                                }
                            });
                        }
                        pDialog.dismiss();
                        deleteRecursive(new File(rootFolder));
                    }
                });

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
                } else if (preferencesManager.isShop()) {
                    if (validateShop()) {
                        if (pDialog != null)
                            pDialog.show();
                        asynUpdateShop();
                    }

                } else {
//                    if (Integer.parseInt(user.getDriverObj().getUpdatePending()) == 1) {
//                        showToastMessage(R.string.message_update_driver);
//                    } else {
                    if (validateShipper()) {
                        if (pDialog != null)
                            pDialog.show();
                        asynUpdateShipper();
                    }
//                    }
                }
                break;
            case R.id.imgphoto1:
                if (PermissionUtil.checkReadWriteStoragePermission(self))
                    choiseImageOne();
                break;
            case R.id.imgphoto2:
                if (PermissionUtil.checkReadWriteStoragePermission(self))
                    choiseImageTwo();
                break;
            case R.id.imgProfile:
            case R.id.txtTapAvatar:
                if (user.getTypeAccount().equals(Constant.TYPE_ACCOUNT_NORMAL)) {
                    Crop.pickImage(UpdateProFileActivity.this);
                } else {
                    Toast.makeText(UpdateProFileActivity.this, getString(R.string.lblValidateUpdateImage), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_pick_on_map:
                Intent intent = new Intent(self, ChooseExtraLocationActivity.class);
                if (latLng != null)
                    intent.putExtra(Constant.LOCATION, latLng);
                startActivityForResult(intent, REQUEST_CODE_GET_LOCATION);
                break;
            default:
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

                avatar = com.app.hotgo.util.ImageUtil.saveBitmap(self, yourSelectedImage);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /* All Update Profile Driver Car */
    // =======================================================================================================
    public void setImgPhoto1(Uri selectedImage, Bitmap image) {
        imgPhoto1 = image;
        imgphoto1.setImageURI(selectedImage);
    }

    public void setImgPhoto1(Bitmap imageBitmap) {
        int dimension = getSquareCropDimensionForBitmap(imageBitmap);
        Bitmap imageConvert = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
        imgPhoto1 = imageConvert;
        imgphoto1.setImageBitmap(imageConvert);
    }

    public int getSquareCropDimensionForBitmap(Bitmap bitmap) {
        return Math.min(bitmap.getWidth(), bitmap.getHeight());
    }

    public void setImgPhoto2(Uri selectedImage, Bitmap image) {
        imgPhoto2 = image;
        imgphoto2.setImageURI(selectedImage);
    }

    public void setImgPhoto2(Bitmap imageBitmap) {
        int dimension = getSquareCropDimensionForBitmap(imageBitmap);
        Bitmap imageConvert = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
        imgPhoto2 = imageConvert;
        imgphoto2.setImageBitmap(imageConvert);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());

        return output;
    }

    public void updateDriver() {
        if (validate()) {
            user = new User();
            user.setPhone(txtUpdatePhone.getText().toString());
            user.setFullName(txtUpdateNameDriver.getText().toString());
            user.getCarObj().setTypeCar(typeCar);
            if (edtDescription.getText().toString().equals("")) {
                user.setDescription(" ");
            } else {
                user.setDescription(edtDescription.getText().toString());
            }
            user.setAddress(txtUpdateAddress.getText().toString());
            if (yourSelectedImage != null) {
                image = convertBitmapListToArrayJson(yourSelectedImage);
            } else {
                image = "";
            }
            ModelManager.updateProfileDriver(PreferencesManager
                            .getInstance(self).getToken(), user.getCarObj()
                            .getCarPlate(), user.getDriverObj().getIdentity(), user.getCarObj().getBrand(), user
                            .getCarObj().getModel(), user.getCarObj().getYear(),
                    user.getCarObj().getStatus(), user.getPhone(), user.getStateId(), user.getCityName(), user.getAddress(), user.getDescription(), user.getFullName(), user.getCarObj().getTypeCar(), user.getAccount(), image, "1",
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
                                onBackPressed();
                            } else {
                                showToastMessage(ParseJsonUtil.getMessage(json));
                            }
                        }
                    });
        }
//        }
    }


    // ================ VALIDATE===========================
    protected void showToast(String message) {
        Toast.makeText(UpdateProFileActivity.this, message, Toast.LENGTH_LONG).show();
    }

    public boolean validate() {
        if (txtUpdatePhone.getText().toString().isEmpty()) {
            txtUpdatePhone.requestFocus();
            showToast(getString(R.string.message_phone));
            return false;
        }

        if (txtUpdateNameDriver.getText().toString().isEmpty()) {
            txtUpdateNameDriver.requestFocus();
            showToast(getString(R.string.message_name));
            return false;
        }
        // if (imgPhoto1 == null || imgPhoto2 == null) {
        // return false;
        // }
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
                                if (PermissionUtil.checkReadWriteStoragePermission(UpdateProFileActivity.this)) {
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
                                if (PermissionUtil.checkCameraPermission(UpdateProFileActivity.this)) {
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
                                if (PermissionUtil.checkReadWriteStoragePermission(UpdateProFileActivity.this)) {
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
                                if (PermissionUtil.checkCameraPermission(UpdateProFileActivity.this)) {
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
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(imageReturnedIntent.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, imageReturnedIntent);
        }

        if (requestCode == REQUEST_CODE_GET_LOCATION && resultCode == Activity.RESULT_OK) {
            latLng = imageReturnedIntent.getParcelableExtra(Constant.LOCATION);
            String address = imageReturnedIntent.getStringExtra(Constant.ADDRESS);
            if (latLng != null) {
                lat = String.valueOf(latLng.latitude);
                longitude = String.valueOf(latLng.longitude);
                txtUpdateAddress.setText(address);
            }

        }
        switch (requestCode) {
            case REQUEST_IMAGE_GALLERY_IMAGE_ONE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    Bitmap image = null;
                    Bitmap imageConvert = null;
                    try {
                        image = ImageUtil.decodeUri(self, selectedImage);
                        int dimension = getSquareCropDimensionForBitmap(image);
                        imageConvert = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                        imageExtra = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                        setImgPhoto1(imageConvert);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_IMAGE_CAPTURE_IMAGE_ONE:
                if (resultCode == RESULT_OK) {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");

                    int dimension = getSquareCropDimensionForBitmap(imageBitmap);
                    Bitmap imageConvert = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
                    try {
                        imageExtra = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setImgPhoto1(imageConvert);
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
                        imageExtra2 = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                        setImgPhoto2(imageConvert);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_IMAGE_CAPTURE_IMAGE_TWO:
                if (resultCode == RESULT_OK) {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageConvert = null;
                    Bitmap imageBitmap = (Bitmap) extras.get("data");

                    int dimension = getSquareCropDimensionForBitmap(imageBitmap);
                    imageConvert = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
                    try {
                        imageExtra2 = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setImgPhoto2(imageConvert);
                }
                break;
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
        }
    }

    public static String convertBitmapListToArrayJson(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            if (fileOrDirectory.listFiles() != null)
                for (File child : fileOrDirectory.listFiles())
                    deleteRecursive(child);

        fileOrDirectory.delete();
    }
}

package com.app.hotgo.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.android.gms.maps.model.LatLng;
import com.app.hotgo.BaseFragment;
import com.app.hotgo.R;
import com.app.hotgo.activities.ChooseExtraLocationActivity;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.network.ProgressDialog;
import com.app.hotgo.object.CarType;
import com.app.hotgo.object.User;
import com.app.hotgo.utility.ImageUtil;
import com.app.hotgo.utility.PermissionUtil;
import com.app.hotgo.widget.CircleImageView;
import com.joooonho.SelectableRoundedImageView;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RegisterAsShopFragment extends BaseFragment implements OnClickListener {

    private static final String TAG = RegisterAsShopFragment.class.getSimpleName();
    private static final int REQUEST_CODE_GET_LOCATION = 1001;
    public final int REQUEST_IMAGE_GALLERY_IMAGE_ONE = 0;
    public final int REQUEST_IMAGE_CAPTURE_IMAGE_ONE = 1;
    public final int REQUEST_IMAGE_GALLERY_IMAGE_TWO = 2;
    public final int REQUEST_IMAGE_CAPTURE_IMAGE_TWO = 3;

    private SelectableRoundedImageView imgPhotoOne, imgPhotoTwo;
    private Bitmap imageOne, imageTwo;

    private EditText edtDescription, edtAddress;
    private TextView tvShipperName, tvPhoneNumber, tvEmail;
    private TextView btnSave;
    private CircleImageView imgProfile;

    private User user;
    private AQuery aq;

    /* FOR DATE TIME */
    String typeCar = "1";
    private TextView tvPickOnMap;
    private TextView carType;
    private TextView carPlate;
    private ArrayList<CarType> listCarTypes = new ArrayList<>();
    private String lat, longitude;
    private File imageExtra, imageExtra2;
    private ProgressDialog pDialog;
    private LatLng latLng;
    private String rootFolder;
    private TextView tvBrowser;
    private Bitmap yourSelectedImage;
    private File avatar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstancfeState) {
//        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        View view = inflater.inflate(R.layout.fragment_register_as_shop, container,
                false);
        rootFolder = Environment.getExternalStorageDirectory() + "/"
                + self.getString(R.string.app_name) + "/images/";
        getDataFromGlobal();
        initUI(view);
        initMenuButton(view);
        setHeaderTitle(getResources().getString(R.string.regiseter_as_shop));

        init(view);
        initControl();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            self.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            getData();
        }
    }

    ;

    private void getDataFromGlobal() {
        user = GlobalValue.getInstance().user;
    }

    private void init(View view) {

        tvShipperName = view.findViewById(R.id.tv_shipper_name);
        tvPhoneNumber = view.findViewById(R.id.tv_phone_number);
        tvEmail = view.findViewById(R.id.tv_email);
        edtAddress = view.findViewById(R.id.edt_address);

        edtDescription = view.findViewById(R.id.edt_description);

        imgProfile = view.findViewById(R.id.imgProfile);
        imgPhotoOne = view.findViewById(R.id.imgPhotoOne);
        imgPhotoTwo = view.findViewById(R.id.imgPhotoTwo);
        btnSave = view.findViewById(R.id.btnSave);
        tvPickOnMap = view.findViewById(R.id.tv_pick_on_map);
        tvBrowser = view.findViewById(R.id.tv_browser);
        carType = view.findViewById(R.id.edt_type_vehicle);
        carPlate = view.findViewById(R.id.edt_vehicle_plate);

        if (pDialog == null) {
            pDialog = new ProgressDialog(getActivity());
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setCancelable(false);
        }
    }

    private void initControl() {
        aq = new AQuery(self);
        imgPhotoOne.setOnClickListener(this);
        imgPhotoTwo.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        tvPickOnMap.setOnClickListener(this);
        tvBrowser.setOnClickListener(this);

        /* CREATE DATE FORMAT */

        /* CREATE DAILOG DOB */
    }


    // get data info user
    public void getData() {
        ModelManager.showInfoProfile(
                PreferencesManager.getInstance(mainActivity).getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            GlobalValue.getInstance().setUser(
                                    ParseJsonUtil.parseInfoProfile(json));
                            user = ParseJsonUtil.parseInfoProfile(json);

                            tvShipperName.setText(user.getFullName());
                            tvEmail.setText(user.getEmail());
                            tvPhoneNumber.setText(user.getPhone());
                            edtAddress.setText(user.getAddress());
//                            initDataTypeCar();
                            user.getIsOnline();
                            aq.id(imgProfile).image(user.getLinkImage());
//                            if (user.getDriverObj().getIsActive() == null
//                                    || user.getDriverObj()
//                                    .getIsActive().equals("0")) {
//                            } else {
//                            }
                        } else {
                            showToast(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToast(getResources().getString(
                                R.string.message_have_some_error));

                    }
                });
    }

//    public void initDataTypeCar() {
//        for (int i = 0; i < GlobalValue.getInstance().getListCarTypes().size(); i++) {
//            if (GlobalValue.getInstance().getListCarTypes().get(i).getId() != null && !GlobalValue.getInstance().getListCarTypes().get(i).getId().equals("")) {
//                listCarTypes.add(GlobalValue.getInstance().getListCarTypes().get(i));
//            }
//        }
//        TypeCarAdapter adapter = new TypeCarAdapter(getActivity(), listCarTypes);
//        txtTypeCar.setAdapter(adapter);
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgPhotoOne:
                if (PermissionUtil.checkReadWriteStoragePermission(self))
                    choiseImageOne();
                break;
            case R.id.imgPhotoTwo:
                if (PermissionUtil.checkReadWriteStoragePermission(self))
                    choiseImageTwo();
                break;
            case R.id.btnSave:

                if (validate()) {
                    if (pDialog != null)
                        pDialog.show();
                    asynRegisterShop();
                }

                break;
            case R.id.tv_pick_on_map:
                Intent intent = new Intent(self, ChooseExtraLocationActivity.class);
                if (latLng != null)
                    intent.putExtra(Constant.LOCATION, latLng);
                startActivityForResult(intent, REQUEST_CODE_GET_LOCATION);
                break;
            case R.id.tv_browser:
                if (user.getTypeAccount().equals(Constant.TYPE_ACCOUNT_NORMAL)) {
                    Crop.pickImage(self);
                } else {
                    Toast.makeText(self, getString(R.string.lblValidateUpdateImage), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    public void setImageOne(Uri selectedImage, Bitmap image) {
        imageOne = image;
        imgPhotoOne.setImageURI(selectedImage);
    }

    public void setImageOne(Bitmap imageBitmap) {
        imageOne = imageBitmap;
        imgPhotoOne.setImageBitmap(imageBitmap);
    }

    public void setImageTwo(Uri selectedImage, Bitmap image) {
        imageTwo = image;
        imgPhotoTwo.setImageURI(selectedImage);
    }

    public void setImageTwo(Bitmap imageBitmap) {
        imageTwo = imageBitmap;
        imgPhotoTwo.setImageBitmap(imageBitmap);
    }

    public void setImageProfile(Bitmap imageBitmap) {
        yourSelectedImage = imageBitmap;
        imgProfile.setImageBitmap(imageBitmap);
        try {
            avatar = com.app.hotgo.util.ImageUtil.saveBitmap(self, yourSelectedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void choiseImageOne() {
        new AlertDialog.Builder(self)
                .setTitle(R.string.app_name)
                .setMessage(R.string.select_photo_from)
                .setPositiveButton(R.string.gallery,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // one can be replaced with any action code
                                Intent pickPhoto = new Intent(
                                        Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
                                        .resolveActivity(self.getPackageManager()) != null) {
                                    startActivityForResult(takePictureIntent,
                                            REQUEST_IMAGE_CAPTURE_IMAGE_ONE);
                                }
                            }
                        }).create().show();
    }

    public void choiseImageTwo() {
        new AlertDialog.Builder(self)
                .setTitle(R.string.app_name)
                .setMessage(R.string.select_photo_from)
                .setPositiveButton(R.string.gallery,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // one can be replaced with any action code
                                Intent pickPhoto = new Intent(
                                        Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
                                        .resolveActivity(self.getPackageManager()) != null) {
                                    startActivityForResult(takePictureIntent,
                                            REQUEST_IMAGE_CAPTURE_IMAGE_TWO);
                                }
                            }
                        }).create().show();
    }

    private void asynRegisterShop() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                registerAsShop();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        };
        asyncTask.execute();
    }

    public void registerAsShop() {

        user = new User();
        user.setDob(tvEmail.getText().toString());
        ModelManager.registerAsShop(self, mainActivity.preferencesManager.getToken(), carType.getText().toString().trim(), carPlate.getText().toString().trim(), imageExtra, imageExtra2, avatar, new ModelManagerListener() {
                    @Override
                    public void onError() {
                        deleteRecursive(new File(rootFolder));
                        self.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainActivity.showToastMessage(getResources().getString(
                                        R.string.message_have_some_error));
                            }
                        });

                        pDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(final String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            self.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(getResources().getString(
                                            R.string.message_register_success));

                                    preferencesManager.setIsShop(true);
                                    preferencesManager.setIsUser(false);
                                    if (ParseJsonUtil.getIsActiveAsShop(json).equals(Constant.KEY_IS_ACTIVE)) {
                                        preferencesManager.setShopIsActive(true);
                                    } else {
                                        preferencesManager.setShopIsActive(false);
                                    }
                                    pDialog.dismiss();
                                    getDataProfile();
                                }
                            });


                        } else {
                            self.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(ParseJsonUtil.getMessage(json));
                                }
                            });
                            pDialog.dismiss();
                        }
                        deleteRecursive(new File(rootFolder));
                    }
                });

    }

    public void getDataProfile() {
        ModelManager.showInfoProfile(
                PreferencesManager.getInstance(mainActivity).getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            GlobalValue.getInstance().setUser(
                                    ParseJsonUtil.parseInfoProfile(json));
                            mainActivity.setMenuByUserType();
                            mainActivity.showFragment(mainActivity.PASSENGER_PAGE1);
                        } else {
                            showToast(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToast(getResources().getString(
                                R.string.message_have_some_error));
                    }
                });
    }

    /* VALIDATE */
    public boolean validate() {
        if (edtAddress.getText().toString().trim().isEmpty()) {
            showToast(getResources().getString(R.string.please_enter_address));
            edtAddress.requestFocus();
            return false;
        }
        if (carPlate.getText().toString().trim().isEmpty()) {
            showToast(getResources().getString(R.string.please_enter_vehicle_plate));
            carPlate.requestFocus();
            return false;
        }
        if (carType.getText().toString().trim().isEmpty()) {
            showToast(getResources().getString(R.string.please_enter_type_vehicle));
            carType.requestFocus();
            return false;
        }

        if (imageOne == null || imageTwo == null) {
            showToast(getString(R.string.message_moreImage));
            return false;
        }

        if (imgProfile == null) {
            showToast(getString(R.string.lblValidateImage));
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GET_LOCATION && resultCode == Activity.RESULT_OK) {
            latLng = data.getParcelableExtra(Constant.LOCATION);
            String address = data.getStringExtra(Constant.ADDRESS);
            if (latLng != null) {
                lat = String.valueOf(latLng.latitude);
                longitude = String.valueOf(latLng.longitude);
                edtAddress.setText(address);
                Log.e(TAG, "lat,long: " + lat + "-" + longitude);
            }

        }
        switch (requestCode) {
            case REQUEST_IMAGE_GALLERY_IMAGE_ONE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = data.getData();
                    Bitmap image = null;
                    Bitmap imageConvert = null;
                    try {
                        image = ImageUtil.decodeUri(self, selectedImage);
                        int dimension = getSquareCropDimensionForBitmap(image);
                        imageConvert = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                        imageExtra = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                        setImageOne(imageConvert);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_IMAGE_CAPTURE_IMAGE_ONE:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    int dimension = getSquareCropDimensionForBitmap(imageBitmap);
                    Bitmap imageConvert = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
                    try {
                        imageExtra = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setImageOne(imageConvert);
                }
                break;
            case REQUEST_IMAGE_GALLERY_IMAGE_TWO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = data.getData();
                    Bitmap image = null;
                    Bitmap imageConvert = null;
                    try {
                        image = ImageUtil.decodeUri(self, selectedImage);
                        int dimension = getSquareCropDimensionForBitmap(image);
                        imageConvert = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                        imageExtra2 = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                        setImageTwo(imageConvert);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
            case REQUEST_IMAGE_CAPTURE_IMAGE_TWO:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageConvert = null;
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    int dimension = getSquareCropDimensionForBitmap(imageBitmap);
                    imageConvert = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
                    try {
                        imageExtra2 = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setImageTwo(imageConvert);
                }
                break;
        }
    }

    public int getSquareCropDimensionForBitmap(Bitmap bitmap) {
        return Math.min(bitmap.getWidth(), bitmap.getHeight());
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}

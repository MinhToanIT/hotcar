package com.app.hotgo.fragment;

import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.app.hotgo.BaseFragment;
import com.app.hotgo.R;
import com.app.hotgo.adapters.TypeCarAdapter;
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

import static android.app.Activity.RESULT_OK;

public class RegisterAsShipperFragment extends BaseFragment implements OnClickListener {
    public final int REQUEST_IMAGE_GALLERY_IMAGE_ONE = 0;
    public final int REQUEST_IMAGE_CAPTURE_IMAGE_ONE = 1;
    public final int REQUEST_IMAGE_GALLERY_IMAGE_TWO = 2;
    public final int REQUEST_IMAGE_CAPTURE_IMAGE_TWO = 3;


    private SelectableRoundedImageView imgPhotoOne, imgPhotoTwo;
    private Bitmap imageOne, imageTwo;
    private File document;

    private EditText edtDescription, edtTypeOfVehicle, edtVehiclePlate;
    private TextView tvShipperName, tvPhoneNumber, tvEmail, tvAddress;
    private TextView btnSave;
    private CircleImageView imgProfile;
    private TextView tvBrowser;

    private User user;
    private AQuery aq;

    /* FOR DATE TIME */
    String typeCar = "1";
    private ArrayList<CarType> listCarTypes = new ArrayList<>();

    private File image, image2, avatar;
    private ProgressDialog pDialog;
    private String rootFolder;
    private Bitmap yourSelectedImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstancfeState) {
        View view = inflater.inflate(R.layout.fragment_register_as_shipper, container,
                false);
        rootFolder = Environment.getExternalStorageDirectory() + "/"
                + self.getString(R.string.app_name) + "/images/";
        getDataFromGlobal();
        initUI(view);
        initMenuButton(view);
        setHeaderTitle(getResources()
                .getString(R.string.regiseter_as_shipper));

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
        tvAddress = view.findViewById(R.id.tv_address);

        edtDescription = view.findViewById(R.id.edt_description);
        edtTypeOfVehicle = view.findViewById(R.id.edt_type_vehicle);
        edtVehiclePlate = view.findViewById(R.id.edt_vehicle_plate);

        imgProfile = view.findViewById(R.id.imgProfile);
        imgPhotoOne = view.findViewById(R.id.imgPhotoOne);
        imgPhotoTwo = view.findViewById(R.id.imgPhotoTwo);
//        txtTypeCar = view.findViewById(R.id.txtTypeCar);
        btnSave = view.findViewById(R.id.btnSave);
        tvBrowser = view.findViewById(R.id.tv_browser);

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
        tvBrowser.setOnClickListener(this);

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
                            tvAddress.setText(user.getAddress());
                            initDataTypeCar();
                            user.getIsOnline();
                            aq.id(imgProfile).image(user.getLinkImage());
                            if (user.getDriverObj().getIsActive() == null
                                    || user.getDriverObj()
                                    .getIsActive().equals("0")) {
                            } else {
                            }
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

    public void initDataTypeCar() {
        for (int i = 0; i < GlobalValue.getInstance().getListCarTypes().size(); i++) {
            if (GlobalValue.getInstance().getListCarTypes().get(i).getId() != null && !GlobalValue.getInstance().getListCarTypes().get(i).getId().equals("")) {
                listCarTypes.add(GlobalValue.getInstance().getListCarTypes().get(i));
            }
        }
        TypeCarAdapter adapter = new TypeCarAdapter(getActivity(), listCarTypes);
//        txtTypeCar.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgPhotoOne:
                if (PermissionUtil.checkReadWriteStoragePermission(self))
                    chooseImageOne();
                break;
            case R.id.imgPhotoTwo:
                if (PermissionUtil.checkReadWriteStoragePermission(self))
                    chooseImageTwo();
                break;
            case R.id.btnSave:
                if (validate()) {
                    if (pDialog != null)
                        pDialog.show();
                    asyncRegisterShipper();
                }
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

    public void chooseImageOne() {
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

    public void chooseImageTwo() {
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


    private void asyncRegisterShipper() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                registerAsShipper();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        };
        asyncTask.execute();
    }

    public void registerAsShipper() {

        user = new User();
        user.setDob(tvEmail.getText().toString());
        user.setDescription(edtDescription.getText().toString().trim());
        user.getCarObj().setVehicleType(edtTypeOfVehicle.getText().toString().trim());
        user.getCarObj().setVehiclePlate(edtVehiclePlate.getText().toString().trim());
        ModelManager.registerAsShipper(self, mainActivity.preferencesManager.getToken(), user.getCarObj().getVehicleType(), user.getCarObj().getVehiclePlate(), user.getDescription(),
                image, image2, avatar, new ModelManagerListener() {
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

                                    preferencesManager.setIsDriver(true);
                                    preferencesManager.setIsUser(false);
                                    if (ParseJsonUtil.getIsActiveAsDriver(json).equals(Constant.KEY_IS_ACTIVE)) {
                                        preferencesManager.setDriverIsActive();
                                    } else {
                                        preferencesManager.setDriverIsUnActive();
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
        if (edtVehiclePlate.getText().toString().isEmpty()) {
            edtVehiclePlate.requestFocus();
            showToast(getString(R.string.please_input_vehicle_plate));
            return false;
        }
        if (edtTypeOfVehicle.getText().toString().length() == 0) {
            edtTypeOfVehicle.requestFocus();
            showToast(getString(R.string.please_input_type_of_vehicle));
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

        switch (requestCode) {
            case REQUEST_IMAGE_GALLERY_IMAGE_ONE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    Bitmap image = null;
                    Bitmap imageConvert = null;
                    try {
                        image = ImageUtil.decodeUri(self, selectedImage);
                        int dimension = getSquareCropDimensionForBitmap(image);
                        imageConvert = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                        this.image = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                        setImageOne(imageConvert);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_IMAGE_CAPTURE_IMAGE_ONE:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    int dimension = getSquareCropDimensionForBitmap(imageBitmap);
                    Bitmap imageConvert = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
                    try {
                        image = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setImageOne(imageConvert);
                }
                break;
            case REQUEST_IMAGE_GALLERY_IMAGE_TWO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    Bitmap image = null;
                    Bitmap imageConvert = null;
                    try {
                        image = ImageUtil.decodeUri(self, selectedImage);
                        int dimension = getSquareCropDimensionForBitmap(image);
                        imageConvert = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                        image2 = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                        setImageTwo(imageConvert);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
            case REQUEST_IMAGE_CAPTURE_IMAGE_TWO:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageConvert = null;
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    int dimension = getSquareCropDimensionForBitmap(imageBitmap);
                    imageConvert = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
                    try {
                        image2 = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
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

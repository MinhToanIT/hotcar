package com.app.hotgo.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.adapters.CategoryAdapter;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.network.ProgressDialog;
import com.app.hotgo.object.CarType;
import com.app.hotgo.object.User;
import com.app.hotgo.utility.ImageUtil;
import com.app.hotgo.utility.PermissionUtil;
import com.app.hotgo.widget.CircleImageView;

import java.io.File;
import java.io.IOException;

public class NewProductActivity extends BaseActivity implements View.OnClickListener {
    public final int REQUEST_IMAGE_GALLERY_IMAGE_ONE = 0;
    public final int REQUEST_IMAGE_CAPTURE_IMAGE_ONE = 1;

    private ImageButton btnBack;
    private TextView btnSave;
    private CircleImageView ivProduct;
    private TextView btnBrowse;
    private EditText edtTitle, edtDescription, edtPrice, edtSize;
    private LinearLayout layoutStatus, layoutSelectCategory;
    private TextView tvStatus, tvCategory;

    private User user;
    private File imageProduct;
    private ProgressDialog pDialog;
    private String rootFolder;
    private String categoryId = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);
        rootFolder = Environment.getExternalStorageDirectory() + "/"
                + self.getString(R.string.app_name) + "/images/";
        initViews();
        initData();
        initControl();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);
        btnBrowse = findViewById(R.id.btn_browse);

        ivProduct = findViewById(R.id.iv_product);

        edtTitle = findViewById(R.id.edt_title);
        edtDescription = findViewById(R.id.edt_description);
        edtPrice = findViewById(R.id.edt_price);
        edtSize = findViewById(R.id.edt_size);

        layoutStatus = findViewById(R.id.btn_status);
        layoutSelectCategory = findViewById(R.id.btn_select_category);

        tvStatus = findViewById(R.id.tv_status);
        tvCategory = findViewById(R.id.tv_category);

        if (pDialog == null) {
            pDialog = new ProgressDialog(self);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setCancelable(false);
        }
    }

    private void initData() {
        user = GlobalValue.getInstance().getUser();
    }

    private void initControl() {
        btnSave.setOnClickListener(this);
        btnBrowse.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        layoutStatus.setOnClickListener(this);
        layoutSelectCategory.setOnClickListener(this);
    }

    private void asynAddProduct() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                addNewProduct();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        };
        asyncTask.execute();
    }

    public void addNewProduct() {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String prize = edtPrice.getText().toString().trim();
        String status = tvStatus.getText().toString().trim().equals(getResources().getString(R.string.active)) ? "1" : "0";
        String size = edtSize.getText().toString().trim();
        ModelManager.addNewProduct(self, preferencesManager.getToken(), title, categoryId, description, prize, size, status, imageProduct, new ModelManagerListener() {
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
                            showToastMessage(ParseJsonUtil.getMessage(json));
                            onBackPressed();
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToastMessage(ParseJsonUtil.getMessage(json));
                        }
                    });
                }
                pDialog.dismiss();
                deleteRecursive(new File(rootFolder));
            }
        });

    }

    public void chooseProductImage() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.select_photo_from)
                .setPositiveButton(R.string.gallery,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // one can be replaced with any action code
                                if (PermissionUtil.checkReadWriteStoragePermission(self)) {
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
                                if (PermissionUtil.checkCameraPermission(self)) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
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
                        imageProduct = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                        ivProduct.setImageBitmap(imageConvert);
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
                        imageProduct = com.app.hotgo.util.ImageUtil.saveBitmap(self, imageConvert);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ivProduct.setImageBitmap(imageConvert);
                }
                break;
        }
    }

    public int getSquareCropDimensionForBitmap(Bitmap bitmap) {
        return Math.min(bitmap.getWidth(), bitmap.getHeight());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_save:
                if (validate()) {
                    if (pDialog != null)
                        pDialog.show();
                    asynAddProduct();
                }

                break;
            case R.id.btn_browse:
                chooseProductImage();
                break;
            case R.id.btn_status:
                showStatusDialog();
                break;
            case R.id.btn_select_category:
                showCategoryDialog();
                break;
        }
    }

    private boolean validate() {
        if (imageProduct == null) {
            showToastMessage(getResources().getString(R.string.please_choose_your_product_image));
            return false;
        }
        if (edtTitle.getText().toString().trim().length() == 0) {
            showToastMessage(getResources().getString(R.string.please_enter_title));
            edtTitle.requestFocus();
            return false;
        }
        if (edtDescription.getText().toString().trim().length() == 0) {
            showToastMessage(getResources().getString(R.string.please_enter_description));
            edtDescription.requestFocus();
            return false;
        }
        if (edtPrice.getText().toString().trim().length() == 0) {
            showToastMessage(getResources().getString(R.string.please_enter_price));
            edtPrice.requestFocus();
            return false;
        }
        if (categoryId.length() == 0) {
            showToastMessage(getResources().getString(R.string.please_choose_category));
            return false;
        }
        return true;
    }

    private void showStatusDialog() {
        CharSequence options[] = new CharSequence[]{getResources().getString(R.string.active), getResources().getString(R.string.inactive)};
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(self);
        builder.setCancelable(false);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        tvStatus.setText(getResources().getString(R.string.active));
                        break;
                    case 1:
                        tvStatus.setText(getResources().getString(R.string.inactive));
                        break;
                }
            }
        });
        builder.create().show();

    }

    private Dialog mDialog;

    private void showCategoryDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_category, null);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(self);
        builder.setCancelable(false);
        builder.setView(view);

        ListView lvCategory = view.findViewById(R.id.lv_category);
        CategoryAdapter categoryAdapter = new CategoryAdapter(self, GlobalValue.getInstance().getListCarTypes());
        lvCategory.setAdapter(categoryAdapter);

        lvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CarType carType = GlobalValue.getInstance().getListCarTypes().get(position);
                tvCategory.setText(carType.getName());
                categoryId = carType.getId();
                mDialog.dismiss();
            }
        });
        mDialog = builder.create();
        mDialog.show();

    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}

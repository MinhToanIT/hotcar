package com.app.hotgo.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.RequestService;
import com.app.hotgo.adapters.ProductAdapter;
import com.app.hotgo.config.Constant;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.ProductObj;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;

import me.drakeet.materialdialog.MaterialDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class SelectProductActivity extends BaseActivity implements View.OnClickListener, ProductAdapter.OnBuyButtonClickListener {
    private ImageButton ivBack;
    private TextView tvTitle;
    private RecyclerView rcvProduct;
    private SwipyRefreshLayout swipyRefreshLayout;
    private double startLat, startLong, endLat, endLong;
    private String categoryId;
    private String startLocation, endLocation;

    private ArrayList<ProductObj> listProducts = new ArrayList<>();
    private ProductAdapter productAdapter;
    private int currentPage = 1;
    private MaterialDialog dialog, dialogPayment;

    private ImageButton ivCloseFilter;
    private EditText edtProductName, edtShopName, edtMinPrice, edtMaxPrice;
    private MaterialRatingBar ratingBar;
    private DrawerLayout drawerLayout;
    private TextView tvReset, tvApply;
    private ImageView ivSearch;
    private RelativeLayout layoutDrawer;

    private String productName = "", shopName = "", minPrice = "", maxPrice = "", rate = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_product);
        initViews();
        initData();
        initControl();
    }

    private void initViews() {
        ivBack = findViewById(R.id.btnBack);
        ivCloseFilter = findViewById(R.id.iv_close_filter);
        ivSearch = findViewById(R.id.iv_search);

        tvTitle = findViewById(R.id.lblTitle);
        tvTitle.setText(getResources().getString(R.string.select_product));
        tvReset = findViewById(R.id.tv_reset);
        tvReset.setPaintFlags(tvReset.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvApply = findViewById(R.id.tv_apply);

        edtProductName = findViewById(R.id.edt_product_name);
        edtShopName = findViewById(R.id.edt_shop_name);
        edtMinPrice = findViewById(R.id.edt_min_price);
        edtMaxPrice = findViewById(R.id.edt_max_price);

        drawerLayout = findViewById(R.id.drawer_layout);
        layoutDrawer = findViewById(R.id.layout_drawer);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        int wScreen = getResources().getDisplayMetrics().widthPixels;
        //        layoutDrawer.setLayoutParams(new DrawerLayout.LayoutParams(wDrawerLayout, DrawerLayout.LayoutParams.MATCH_PARENT));
        layoutDrawer.getLayoutParams().width = (int) (wScreen * 4 / 5F);
        ratingBar = findViewById(R.id.ratingBar);

        swipyRefreshLayout = findViewById(R.id.swrLayout);
        rcvProduct = findViewById(R.id.rcv_product);
        rcvProduct.setHasFixedSize(true);
        rcvProduct.setLayoutManager(new LinearLayoutManager(self));

    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            startLat = intent.getDoubleExtra(Constant.KEY_STARTLOCATION_LATITUDE, 0);
            startLong = intent.getDoubleExtra(Constant.KEY_STARTLOCATION_LONGITUDE, 0);
            endLat = intent.getDoubleExtra(Constant.KEY_ENDLOCATION_LATITUDE, 0);
            endLong = intent.getDoubleExtra(Constant.KEY_ENDLOCATION_LONGITUDE, 0);
            categoryId = intent.getStringExtra(Constant.KEY_CATEGORY_ID);
            startLocation = intent.getStringExtra(Constant.KEY_ADDRESS_START);
            endLocation = intent.getStringExtra(Constant.KEY_ADDRESS_TO);
        }
        productAdapter = new ProductAdapter(self, listProducts);
        rcvProduct.setAdapter(productAdapter);
        getListProduct(false);
    }

    private void initControl() {
        ivBack.setOnClickListener(this);
        ivCloseFilter.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        tvApply.setOnClickListener(this);
        tvReset.setOnClickListener(this);

        productAdapter.setOnBuyButtonClickListener(this);

        swipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                Log.d(TAG, "Refresh triggered at "
                        + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"));
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    currentPage = 1;
                    listProducts.clear();
                    getListProduct(true);
                } else {
                    getListProduct(true);
                }
            }
        });
    }

    private void getListProduct(final boolean isPull) {
        productName = edtProductName.getText().toString().trim();
        shopName = edtShopName.getText().toString().trim();
        minPrice = edtMinPrice.getText().toString().trim();
        maxPrice = edtMaxPrice.getText().toString().trim();
        rate = String.valueOf(ratingBar.getRating() * 2);

        ModelManager.searchProduct(self, preferencesManager.getToken(), startLat, startLong, endLat, endLong, categoryId,
                productName, shopName, minPrice, maxPrice, rate, currentPage + "", !isPull, new ModelManagerListener() {
                    @Override
                    public void onError() {
                        if (isPull) {
                            swipyRefreshLayout.setRefreshing(false);
                        }
                        showToastMessage(getResources().getString(R.string.message_have_some_error));
                    }

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            if (ParseJsonUtil.parseListProductByCategory(json).size() != 0) {
                                listProducts.addAll(ParseJsonUtil.parseListProductByCategory(json));
                                currentPage++;
                            } else {
                                if (currentPage > 1) {
                                    showToastMessage(getResources().getString(
                                            R.string.message_have_no_more_data));
                                } else {
                                    showToastMessage(getResources().getString(R.string.msg_no_data));
                                }

                            }
                            productAdapter.notifyDataSetChanged();
                        }
                        if (isPull) {
                            swipyRefreshLayout.setRefreshing(false);
                        }
                    }

                });
    }

    public void showDialogCreateRequest(final int productId, final int quantity, final float price, final String distance) {
        String msg = getString(R.string.msgEstimatedFare);

        dialog = new MaterialDialog(self);
        dialog.setPositiveButton(getResources().getString(R.string.book_now), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (globalValue.getUser().getBalance() != null) {
                    if (globalValue.getUser().getBalance() >= price) {
//                        createRequest(productId, quantity, distance);
                    } else {
                        String msg = getString(R.string.msgValidateBalance);
                        msg = msg.replace("[a]", globalValue.getUser().getBalance() + "");
                        msg = msg.replace("[b]", "$" + price + "");
                        showDialogAddPaymentForRequest(msg);
                    }
                }
            }
        }).setNegativeButton(getResources().getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        msg = msg.replace("%.2f", price + "");
        dialog.setMessage(msg);
        dialog.show();
    }

    public void showDialogAddPaymentForRequest(String message) {
        dialogPayment = new MaterialDialog(self);
        dialogPayment.setMessage(message);
        dialogPayment.setPositiveButton(getResources().getString(R.string.add_point), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPayment.dismiss();
                gotoActivity(PaymentPointActivity.class);
            }
        });
        dialogPayment.setNegativeButton(getResources().getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPayment.dismiss();
            }
        });
        dialogPayment.show();
    }

//    private void createRequest(final int productId, final int quantity, final String distance) {
//        ModelManager.createRequest(preferencesManager.getToken(), String.valueOf(productId), String.valueOf(quantity), startLat + "", startLong + "", startLocation,
//                endLat + "", endLong + "", endLocation, distance, self, true, new ModelManagerListener() {
//                    @Override
//                    public void onError() {
//                        showToastMessage(getResources().getString(R.string.message_have_some_error));
//                    }
//
//                    @Override
//                    public void onSuccess(String json) {
//                        if (ParseJsonUtil.isSuccess(json)) {
//                            setDataCreateRequest(String.valueOf(productId), String.valueOf(quantity), distance);
//                            createServiceRequest();
//                            globalValue.setEstimate_fare(ParseJsonUtil.getEstimateFare(json));
//                            preferencesManager.putStringValue("countDriver", ParseJsonUtil.getCountDriver(json));
//                            gotoActivity(WaitDriverConfirmActivity.class);
//                            dialog.dismiss();
//                        } else {
//                            showToastMessage(ParseJsonUtil.getMessage(json));
//                        }
//                    }
//                });
//    }

    public void setDataCreateRequest(String productId, String quantity, String distance) {
        preferencesManager.putStringValue(Constant.KEY_STARTLOCATION_LATITUDE, startLat + "");
        preferencesManager.putStringValue(Constant.KEY_STARTLOCATION_LONGITUDE, startLong + "");
        preferencesManager.putStringValue(Constant.KEY_ENDLOCATION_LATITUDE, endLat + "");
        preferencesManager.putStringValue(Constant.KEY_ENDLOCATION_LONGITUDE, endLong + "");
        preferencesManager.putStringValue(Constant.KEY_ADDRESS_START, startLocation);
        preferencesManager.putStringValue(Constant.KEY_ADDRESS_TO, endLocation);
        preferencesManager.putStringValue(Constant.KEY_QUANTITY, quantity);
        preferencesManager.putStringValue(Constant.KEY_PRODUCT_ID, productId);
        preferencesManager.putStringValue(Constant.KEY_ESTIMATE_DISTANCE,distance);
    }

    public void createServiceRequest() {
        startService(new Intent(self, RequestService.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                finish();
                break;
            case R.id.iv_close_filter:
                if (drawerLayout.isDrawerOpen(Gravity.RIGHT))
                    drawerLayout.closeDrawers();
                break;
            case R.id.tv_reset:
                edtProductName.setText("");
                edtShopName.setText("");
                edtMinPrice.setText("");
                edtMaxPrice.setText("");
                ratingBar.setRating(0);

                currentPage = 1;
                listProducts.clear();
                getListProduct(false);
                if (drawerLayout.isDrawerOpen(Gravity.RIGHT))
                    drawerLayout.closeDrawers();
                break;
            case R.id.tv_apply:
                currentPage = 1;
                listProducts.clear();
                getListProduct(false);
                if (drawerLayout.isDrawerOpen(Gravity.RIGHT))
                    drawerLayout.closeDrawers();
                break;
            case R.id.iv_search:
                drawerLayout.openDrawer(Gravity.RIGHT);
                break;
        }
    }

    @Override
    public void onBuyButtonClick(int position, int quantity, float price, String distance) {
        showDialogCreateRequest(Integer.parseInt(listProducts.get(position).getId()), quantity, price, distance);
    }
}

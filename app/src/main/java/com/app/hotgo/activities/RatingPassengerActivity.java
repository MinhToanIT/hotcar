package com.app.hotgo.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.utility.NetworkUtil;
import com.joooonho.SelectableRoundedImageView;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class RatingPassengerActivity extends BaseActivity {

    private TextView tvName, tvPhone;
    private RatingBar ratingBar, ratingBarUser;
    private TextView btnSend;

    private TextView txtStar;
    private LinearLayout llHelp;
    AQuery aq;
    private String phoneAdmin = "";
    private DatabaseReference ref;

    private TextView tvShopName, tvAddress, tvDestination, tvProductName, tvQuantity, tvPrice, tvStar;
    private TextView tvDistance, tvShippingPrice, tvTotalPrice;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_passenger);
        aq = new AQuery(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        initUI();
        findViews();
        getDriverEarn();
        initControl();

    }

    private void findViews() {
        txtStar = findViewById(R.id.txtStar);

        tvName = findViewById(R.id.lblName);
        tvPhone = findViewById(R.id.lblPhone);
        ratingBar = findViewById(R.id.ratingBar);
        ratingBarUser = findViewById(R.id.ratingBarUser);
        llHelp = findViewById(R.id.llHelp);
        btnSend = findViewById(R.id.btnSend);


        tvDestination = findViewById(R.id.tv_destination);
        tvPrice = findViewById(R.id.tv_price);
        tvDistance = findViewById(R.id.tv_distance);
        tvStar = findViewById(R.id.tv_star);
        tvAddress = findViewById(R.id.tv_PickUp);

        if (globalValue
                .getCurrentOrder() != null && !globalValue
                .getCurrentOrder()
                .getPassengerRate().equals(""))
            ratingBarUser.setRating(Float
                    .parseFloat(globalValue
                            .getCurrentOrder()
                            .getPassengerRate()) / 2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initControl() {
        btnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ratingBar.getRating() * 2 != 0) {
                    rateCustomer(globalValue.getCurrentOrder().getId(),
                            ratingBar.getRating() * 2 + "");

                    preferencesManager.setDriverCurrentScreen("");
                    preferencesManager.setDriverIsNotInTrip();
                    preferencesManager.setCurrentOrderId("");
                } else {
                    Toast.makeText(self, getString(R.string.lblValidateRate), Toast.LENGTH_SHORT).show();
                }

            }
        });
        llHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtil.checkNetworkAvailable(RatingPassengerActivity.this)) {
                    ModelManager.sendNeedHelp(preferencesManager.getToken(), preferencesManager.getCurrentOrderId(), context, true, new ModelManagerListener() {
                        @Override
                        public void onError() {

                        }

                        @Override
                        public void onSuccess(String json) {
                            Log.e("Success", "Success");
                        }
                    });
                }
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"
                        + phoneAdmin));
                startActivity(callIntent);
            }
        });
    }

    private void initData() {
        ModelManager.getGeneralSettings(preferencesManager.getToken(),
                self, true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            phoneAdmin = ParseJsonUtil
                                    .getPhoneAdmin(json);
                        }
                    }

                    @Override
                    public void onError() {
                    }
                });
        ModelManager.showTripDetail(preferencesManager.getToken(),
                preferencesManager.getCurrentOrderId(), context, true,
                new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        // TODO Auto-generated method stub
                        if (ParseJsonUtil.isSuccess(json)) {
                            globalValue.setCurrentOrder(ParseJsonUtil
                                    .parseCurrentOrder(json));

                            tvDestination.setText(globalValue.getCurrentOrder().getEndLocation());
                            tvAddress.setText(globalValue.getCurrentOrder().getStartLocation());
                            tvPrice.setText(getResources().getString(R.string.lblCurrency) + globalValue.getCurrentOrder().getPrice());
                            tvDistance.setText(globalValue.getCurrentOrder().getDistance() + " km");

                            tvName.setText(globalValue.getCurrentOrder()
                                    .getPassengerName());
                            tvPhone.setText(globalValue.getCurrentOrder().getPassenger_phone(false));
                            tvStar.setText(Float
                                    .parseFloat(globalValue
                                            .getCurrentOrder()
                                            .getDriverRate()) / 2 + "(" + globalValue.getCurrentOrder().getDriverRateCount() + ")");

                            aq.id(R.id.imgPassenger).image(
                                    globalValue.getCurrentOrder().getImagePassenger());
                            if (globalValue.getCurrentOrder()
                                    .getPassengerRate().isEmpty()) {
                                txtStar.setText("0");
                            } else {
                                txtStar.setText("" + Float
                                        .parseFloat(globalValue
                                                .getCurrentOrder()
                                                .getPassengerRate()) / 2);
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

    public String getTime(long startTime, long endTime) {
        DateTime today = new DateTime(endTime * 1000L);
        DateTime yesterday = new DateTime(startTime * 1000L);
        Duration duration = new Duration(yesterday, today);
        long timeInMilliseconds = duration.getStandardSeconds();
        long mins = timeInMilliseconds / 60;
        long hour = mins / 60;
        if (mins < 1) {
            return "0 " + getResources().getString(R.string.minute);
        } else {
            if (hour < 1) {
                return mins + " " + getResources().getString(R.string.minute1);
            } else {
                return hour + " " + getResources().getString(R.string.hour) + mins + " " + getResources().getString(R.string.minute1);
            }
        }
    }

    public String convertLinkToString(String link) {
        for (int i = 0; i < GlobalValue.getInstance().getListCarTypes().size(); i++) {
            if (GlobalValue.getInstance().getListCarTypes().get(i).getId() != null && !GlobalValue.getInstance().getListCarTypes().get(i).getId().equals("")) {
                if (link.equals(GlobalValue.getInstance().getListCarTypes().get(i).getId())) {
                    return GlobalValue.getInstance().getListCarTypes().get(i).getName();
                }
            }

        }
        return link;
    }

    private void rateCustomer(String tripId, String rate) {
        ModelManager.rateCustomer(preferencesManager.getToken(), tripId, rate,
                self, true, new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            preferencesManager.setDriverStatus("1");
                            ref.child("user").child(preferencesManager.getUserID()).child("status").setValue("1");
                            preferencesManager.setDriverCurrentScreen("");
                            preferencesManager.setDriverIsNotInTrip();
                            gotoActivityWithClearTop(RequestPassengerActivity.class);
                            finish();
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

    public void getDriverEarn() {
        ModelManager.getGeneralSettings(preferencesManager.getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            globalValue.setDriver_earn(ParseJsonUtil.getDriverEarn(json));
                            initData();
                        }
                    }

                    @Override
                    public void onError() {
                        showToastMessage(getResources().getString(
                                R.string.message_have_some_error));
                    }
                });
    }

}

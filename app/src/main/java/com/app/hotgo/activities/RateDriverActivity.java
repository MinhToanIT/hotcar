package com.app.hotgo.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.utility.NetworkUtil;
import com.joooonho.SelectableRoundedImageView;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.json.JSONObject;

import me.drakeet.materialdialog.MaterialDialog;

public class RateDriverActivity extends BaseActivity {
    private TextView btnSend, btnRate;
    private TextView lblNorify;
    private String point;
    private TextView lblName;

    private LinearLayout loPayment, llHelp;
    private TextView stepNext, stepCircle;

    private TextView lblPhone, tvStep;
    private RatingBar ratingBar, ratingBarUser;
    private TextView txtStar;

    private SelectableRoundedImageView imgPassenger;
    //    private ImageView imgCar;
    private AQuery aq;
    private String phoneAdmin = "";
    private boolean checkRate = true;

    private TextView tvShopName, tvAddress, tvDestination, tvProductName, tvPrice, tvStar;
    private TextView tvDistance, tvPickup ;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_trip);
        aq = new AQuery(self);
        initUI();
        initView();
        initLocalBroadcastManager();
        initData();
        initControl();

        // Starting PayPal service
        startPaypalService();
    }

    @Override
    public void onResume() {
        preferencesManager.setPassengerCurrentScreen(RateDriverActivity.class
                .getSimpleName());
        super.onResume();
    }

    ;

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
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
//        if (!getPreviousActivityName().equals(
//                StartTripForPassengerActivity.class.getName())) {
        ModelManager.showTripDetail(preferencesManager.getToken(),
                preferencesManager.getCurrentOrderId(), context, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            globalValue.setCurrentOrder(ParseJsonUtil
                                    .parseCurrentOrder(json));
                            lblPhone.setText(globalValue.getCurrentOrder()
                                    .getDriver_phone(true));
                            lblName.setText(globalValue.getCurrentOrder()
                                    .getDriverName());
                            tvDestination.setText(globalValue.getCurrentOrder().getEndLocation());
                            tvPickup.setText(globalValue.getCurrentOrder().getStartLocation());
                            tvPrice.setText( globalValue.getCurrentOrder().getPrice() + getResources().getString(R.string.lblCurrency) );
                            tvDistance.setText(globalValue.getCurrentOrder().getDistance() + " km");
                            tvStar.setText(Float
                                    .parseFloat(globalValue
                                            .getCurrentOrder()
                                            .getDriverRate()) / 2 + "(" + globalValue.getCurrentOrder().getDriverRateCount() + ")");
                            if (globalValue.getCurrentOrder()
                                    .getDriverRate().isEmpty()) {
                                ratingBarUser.setRating(0);
                                txtStar.setText("0");
                            } else {
                                ratingBarUser.setRating(Float
                                        .parseFloat(globalValue
                                                .getCurrentOrder()
                                                .getDriverRate()) / 2);
                                txtStar.setText("" + Float
                                        .parseFloat(globalValue
                                                .getCurrentOrder()
                                                .getDriverRate()) / 2);
                            }
                            Log.e("eeeeeeeeee", "image: " + globalValue.getCurrentOrder()
                                    .getCarImage());
                            aq.id(imgPassenger).image(
                                    globalValue.getCurrentOrder()
                                            .getImageDriver());
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

    private void initView() {
        ratingBar = findViewById(R.id.ratingBar);
        //btnSend = findViewById(R.id.btnSend);
        btnRate = findViewById(R.id.btnRate);
        tvStep = findViewById(R.id.tv_step);
        lblPhone = findViewById(R.id.lblPhone);
        ratingBarUser = findViewById(R.id.ratingBar_user);
        lblName = findViewById(R.id.lblName);

        stepNext = findViewById(R.id.step_full);
        stepCircle = findViewById(R.id.step_circle);
        txtStar = findViewById(R.id.txtStar);

        loPayment = findViewById(R.id.lo_payment);

        imgPassenger = findViewById(R.id.imgPassenger);
        llHelp = findViewById(R.id.llHelp);
        lblNorify = findViewById(R.id.lblNorify);

        tvAddress = findViewById(R.id.tv_address);
        tvDestination = findViewById(R.id.tv_destination);
        tvPickup = findViewById(R.id.tv_PickUp);
        tvPrice = findViewById(R.id.tv_price);
        tvDistance = findViewById(R.id.tv_distance);
        tvStar = findViewById(R.id.tv_star);
    }

    private void initControl() {
//        btnSend.setEnabled(false);
        /*btnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkRate) {
                    btnSend.setEnabled(false);
                    showDialogPayment();
                } else {
                    Toast.makeText(RateDriverActivity.this, "Bạn phải đanh giá trước khi thanh toán", Toast.LENGTH_SHORT).show();
                }

            }
        });*/
        btnRate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (globalValue.getCurrentOrder().getDriverRateTrip() != null && !globalValue.getCurrentOrder().getDriverRateTrip().equals("0")) {
                    Toast.makeText(self, getString(R.string.you_already_rated_this_task), Toast.LENGTH_SHORT).show();
                    checkRate = false;
                } else {
                    rate();
                }
            }
        });
        llHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtil.checkNetworkAvailable(RateDriverActivity.this)) {
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

    public void showDialogPayment() {
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_payment, null, false);
        TextView byCash = (TextView) v.findViewById(R.id.btnPayCash);
        TextView byPaypal = (TextView) v.findViewById(R.id.btnPayPal);
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(v)
                .create();

        byPaypal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                payment(Constant.PAY_TRIP_BY_BALANCE);
                alertDialog.dismiss();
            }
        });
        byCash.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                payment(Constant.PAY_TRIP_BY_CASH);
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void rate() {
        final String rate = ratingBar.getRating() * 2 + "";
        if (Double.parseDouble(rate) != 0) {
            ModelManager.rateDriver(preferencesManager.getToken(), globalValue
                            .getCurrentOrder().getId(), rate, context, true,
                    new ModelManagerListener() {
                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                preferencesManager.setDriverCurrentScreen("");
                                preferencesManager.setPassengerIsInTrip(false);
                                globalValue.getCurrentOrder().setDriverRateTrip(rate);
                                checkRate = false;
                                if (preferencesManager.IsStartWithOutMain()) {
								gotoActivity(MainActivity.class);
								finish();
                                } else {
								finish();
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
        } else {
            Toast.makeText(self, getString(R.string.lblValidateRate), Toast.LENGTH_SHORT).show();
        }

    }

    private void showRate(boolean isRate) {
        if (isRate) {
//			loRate.setVisibility(View.VISIBLE);
            loPayment.setVisibility(View.GONE);
            stepNext.setVisibility(View.VISIBLE);
            stepCircle.setVisibility(View.GONE);
            tvStep.setText(getString(R.string.lbl_request_by_finished));
            lblNorify.setText(getString(R.string.lbl_notify_pasenger_finish));
        } else {
//			loRate.setVisibility(View.GONE);
            loPayment.setVisibility(View.VISIBLE);
            stepNext.setVisibility(View.GONE);
            stepCircle.setVisibility(View.VISIBLE);
            tvStep.setText(getString(R.string.lbl_request_by_arrived1));
            lblNorify.setText(getString(R.string.lbl_notify_pasenger_arriving));

        }

    }

    private void setPayment(String point, String paymentId, final String paymentMethod) {
        ModelManager.payment(preferencesManager.getToken(), point, paymentId,
                paymentMethod, self, true, new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            payment(Constant.PAY_TRIP_BY_BALANCE);
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

    private void payment(String payBy) {
        ModelManager.tripPayment(preferencesManager.getToken(),
                preferencesManager.getCurrentOrderId(), payBy, this, true,
                new ModelManagerListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onSuccess(String json) {

                        if (ParseJsonUtil.isSuccess(json)) {
//							showRate(true);
                            preferencesManager
                                    .setPassengerHaveDonePayment(true);
                            preferencesManager.setPassengerCurrentScreen("");
                            finishAffinity();
                            gotoActivity(MainActivity.class);
                            finish();
                        } else {
                            if (ParseJsonUtil.getMessage(json).endsWith(
                                    "Your balance is short")) {
                                String missingFare = "";
                                missingFare = ParseJsonUtil
                                        .getMissingFare(json);
                                buyPoint(missingFare);
                                point = missingFare;
                            } else {
                                showToastMessage(ParseJsonUtil.getMessage(json));
                            }
                        }
                        btnSend.setEnabled(true);
                    }

                    @Override
                    public void onError() {
                        showToastMessage(R.string.message_have_some_error);
                    }
                });
    }

    private void initLocalBroadcastManager() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_DRIVER_CONFIRM_PAID_STATUS_CONFIRM);
        intentFilter.addAction(Constant.ACTION_DRIVER_CONFIRM_PAID_STATUS_CANCEL);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constant.KEY_ACTION);
            if (action.equals(Constant.ACTION_DRIVER_CONFIRM_PAID_STATUS_CONFIRM)) {
                showToastMessage(getResources().getString(R.string.payment_successfully));
                preferencesManager.setPassengerHaveDonePayment(true);
                preferencesManager.setPassengerCurrentScreen("");
                finishAffinity();
                gotoActivity(MainActivity.class);
                finish();
            }
        }
    };

    private void buyPoint(final String point) {

//        new AlertDialog.Builder(this)
//                .setTitle(R.string.app_name)
//                .setMessage(title)
//                .setPositiveButton(android.R.string.ok,
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface arg0, int arg1) {
//                                requestPaypalPayment(Double.parseDouble(point),
//                                        "GET POINT",
//                                        getString(R.string.currency));
//                            }
//                        }).create().show();

        final MaterialDialog dialog = new MaterialDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_payment_method, null);
        dialog.setContentView(view);
        String title = getResources().getString(
                R.string.message_your_balance_is_not_eounght)
                + " "
                + point
                + " "
                + getResources().getString(R.string.message_point);
        dialog.setTitle(title);
        TextView txtPaypal = (TextView) view.findViewById(R.id.txtPaypal);
        TextView txtStripe = (TextView) view.findViewById(R.id.txtStripe);
        txtPaypal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                requestPaypalPayment(Double.parseDouble(point),
                        "GET POINT",
                        getString(R.string.currency));
            }
        });
        txtStripe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(RateDriverActivity.this, PaymentStripeFinishActivity.class);
                intent.putExtra("amount", point + "");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left2);
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm = data
                    .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    Log.i("paymentExample",
                            "Payment OK. Response :" + confirm.toJSONObject());
                    JSONObject json = confirm.toJSONObject();

                    // if (ParseJsonUtil.paymentIsPaypal(json)) {
                    setPayment(point,
                            ParseJsonUtil.getTransactionFromPaypal(json),
                            Constant.PAYMENT_BY_PAYPAL);
                    /*
                     * } else { setPayment(point,
                     * ParseJsonUtil.getTransactionFromCart(json),
                     * Constant.PAYMENT_BY_CART); }
                     */

                } catch (Exception e) {
                    Log.e("paymentExample",
                            "an extremely unlikely failure occurred: ", e);
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("paymentExample", "The user canceled.");
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Log.i("paymentExample",
                    "An invalid payment was submitted. Please see the docs.");
        }
    }


}

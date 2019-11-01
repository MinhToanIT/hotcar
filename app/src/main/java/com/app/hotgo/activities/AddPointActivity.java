package com.app.hotgo.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.User;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONObject;

//import com.paypal.android.sdk.payments.PayPalConfiguration;

public class AddPointActivity extends BaseActivity {

    private TextView lblBalance;
    private EditText txtPoint;
    private LinearLayout btnPayment, btnStripe;
    private TextView lblError;
    static String paymentId;
    private String point;
    private String pointAdd = "";

    User user;

    private static final int REQUEST_CODE_PAYMENT = 1;
    private int MY_SCAN_REQUEST_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        initAndSetHeader(R.string.title_payment);
        getDataFromGlobal();
        getDataFromIntent();
        initView();
        initControl();

        // Starting PayPal service
        startPaypalService();
    }

    public void getDataFromIntent() {
        pointAdd = getIntent().getStringExtra("point");
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    private void getDataFromGlobal() {
        user = GlobalValue.getInstance().user;
    }

    public void initView() {
        initUI();
        lblBalance =  findViewById(R.id.lblBalance);
        txtPoint =  findViewById(R.id.txtPoint);
        btnPayment =  findViewById(R.id.btnPayment);
        btnStripe =  findViewById(R.id.btnStripe);

        // get balance
        lblBalance.setText(getString(R.string.lblCurrency) + user.getBalance().toString());
        txtPoint.setText(point);
    }

    public void initControl() {
        btnPayment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                payment();
            }
        });
        btnStripe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!txtPoint.getText()
                        .toString().trim().equals("")) {
                    Double amount1 = Double.parseDouble(txtPoint.getText()
                            .toString().trim());
                    if (amount1 < Double.parseDouble(point)) {
                        showToastMessage(getResources().getString(R.string.point_invalid));
                    } else {
                        Intent intent = new Intent(AddPointActivity.this, PaymentStripeActivity.class);
                        intent.putExtra("amount", amount1 + "");
                        startActivity(intent);
                    }

                } else {
                    showToastMessage(getResources().getString(R.string.amount_not_blank));
                }


            }
        });

    }

    private void setPayment(String point, String paymentId, String paymentMethod) {

        ModelManager.payment(preferencesManager.getToken(), txtPoint.getText()
                        .toString(), paymentId, paymentMethod, self, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            showToastMessage(R.string.lbl_success);
                            if (ParseJsonUtil.getMessage(json)
                                    .equalsIgnoreCase("ok")) {
                                txtPoint.setText("");
                                // finish();
                            }
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

    public void payment() {
        if (txtPoint.getText().toString().trim().length() != 0) {
            Double moneyInt = Double.parseDouble(txtPoint.getText().toString()
                    .trim());
            if (moneyInt < 0) {
                showToastMessage(getResources().getString(R.string.point_invalid));
            } else {
                if (moneyInt < Double.parseDouble(point)) {
                    showToastMessage(getResources().getString(R.string.point_invalid));
                } else {
                    requestPaypalPayment(
                            Double.parseDouble(txtPoint.getText().toString()),
                            "GET POINT", getString(R.string.currency));
                }

            }
        } else {
            showToastMessage(R.string.message_please_enter_amount);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("eee", "onActivityResult");
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm = data
                    .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    Log.i("paymentExample",
                            "Payment OK. Response :" + confirm.toJSONObject());
                    JSONObject json = confirm.toJSONObject();

                    setPayment(point,
                            ParseJsonUtil.getTransactionFromPaypal(json),
                            Constant.PAYMENT_BY_PAYPAL);
                    getData();

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

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.e("eee", "onResume" + "  getData");
        getData();
        txtPoint.setText("");
    }

    public void getData() {
        ModelManager.showInfoProfile(
                PreferencesManager.getInstance(mainActivity).getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        Log.e("json", "json:" + json);
                        if (ParseJsonUtil.isSuccess(json)) {
                            GlobalValue.getInstance().setUser(
                                    ParseJsonUtil.parseInfoProfile(json));
                            user = ParseJsonUtil.parseInfoProfile(json);
                            lblBalance.setText(getString(R.string.lblCurrency) + user.getBalance().toString());
                            Log.e("eee", "getData" + "  lblBalance");
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });
    }
}

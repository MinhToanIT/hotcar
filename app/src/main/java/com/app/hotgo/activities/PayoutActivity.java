package com.app.hotgo.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.Transfer;
import com.app.hotgo.object.User;

public class PayoutActivity extends BaseActivity {

    TextView lbl_information;
    TextView lblError;
    LinearLayout btnContinue, btnStripe;
    TextView lblBalance;
    EditText lblPoint;
    User user;
    Transfer payment = new Transfer();
    String minRedeem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payout);
        initUI();
        getDataFromGlobal();
        initUIInThis();
        getMinRedeem();
        initAndSetHeader(R.string.title_redeem);

    }

    private void getDataFromGlobal() {
        user = GlobalValue.getInstance().user;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void initUIInThis() {

        lblBalance =  findViewById(R.id.lbl_Balance);
        lblPoint =  findViewById(R.id.lbl_Point);
        lblError =  findViewById(R.id.lbl_Error);
        lbl_information =  findViewById(R.id.lbl_information);
        btnStripe =  findViewById(R.id.btnStripe);
        lblBalance.setText(getString(R.string.lblCurrency) + String.valueOf(user.getBalance()));

        btnContinue =  findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (preferencesManager.isDriver()) {
                    if (user.getDriverObj().getBankAccount() != null && !user.getDriverObj().getBankAccount().equals("")) {
                        if (lblPoint.getText().toString().length() != 0) {
                            Double amount = Double.parseDouble(lblPoint.getText()
                                    .toString().trim());
                            Double balance = user.getBalance();
                            if (amount > balance) {
                                showToastMessage(R.string.message_point_invalid);
                            } else if (amount >= Double.parseDouble(minRedeem)) {
                                payOut();
                            } else {
                                showToastMessage(self.getResources()
                                        .getString(R.string.message_point_min)
                                        .replace("500", minRedeem));
                            }
                        } else {
                            showToastMessage(R.string.message_please_enter_point);
                        }
                    } else {
                        showToastMessage(getResources().getString(R.string.please_update_payout_paypal));
                    }
                } else {
                    if (user.getPayout() != null && !user.getPayout().equals("")) {
                        if (lblPoint.getText().toString().length() != 0) {
                            Double amount = Double.parseDouble(lblPoint.getText()
                                    .toString().trim());
                            Double balance = user.getBalance();
                            if (amount > balance) {
                                showToastMessage(R.string.message_point_invalid);
                            } else if (amount >= Double.parseDouble(minRedeem)) {
                                payOut();
                            } else {
                                showToastMessage(self.getResources()
                                        .getString(R.string.message_point_min)
                                        .replace("500", minRedeem));
                            }
                        } else {
                            showToastMessage(R.string.message_please_enter_point);
                        }
                    } else {
                        showToastMessage(getResources().getString(R.string.please_update_payout_paypal));
                    }
                }


            }
        });


        lbl_information.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
                        .parse("http://www.linkrider.net/#!faq/c1yvs"));
                startActivity(browserIntent);
            }
        });
    }

    private void payOut() {
        ModelManager.payout(PreferencesManager.getInstance(self).getToken(),
                lblPoint.getText().toString(), self, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {

                            showToastMessage(R.string.lblPayoutSuccess);
                            onBackPressed();
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

    private void getMinRedeem() {
        ModelManager.getGeneralSettings(preferencesManager.getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            minRedeem = ParseJsonUtil.getMinRedeem(json);
                            // minRedeem = mainActivity.getResources()
                            // .getString(R.string.message_point_min)
                            // .replace("500", minRedeem);
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
}

package com.app.hotgo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.config.Constant;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;


public class Ac_ConfirmPayByCash extends BaseActivity {
    private TextView btnConfirm, btnCancel;
    private String tripId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac__confirm_pay_by_cash);

        init();
        initData();
        initControl();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            tripId = intent.getStringExtra(Constant.TRIP_ID);
        }
        if (tripId == null || tripId.isEmpty()) {
            tripId = preferencesManager.getTripId();
        }
    }

    public void init() {
        btnConfirm =  findViewById(R.id.btnConfirm);
        btnCancel =  findViewById(R.id.btnCancel);

    }

    public void initControl() {
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                btnCancel.setEnabled(false);
//                btnConfirm.setEnabled(false);
                confirm(Constant.ACTION_CONFIRM);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                btnConfirm.setEnabled(false);
//                btnCancel.setEnabled(false);
                confirm(Constant.ACTION_CANCEL);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void confirm(String action) {
        ModelManager.confirmPayByCash(tripId, action, this, true, new ModelManagerListener() {
            @Override
            public void onError() {
                showToastMessage(getString(R.string.message_have_some_error));
//                btnConfirm.setEnabled(true);
//                btnCancel.setEnabled(true);
            }

            @Override
            public void onSuccess(String json) {
                if (ParseJsonUtil.isSuccess(json)) {
                    if (preferencesManager.getPreviousScreen().equals(SplashActivity.class.getSimpleName())) {
                        gotoActivity(SplashActivity.class);
                        preferencesManager.setPreviousScreen("");
                        finishAffinity();
                    } else {
                        finish();
                    }
                    preferencesManager.isFromBeforeOnline(false);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

                } else {
                    showToastMessage(ParseJsonUtil.getMessage(json));
                }
//                btnConfirm.setEnabled(true);
//                btnCancel.setEnabled(true);
            }
        });
    }

}

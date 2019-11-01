package com.app.hotgo.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;

/**
 * Created by Administrator on 4/18/2017.
 */

public class StartTaskDriverActivity extends BaseActivity {
    private TextView lblJobName, txtTimeStart, lblCountTime, lblDuration;
    private TextView btnStartTask, btnEndTask;
    private PreferencesManager preferencesManager;
    private long startTime = 0L;

    private Handler customHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_task);
        preferencesManager = PreferencesManager.getInstance(this);
        initView();
//        checkDataView();
        initControl();
    }

    public void initView() {
        lblJobName =  findViewById(R.id.lblJobName);
        txtTimeStart =  findViewById(R.id.txtTimeStart);
        lblCountTime =  findViewById(R.id.lblCountTime);
        lblDuration =  findViewById(R.id.lblDuration);
        btnStartTask =  findViewById(R.id.btnStartTask);
        btnEndTask =  findViewById(R.id.btnEndTask);
    }

    public void checkDataView() {
        if (preferencesManager.taskerIsInTask()) {
            btnStartTask.setVisibility(View.GONE);
            btnEndTask.setVisibility(View.VISIBLE);
        } else {
            btnStartTask.setVisibility(View.VISIBLE);
            btnEndTask.setVisibility(View.GONE);
        }
    }

    public void initControl() {
        btnStartTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatusTask();
            }
        });
        btnEndTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customHandler.postDelayed(updateTimerThread, 0);
            }
        });
    }



    public void updateStatusTask() {
        ModelManager.changeStatus(preferencesManager.getToken(), globalValue.getCurrentOrder().getId(), Constant.STATUS_START_TASK, StartTaskDriverActivity.this, true, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
            }
        });
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
//            lblCountTime.setText("" + mins + ":"
//                    + String.format("%02d", secs) + ":"
//                    + String.format("%03d", milliseconds));
            lblDuration.setText("" + mins + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            customHandler.postDelayed(this, 0);
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customHandler.removeCallbacks(updateTimerThread);
    }
}

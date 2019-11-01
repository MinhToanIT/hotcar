package com.app.hotgo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.app.hotgo.RequestService;



public class RequestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, RequestService.class);
        context.startService(serviceIntent);
    }
}

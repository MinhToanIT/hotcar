package com.app.hotgo.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.app.hotgo.R;
import com.app.hotgo.activities.Ac_ConfirmPayByCash;
import com.app.hotgo.activities.SplashActivity;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.CurrentOrder;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "GCMIntentService";
    public static final String KEY_DATA = "data";
    public static final String KEY_BODY = "body";
    public static final String KEY_ACTION = "action";
    public static final String KEY_STATUS = "KEY_STATUS";
    private static int NOTIFICATION_ID = 0;
    private static int REQUEST_CODE = 0;
    private PreferencesManager preferencesManager = PreferencesManager
            .getInstance(this);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        Log.d("FCM", data.toString());

        processReciverPush(this, remoteMessage);


    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        saveToken(token);
    }

    private void saveToken(String token) {
        if (!MyGcmSharedPrefrences.getToken(this).equals(token)) {
            MyGcmSharedPrefrences.saveToken(MyFirebaseMessagingService.this, token);
        }
    }

    private void processReciverPush(Context context, RemoteMessage remoteMessage) {
        String action = remoteMessage.getData().get(KEY_ACTION);
        String status = "";
        if (null == action) {
            return;
        }
        switch (action) {
            // Done
            case Constant.ACTION_PASSENGER_CREATE_REQUEST:
                //if (preferencesManager.isDriver()) {
                processCreateRequest(context, remoteMessage);
                //}
                break;
            case Constant.STATUS_ARRIVED_B:
                processTaskerArrivedB(context, remoteMessage);
                break;
            case Constant.STATUS_START_TASK:
                processTaskerStartTask(context, remoteMessage);
                break;
            case Constant.STATUS_ARRIVED_A:
                processDriverArrived(context, remoteMessage);
                break;
            case Constant.ACTION_CANCEL_TRIP:
                processCancelTrip(context, remoteMessage);
                break;
            case Constant.ACTION_PASSENGER_CANCEL_REQUEST:
                //if (preferencesManager.isDriver()
                //&& preferencesManager.getDriverCurrentScreen().equals(
                //"RequestPassengerActivity")) {
                processCancelRequest(context, remoteMessage);
                //}
                break;
            case Constant.ACTION_DRIVER_CONFIRM:
                //if (preferencesManager.isUser()
                //|| !preferencesManager.driverIsOnline()) {
                processDriverConfirm(context, remoteMessage);
                //}
                break;
            case Constant.ACTION_DRIVER_START_TRIP:
                //if (preferencesManager.isUser()
                //|| !preferencesManager.driverIsOnline()) {
                processDriverStartTrip(context, remoteMessage);
                //}
                break;
            case Constant.ACTION_DRIVER_END_TRIP:
                //if (preferencesManager.isUser()
                // || !preferencesManager.driverIsOnline()) {
                processDriverEndTrip(context, remoteMessage);
                //}
                break;
            case Constant.ACTION_APROVAL_UPDATE:
                processActionApproval(context, remoteMessage);
                break;
            case Constant.ACTION_APROVAL_REDEEM:
                processActionApproval(context, remoteMessage);
                break;
            case Constant.ACTION_APROVAL_TRANFER:
                processActionApproval(context, remoteMessage);
                break;
            case Constant.ACTION_INFORMATION:
                processInforPromotion(context, remoteMessage);
                break;
            case Constant.ACTION_DRIVER_APPROVED:
                processInforPromotion(context, remoteMessage);
                break;
//            case Constant.ACTION_DRIVER_ARRIVED:
//                processDriverArrived(context, remoteMessage);
//                break;

            case Constant.ACTION_PAY_BY_CASH:
                processActionConfirmPaidByCash(context, remoteMessage);
                break;

            case Constant.ACTION_DRIVER_CONFIRM_PAID_STATUS_CONFIRM:
                progcessConfirmPaidStatusConfirm(context, remoteMessage);
                break;
            case Constant.ACTION_DRIVER_CONFIRM_PAID_STATUS_CANCEL:
                progcessConfirmPaidStatusCancel(context, remoteMessage);
                break;

        }
    }

    private void progcessConfirmPaidStatusCancel(Context context, RemoteMessage remoteMessage) {
        boolean isShow = preferencesManager.appIsShow();
        if (isShow) {
            String msg = remoteMessage.getData().get(KEY_BODY);
            showMessage(msg);
            Log.e("push cancel", msg);
        }
    }

    public void showMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void progcessConfirmPaidStatusConfirm(Context context, RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get(KEY_BODY);
//        preferencesManager.setPassengerCurrentScreen("RateDriverActivity");
        if (preferencesManager.appIsShow()) {
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            Intent intent = new Intent(Constant.ACTION_DRIVER_CONFIRM_PAID_STATUS_CONFIRM);
            // You can also include some extra remoteMessage.
            intent.putExtra(Constant.KEY_DATA, remoteMessage.getData().get(KEY_DATA));
            intent.putExtra(Constant.KEY_ACTION, remoteMessage.getData().get(KEY_ACTION));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
//            preferencesManager.setHaveANotifyPaidConfirm(data.getString("trip_id"), data.getString("payment_method"));
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            /* CREATE NOTIFICATION */
            createNotification(context, msg);
//            NotificationManager mNotificationManager = (NotificationManager) context
//                    .getSystemService(Context.NOTIFICATION_SERVICE);
//            Intent intent = new Intent(context, SplashActivity.class);
//            intent.putExtra("pushNotification", true);
//            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                    context)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(context.getString(R.string.app_name))
//                    .setStyle(
//                            new NotificationCompat.BigTextStyle().bigText(msg))
//                    .setContentText(msg)
//                    .setAutoCancel(true);
//            mBuilder.setContentIntent(contentIntent);
//
//            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    private void processActionConfirmPaidByCash(Context context, RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get(KEY_BODY);
        preferencesManager.setTripId(remoteMessage.getData().get("trip_id"));
        if (preferencesManager.appIsShow()) {
            /* CALL VIBRATE AND RINGTONE */
            if (preferencesManager.driverIsOnline()) {
                vibratorAndRingtone();
                Intent intent = new Intent(context, Ac_ConfirmPayByCash.class);
                // You can also include some extra data.
                intent.putExtra(Constant.KEY_DATA, remoteMessage);
                intent.putExtra(Constant.TRIP_ID, remoteMessage.getData().get("trip_id"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else {
//            preferencesManager.setHaveANotifyPaidConfirm(data.getString("trip_id"), data.getString("payment_method"));
//            preferencesManager.setCurrentOrderId("trip_id");
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            /* CREATE NOTIFICATION */
            createNotification(context, msg);
//            NotificationManager mNotificationManager = (NotificationManager) context
//                    .getSystemService(Context.NOTIFICATION_SERVICE);
//            Intent intent = new Intent(context, SplashActivity.class);
//            intent.putExtra("pushNotification", true);
//            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                    context)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(context.getString(R.string.app_name))
//                    .setStyle(
//                            new NotificationCompat.BigTextStyle().bigText(msg))
//                    .setContentText(msg).setAutoCancel(true);
//            mBuilder.setContentIntent(contentIntent);
//
//            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }


    private void processActionApproval(Context context, RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get(KEY_BODY);
        if (preferencesManager.appIsShow()) {
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            Intent intent = new Intent(Constant.PUSH_NOTIFY_HISTORY);
            // You can also include some extra data.
            intent.putExtra(Constant.KEY_DATA, remoteMessage.getData().get(KEY_DATA));
            intent.putExtra(Constant.KEY_ACTION,
                    remoteMessage.getData().get(KEY_ACTION));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            preferencesManager.setHistoryPush(true);
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            /* CREATE NOTIFICATION */
            createNotification(context, msg);
//            NotificationManager mNotificationManager = (NotificationManager) context
//                    .getSystemService(Context.NOTIFICATION_SERVICE);
//            Intent intent = new Intent(context, SplashActivity.class);
//            intent.putExtra(Constant.PUSH_NOTIFY_HISTORY, true);
//            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                    context)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(context.getString(R.string.app_name))
//                    .setStyle(
//                            new NotificationCompat.BigTextStyle().bigText(msg))
//                    .setContentText(msg).setAutoCancel(true);
//            mBuilder.setContentIntent(contentIntent);
//
//            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    private void processTaskerStartTask(Context context, RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get(KEY_BODY);
        preferencesManager.setBeginTask("1");
        if (preferencesManager.appIsShow()) {
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            Intent intent = new Intent(Constant.ACTION_TASKER_START_TASK);
            // You can also include some extra data.
            intent.putExtra(Constant.KEY_DATA, remoteMessage.getData().get(KEY_DATA));
            intent.putExtra(Constant.KEY_ACTION,
                    Constant.ACTION_TASKER_START_TASK);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            preferencesManager.setStartWithOutMain(true);
            preferencesManager.setPassengerHavePush(true);
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            /* CREATE NOTIFICATION */
            createNotification(context, msg);
//            NotificationManager mNotificationManager = (NotificationManager) context
//                    .getSystemService(Context.NOTIFICATION_SERVICE);
//            Intent intent = new Intent(context, SplashActivity.class);
//            intent.putExtra("pushNotification", true);
//            intent.addFlags(
//                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                    context)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(context.getString(R.string.app_name))
//                    .setStyle(
//                            new NotificationCompat.BigTextStyle().bigText(msg))
//                    .setContentText(msg).setAutoCancel(true);
//            mBuilder.setContentIntent(contentIntent);
//
//            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    private void processTaskerArrivedB(Context context, RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get(KEY_BODY);
        preferencesManager.setArrivedB("1");
        if (preferencesManager.appIsShow()) {
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            Intent intent = new Intent(Constant.ACTION_TASKER_ARRIVED_B);
            // You can also include some extra data.
            intent.putExtra(Constant.KEY_DATA, remoteMessage.getData().get(KEY_DATA));
            intent.putExtra(Constant.KEY_ACTION,
                    Constant.ACTION_TASKER_ARRIVED_B);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            preferencesManager.setStartWithOutMain(true);
            preferencesManager.setPassengerHavePush(true);
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            /* CREATE NOTIFICATION */
            createNotification(context, msg);
//            NotificationManager mNotificationManager = (NotificationManager) context
//                    .getSystemService(Context.NOTIFICATION_SERVICE);
//            Intent intent = new Intent(context, SplashActivity.class);
//            intent.putExtra("pushNotification", true);
//            intent.addFlags(
//                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                    context)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(context.getString(R.string.app_name))
//                    .setStyle(
//                            new NotificationCompat.BigTextStyle().bigText(msg))
//                    .setContentText(msg).setAutoCancel(true);
//            mBuilder.setContentIntent(contentIntent);
//
//            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    private void processDriverArrived(Context context, RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get(KEY_BODY);
        preferencesManager
                .setPassengerCurrentScreen("ShowPassengerActivity");
        preferencesManager.setArrived("1");
        if (preferencesManager.appIsShow()) {
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            Intent intent = new Intent(Constant.ACTION_DRIVER_ARRIVED);
            // You can also include some extra data.
            intent.putExtra(Constant.KEY_DATA, remoteMessage.getData().get(KEY_DATA));
            intent.putExtra(Constant.KEY_ACTION,
                    Constant.ACTION_DRIVER_ARRIVED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            preferencesManager.setStartWithOutMain(true);
            preferencesManager.setPassengerHavePush(true);
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            /* CREATE NOTIFICATION */
            createNotification(context, msg);
//            NotificationManager mNotificationManager = (NotificationManager) context
//                    .getSystemService(Context.NOTIFICATION_SERVICE);
//            Intent intent = new Intent(context, SplashActivity.class);
//            intent.putExtra("pushNotification", true);
//            intent.addFlags(
//                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                    context)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(context.getString(R.string.app_name))
//                    .setStyle(
//                            new NotificationCompat.BigTextStyle().bigText(msg))
//                    .setContentText(msg).setAutoCancel(true);
//            mBuilder.setContentIntent(contentIntent);
//
//            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    // new code
    private void processInforPromotion(Context context, RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get(KEY_BODY);
        /* CALL VIBRATE AND RINGTONE */
        vibratorAndRingtone();
        /* CREATE NOTIFICATION */
        createNotification(context,msg);
//        NotificationManager mNotificationManager = (NotificationManager) context
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//        Intent intent = new Intent(context, SplashActivity.class);
//        intent.putExtra(Constant.PUSH_NOTIFY_HISTORY, true);
//        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
//                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                context).setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle(context.getString(R.string.app_name))
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
//                .setContentText(msg).setAutoCancel(true);
//        mBuilder.setContentIntent(contentIntent);
//
//        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
    // end new code

    private void processDriverConfirm(Context context, RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get(KEY_BODY);
        String trip_status = remoteMessage.getData().get("trip_status");
        /* PROCESS DATA */
        String json = remoteMessage.getData().get(KEY_DATA);
        CurrentOrder currentOrder = new CurrentOrder();
        currentOrder.setId(ParseJsonUtil.parseOrderIdFromDriverConfirm(json));
        Log.e("currentOrder", "currentOrder:" + currentOrder.getStatus() + " ");
        GlobalValue.getInstance().setCurrentOrder(currentOrder);
        preferencesManager.setCurrentOrderId(currentOrder.getId());
        preferencesManager.setPassengerIsInTrip(true);
        preferencesManager.setPassengerCurrentScreen("ConfirmActivity");
        preferencesManager.setPassengerHavePush(true);
        preferencesManager.setPassengerWaitConfirm(false);
        if (preferencesManager.appIsShow()) {
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            Intent intent = new Intent(Constant.ACTION_DRIVER_CONFIRM);
            // You can also include some extra data.
            intent.putExtra(Constant.KEY_DATA, remoteMessage.getData().get(KEY_DATA));
            intent.putExtra(Constant.KEY_ACTION,
                    remoteMessage.getData().get(KEY_ACTION));
            intent.putExtra("trip_status",
                    trip_status);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            preferencesManager.setStartWithOutMain(true);
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            /* CREATE NOTIFICATION */
            createNotification(context, msg);

//            NotificationManager mNotificationManager = (NotificationManager) context
//                    .getSystemService(Context.NOTIFICATION_SERVICE);
//            Intent intent = new Intent(context, SplashActivity.class);
//            intent.putExtra("pushNotification", true);
//            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                    context)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(context.getString(R.string.app_name))
//                    .setStyle(
//                            new NotificationCompat.BigTextStyle().bigText(msg))
//                    .setContentText(msg).setAutoCancel(true);
//            mBuilder.setContentIntent(contentIntent);

//            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    private void processDriverStartTrip(Context context, RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get(KEY_BODY);
        preferencesManager
                .setPassengerCurrentScreen("StartTripForPassengerActivity");
        if (preferencesManager.appIsShow()) {
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            Intent intent = new Intent(Constant.ACTION_DRIVER_START_TRIP);
            // You can also include some extra remoteMessage.
            intent.putExtra(Constant.KEY_DATA, remoteMessage.getData().get(KEY_DATA));
            intent.putExtra(Constant.KEY_ACTION,
                    remoteMessage.getData().get(KEY_ACTION));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            preferencesManager.setStartWithOutMain(true);
            preferencesManager.setPassengerHavePush(true);
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            /* CREATE NOTIFICATION */
            createNotification(context, msg);
//            NotificationManager mNotificationManager = (NotificationManager) context
//                    .getSystemService(Context.NOTIFICATION_SERVICE);
//            Intent intent = new Intent(context, SplashActivity.class);
//            intent.putExtra("pushNotification", true);
//            intent.addFlags(
//                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                    context)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(context.getString(R.string.app_name))
//                    .setStyle(
//                            new NotificationCompat.BigTextStyle().bigText(msg))
//                    .setContentText(msg).setAutoCancel(true);
//            mBuilder.setContentIntent(contentIntent);
//
//            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    private void processDriverEndTrip(Context context, RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get(KEY_BODY);
        preferencesManager.setPassengerCurrentScreen("RateDriverActivity");
        preferencesManager.setPassengerHaveDonePayment(false);
        if (preferencesManager.appIsShow()) {
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            Intent intent = new Intent(Constant.ACTION_DRIVER_END_TRIP);
            // You can also include some extra data.
            intent.putExtra(Constant.KEY_DATA, remoteMessage.getData().get(KEY_DATA));
            intent.putExtra(Constant.KEY_ACTION,
                    remoteMessage.getData().get(KEY_ACTION));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            preferencesManager.setStartWithOutMain(true);
            preferencesManager.setPassengerHavePush(true);
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            /* CREATE NOTIFICATION */
            createNotification(context, msg);
//            NotificationManager mNotificationManager = (NotificationManager) context
//                    .getSystemService(Context.NOTIFICATION_SERVICE);
//            Intent intent = new Intent(context, SplashActivity.class);
//            intent.putExtra("pushNotification", true);
//            intent.addFlags(
//                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                    context)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(context.getString(R.string.app_name))
//                    .setStyle(
//                            new NotificationCompat.BigTextStyle().bigText(msg))
//                    .setContentText(msg).setAutoCancel(true);
//            mBuilder.setContentIntent(contentIntent);
//
//            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    // For driver
    private void processCreateRequest(Context context, RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get(KEY_BODY);
        preferencesManager.setDriverCurrentScreen("RequestPassengerActivity");
        preferencesManager.setDriverIsInTrip();
        if (preferencesManager.driverIsOnline()) {
            if (preferencesManager.appIsShow()) {
                /* CALL VIBRATE AND RINGTONE */
                vibratorAndRingtone();
                Intent intent = new Intent(
                        Constant.ACTION_PASSENGER_CREATE_REQUEST);
                // You can also include some extra data.
                intent.putExtra(Constant.KEY_DATA,
                        remoteMessage.getData().get(KEY_DATA));
                intent.putExtra(Constant.KEY_ACTION,
                        remoteMessage.getData().get(KEY_ACTION));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            } else {
                preferencesManager.setDriverHavePush();
                /* CALL VIBRATE AND RINGTONE */
                vibratorAndRingtone();
                /* CREATE NOTIFICATION */
                createNotification(context, msg);
//                NotificationManager mNotificationManager = (NotificationManager) context
//                        .getSystemService(Context.NOTIFICATION_SERVICE);
//                Intent intent = new Intent(context, SplashActivity.class);
//                intent.putExtra("pushNotification", true);
//                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
//                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                PendingIntent contentIntent = PendingIntent.getActivity(
//                        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                        context)
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setContentTitle(context.getString(R.string.app_name))
//                        .setStyle(
//                                new NotificationCompat.BigTextStyle()
//                                        .bigText(msg)).setContentText(msg)
//                        .setAutoCancel(true);
//                mBuilder.setContentIntent(contentIntent);
//
//                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }
        }
    }

    private void processCancelRequest(Context context, RemoteMessage remoteMessage) {
        if (preferencesManager.appIsShow()
                && preferencesManager.getDriverCurrentScreen().equals(
                "RequestPassengerActivity")) {
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            Intent intent = new Intent(Constant.ACTION_PASSENGER_CREATE_REQUEST);
            // You can also include some extra data.
            intent.putExtra(Constant.KEY_DATA, remoteMessage.getData().get(KEY_DATA));
            intent.putExtra(Constant.KEY_ACTION,
                    remoteMessage.getData().get(KEY_ACTION));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {

        }
    }

    private void processCancelTrip(Context context, RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get(KEY_BODY);
        preferencesManager.setDriverIsNotInTrip();
        preferencesManager.setPassengerCurrentScreen("");
        preferencesManager.setDriverCurrentScreen("");
        // preferencesManager.setPassengerIsInTrip(false);
        // preferencesManager.setPassengerWaitConfirm(false);
        if (preferencesManager.appIsShow()) {
            /* CALL VIBRATE AND RINGTONE */
            if (preferencesManager.isUser()
                    || !preferencesManager.driverIsOnline()) {
                vibratorAndRingtone();
                Intent intent = new Intent(Constant.ACTION_CANCEL_TRIP);
                // You can also include some extra data.
                intent.putExtra(Constant.KEY_DATA,
                        remoteMessage.getData().get(KEY_DATA));
                intent.putExtra(Constant.KEY_ACTION,
                        remoteMessage.getData().get(KEY_ACTION));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            } else {
                if (preferencesManager.driverIsOnline()) {
                    vibratorAndRingtone();
                    Intent intent = new Intent(Constant.ACTION_CANCEL_TRIP);
                    // You can also include some extra data.
                    intent.putExtra(Constant.KEY_DATA,
                            remoteMessage.getData().get(KEY_DATA));
                    intent.putExtra(Constant.KEY_ACTION,
                            remoteMessage.getData().get(KEY_ACTION));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(
                            intent);
                }
            }

        } else {
            preferencesManager.setStartWithOutMain(true);
            /* CALL VIBRATE AND RINGTONE */
            vibratorAndRingtone();
            /* CREATE NOTIFICATION */
            createNotification(context, msg);
//            NotificationManager mNotificationManager = (NotificationManager) context
//                    .getSystemService(Context.NOTIFICATION_SERVICE);
//            Intent intent = new Intent(context, SplashActivity.class);
//            intent.putExtra("pushNotification", true);
//            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                    context)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(context.getString(R.string.app_name))
//                    .setStyle(
//                            new NotificationCompat.BigTextStyle().bigText(msg))
//                    .setContentText(msg).setAutoCancel(true);
//            mBuilder.setContentIntent(contentIntent);
//
//            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    private void vibratorAndRingtone() {
        // For Vibrate
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
        // For Sound
        Uri notification = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
                notification);
        r.play();
    }

    private void createNotification(Context context, String msg) {
        String channelId = "channel-01";
        String channelName = "Channel Name";


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder mBuilder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mBuilder = new NotificationCompat.Builder(context, channelId)
                    .setColor(Color.argb(255, 245, 109, 10))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setStyle(
                            new NotificationCompat.BigTextStyle().bigText(msg))
                    .setContentText(msg).setAutoCancel(true);
        } else {
            mBuilder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setStyle(
                            new NotificationCompat.BigTextStyle().bigText(msg))
                    .setContentText(msg).setAutoCancel(true);
        }


        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra("pushNotification", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}

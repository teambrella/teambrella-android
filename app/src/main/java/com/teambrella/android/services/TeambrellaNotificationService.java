package com.teambrella.android.services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.teambrella.android.R;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.claim.ClaimActivity;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Teambrella Notification Service
 */
public class TeambrellaNotificationService extends Service implements TeambrellaServer.SocketClientListener {


    public static final String LOG_TAG = TeambrellaNotificationService.class.getSimpleName();

    public static final String CONNECT_ACTION = "connect";
    public static final String MESSAGE_ACTION = "message";
    public static final String STOP_ACTION = "stop";

    public static final String EXTRA_CLAIM_ID = "EXTRA_CLAIM_ID";
    public static final String EXTRA_TEAM_ID = "EXTRA_TEAM_ID";
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    public static final String EXTRA_TEAMMATE_ID = "EXTRA_TEAMMATE_ID";
    public static final String EXTRA_TOPIC_ID = "EXTRA_TOPIC_ID";
    public static final String EXTRA_POST_ID = "EXTRA_POST_ID";
    public static final String EXTRA_TEAMNAME = "EXTRA_TEAMNAME";
    public static final String EXTRA_NAME = "EXTRA_NAME";
    public static final String EXTRA_IMGURL = "EXTRA_IMGURL";
    public static final String EXTRA_TEAM_IMGURL = "EXTRA_TEAMIMGURL";
    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    public static final String EXTRA_COUNT = "EXTRA_COUNT";
    public static final String EXTRA_CRYPTO_AMOUNT = "EXTRA_CRYPTO_AMOUNT";
    public static final String EXTRA_FIAT_AMOUNT = "EXTRA_FIAT_AMOUNT";


    public static final String ON_CREATED_POST = "ON_CREATED_POST";
    public static final String ON_DELETED_POST = "ON_DELETED_POST";
    public static final String ON_TYPING = "ON_TYPING";
    public static final String ON_NEW_CLAIM = "ON_NEW_CLAIM";
    public static final String ON_PRIVATE_MSG = "ON_PRIVATE_MSG";
    public static final String ON_WALLET_FUNDED = "ON_WALLET_FUNDED";
    public static final String ON_POSTS_SINCE_INTERACTED = "ON_POSTS_SINCE_INTERACTED";

    public enum NotificationTypes {
        CREATED_POST(1),
        DELETED_POST(2),
        TYPING(3),
        NEW_CLAIM(4),
        PRIVATE_MSG(5),
        WALLET_FUNDED(6),
        POSTS_SINCE_INTERACTED(7);

        private final int id;

        @SuppressLint("UseSparseArrays")
        private static Map<Integer, NotificationTypes> map = new HashMap<>();

        static {
            for (NotificationTypes item : NotificationTypes.values()) {
                map.put(item.id, item);
            }
        }

        NotificationTypes(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }

        public static NotificationTypes valueOf(int id) {
            return map.get(id);
        }
    }

    private TeambrellaServer.TeambrellaSocketClient mTeambrellaSocketClient;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;

        if (action != null) {
            switch (action) {
                case CONNECT_ACTION:
                    URI uri = URI.create(new Uri.Builder()
                            .scheme("wss")
                            .authority("surilla.com")
                            .appendEncodedPath("wshandler.ashx")
                            .build().toString());
                    mTeambrellaSocketClient = new TeambrellaServer(this, TeambrellaUser.get(this).getPrivateKey())
                            .createSocketClient(uri, intent.getIntExtra(EXTRA_TEAM_ID, 0), this);
                    mTeambrellaSocketClient.connect();
                    return START_STICKY;
                case STOP_ACTION:
                    if (mTeambrellaSocketClient != null) {
                        mTeambrellaSocketClient.close();
                    }
                    stopSelf();
                    return START_NOT_STICKY;

                case MESSAGE_ACTION:
                    String message = intent.getStringExtra(EXTRA_MESSAGE);
                    onMessage(message);
                    Log.e("TEST", message);
                    return START_STICKY;

            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onMessage(String message) {
        String messageParts[] = message.split(";");
        Intent intent = null;

        NotificationTypes type = NotificationTypes.valueOf(Integer.parseInt(messageParts[0]));
        switch (type) {
            case CREATED_POST:
                intent = new Intent(ON_CREATED_POST);
                break;

            case DELETED_POST:
                intent = new Intent(ON_DELETED_POST);
                break;

            case TYPING:
                intent = new Intent(ON_TYPING);
                break;

            case NEW_CLAIM:
                intent = new Intent(ON_NEW_CLAIM);
                intent.putExtra(EXTRA_TEAM_ID, Integer.parseInt(messageParts[1]));
                intent.putExtra(EXTRA_TEAMMATE_ID, Integer.parseInt(messageParts[2]));
                intent.putExtra(EXTRA_CLAIM_ID, Integer.parseInt(messageParts[3]));
                intent.putExtra(EXTRA_NAME, messageParts[4]);
                intent.putExtra(EXTRA_IMGURL, messageParts[5]);
                intent.putExtra(EXTRA_FIAT_AMOUNT, messageParts[6]);
                intent.putExtra(EXTRA_TEAM_IMGURL, messageParts[7]);
                intent.putExtra(EXTRA_TEAMNAME, messageParts[8]);
                break;

            case PRIVATE_MSG:
                intent = new Intent(ON_PRIVATE_MSG);
                intent.putExtra(EXTRA_USER_ID, messageParts[1]);
                intent.putExtra(EXTRA_NAME, messageParts[2]);
                intent.putExtra(EXTRA_IMGURL, messageParts[3]);
                intent.putExtra(EXTRA_MESSAGE, messageParts[4]);
                break;

            case WALLET_FUNDED:
                intent = new Intent(ON_WALLET_FUNDED);
                intent.putExtra(EXTRA_TEAM_ID, Integer.parseInt(messageParts[1]));
                intent.putExtra(EXTRA_USER_ID, messageParts[2]);
                intent.putExtra(EXTRA_CRYPTO_AMOUNT, messageParts[3]);
                intent.putExtra(EXTRA_FIAT_AMOUNT, messageParts[4]);
                intent.putExtra(EXTRA_TEAM_IMGURL, messageParts[4]);
                intent.putExtra(EXTRA_TEAMNAME, messageParts[5]);
                break;

            case POSTS_SINCE_INTERACTED:
                intent = new Intent(ON_POSTS_SINCE_INTERACTED);
                intent.putExtra(EXTRA_COUNT, Integer.parseInt(messageParts[1]));
                break;
        }

        sendOrderedBroadcast(intent, null, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(LOG_TAG, "on last recieve");
                processMessage(intent);
            }
        }, null, Activity.RESULT_OK, null, null);
        //((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(300);

    }

    private void processMessage(Intent intent) {
        String action = intent != null ? intent.getAction() : null;

        if (action == null) {
            return;
        }

        if (!Objects.equals(action, ON_NEW_CLAIM)
                && !action.equals(ON_PRIVATE_MSG)
                && !action.equals(ON_WALLET_FUNDED)
                && !action.equals(ON_POSTS_SINCE_INTERACTED)) {
            return;
        }

        Intent resultIntent = null;
        int mNotificationId = 1;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_teambrella_status)
                        .setAutoCancel(true);


        switch (action) {
            case ON_NEW_CLAIM:
                resultIntent = ClaimActivity.getLaunchIntent(this,
                        intent.getIntExtra(EXTRA_CLAIM_ID, 0),
                        null,
                        intent.getIntExtra(EXTRA_TEAM_ID, 0));

                mBuilder.setContentTitle(getString(R.string.notification_claim_header, intent.getStringExtra(EXTRA_TEAMNAME)));
                mBuilder.setContentText(getString(R.string.notification_claim_text, intent.getStringExtra(EXTRA_NAME), intent.getStringExtra(EXTRA_FIAT_AMOUNT)));
                mNotificationId = 1;
                break;

            case ON_PRIVATE_MSG:
                mNotificationId = 2;
                break;

            case ON_WALLET_FUNDED:
                resultIntent = ClaimActivity.getLaunchIntent(this,
                        intent.getIntExtra(EXTRA_CLAIM_ID, 0),
                        null,
                        intent.getIntExtra(EXTRA_TEAM_ID, 0));

                mBuilder.setContentTitle(getString(R.string.notification_funded_header));
                mBuilder.setContentText("+ " + intent.getStringExtra(EXTRA_FIAT_AMOUNT) + "mETH");
                mNotificationId = 3;
                break;

            case ON_POSTS_SINCE_INTERACTED:
                mNotificationId = 4;
                break;
        }

//        PendingIntent resultPendingIntent =
//                PendingIntent.getActivity(
//                        this,
//                        0,
//                        resultIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//
//        mBuilder.setContentIntent(resultPendingIntent);
//        Notification notification = mBuilder.build();
//        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        mNotifyMgr.notify(mNotificationId, notification);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.e(LOG_TAG, "on close " + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(LOG_TAG, "on error");
    }
}

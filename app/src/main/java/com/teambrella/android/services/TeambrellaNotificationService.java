package com.teambrella.android.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
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
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Teambrella Notification Service
 */
public class TeambrellaNotificationService extends Service implements TeambrellaServer.SocketClientListener {


    public interface ITeambrellaNotificationServiceBinder extends IBinder {

        interface INotificationServiceListener {

            boolean onPostCreated(int teamId, int teammateId, String topicId, String postId, String name, String avatar, String text);

            boolean onPostDeleted(int teamId, int teammateId, String topicId, String postId);

            boolean onTyping(int teamId, int teammateId, String topicId, String name);

            boolean onNewClaim(int teamId, int teammateId, int claimId, String name, String avatar, String amount, String teamUrl, String teamName);

            boolean onPrivateMessage(String userId, String name, String avatar, String text);

            boolean onWalletFunded(int teammateId, String userId, String cryptoAmount, String currencyAmount, String teamUrl, String teamName);

            boolean onPostsSinceInteracted(int count);

        }

        void registerListener(INotificationServiceListener listener);

        void unregisterListener(INotificationServiceListener listener);
    }


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


    private CopyOnWriteArrayList<ITeambrellaNotificationServiceBinder.INotificationServiceListener> mListeners = new CopyOnWriteArrayList<>();


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
        return new TeambrellaNotificationServiceBinder();
    }


    private class TeambrellaNotificationServiceBinder extends Binder implements ITeambrellaNotificationServiceBinder {

        @Override
        public void registerListener(INotificationServiceListener listener) {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }

        @Override
        public void unregisterListener(INotificationServiceListener listener) {
            mListeners.remove(listener);
        }
    }


    private boolean notifyPostCreated(int teamId, int teammateId, String topicId, String postId, String name, String avatar, String text) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onPostCreated(teamId, teammateId, topicId, postId, name, avatar, text);
        }
        return result;
    }

    private boolean notifyPostDeleted(int teamId, int teammateId, String topicId, String postId) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onPostDeleted(teamId, teammateId, topicId, postId);
        }
        return result;
    }

    private boolean notifyTyping(int teamId, int teammateId, String topicId, String name) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onTyping(teamId, teammateId, topicId, name);
        }
        return result;
    }

    private boolean notifyNewClaim(int teamId, int teammateId, int claimId, String name, String avatar, String amount, String teamUrl, String teamName) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onNewClaim(teamId, teammateId, claimId, name, avatar, amount, teamUrl, teamName);
        }
        return result;
    }

    private boolean notifyPrivateMessage(String userId, String name, String avatar, String text) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onPrivateMessage(userId, name, avatar, text);
        }
        return result;
    }

    private boolean notifyWalletFunded(int teammateId, String userId, String cryptoAmount, String currencyAmount, String teamUrl, String teamName) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onWalletFunded(teammateId, userId, cryptoAmount, currencyAmount, teamUrl, teamName);
        }
        return result;
    }

    private boolean notifyPostsSinceInteracted(int count) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onPostsSinceInteracted(count);
        }
        return result;
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
            case CREATED_POST: {
                int teamId = Integer.parseInt(messageParts[1]);
                int teammateId = Integer.parseInt(messageParts[2]);
                String topicId = messageParts[3];
                String postId = messageParts[4];
                String name = messageParts[5];
                String avatar = messageParts[6];
                String text = messageParts[7];
                if (!notifyPostCreated(teamId, teammateId, topicId, postId, name, avatar, text)) {
                    intent = new Intent(ON_CREATED_POST);
                }
            }
            break;

            case DELETED_POST: {
                int teamId = Integer.parseInt(messageParts[1]);
                int teammateId = Integer.parseInt(messageParts[2]);
                String topicId = messageParts[3];
                String postId = messageParts[4];
                if (!notifyPostDeleted(teamId, teammateId, topicId, postId)) {
                    intent = new Intent(ON_DELETED_POST);
                }
            }
            break;

            case TYPING: {
                int teamId = Integer.parseInt(messageParts[1]);
                int teammateId = Integer.parseInt(messageParts[2]);
                String topicId = messageParts[3];
                String name = messageParts[4];
                if (!notifyTyping(teamId, teammateId, topicId, name)) {
                    intent = new Intent(ON_TYPING);
                }
            }
            break;

            case NEW_CLAIM: {
                int teamId = Integer.parseInt(messageParts[1]);
                int teammateId = Integer.parseInt(messageParts[2]);
                int claimId = Integer.parseInt(messageParts[3]);
                String name = messageParts[4];
                String imageUrl = messageParts[5];
                String amount = messageParts[6];
                String teamImgUrl = messageParts[7];
                String teamName = messageParts[8];

                if (!notifyNewClaim(teamId, teammateId, claimId, name, imageUrl, amount, teamImgUrl, teamName)) {
                    intent = new Intent(ON_NEW_CLAIM);
                    intent.putExtra(EXTRA_TEAM_ID, teamId);
                    intent.putExtra(EXTRA_TEAMMATE_ID, teammateId);
                    intent.putExtra(EXTRA_CLAIM_ID, claimId);
                    intent.putExtra(EXTRA_NAME, name);
                    intent.putExtra(EXTRA_IMGURL, imageUrl);
                    intent.putExtra(EXTRA_FIAT_AMOUNT, amount);
                    intent.putExtra(EXTRA_TEAM_IMGURL, teamImgUrl);
                    intent.putExtra(EXTRA_TEAMNAME, teamName);
                }
            }
            break;

            case PRIVATE_MSG: {
                String userId = messageParts[1];
                String name = messageParts[2];
                String imgUrl = messageParts[3];
                String text = messageParts[4];
                if (!notifyPrivateMessage(userId, name, imgUrl, text)) {
                    intent = new Intent(ON_PRIVATE_MSG);
                    intent.putExtra(EXTRA_USER_ID, userId);
                    intent.putExtra(EXTRA_NAME, name);
                    intent.putExtra(EXTRA_IMGURL, imgUrl);
                    intent.putExtra(EXTRA_MESSAGE, text);
                }
            }
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

        if (intent != null) {
            processMessage(intent);
        }

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

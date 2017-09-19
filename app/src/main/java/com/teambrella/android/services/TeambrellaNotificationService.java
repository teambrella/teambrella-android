package com.teambrella.android.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.util.ConnectivityUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Teambrella Notification Service
 */
public class TeambrellaNotificationService extends Service implements TeambrellaServer.SocketClientListener {


    public interface ITeambrellaNotificationServiceBinder extends IBinder {

        interface INotificationServiceListener {

            boolean onPostCreated(int teamId, String userId, String topicId, String postId, String name, String avatar, String text);

            boolean onPostDeleted(int teamId, String userId, String topicId, String postId);

            boolean onTyping(int teamId, String userId, String topicId, String name);

            boolean onNewClaim(int teamId, String userId, int claimId, String name, String avatar, String amount, String teamUrl, String teamName);

            boolean onPrivateMessage(String userId, String name, String avatar, String text);

            boolean onWalletFunded(int teamId, String userId, String cryptoAmount, String currencyAmount, String teamUrl, String teamName);

            boolean onPostsSinceInteracted(int count);

        }

        void registerListener(INotificationServiceListener listener);

        void unregisterListener(INotificationServiceListener listener);
    }


    public static final String LOG_TAG = TeambrellaNotificationService.class.getSimpleName();

    public static final String CONNECT_ACTION = "connect";
    public static final String MESSAGE_ACTION = "message";

    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    private CopyOnWriteArrayList<ITeambrellaNotificationServiceBinder.INotificationServiceListener> mListeners = new CopyOnWriteArrayList<>();


    public enum NotificationTypes {
        CREATED_POST(1),
        DELETED_POST(2),
        TYPING(13),
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
    private TeambrellaNotificationManager mTeambrellaNotificationManager;


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


    private boolean notifyPostCreated(int teamId, String userId, String topicId, String postId, String name, String avatar, String text) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onPostCreated(teamId, userId, topicId, postId, name, avatar, text);
        }
        return result;
    }

    private boolean notifyPostDeleted(int teamId, String userId, String topicId, String postId) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onPostDeleted(teamId, userId, topicId, postId);
        }
        return result;
    }

    private boolean notifyTyping(int teamId, String userId, String topicId, String name) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onTyping(teamId, userId, topicId, name);
        }
        return result;
    }

    private boolean notifyNewClaim(int teamId, String userId, int claimId, String name, String avatar, String amount, String teamUrl, String teamName) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onNewClaim(teamId, userId, claimId, name, avatar, amount, teamUrl, teamName);
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
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectivityBroadcastReceiver, filter);
        mTeambrellaNotificationManager = new TeambrellaNotificationManager(this);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mConnectivityBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;

        if (action != null) {
            switch (action) {
                case CONNECT_ACTION:
                    if (mTeambrellaSocketClient == null && TeambrellaUser.get(this).getPrivateKey() != null) {
                        URI uri = URI.create(new Uri.Builder()
                                .scheme("wss")
                                .authority(TeambrellaServer.AUTHORITY)
                                .appendEncodedPath("wshandler.ashx")
                                .build().toString());
                        mTeambrellaSocketClient = new TeambrellaServer(this, TeambrellaUser.get(this).getPrivateKey())
                                .createSocketClient(uri, this);
                        mTeambrellaSocketClient.connect();
                    }
                    return START_STICKY;
                case MESSAGE_ACTION:
                    String message = intent.getStringExtra(EXTRA_MESSAGE);
                    Log.e(LOG_TAG, message);
                    onMessage(message);
                    return START_STICKY;
                case Intent.ACTION_BOOT_COMPLETED:
                    Log.e(LOG_TAG, "boot complete");
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onMessage(String message) {
        try {
            processMessage(message);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }

    }

    private void processMessage(String message) {
        Log.d(LOG_TAG, message);
        String messageParts[] = message.split(";");
        NotificationTypes type = NotificationTypes.valueOf(Integer.parseInt(messageParts[0]));
        switch (type) {
            case CREATED_POST: {
                int teamId = Integer.parseInt(messageParts[1]);
                String userId = messageParts[2];
                String topicId = messageParts[3];
                String postId = messageParts[4];
                String name = messageParts[5];
                String avatar = messageParts[6];
                String text = messageParts[7];
                notifyPostCreated(teamId, userId, topicId, postId, name, avatar, text);
            }
            break;

            case DELETED_POST: {
                int teamId = Integer.parseInt(messageParts[1]);
                String userId = messageParts[2];
                String topicId = messageParts[3];
                String postId = messageParts[4];
                notifyPostDeleted(teamId, userId, topicId, postId);
            }
            break;

            case TYPING: {
                int teamId = Integer.parseInt(messageParts[1]);
                String userId = messageParts[2];
                String topicId = messageParts[3];
                String name = messageParts[4];
                notifyTyping(teamId, userId, topicId, name);
            }
            break;

            case NEW_CLAIM: {
                int teamId = Integer.parseInt(messageParts[1]);
                String userId = messageParts[2];
                int claimId = Integer.parseInt(messageParts[3]);
                String name = messageParts[4];
                String imageUrl = messageParts[5];
                String amount = messageParts[6];
                String teamImgUrl = messageParts[7];
                String teamName = messageParts[8];
                if (!notifyNewClaim(teamId, userId, claimId, name, imageUrl, amount, teamImgUrl, teamName)) {
                    mTeambrellaNotificationManager.showNewClaimNotification(teamId, claimId, name, amount, teamName);
                }
            }
            break;

            case PRIVATE_MSG: {
                String userId = messageParts[1];
                String name = messageParts[2];
                String imgUrl = messageParts[3];
                String text = messageParts[4];
                if (!notifyPrivateMessage(userId, name, imgUrl, text)) {
                    mTeambrellaNotificationManager.showPrivateMessageNotification(userId, name, imgUrl, text);
                }
            }
            break;

            case WALLET_FUNDED: {
                int teamId = Integer.parseInt(messageParts[1]);
                String userId = messageParts[2];
                String mEthAmount = messageParts[3];
                String amount = messageParts[4];
                String teamImgUrl = messageParts[5];
                String teamName = messageParts[6];
                if (!notifyWalletFunded(teamId, userId, mEthAmount, amount, teamImgUrl, teamName)) {
                    mTeambrellaNotificationManager.showWalletIsFundedNotification(mEthAmount);
                }
            }
            break;

            case POSTS_SINCE_INTERACTED:
                int count = Integer.parseInt(messageParts[1]);
                if (!notifyPostsSinceInteracted(count)) {
                    mTeambrellaNotificationManager.showNewMessagesSinceLastVisit(count);
                }
                break;
        }
    }


    @Override
    public void onOpen() {
        Log.e(LOG_TAG, "on Open");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.e(LOG_TAG, "on close " + reason);
        if (mTeambrellaSocketClient != null) {
            mTeambrellaSocketClient = null;
        }
    }

    @Override
    public void onError(Exception ex) {
        Log.e(LOG_TAG, "on error " + ex.getMessage());
        if (mTeambrellaSocketClient != null) {
            mTeambrellaSocketClient.close();
            mTeambrellaSocketClient = null;
        }
    }


    private BroadcastReceiver mConnectivityBroadcastReceiver = new BroadcastReceiver() {

        private Handler mHandler = new Handler();

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityUtils.isNetworkAvailable(TeambrellaNotificationService.this)) {
                mHandler.removeCallbacks(mRunnable);
                mHandler.postDelayed(mRunnable, 1000);
            }
        }

        private Runnable mRunnable = () ->
                startService(new Intent(TeambrellaNotificationService.this, TeambrellaNotificationService.class).setAction(CONNECT_ACTION));
    };


}

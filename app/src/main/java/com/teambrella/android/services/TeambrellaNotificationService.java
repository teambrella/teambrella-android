package com.teambrella.android.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.teambrella.android.services.push.BundleNotificationMessage;
import com.teambrella.android.services.push.INotificationMessage;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.util.log.Log;

import java.util.concurrent.CopyOnWriteArrayList;

import static com.teambrella.android.services.push.KPushNotifications.CREATED_POST;
import static com.teambrella.android.services.push.KPushNotifications.DELETED_POST;
import static com.teambrella.android.services.push.KPushNotifications.NEW_CLAIM;
import static com.teambrella.android.services.push.KPushNotifications.POSTS_SINCE_INTERACTED;
import static com.teambrella.android.services.push.KPushNotifications.PRIVATE_MSG;
import static com.teambrella.android.services.push.KPushNotifications.TOPIC_MESSAGE_NOTIFICATION;
import static com.teambrella.android.services.push.KPushNotifications.TYPING;
import static com.teambrella.android.services.push.KPushNotifications.WALLET_FUNDED;


/**
 * Teambrella Notification Service
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class TeambrellaNotificationService extends Service {


    private static final String ACTION_ON_PUSH_MESSAGE = "onPushMessageAction";


    public interface ITeambrellaNotificationServiceBinder extends IBinder {

        interface INotificationServiceListener {

            boolean onPostCreated(int teamId, String userId, String topicId, String postId, String name, String avatar, String text);

            boolean onPostDeleted(int teamId, String userId, String topicId, String postId);

            boolean onTyping(int teamId, String userId, String topicId, String name);

            boolean onNewClaim(int teamId, String userId, int claimId, String name, String avatar, String amount, String teamUrl, String teamName);

            boolean onPrivateMessage(String userId, String name, String avatar, String text);

            boolean onWalletFunded(int teamId, String userId, String cryptoAmount, String currencyAmount, String teamUrl, String teamName);

            boolean onPostsSinceInteracted(int count);

            boolean onChatNotification(String topicId);

        }

        void registerListener(INotificationServiceListener listener);

        void unregisterListener(INotificationServiceListener listener);
    }


    public static final String LOG_TAG = TeambrellaNotificationService.class.getSimpleName();

    public static final String CONNECT_ACTION = "connect";

    private CopyOnWriteArrayList<ITeambrellaNotificationServiceBinder.INotificationServiceListener> mListeners = new CopyOnWriteArrayList<>();
    private TeambrellaNotificationSocketClient mTeambrellaSocketClient;
    private TeambrellaNotificationManager mTeambrellaNotificationManager;
    private TeambrellaUser mUser;


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

    public static void onPushMessage(Context context, INotificationMessage message) {
        context.startService(new Intent(context, TeambrellaNotificationService.class)
                .setAction(ACTION_ON_PUSH_MESSAGE)
                .putExtras(new BundleNotificationMessage(message).getData())
        );
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean notifyPostCreated(int teamId, String userId, String topicId, String postId, String name, String avatar, String text) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onPostCreated(teamId, userId, topicId, postId, name, avatar, text);
        }
        return result;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean notifyPostDeleted(int teamId, String userId, String topicId, String postId) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onPostDeleted(teamId, userId, topicId, postId);
        }
        return result;
    }

    @SuppressWarnings("UnusedReturnValue")
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

    private boolean notifyWalletFunded(int teamId, String userId, String cryptoAmount, String currencyAmount, String teamUrl, String teamName) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onWalletFunded(teamId, userId, cryptoAmount, currencyAmount, teamUrl, teamName);
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

    private boolean notifyChatNotification(String topicId) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onChatNotification(topicId);
        }
        return result;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mTeambrellaNotificationManager = new TeambrellaNotificationManager(this);
        mUser = TeambrellaUser.get(this);
    }

    @Override
    public void onDestroy() {
        if (mTeambrellaSocketClient != null) {
            mTeambrellaSocketClient.close();
        }
        super.onDestroy();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (action != null) {
            switch (action) {
                case CONNECT_ACTION:
                    TeambrellaUser user = TeambrellaUser.get(this);

                    if (mTeambrellaSocketClient != null) {
                        if (mTeambrellaSocketClient.isClosed()
                                || mTeambrellaSocketClient.isClosing()) {
                            mTeambrellaSocketClient.close();
                            mTeambrellaSocketClient = null;
                        }
                    }

                    if (mTeambrellaSocketClient == null && !user.isDemoUser() && user.getPrivateKey() != null) {
                        mTeambrellaSocketClient = new TeambrellaNotificationSocketClient(this);
                    }
                    return START_STICKY;

                case Intent.ACTION_BOOT_COMPLETED:
                    Log.e(LOG_TAG, "boot complete");
                    return START_STICKY;

                case ACTION_ON_PUSH_MESSAGE: {
                    INotificationMessage message = new BundleNotificationMessage(intent.getExtras());
                    if (!handlePushMessage(message)) {
                        mTeambrellaNotificationManager.handlePushMessage(message);
                    }
                }

            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    private boolean handlePushMessage(INotificationMessage message) {
        switch (message.getCmd()) {
            case CREATED_POST:
                notifyPostCreated(message.getTeamId(), message.getSenderUserId(), message.getTopicId()
                        , message.getPostId(), message.getSenderUserName(), message.getSenderAvatar(), message.getContent());
                return true;
            case DELETED_POST: {
                notifyPostDeleted(message.getTeamId(), message.getSenderUserId(), message.getTopicId(), message.getPostId());
                return true;
            }
            case TYPING: {
                notifyTyping(message.getTeamId(), message.getSenderUserId(), message.getTopicId(), message.getSenderUserName());
                return true;
            }
            case NEW_CLAIM: {
                return notifyNewClaim(message.getTeamId(), message.getSenderUserId(), message.getClaimId(), message.getSenderUserName(), message.getSenderAvatar()
                        , message.getAmount(), message.getTeamLogo(), message.getTeamName());
            }
            case PRIVATE_MSG: {
                return notifyPrivateMessage(message.getSenderUserId(), message.getSenderUserName(), message.getSenderAvatar(), message.getMessage());
            }
            case WALLET_FUNDED: {
                return notifyWalletFunded(message.getTeamId(), message.getSenderUserId(), message.getBalanceCrypto()
                        , message.getBalanceFiat(), message.getTeamLogo(), message.getTeamName());
            }
            case POSTS_SINCE_INTERACTED: {
                return notifyPostsSinceInteracted(message.getCount());
            }
            case TOPIC_MESSAGE_NOTIFICATION: {
                return notifyChatNotification(message.getTopicId());
            }

        }

        return false;
    }

}

package com.teambrella.android.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.util.TeambrellaUtilService;
import com.teambrella.android.util.log.Log;

import java.net.URI;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Teambrella Notification Service
 */
public class TeambrellaNotificationService extends Service implements TeambrellaServer.SocketClientListener {

    private static final String CMD = "Cmd";
    private static final String TIMESTAMP = TeambrellaModel.ATTR_STATUS_TIMESTAMP;
    private static final String USER_ID = TeambrellaModel.ATTR_DATA_USER_ID;
    private static final String TEAM_ID = TeambrellaModel.ATTR_DATA_TEAM_ID;
    private static final String TOPIC_ID = TeambrellaModel.ATTR_DATA_TOPIC_ID;
    private static final String POST_ID = "PostId";
    private static final String USER_NAME = "UserName";
    private static final String USER_IMAGE = TeambrellaModel.ATTR_DATA_AVATAR;
    private static final String CONTENT = "Content";
    private static final String CLAIM_ID = TeambrellaModel.ATTR_DATA_CLAIM_ID;
    private static final String AMOUNT = TeambrellaModel.ATTR_DATA_AMOUNT;
    private static final String TEAM_LOGO = TeambrellaModel.ATTR_DATA_TEAM_LOGO;
    private static final String TEAM_NAME = TeambrellaModel.ATTR_DATA_TEAM_NAME;
    private static final String COUNT = TeambrellaModel.ATTR_DATA_COUNT;
    private static final String BALANCE_CRYPTO = "BalanceCrypto";
    private static final String BALANCE_FIAT = "BalanceFiat";
    private static final String MESSAGE = TeambrellaModel.ATTR_REQUEST_MESSAGE;
    private static final String TEAMMATE_ID = TeambrellaModel.ATTR_DATA_TEAMMATE_ID;
    private static final String OBJECT_NAME = TeambrellaModel.ATTR_DATA_OBJECT_NAME;
    private static final String CLAIM_PHOTO = TeambrellaModel.ATTR_DATA_SMALL_PHOTO;
    private static final String TOPIC_NAME = "TopicName";
    private static final String MY_TOPIC = "MyTopic";
    private static final String TEAMMATE = "Teammate";
    private static final String CLAIM = "Claim";
    private static final String DISCUSSION = "Discussion";


    private static final int CREATED_POST = 1;
    private static final int DELETED_POST = 2;
    private static final int TYPING = 13;
    private static final int NEW_CLAIM = 4;
    private static final int PRIVATE_MSG = 5;
    private static final int WALLET_FUNDED = 6;
    private static final int POSTS_SINCE_INTERACTED = 7;
    private static final int NEW_TEAMMATE = 8;
    private static final int NEW_DISCUSSION = 9;
    private static final int TOPIC_MESSAGE_NOTIFICATION = 21;
    private static final int DEBUG_DB = 101;
    private static final int DEBUG_UPDATE = 102;
    private static final int DEBUG_SYNC = 103;


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
    public static final String MESSAGE_ACTION = "message";

    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    private CopyOnWriteArrayList<ITeambrellaNotificationServiceBinder.INotificationServiceListener> mListeners = new CopyOnWriteArrayList<>();
    private Gson mGson = new GsonBuilder().setLenient().create();

    private TeambrellaServer.TeambrellaSocketClient mTeambrellaSocketClient;
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


    @SuppressWarnings("UnusedReturnValue")
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
                        URI uri = URI.create(new Uri.Builder()
                                .scheme("wss")
                                .authority(TeambrellaServer.AUTHORITY)
                                .appendEncodedPath("wshandler.ashx")
                                .build().toString());
                        mTeambrellaSocketClient = new TeambrellaServer(this, user.getPrivateKey(), user.getDeviceCode()
                                , FirebaseInstanceId.getInstance().getToken(), user.getInfoMask(this))
                                .createSocketClient(uri, this, mUser.getNotificationTimeStamp());
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
            Log.reportNonFatal(LOG_TAG, e);
        }

    }

    private void processMessage(String message) {

        JsonWrapper messageWrapper = new JsonWrapper(mGson.fromJson(message, JsonObject.class));


        if (BuildConfig.DEBUG) {
            Log.e(LOG_TAG, message);
        }

        int command = messageWrapper.getInt(CMD, -1);
        long timestamp = messageWrapper.getLong(TIMESTAMP, -1);

        if (timestamp != -1 && timestamp <= mUser.getNotificationTimeStamp()) {
            return;
        }

        if (timestamp > 0) {
            mUser.setNotificationTimeStamp(timestamp);
        }

        switch (command) {
            case CREATED_POST: {
                notifyPostCreated(messageWrapper.getInt(TEAM_ID)
                        , messageWrapper.getString(USER_ID)
                        , messageWrapper.getString(TOPIC_ID)
                        , messageWrapper.getString(POST_ID)
                        , messageWrapper.getString(USER_IMAGE)
                        , messageWrapper.getString(USER_IMAGE)
                        , messageWrapper.getString(CONTENT));
            }
            break;

            case DELETED_POST: {
                int teamId = messageWrapper.getInt(TEAM_ID);
                String userId = messageWrapper.getString(USER_ID);
                String topicId = messageWrapper.getString(TOPIC_ID);
                String postId = messageWrapper.getString(POST_ID);
                notifyPostDeleted(teamId, userId, topicId, postId);
            }
            break;

            case TYPING: {
                int teamId = messageWrapper.getInt(TEAM_ID);
                String userId = messageWrapper.getString(USER_ID);
                String topicId = messageWrapper.getString(TOPIC_ID);
                String userName = messageWrapper.getString(USER_NAME);
                notifyTyping(teamId, userId, topicId, userName);
            }
            break;

            case NEW_CLAIM: {
                int teamId = messageWrapper.getInt(TEAM_ID);
                String userId = messageWrapper.getString(USER_ID);
                int claimId = messageWrapper.getInt(CLAIM_ID);
                String name = messageWrapper.getString(USER_NAME);
                String imageUrl = messageWrapper.getString(USER_IMAGE);
                String amount = messageWrapper.getString(AMOUNT);
                String teamImgUrl = messageWrapper.getString(TEAM_LOGO);
                String teamName = messageWrapper.getString(TEAM_NAME);
                if (!notifyNewClaim(teamId, userId, claimId, name, imageUrl, amount, teamImgUrl, teamName)) {
                    mTeambrellaNotificationManager.showNewClaimNotification(teamId, claimId, name, amount, teamName);
                }
            }
            break;

            case PRIVATE_MSG: {
                String userId = messageWrapper.getString(USER_ID);
                String name = messageWrapper.getString(USER_NAME);
                String imgUrl = messageWrapper.getString(USER_IMAGE);
                String text = messageWrapper.getString(MESSAGE);
                if (!notifyPrivateMessage(userId, name, imgUrl, text)) {
                    mTeambrellaNotificationManager.showPrivateMessageNotification(userId, name, imgUrl, text);
                }
            }
            break;

            case WALLET_FUNDED: {
                int teamId = messageWrapper.getInt(TEAM_ID);
                String userId = messageWrapper.getString(USER_ID);
                String mEthAmount = messageWrapper.getString(BALANCE_CRYPTO);
                String amount = messageWrapper.getString(BALANCE_FIAT);
                String teamImgUrl = messageWrapper.getString(TEAM_LOGO);
                String teamName = messageWrapper.getString(TEAM_NAME);
                if (!notifyWalletFunded(teamId, userId, mEthAmount, amount, teamImgUrl, teamName)) {
                    mTeambrellaNotificationManager.showWalletIsFundedNotification(mEthAmount);
                }
            }
            break;

            case POSTS_SINCE_INTERACTED:
                int count = messageWrapper.getInt(COUNT);
                if (!notifyPostsSinceInteracted(count)) {
                    mTeambrellaNotificationManager.showNewMessagesSinceLastVisit(count);
                }
                break;

            case NEW_TEAMMATE: {
                mTeambrellaNotificationManager.showNewTeammates(messageWrapper.getString(USER_NAME), messageWrapper.getInt(COUNT), messageWrapper.getString(TEAM_NAME));
            }
            break;
            case NEW_DISCUSSION: {
                //noinspection unused
                int teamId = messageWrapper.getInt(TEAM_ID);
                String userId = messageWrapper.getString(USER_ID);
                String topicId = messageWrapper.getString(TOPIC_ID);
                String topicName = messageWrapper.getString(TOPIC_NAME);
                //noinspection unused
                String postId = messageWrapper.getString(POST_ID);
                String userName = messageWrapper.getString(USER_NAME);
                //noinspection unused
                String avatar = messageWrapper.getString(USER_IMAGE);
                //noinspection unused
                String teamUrl = messageWrapper.getString(TEAM_LOGO);
                String teamName = messageWrapper.getString(TEAM_NAME);
                if (userId != null && !userId.equalsIgnoreCase(TeambrellaUser.get(this).getUserId())) {
                    mTeambrellaNotificationManager.showNewDiscussion(teamName, userName, teamId, topicName, topicId);
                }
            }
            break;

            case TOPIC_MESSAGE_NOTIFICATION: {
                String userId = messageWrapper.getString(USER_ID);
                if (!notifyChatNotification(messageWrapper.getString(TOPIC_ID))) {
                    if (userId != null && !userId.equalsIgnoreCase(TeambrellaUser.get(this).getUserId())) {
                        JsonWrapper teammate = messageWrapper.getObject(TEAMMATE);
                        JsonWrapper claim = messageWrapper.getObject(CLAIM);
                        JsonWrapper discussion = messageWrapper.getObject(DISCUSSION);
                        if (teammate != null) {
                            mTeambrellaNotificationManager.showNewPublicChatMessage(TeambrellaNotificationManager.ChatType.APPLICATION
                                    , teammate.getString(USER_NAME)
                                    , messageWrapper.getString(USER_NAME)
                                    , messageWrapper.getString(CONTENT)
                                    , messageWrapper.getBoolean(MY_TOPIC, false)
                                    , messageWrapper.getString(TOPIC_ID)
                                    , ChatActivity.getTeammateChat(this
                                            , messageWrapper.getInt(TEAM_ID)
                                            , teammate.getString(USER_ID)
                                            , teammate.getString(USER_NAME)
                                            , teammate.getString(USER_IMAGE)
                                            , messageWrapper.getString(TOPIC_ID)
                                            , TeambrellaModel.TeamAccessLevel.FULL_ACCESS));
                        } else if (claim != null) {
                            mTeambrellaNotificationManager.showNewPublicChatMessage(TeambrellaNotificationManager.ChatType.CLAIM
                                    , claim.getString(USER_NAME)
                                    , messageWrapper.getString(USER_NAME)
                                    , messageWrapper.getString(CONTENT)
                                    , messageWrapper.getBoolean(MY_TOPIC, false)
                                    , messageWrapper.getString(TOPIC_ID)
                                    , ChatActivity.getClaimChat(this
                                            , messageWrapper.getInt(TEAM_ID)
                                            , claim.getInt(CLAIM_ID)
                                            , claim.getString(OBJECT_NAME)
                                            , claim.getString(CLAIM_PHOTO)
                                            , messageWrapper.getString(TOPIC_ID)
                                            , TeambrellaModel.TeamAccessLevel.FULL_ACCESS));
                        } else if (discussion != null) {
                            mTeambrellaNotificationManager.showNewPublicChatMessage(TeambrellaNotificationManager.ChatType.DISCUSSION
                                    , discussion.getString(TOPIC_NAME)
                                    , messageWrapper.getString(USER_NAME)
                                    , messageWrapper.getString(CONTENT)
                                    , messageWrapper.getBoolean(MY_TOPIC, false)
                                    , messageWrapper.getString(TOPIC_ID)
                                    , ChatActivity.getFeedChat(this
                                            , discussion.getString(TOPIC_NAME)
                                            , messageWrapper.getString(TOPIC_ID)
                                            , messageWrapper.getInt(TEAM_ID)
                                            , TeambrellaModel.TeamAccessLevel.FULL_ACCESS));
                        }
                    }
                }
            }
            break;

            case DEBUG_DB:
                TeambrellaUtilService.scheduleDebugDB(this);
                break;
            case DEBUG_SYNC:
                TeambrellaUtilService.oneoffWalletSync(this, true, true);
                break;
            case DEBUG_UPDATE:
                TeambrellaUtilService.oneOffUpdate(this, true);
                break;
        }
    }


    @Override
    public void onOpen() {
        if (BuildConfig.DEBUG) {
            Log.e(LOG_TAG, "on Open");
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (BuildConfig.DEBUG) {
            Log.e(LOG_TAG, "on close " + reason);
        }
        if (mTeambrellaSocketClient != null) {
            mTeambrellaSocketClient.close();
            mTeambrellaSocketClient = null;
        }
    }

    @Override
    public void onError(Exception ex) {
        if (BuildConfig.DEBUG) {
            Log.e(LOG_TAG, "on error " + ex.getMessage());
        }
        if (!BuildConfig.DEBUG) {
            Crashlytics.logException(ex);
        }
        if (mTeambrellaSocketClient != null) {
            mTeambrellaSocketClient.close();
            mTeambrellaSocketClient = null;
        }
    }
}

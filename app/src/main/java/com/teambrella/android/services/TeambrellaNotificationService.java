package com.teambrella.android.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.util.TeambrellaUtilService;
import com.teambrella.android.util.log.Log;

import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Teambrella Notification Service
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class TeambrellaNotificationService extends Service {

    private static final String EXTRA_TEAM_ID = "team_id";
    private static final String EXTRA_SENDER_NAME = "sender_name";
    private static final String EXTRA_SENDER_AVATAR = "sender_avatar";
    private static final String EXTRA_TEAMMATE_USER_ID = "teammte_user_id";
    private static final String EXTRA_TEAMMATE_AVATAR = "teammate_avatar";
    private static final String EXTRA_SENDER_USER_ID = "user_id";
    private static final String EXTRA_TOPIC_ID = "topic_id";
    private static final String EXTRA_TOPIC_NAME = "topic_name";
    private static final String EXTRA_POST_ID = "post_id";
    private static final String EXTRA_NAME = "name";
    private static final String EXTRA_CLAIMER_NAME = "claimer_name";
    private static final String EXTRA_TEXT = "text";
    private static final String EXTRA_CLAIM_ID = "claimId";
    private static final String EXTRA_AMOUNT = "amount";
    private static final String EXTRA_TEAM_URL = "teamUrl";
    private static final String EXTRA_TEAM_NAME = "teamName";
    private static final String EXTRA_TEAMMATE_ID = "teammateId";
    private static final String EXTRA_BALANCE_CRYPTO = "balanceCrypto";
    private static final String EXTRA_BALANCE_FIAT = "balanceFiat";
    private static final String EXTRA_COUNT = "count";
    private static final String EXTRA_TEAMMATE_NAME = "teammateName";
    private static final String EXTRA_IS_MY_TOPIC = "isMyTopic";
    private static final String EXTRA_SUBJECT_NAME = "subject_name";
    private static final String EXTRA_OBJECT_NAME = "object_name";
    private static final String EXTRA_CLAIM_PHOTO = "object_photo";


    private static final String ACTION_ON_POST_CREATED = "on_post_created_action";
    private static final String ACTION_ON_POST_DELETED = "on_post_deleted_action";
    private static final String ACTION_ON_TYPING = "on_typing_action";
    private static final String ACTION_ON_NEW_CLAIM = "on_new_claim_action";
    private static final String ACTION_ON_PRIVATE_MESSAGE = "on_private_message_action";
    private static final String ACTION_ON_WALLET_FUNDED = "on_wallet_funded_action";
    private static final String ACTION_ON_POSTS_SINCE_INTERACTED = "on_posts_since_interacted_action";
    private static final String ACTION_ON_NEW_DISCUSSION = "on_new_discussion_action";
    private static final String ACTION_ON_NEW_TEAMMATE = "on_new_teammate_action";
    private static final String ACTION_ON_NEW_APPLICATION_CHAT_MESSAGE = "on_new_application_chat_message_action";
    private static final String ACTION_ON_NEW_CLAIM_MESSAGE = "on_new_claim_chat_message_action";
    private static final String ACTION_ON_NEW_DISCUSSION_MESSAGE = "on_new_discussion_message_action";


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
    private Gson mGson = new GsonBuilder().setLenient().create();

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

    public static void onPostCreated(Context context, int teamId, String senderUserId,
                                     String topicId, String postId, String senderName, String senderAvatar, String text) {
        context.startService(new Intent(context, TeambrellaNotificationService.class)
                .setAction(ACTION_ON_POST_CREATED)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_SENDER_USER_ID, senderUserId)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_POST_ID, postId)
                .putExtra(EXTRA_SENDER_NAME, senderName)
                .putExtra(EXTRA_SENDER_AVATAR, senderAvatar)
                .putExtra(EXTRA_TEXT, text));
    }

    public static void onPostDeleted(Context context, int teamId, String senderUserId,
                                     String topicId, String postId) {
        context.startService(new Intent(context, TeambrellaNotificationService.class)
                .setAction(ACTION_ON_POST_DELETED)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_SENDER_USER_ID, senderUserId)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_POST_ID, postId));
    }

    public static void onTyping(Context context, int teamId, String senderUserId, String topicId, String senderName) {
        context.startService(new Intent(context, TeambrellaNotificationService.class)
                .setAction(ACTION_ON_TYPING)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_SENDER_USER_ID, senderUserId)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_SENDER_NAME, senderName)
        );
    }

    public static void onNewClaim(Context context, int teamId, String senderUserId, int claimId,
                                  String senderName, String senderAvatar, String amount,
                                  String teamUrl, String teamName) {
        context.startService(new Intent(context, TeambrellaNotificationService.class)
                .setAction(ACTION_ON_NEW_CLAIM)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_SENDER_USER_ID, senderUserId)
                .putExtra(EXTRA_CLAIM_ID, claimId)
                .putExtra(EXTRA_SENDER_NAME, senderName)
                .putExtra(EXTRA_SENDER_AVATAR, senderAvatar)
                .putExtra(EXTRA_AMOUNT, amount)
                .putExtra(EXTRA_TEAM_URL, teamUrl)
                .putExtra(EXTRA_TEAM_NAME, teamName)
        );
    }


    public static void onPrivateMessage(Context context, String senderUserId, String senderName, String senderAvatar, String text) {
        context.startService(new Intent(context, TeambrellaNotificationService.class)
                .setAction(ACTION_ON_PRIVATE_MESSAGE)
                .putExtra(EXTRA_SENDER_USER_ID, senderUserId)
                .putExtra(EXTRA_SENDER_NAME, senderName)
                .putExtra(EXTRA_SENDER_AVATAR, senderAvatar)
                .putExtra(EXTRA_TEXT, text)
        );
    }

    public static void onWalletFunded(Context context, int teammateId, String teammteUserId, String cryptoAmount, String currencyAmount, String teamUrl, String teamName) {
        context.startService(new Intent(context, TeambrellaNotificationService.class)
                .setAction(ACTION_ON_WALLET_FUNDED)
                .putExtra(EXTRA_TEAMMATE_ID, teammateId)
                .putExtra(EXTRA_TEAMMATE_USER_ID, teammteUserId)
                .putExtra(EXTRA_BALANCE_CRYPTO, cryptoAmount)
                .putExtra(EXTRA_BALANCE_FIAT, currencyAmount)
                .putExtra(EXTRA_TEAM_URL, teamUrl)
                .putExtra(EXTRA_TEAM_NAME, teamName)
        );
    }


    public static void onPostSinceInteracted(Context context, int count) {
        context.startService(new Intent(context, TeambrellaNotificationService.class)
                .setAction(ACTION_ON_POSTS_SINCE_INTERACTED)
                .putExtra(EXTRA_COUNT, count)
        );
    }

    public static void onNewTeammate(Context context, String name, int count, String teamName) {
        context.startService(new Intent(context, TeambrellaNotificationService.class)
                .setAction(ACTION_ON_NEW_TEAMMATE)
                .putExtra(EXTRA_NAME, name)
                .putExtra(EXTRA_COUNT, count)
                .putExtra(EXTRA_TEAM_NAME, teamName)

        );
    }

    public static void onNewDiscussion(Context context, int teamId, String senderUserId, String topicId,
                                       String topicName, String postId, String senderName,
                                       String senderAvatar, String teamUrl, String teamName) {
        context.startService(new Intent(context, TeambrellaNotificationService.class)
                .setAction(ACTION_ON_NEW_DISCUSSION)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_SENDER_USER_ID, senderUserId)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_POST_ID, postId)
                .putExtra(EXTRA_SENDER_NAME, senderName)
                .putExtra(EXTRA_SENDER_AVATAR, senderAvatar)
                .putExtra(EXTRA_TOPIC_NAME, topicName)
                .putExtra(EXTRA_TEAM_URL, teamUrl)
                .putExtra(EXTRA_TEAM_NAME, teamName)
        );
    }


    public static void onNewApplicationChatMessage(Context context, int teamId, String userId, String topicId,
                                                   String teammateName, String senderName, String senderAvatar,
                                                   String text, Boolean isMyTopic) {
        context.startService(new Intent(context, TeambrellaNotificationService.class)
                .setAction(ACTION_ON_NEW_APPLICATION_CHAT_MESSAGE)
                .putExtra(EXTRA_TEAM_ID, teamId)
                //.putExtra(EXTRA_SENDER_USER_ID, userId)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_SENDER_NAME, senderName)
                .putExtra(EXTRA_SENDER_AVATAR, senderAvatar)
                .putExtra(EXTRA_TEXT, text)
                .putExtra(EXTRA_TEAMMATE_NAME, teammateName)
                .putExtra(EXTRA_IS_MY_TOPIC, isMyTopic)
        );
    }

    public static void onNewClaimChatMessage(Context context, int teamId, int claimId, String claimerName,
                                             String senderName, String topicId, String text, String objectName, String claimPhoto, Boolean isMyTopic) {
        context.startService(new Intent(context, TeambrellaUtilService.class)
                .setAction(ACTION_ON_NEW_CLAIM_MESSAGE)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_CLAIM_ID, claimId)
                .putExtra(EXTRA_SUBJECT_NAME, claimerName)
                .putExtra(EXTRA_NAME, senderName)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_TEXT, text)
                .putExtra(EXTRA_OBJECT_NAME, objectName)
                .putExtra(EXTRA_CLAIM_PHOTO, claimPhoto)
                .putExtra(EXTRA_IS_MY_TOPIC, isMyTopic)
        );
    }


    public static void onNewDiscussionChatMessage(Context context, int teamId, String senderName, String topicName, String topicId, Boolean isMyTopic) {
        context.startService(new Intent(context, TeambrellaUtilService.class)
                .setAction(ACTION_ON_NEW_DISCUSSION_MESSAGE)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_SENDER_NAME, senderName)
                .putExtra(EXTRA_TOPIC_NAME, topicName)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_IS_MY_TOPIC, isMyTopic)
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

                case ACTION_ON_POST_CREATED:
                    notifyPostCreated(intent.getIntExtra(EXTRA_TEAM_ID, 0)
                            , intent.getStringExtra(EXTRA_SENDER_USER_ID)
                            , intent.getStringExtra(EXTRA_TOPIC_ID)
                            , intent.getStringExtra(EXTRA_POST_ID)
                            , intent.getStringExtra(EXTRA_SENDER_NAME)
                            , intent.getStringExtra(EXTRA_SENDER_AVATAR)
                            , intent.getStringExtra(EXTRA_TEXT));
                    return START_STICKY;
                case ACTION_ON_POST_DELETED:
                    notifyPostDeleted(intent.getIntExtra(EXTRA_TEAM_ID, 0)
                            , intent.getStringExtra(EXTRA_SENDER_USER_ID)
                            , intent.getStringExtra(EXTRA_TOPIC_ID)
                            , intent.getStringExtra(EXTRA_POST_ID));
                    return START_STICKY;

                case ACTION_ON_TYPING:
                    notifyTyping(intent.getIntExtra(EXTRA_TEAM_ID, 0)
                            , intent.getStringExtra(EXTRA_SENDER_USER_ID)
                            , intent.getStringExtra(EXTRA_TOPIC_ID)
                            , intent.getStringExtra(EXTRA_SENDER_NAME));
                    return START_STICKY;

                case ACTION_ON_NEW_CLAIM: {
                    int teamId = intent.getIntExtra(EXTRA_TEAM_ID, 0);
                    String userId = intent.getStringExtra(EXTRA_SENDER_USER_ID);
                    int claimId = intent.getIntExtra(EXTRA_CLAIM_ID, 0);
                    String name = intent.getStringExtra(EXTRA_SENDER_NAME);
                    String avatar = intent.getStringExtra(EXTRA_SENDER_AVATAR);
                    String amount = intent.getStringExtra(EXTRA_AMOUNT);
                    String teamUrl = intent.getStringExtra(EXTRA_TEAM_URL);
                    String teamName = intent.getStringExtra(EXTRA_TEAM_NAME);
                    if (!notifyNewClaim(teamId, userId, claimId, name, avatar, amount, teamUrl, teamName)) {
                        mTeambrellaNotificationManager.showNewClaimNotification(teamId, claimId, name, amount, teamName);
                    }
                    return START_STICKY;
                }

                case ACTION_ON_PRIVATE_MESSAGE: {
                    String userId = intent.getStringExtra(EXTRA_SENDER_USER_ID);
                    String name = intent.getStringExtra(EXTRA_SENDER_NAME);
                    String imgUrl = intent.getStringExtra(EXTRA_SENDER_AVATAR);
                    String text = intent.getStringExtra(EXTRA_TEXT);
                    if (!notifyPrivateMessage(userId, name, imgUrl, text)) {
                        mTeambrellaNotificationManager.showPrivateMessageNotification(userId, name, imgUrl, text);
                    }
                    return START_STICKY;
                }

                case ACTION_ON_WALLET_FUNDED: {
                    int teamId = intent.getIntExtra(EXTRA_TEAM_ID, 0);
                    String userId = intent.getStringExtra(EXTRA_TEAMMATE_USER_ID);
                    String mEthAmount = intent.getStringExtra(EXTRA_BALANCE_CRYPTO);
                    String amount = intent.getStringExtra(EXTRA_BALANCE_FIAT);
                    String teamImgUrl = intent.getStringExtra(EXTRA_TEAM_URL);
                    String teamName = intent.getStringExtra(EXTRA_TEAM_NAME);
                    if (!notifyWalletFunded(teamId, userId, mEthAmount, amount, teamImgUrl, teamName)) {
                        mTeambrellaNotificationManager.showWalletIsFundedNotification(mEthAmount);
                    }
                    return START_STICKY;
                }

                case ACTION_ON_POSTS_SINCE_INTERACTED: {

                    int count = intent.getIntExtra(EXTRA_COUNT, 0);
                    if (count > 0 && !notifyPostsSinceInteracted(count)) {
                        mTeambrellaNotificationManager.showNewMessagesSinceLastVisit(count);
                    }
                    return START_STICKY;
                }


                case ACTION_ON_NEW_DISCUSSION: {

                    //noinspection unused
                    int teamId = intent.getIntExtra(EXTRA_TEAM_ID, 0);
                    String userId = intent.getStringExtra(EXTRA_SENDER_USER_ID);
                    String topicId = intent.getStringExtra(EXTRA_TOPIC_ID);
                    String topicName = intent.getStringExtra(EXTRA_TOPIC_NAME);
                    String postId = intent.getStringExtra(EXTRA_POST_ID);
                    String userName = intent.getStringExtra(EXTRA_SENDER_NAME);
                    //noinspection unused
                    String avatar = intent.getStringExtra(EXTRA_SENDER_AVATAR);
                    //noinspection unused
                    String teamUrl = intent.getStringExtra(EXTRA_TEAM_URL);
                    String teamName = intent.getStringExtra(EXTRA_TEAM_NAME);
                    if (userId != null && !userId.equalsIgnoreCase(TeambrellaUser.get(this).getUserId())) {
                        mTeambrellaNotificationManager.showNewDiscussion(teamName, userName, teamId, topicName, topicId);
                    }

                    return START_STICKY;
                }

                case ACTION_ON_NEW_TEAMMATE: {

                    mTeambrellaNotificationManager.showNewTeammates(intent.getStringExtra(EXTRA_NAME)
                            , intent.getIntExtra(EXTRA_COUNT, 0)
                            , intent.getStringExtra(EXTRA_TEAM_NAME));
                }

                case ACTION_ON_NEW_APPLICATION_CHAT_MESSAGE: {

                    mTeambrellaNotificationManager.showNewPublicChatMessage(TeambrellaNotificationManager.ChatType.APPLICATION
                            , intent.getStringExtra(EXTRA_TEAMMATE_NAME)
                            , intent.getStringExtra(EXTRA_SENDER_NAME)
                            , intent.getStringExtra(EXTRA_TEXT)
                            , intent.getBooleanExtra(EXTRA_IS_MY_TOPIC, false)
                            , intent.getStringExtra(EXTRA_TOPIC_ID)
                            , ChatActivity.getTeammateChat(this
                                    , intent.getIntExtra(EXTRA_TEAM_ID, 0)
                                    , intent.getStringExtra(EXTRA_TEAMMATE_USER_ID)
                                    , intent.getStringExtra(EXTRA_TEAMMATE_NAME)
                                    , intent.getStringExtra(EXTRA_TEAMMATE_AVATAR)
                                    , intent.getStringExtra(EXTRA_TOPIC_ID)
                                    , TeambrellaModel.TeamAccessLevel.FULL_ACCESS));
                    return START_STICKY;
                }

                case ACTION_ON_NEW_CLAIM_MESSAGE: {

                    mTeambrellaNotificationManager.showNewPublicChatMessage(TeambrellaNotificationManager.ChatType.CLAIM
                            , intent.getStringExtra(EXTRA_CLAIMER_NAME)
                            , intent.getStringExtra(EXTRA_SENDER_NAME)
                            , intent.getStringExtra(EXTRA_TEXT)
                            , intent.getBooleanExtra(EXTRA_IS_MY_TOPIC, false)
                            , intent.getStringExtra(EXTRA_TOPIC_ID)
                            , ChatActivity.getClaimChat(this
                                    , intent.getIntExtra(EXTRA_TEAM_ID, 0)
                                    , intent.getIntExtra(EXTRA_CLAIM_ID, 0)
                                    , intent.getStringExtra(EXTRA_OBJECT_NAME)
                                    , intent.getStringExtra(EXTRA_CLAIM_PHOTO)
                                    , intent.getStringExtra(EXTRA_TOPIC_ID)
                                    , TeambrellaModel.TeamAccessLevel.FULL_ACCESS
                                    , null));
                    return START_STICKY;
                }

                case ACTION_ON_NEW_DISCUSSION_MESSAGE: {

                    mTeambrellaNotificationManager.showNewPublicChatMessage(TeambrellaNotificationManager.ChatType.DISCUSSION
                            , intent.getStringExtra(EXTRA_TOPIC_NAME)
                            , intent.getStringExtra(EXTRA_SENDER_NAME)
                            , intent.getStringExtra(EXTRA_TEXT)
                            , intent.getBooleanExtra(EXTRA_IS_MY_TOPIC, false)
                            , intent.getStringExtra(EXTRA_TOPIC_ID)
                            , ChatActivity.getFeedChat(this
                                    , intent.getStringExtra(EXTRA_TOPIC_NAME)
                                    , intent.getStringExtra(EXTRA_TOPIC_ID)
                                    , intent.getIntExtra(EXTRA_TEAM_ID, 0)
                                    , TeambrellaModel.TeamAccessLevel.FULL_ACCESS));

                    return START_STICKY;
                }

            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
}

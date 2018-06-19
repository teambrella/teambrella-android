package com.teambrella.android.util;

import android.content.Context;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.TeambrellaApplication;

/**
 * Statistic Helper
 */
public class StatisticHelper {

    public static final String MESSAGE_TEXT = "text";
    public static final String MESSAGE_IMAGE = "image";


    private static final String TRY_DEMO = "try_demo";
    private static final String USER_REGISTERED = "user_registered";
    private static final String CHAT_MESSAGE = "chat_message";
    private static final String PRIVATE_MESSAGE = "private_message";
    private static final String INVITE_FRIENDS = "invite_friends";
    private static final String WALLET_SYNC = "wallet_sync";
    private static final String PUSH_MESSAGE = "push_message";
    private static final String SHARE_PAID_CLAIM = "share_paid_claim";
    private static final String WALLET_BACKUP_CHECK = "wallet_backup_check";


    private static final String TEAM_ID = "team_id";
    private static final String TOPIC_ID = "topic_id";
    private static final String CLAIM_ID = "claim_id";
    private static final String TEAMMATE_ID = "teammate_id";
    private static final String VOTE = "vote";
    private static final String TYPE = "type";
    private static final String CMD = "Cmd";
    private static final String SOCKET = "Socket";
    private static final String STATUS = "status";


    private static final String CATEGORY_WALLET = "Wallet";
    private static final String ACTION_SYNC = "Sync";
    private static final String ACTION_SAVE = "Save";


    private static final String APPLICATION_VOTE = "application_vote";
    private static final String CLAIM_VOTE = "claim_vote";


    private static FirebaseAnalytics getAnalytics(Context context) {
        return ((TeambrellaApplication) context.getApplicationContext()).getFireBaseAnalytics();
    }


    public static void onTryDemo(Context context) {
        FirebaseAnalytics analytics = getAnalytics(context);
        analytics.logEvent(TRY_DEMO, null);
    }

    public static void onApplicationVote(Context context, int teamId, int teammateId, double vote) {
        FirebaseAnalytics analytics = getAnalytics(context);
        Bundle params = new Bundle();
        params.putString(TEAM_ID, Integer.toString(teamId));
        params.putString(TEAMMATE_ID, Integer.toString(teammateId));
        params.putDouble(VOTE, vote);
        analytics.logEvent(APPLICATION_VOTE, params);
    }

    public static void onClaimVote(Context context, int teamId, int claimId, int vote) {
        FirebaseAnalytics analytics = getAnalytics(context);
        Bundle params = new Bundle();
        params.putString(TEAM_ID, Integer.toString(teamId));
        params.putString(CLAIM_ID, Integer.toString(claimId));
        params.putDouble(VOTE, vote);
        analytics.logEvent(CLAIM_VOTE, params);
    }

    public static void onUserRegistered(Context context) {
        FirebaseAnalytics analytics = getAnalytics(context);
        analytics.logEvent(USER_REGISTERED, null);
    }

    public static void onWalletBackUpCheck(Context context, String status) {
        FirebaseAnalytics analytics = getAnalytics(context);
        Bundle params = new Bundle();
        params.putString(STATUS, status);
        analytics.logEvent(WALLET_BACKUP_CHECK, params);
    }

    public static void onWalletSync(Context context, String tag) {
        Tracker tracker = ((TeambrellaApplication) context.getApplicationContext()).geTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_WALLET)
                .setAction(ACTION_SYNC)
                .setLabel(tag)
                .build());

        FirebaseAnalytics analytics = getAnalytics(context);
        analytics.logEvent(WALLET_SYNC, null);
    }

    public static void onWalletSaved(Context context, String userId) {
        Tracker tracker = ((TeambrellaApplication) context.getApplicationContext()).geTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_WALLET)
                .setAction(ACTION_SAVE)
                .setLabel(userId)
                .build());
    }

    public static void onChatMessage(Context context, int teamId, String topicId, String type) {
        FirebaseAnalytics analytics = getAnalytics(context);
        Bundle params = new Bundle();
        params.putString(TEAM_ID, Integer.toString(teamId));
        params.putString(TOPIC_ID, topicId);
        params.putString(TYPE, type);
        analytics.logEvent(CHAT_MESSAGE, params);
    }

    public static void onPrivateMessage(Context context) {
        FirebaseAnalytics analytics = getAnalytics(context);
        analytics.logEvent(PRIVATE_MESSAGE, null);
    }

    public static void onPushMessage(Context context, String cmd, boolean socket) {
        FirebaseAnalytics analytics = getAnalytics(context);
        Bundle params = new Bundle();
        params.putString(CMD, cmd);
        params.putBoolean(SOCKET, socket);
        analytics.logEvent(PUSH_MESSAGE, params);
    }

    public static void onInviteFriends(Context context, int teamId) {
        FirebaseAnalytics analytics = getAnalytics(context);
        Bundle params = new Bundle();
        params.putString(TEAM_ID, Integer.toString(teamId));
        analytics.logEvent(INVITE_FRIENDS, params);
    }

    public static void sharePaidClaim(Context context, int claimId) {
        FirebaseAnalytics analytics = getAnalytics(context);
        Bundle params = new Bundle();
        params.putString(CLAIM_ID, Integer.toString(claimId));
        analytics.logEvent(SHARE_PAID_CLAIM, params);
    }


    public static void setUserId(String userId) {
        if (!BuildConfig.DEBUG) {
            Crashlytics.setUserIdentifier(userId);
        }
    }
}
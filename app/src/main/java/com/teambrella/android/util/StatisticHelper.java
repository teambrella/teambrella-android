package com.teambrella.android.util;

import android.content.Context;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.TeambrellaApplication;

/**
 * Statistic Helper
 */
public class StatisticHelper {

    private static final String TRY_DEMO = "Try Demo";
    private static final String APPLICATION_VOTE = "Application Vote";
    private static final String CLAIM_VOTE = "Claim Vote";
    private static final String USER_REGISTERED = "User Registered";
    private static final String NEW_POST = "New Post";
    private static final String NEW_CLAIM = "New Claim";
    private static final String WALLET_FUNDED = "Wallet Funded";
    private static final String NEW_TEAMMATE = "New Teammate";
    private static final String NEW_DISCUSSION = "New Discussion";
    private static final String BACK_UP = "Back Up";
    private static final String RESTORE = "Restore";
    private static final String GSL_BACKUP = "GSL Back Up";


    private static final String TEAM_ID = "TeamId";
    private static final String USER_ID = "UserId";
    private static final String TOPIC_ID = "TopicId";
    private static final String POST_ID = "PostId";
    private static final String CLAIM_ID = "ClaimId";
    private static final String AMOUNT = "Amount";
    private static final String TEAMMATE_ID = "TeammateId";
    private static final String VOTE = "Vote";


    private static final String CATEGORY_WALLET = "Wallet";
    private static final String ACTION_SYNC = "Sync";
    private static final String ACTION_SAVE = "Save";


    private static final String FIREBASE_APPLICATION_VOTE = "application_vote";

    public static void onTryDemo() {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(TRY_DEMO));
        }
    }

    public static void onApplicationVote(Context context, int teamId, double vote) {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(APPLICATION_VOTE)
                    .putCustomAttribute(TEAM_ID, Integer.toString(teamId))
                    .putCustomAttribute(VOTE, vote));
        }

        FirebaseAnalytics analytics = ((TeambrellaApplication) context.getApplicationContext()).getFireBaseAnalytics();
        Bundle params = new Bundle();
        params.putInt(TEAM_ID, teamId);
        params.putDouble(VOTE, vote);
        analytics.logEvent(FIREBASE_APPLICATION_VOTE, params);
    }

    public static void onClaimVote(int teamId, int vote) {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(CLAIM_VOTE)
                    .putCustomAttribute(TEAM_ID, Integer.toString(teamId))
                    .putCustomAttribute(VOTE, vote));
        }

    }

    public static void onUserRegistered() {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(USER_REGISTERED));
        }
    }


    public static void onPostCreatedNotification(int teamId, String userId, String topicId, String postId) {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(NEW_POST)
                    .putCustomAttribute(TEAM_ID, teamId)
                    .putCustomAttribute(USER_ID, userId)
                    .putCustomAttribute(TOPIC_ID, topicId)
                    .putCustomAttribute(POST_ID, postId));
        }
    }

    public static void onNewClaimNotification(int teamId, String userId, int claimId) {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(NEW_CLAIM)
                    .putCustomAttribute(TEAM_ID, Integer.toString(teamId))
                    .putCustomAttribute(USER_ID, userId)
                    .putCustomAttribute(CLAIM_ID, Integer.toString(claimId)));
        }
    }

    public static void onWalletFundedNotification(int teamId, String userId, String amount) {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(WALLET_FUNDED)
                    .putCustomAttribute(TEAM_ID, Integer.toString(teamId))
                    .putCustomAttribute(USER_ID, userId)
                    .putCustomAttribute(AMOUNT, amount));
        }
    }

    public static void onNewTeammateNotification(int teamId, String userId, int teammateId) {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(NEW_TEAMMATE)
                    .putCustomAttribute(TEAM_ID, Integer.toString(teamId))
                    .putCustomAttribute(USER_ID, userId)
                    .putCustomAttribute(TEAMMATE_ID, Integer.toString(teammateId)));
        }
    }

    public static void onNewDiscussionNotification(int teamId, String topicId) {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(NEW_DISCUSSION)
                    .putCustomAttribute(TEAM_ID, Integer.toString(teamId))
                    .putCustomAttribute(TOPIC_ID, topicId));
        }
    }


    public static void setUserId(String userId) {
        if (!BuildConfig.DEBUG) {
            Crashlytics.setUserIdentifier(userId);
        }
    }

    public static void onBackUp() {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(BACK_UP));
        }
    }


    public static void onRestore() {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(RESTORE));
        }
    }

    static void onWalletSync(Context context, String tag) {
        Tracker tracker = ((TeambrellaApplication) context.getApplicationContext()).geTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_WALLET)
                .setAction(ACTION_SYNC)
                .setLabel(tag)
                .build());
    }

    public static void onWalletSaved(Context context, String userId) {
        Tracker tracker = ((TeambrellaApplication) context.getApplicationContext()).geTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_WALLET)
                .setAction(ACTION_SAVE)
                .setLabel(userId)
                .build());

        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(GSL_BACKUP).putCustomAttribute(USER_ID, userId));
        }
    }

    public static void messageSent(Context context) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
    }
}

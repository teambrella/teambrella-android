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

    private static final String TRY_DEMO = "try_demo";
    private static final String USER_REGISTERED = "user_registered";
    private static final String GSL_BACKUP = "GSL Back Up";


    private static final String TEAM_ID = "team_id";
    private static final String USER_ID = "user_id";
    private static final String TOPIC_ID = "topic_id";
    private static final String POST_ID = "post_id";
    private static final String CLAIM_ID = "claim_id";
    private static final String TEAMMATE_ID = "teammate_id";
    private static final String VOTE = "vote";


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


    public static void setUserId(String userId) {
        if (!BuildConfig.DEBUG) {
            Crashlytics.setUserIdentifier(userId);
        }
    }
}
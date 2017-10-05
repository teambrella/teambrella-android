package com.teambrella.android.util;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.teambrella.android.BuildConfig;

/**
 * Statistic Helper
 */
public class StatisticHelper {

    private static final String TRY_DEMO = "Try Demo";
    private static final String APPLICATION_VOTE = "Application Vote";
    private static final String CLAIM_VOTE = "Claim Vote";
    private static final String USER_REGISTERED = "User Registered";


    private static final String TEAM_ID = "TeamId";
    private static final String VOTE = "Vote";

    public static void onTryDemo() {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(TRY_DEMO));
        }
    }

    public static void onApplicationVote(int teamId, double vote) {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(APPLICATION_VOTE)
                    .putCustomAttribute(TEAM_ID, Integer.toString(teamId))
                    .putCustomAttribute(VOTE, vote));
        }
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

    public static void setUserId(String userId) {
        if (!BuildConfig.DEBUG) {
            Crashlytics.setUserIdentifier(userId);
        }
    }
}

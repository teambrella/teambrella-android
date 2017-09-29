package com.teambrella.android.util;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

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
        Answers.getInstance().logCustom(new CustomEvent(TRY_DEMO));
    }

    public static void onApplicationVote(int teamId, double vote) {
        Answers.getInstance().logCustom(new CustomEvent(APPLICATION_VOTE)
                .putCustomAttribute(TEAM_ID, Integer.toString(teamId))
                .putCustomAttribute(VOTE, vote));
    }

    public static void onClaimVote(int teamId, int vote) {
        Answers.getInstance().logCustom(new CustomEvent(CLAIM_VOTE)
                .putCustomAttribute(TEAM_ID, Integer.toString(teamId))
                .putCustomAttribute(VOTE, vote));
    }

    public static void onUserRegistered() {
        Answers.getInstance().logCustom(new CustomEvent(USER_REGISTERED));
    }
}

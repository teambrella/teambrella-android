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
    private static final String NEW_POST = "New Post";
    private static final String NEW_CLAIM = "New Claim";
    private static final String WALLET_FUNDED = "Wallet Funded";
    private static final String NEW_TEAMMATE = "New Teammate";
    private static final String NEW_DISCUSSION = "New Discussion";


    private static final String TEAM_ID = "TeamId";
    private static final String USER_ID = "UserId";
    private static final String TOPIC_ID = "TopicId";
    private static final String POST_ID = "PostId";
    private static final String CLAIM_ID = "ClaimId";
    private static final String AMOUNT = "Amount";
    private static final String TEAMMATE_ID = "TeammateId";
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
}

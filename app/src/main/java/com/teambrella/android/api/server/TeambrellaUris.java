package com.teambrella.android.api.server;

import android.content.UriMatcher;
import android.net.Uri;
import android.util.Pair;

import java.util.List;

/**
 * Teambrella's Uris
 */
public class TeambrellaUris {

    private static final String AUTHORITY = "teambrella";
    private static final String SEGMENT_TEAM = "team";
    private static final String SEGMENT_CLAIMS = "claims";
    private static final String SEGMENT_ME = "me";
    private static final String SEGMENT_LIST = "list";
    private static final String SEGMENT_ONE = "one";
    private static final String SEGMENT_UPDATES = "updates";
    private static final String SEGMENT_REGISTER = "registerKey";
    private static final String SEGMENT_CHAT = "chat";
    private static final String SEGMENT_NEW_POST = "newPost";
    private static final String SEGMENT_TEAMS = "teams";
    private static final String SEGMENT_VOTE = "vote";
    private static final String SEGMENT_TEAMMATE = "teammate";
    private static final String SEGMENT_FEED = "feed";
    private static final String SEGMENT_HOME = "home";
    private static final String SEGMENT_PROXY = "proxy";
    private static final String SEGMENT_MY = "my";
    private static final String SEGMENT_IA_AM = "iam";
    private static final String SEGMENT_RATING = "rating";
    private static final String SEGMENT_SET_MY_PROXY = "setMyProxy";
    private static final String SEGMENT_SET_POSITION = "setPosition";


    static final String KEY_FACEBOOK_TOKEN = "facebookToken";
    static final String KEY_OFFSET = "Offset";
    static final String KEY_LIMIT = "Limit";
    static final String KEY_TEAM_ID = "TeamId";
    public static final String KEY_ADD = "add";
    static final String KEY_TEAMMATE_ID = "TeammateId";
    static final String KEY_SINCE = "Since";
    static final String KEY_ID = "Id";
    static final String KEY_TEXT = "Text";
    static final String KEY_VOTE = "Vote";
    static final String KEY_POSITION = "Position";

    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final int TEAMMATES_LIST = 1;
    public static final int TEAMMATES_ONE = 2;
    public static final int ME_UPDATES = 3;
    public static final int ME_REGISTER_KEY = 4;
    public static final int CLAIMS_LIST = 5;
    public static final int CLAIMS_ONE = 6;
    public static final int CLAIMS_CHAT = 7;
    public static final int NEW_POST = 8;
    public static final int MY_TEAMS = 9;
    public static final int SET_CLAIM_VOTE = 10;
    public static final int SET_TEAMMATE_VOTE = 11;
    public static final int GET_HOME = 12;
    public static final int GET_FEED = 13;
    public static final int TEAMMATE_CHAT = 14;
    public static final int FEED_CHAT = 15;
    public static final int MY_PROXIES = 16;
    public static final int PROXY_FOR = 17;
    public static final int USER_RATING = 18;
    public static final int SET_MY_PROXY = 19;
    public static final int SET_PROXY_POSITION = 20;


    static {
        sUriMatcher.addURI(AUTHORITY, SEGMENT_TEAM + "/#/" + SEGMENT_LIST, TEAMMATES_LIST);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_TEAM + "/#/" + SEGMENT_ONE + "/*", TEAMMATES_ONE);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_ME + "/" + SEGMENT_UPDATES, ME_UPDATES);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_ME + "/" + SEGMENT_REGISTER, ME_REGISTER_KEY);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_ME + "/" + SEGMENT_TEAMS, MY_TEAMS);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_CLAIMS + "/" + SEGMENT_LIST, CLAIMS_LIST);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_CLAIMS + "/" + SEGMENT_ONE, CLAIMS_ONE);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_CLAIMS + "/" + SEGMENT_CHAT, CLAIMS_CHAT);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_CHAT + "/" + SEGMENT_NEW_POST, NEW_POST);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_CLAIMS + "/" + SEGMENT_VOTE, SET_CLAIM_VOTE);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_TEAMMATE + "/" + SEGMENT_VOTE, SET_TEAMMATE_VOTE);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_FEED + "/" + SEGMENT_HOME, GET_HOME);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_FEED + "/" + SEGMENT_LIST, GET_FEED);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_TEAMMATE + "/" + SEGMENT_CHAT, TEAMMATE_CHAT);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_FEED + "/" + SEGMENT_CHAT, FEED_CHAT);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_PROXY + "/" + SEGMENT_MY, MY_PROXIES);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_PROXY + "/" + SEGMENT_IA_AM, PROXY_FOR);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_PROXY + "/" + SEGMENT_RATING, USER_RATING);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_PROXY + "/" + SEGMENT_SET_MY_PROXY, SET_MY_PROXY);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_PROXY + "/" + SEGMENT_SET_POSITION, SET_PROXY_POSITION);
    }


    /**
     * Get team Uri
     *
     * @param teamId team ID
     * @return uri
     */
    public static Uri getTeamUri(int teamId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_TEAM)
                .appendEncodedPath(Integer.toString(teamId))
                .appendEncodedPath(SEGMENT_LIST)
                .build();
    }

    public static Uri getFeedUri(int teamId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_FEED)
                .appendEncodedPath(SEGMENT_LIST)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .build();

    }


    public static Uri getNewPostUri(String topicId, String text) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_CHAT)
                .appendEncodedPath(SEGMENT_NEW_POST)
                .appendQueryParameter(KEY_ID, topicId)
                .appendQueryParameter(KEY_TEXT, text)
                .build();
    }

    public static Uri getHomeUri(int teamId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_FEED)
                .appendEncodedPath(SEGMENT_HOME)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .build();
    }


    public static Uri getClaimsUri(int teamId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_CLAIMS)
                .appendEncodedPath(SEGMENT_LIST)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .build();
    }


    public static Uri getTeammateChatUri(int teamId, String userId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_TEAMMATE)
                .appendEncodedPath(SEGMENT_CHAT)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .appendQueryParameter(KEY_ID, userId)
                .build();
    }


    public static Uri getFeedChatUri(String topicId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_FEED)
                .appendEncodedPath(SEGMENT_CHAT)
                .appendQueryParameter(KEY_ID, topicId)
                .build();
    }


    public static Uri getClaimVoteUri(int claimId, int vote) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_CLAIMS)
                .appendEncodedPath(SEGMENT_VOTE)
                .appendQueryParameter(KEY_ID, Integer.toString(claimId))
                .appendQueryParameter(KEY_VOTE, Integer.toString(vote))
                .build();
    }


    public static Uri getTeammateVoteUri(int teammateId, double vote) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_TEAMMATE)
                .appendEncodedPath(SEGMENT_VOTE)
                .appendQueryParameter(KEY_ID, Integer.toString(teammateId))
                .appendQueryParameter(KEY_VOTE, Double.toString(vote))
                .build();
    }

    public static Uri getClaimsUri(int teamId, int teammateId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_CLAIMS)
                .appendEncodedPath(SEGMENT_LIST)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .appendQueryParameter(KEY_TEAMMATE_ID, Integer.toString(teammateId))
                .build();
    }

    public static Uri getClaimUri(int claimId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_CLAIMS)
                .appendEncodedPath(SEGMENT_ONE)
                .appendQueryParameter(KEY_ID, Integer.toString(claimId))
                .build();
    }

    public static Uri getMyTeams() {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_ME)
                .appendEncodedPath(SEGMENT_TEAMS)
                .build();
    }


    public static Uri getClaimChatUri(int claimId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_CLAIMS)
                .appendEncodedPath(SEGMENT_CHAT)
                .appendQueryParameter(KEY_ID, Integer.toString(claimId))
                .build();
    }

    public static Uri appendChatSince(Uri uri, long since) {
        return uri.buildUpon()
                .appendQueryParameter(KEY_SINCE, Long.toString(since))
                .build();
    }

    public static Uri getSetProxyPositionUri(int position, String userId, int teamId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_PROXY)
                .appendEncodedPath(SEGMENT_SET_POSITION)
                .appendQueryParameter(KEY_POSITION, Integer.toString(position))
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .appendQueryParameter(KEY_ID, userId)
                .build();

    }


    public static Uri getRegisterUri(String facebookToken) {
        return new Uri.Builder().authority(AUTHORITY).appendEncodedPath(SEGMENT_ME)
                .appendEncodedPath(SEGMENT_REGISTER).appendQueryParameter(KEY_FACEBOOK_TOKEN, facebookToken).build();
    }

    public static Uri appendPagination(Uri uri, int offset, int limit) {
        return uri.buildUpon()
                .appendQueryParameter(KEY_OFFSET, Integer.toString(offset))
                .appendQueryParameter(KEY_LIMIT, Integer.toString(limit))
                .build();
    }


    /**
     * Get teammate Uri
     *
     * @param teamId team ID
     * @param userId user ID
     * @return uri
     */
    public static Uri getTeammateUri(int teamId, String userId) {
        return new Uri.Builder().authority(AUTHORITY).appendEncodedPath(SEGMENT_TEAM).
                appendEncodedPath(Integer.toString(teamId)).appendEncodedPath(SEGMENT_ONE)
                .appendEncodedPath(userId).build();
    }

    public static Uri getMyProxiesUri(int teamId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_PROXY)
                .appendEncodedPath(SEGMENT_MY)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .build();
    }

    public static Uri getProxyForUri(int teamId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_PROXY)
                .appendEncodedPath(SEGMENT_IA_AM)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .build();
    }

    public static Uri getUserRatingUri(int teamId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_PROXY)
                .appendEncodedPath(SEGMENT_RATING)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .build();
    }

    public static Uri setMyProxyUri(String userId, boolean add) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_PROXY)
                .appendEncodedPath(SEGMENT_SET_MY_PROXY)
                .appendQueryParameter(KEY_ID, userId)
                .appendQueryParameter(KEY_ADD, Boolean.toString(add))
                .build();
    }


    /**
     * Get updates Uri
     *
     * @return uri
     */
    public static Uri getUpdates() {
        return new Uri.Builder().authority(AUTHORITY).appendEncodedPath(SEGMENT_ME)
                .appendEncodedPath(SEGMENT_UPDATES).build();
    }

    static int getTeamId(Uri uri) {
        return Integer.parseInt(uri.getPathSegments().get(1));
    }


    static Pair<Integer, String> getTeamAndTeammateId(Uri uri) {
        List<String> segments = uri.getPathSegments();
        return new Pair<>(Integer.parseInt(segments.get(1)), segments.get(3));
    }
}

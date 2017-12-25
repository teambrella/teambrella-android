package com.teambrella.android.api.server;

import android.content.UriMatcher;
import android.net.Uri;
import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Teambrella's Uris
 */
@SuppressWarnings("WeakerAccess")
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
    private static final String SEGMENT_NEW_FILE = "newFile";
    private static final String SEGMENT_NEW_CLAIM = "newClaim";
    private static final String SEGMENT_NEW_CHAT = "newChat";
    private static final String SEGMENT_GET_COVERAGE_FOR_DATE = "getCoverageForDate";
    private static final String SEGMENT_PRIVATE_MESSAGE = "privateMessage";
    private static final String SEGMENT_VOTES = "votes";
    private static final String SEGMENT_WALLET = "wallet";
    private static final String SEGMENT_DEMO = "demo";
    private static final String SEGMENT_DEBUG_DB = "debugDb";
    private static final String SEGMENT_DEBUG_LOG = "debugLog";
    private static final String SEGMENT_WITHDRAWALS = "withdrawals";
    private static final String SEGMENT_NEW_WITHDRAW = "newWithdraw";
    private static final String SEGMENT_MUTE = "mute";


    public static final String KEY_FACEBOOK_TOKEN = "facebookToken";
    public static final String KEY_SIG_OF_PUBLIC_KEY = "sigOfPublicKey";
    public static final String KEY_OFFSET = "Offset";
    public static final String KEY_LIMIT = "Limit";
    public static final String KEY_TEAM_ID = "TeamId";
    public static final String KEY_OPT_IN = "OptIn";
    public static final String KEY_ADD = "add";
    public static final String KEY_TEAMMATE_ID = "TeammateId";
    public static final String KEY_SINCE = "Since";
    public static final String KEY_ID = "Id";
    public static final String KEY_TEXT = "Text";
    public static final String KEY_VOTE = "Vote";
    public static final String KEY_POSITION = "Position";
    public static final String KEY_URI = "uri";
    public static final String KEY_DATE = "date";
    public static final String KEY_EXPENSES = "expenses";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TITLE = "title";
    public static final String KEY_IMAGES = "images";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_SORTED_BY_RISK = "sortedByRisk";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_MUTED = "muted";
    public static final String KEY_POST_ID = "postId";


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
    public static final int NEW_FILE = 21;
    public static final int GET_COVERAGE_FOR_DATE = 22;
    public static final int NEW_CLAIM = 23;
    public static final int NEW_CHAT = 24;
    public static final int INBOX = 25;
    public static final int CONVERSATION_CHAT = 26;
    public static final int NEW_PRIVATE_MESSAGE = 27;
    public static final int APPLICATION_VOTES = 28;
    public static final int CLAIMS_VOTES = 29;
    public static final int WALLET = 30;
    public static final int DEMO_TEAMS = 31;
    public static final int DEBUG_DB = 32;
    public static final int DEBUG_LOG = 33;
    public static final int WITHDRAWALS = 34;
    public static final int NEW_WITHDRAW = 35;
    public static final int MUTE = 36;


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
        sUriMatcher.addURI(AUTHORITY, SEGMENT_NEW_FILE, NEW_FILE);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_ME + "/" + SEGMENT_GET_COVERAGE_FOR_DATE, GET_COVERAGE_FOR_DATE);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_CLAIMS + "/" + SEGMENT_NEW_CLAIM, NEW_CLAIM);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_FEED + "/" + SEGMENT_NEW_CHAT, NEW_CHAT);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_PRIVATE_MESSAGE + "/" + SEGMENT_LIST, INBOX);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_PRIVATE_MESSAGE + "/" + SEGMENT_CHAT, CONVERSATION_CHAT);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_PRIVATE_MESSAGE + "/" + SEGMENT_NEW_POST, NEW_PRIVATE_MESSAGE);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_TEAMMATE + "/" + SEGMENT_VOTES, APPLICATION_VOTES);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_CLAIMS + "/" + SEGMENT_VOTES, CLAIMS_VOTES);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_WALLET + "/" + SEGMENT_ONE, WALLET);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_DEMO + "/" + SEGMENT_TEAMS, DEMO_TEAMS);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_ME + "/" + SEGMENT_DEBUG_DB, DEBUG_DB);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_ME + "/" + SEGMENT_DEBUG_LOG, DEBUG_LOG);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_WALLET + "/" + SEGMENT_WITHDRAWALS, WITHDRAWALS);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_WALLET + "/" + SEGMENT_NEW_WITHDRAW, NEW_WITHDRAW);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_FEED + "/" + SEGMENT_MUTE, MUTE);
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


    /**
     * Get team Uri
     *
     * @param teamId team ID
     * @return uri
     */
    public static Uri getTeammatesUri(int teamId, boolean sortedByRisk) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_TEAM)
                .appendEncodedPath(Integer.toString(teamId))
                .appendEncodedPath(SEGMENT_LIST)
                .appendQueryParameter(KEY_SORTED_BY_RISK, Boolean.toString(sortedByRisk))
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


    public static Uri getNewFileUri(String path) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendPath(SEGMENT_NEW_FILE)
                .appendQueryParameter(KEY_URI, path)
                .build();
    }

    public static Uri getDebugDbUri(String path) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendPath(SEGMENT_ME)
                .appendEncodedPath(SEGMENT_DEBUG_DB)
                .appendQueryParameter(KEY_URI, path)
                .build();
    }

    public static Uri getDebugLogUri(String path) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendPath(SEGMENT_ME)
                .appendEncodedPath(SEGMENT_DEBUG_LOG)
                .appendQueryParameter(KEY_URI, path)
                .build();
    }


    public static Uri getNewPostUri(String topicId, String postId, String text, String images) {
        Uri.Builder builder = new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_CHAT)
                .appendEncodedPath(SEGMENT_NEW_POST)
                .appendQueryParameter(KEY_ID, topicId)
                .appendQueryParameter(KEY_POST_ID, postId);

        if (text != null) {
            builder.appendQueryParameter(KEY_TEXT, text);
        }

        if (images != null) {
            builder.appendQueryParameter(KEY_IMAGES, images);
        }

        return builder.build();
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


    public static Uri getNewClaimUri(int teamId, Date date, float expenses, String message, String images, String address) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_CLAIMS)
                .appendEncodedPath(SEGMENT_NEW_CLAIM)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .appendQueryParameter(KEY_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date))
                .appendQueryParameter(KEY_EXPENSES, Float.toString(expenses))
                .appendQueryParameter(KEY_MESSAGE, message)
                .appendQueryParameter(KEY_IMAGES, images)
                .appendQueryParameter(KEY_ADDRESS, address)
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

    public static Uri getDemoTeams(String language) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_DEMO)
                .appendEncodedPath(SEGMENT_TEAMS)
                .appendQueryParameter(KEY_LANGUAGE, language)
                .build();
    }


    public static Uri getNewChatUri(int teamId, String title, String message) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_FEED)
                .appendEncodedPath(SEGMENT_NEW_CHAT)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .appendQueryParameter(KEY_TITLE, title)
                .appendQueryParameter(KEY_MESSAGE, message)
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

    public static Uri getCoverageForDate(int teamId, Date date) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_ME)
                .appendEncodedPath(SEGMENT_GET_COVERAGE_FOR_DATE)
                .appendQueryParameter(KEY_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date))
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
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


    public static Uri getRegisterUri(String facebookToken, String sigOfPublicKey) {
        return new Uri.Builder().authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_ME)
                .appendEncodedPath(SEGMENT_REGISTER)
                .appendQueryParameter(KEY_FACEBOOK_TOKEN, facebookToken)
                .appendQueryParameter(KEY_SIG_OF_PUBLIC_KEY, sigOfPublicKey)
                .build();
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

    public static Uri getUserRatingUri(int teamId, boolean optIn) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_PROXY)
                .appendEncodedPath(SEGMENT_RATING)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .appendQueryParameter(KEY_OPT_IN, Boolean.toString(optIn))
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

    public static Uri getInbox() {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_PRIVATE_MESSAGE)
                .appendEncodedPath(SEGMENT_LIST)
                .build();
    }

    public static Uri getConversationChat(String userId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_PRIVATE_MESSAGE)
                .appendEncodedPath(SEGMENT_CHAT)
                .appendQueryParameter(KEY_ID, userId)
                .build();
    }

    public static Uri getNewConversationMessage(String userId, String text) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_PRIVATE_MESSAGE)
                .appendEncodedPath(SEGMENT_NEW_POST)
                .appendQueryParameter(KEY_ID, userId)
                .appendQueryParameter(KEY_MESSAGE, text)
                .build();
    }

    public static Uri getAllVotesForClaim(int teamId, int claimId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_CLAIMS)
                .appendEncodedPath(SEGMENT_VOTES)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .appendQueryParameter(KEY_ID, Integer.toString(claimId))
                .build();
    }

    public static Uri getAllVotesForTeammate(int teamId, int teammateId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_TEAMMATE)
                .appendEncodedPath(SEGMENT_VOTES)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .appendQueryParameter(KEY_TEAMMATE_ID, Integer.toString(teammateId))
                .build();
    }

    public static Uri getWallet(int teamId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_WALLET)
                .appendEncodedPath(SEGMENT_ONE)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .build();
    }

    public static Uri getWithdrawals(int teamId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_WALLET)
                .appendEncodedPath(SEGMENT_WITHDRAWALS)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .build();
    }

    public static Uri getNewWithdrawUri(int teamId, float amount, String address) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_WALLET)
                .appendEncodedPath(SEGMENT_NEW_WITHDRAW)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
                .appendQueryParameter(KEY_ADDRESS, address)
                .appendQueryParameter(KEY_AMOUNT, Float.toString(amount))
                .build();
    }

    public static Uri getSetChatMuted(String topicId, boolean muted) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_FEED)
                .appendEncodedPath(SEGMENT_MUTE)
                .appendQueryParameter(KEY_ID, topicId)
                .appendQueryParameter(KEY_MUTED, Boolean.toString(muted))
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

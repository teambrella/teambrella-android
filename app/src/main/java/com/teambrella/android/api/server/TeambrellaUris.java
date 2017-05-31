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

    static final String KEY_FACEBOOK_TOKEN = "facebookToken";
    static final String KEY_OFFSET = "Offset";
    static final String KEY_LIMIT = "Limit";
    static final String KEY_TEAM_ID = "TeamId";

    static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static final int TEAMMATES_LIST = 1;
    static final int TEAMMATES_ONE = 2;
    static final int ME_UPDATES = 3;
    static final int ME_REGISTER_KEY = 4;
    static final int CLAIMS_LIST = 5;


    static {
        sUriMatcher.addURI(AUTHORITY, SEGMENT_TEAM + "/#/" + SEGMENT_LIST, TEAMMATES_LIST);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_TEAM + "/#/" + SEGMENT_ONE + "/*", TEAMMATES_ONE);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_ME + "/" + SEGMENT_UPDATES, ME_UPDATES);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_ME + "/" + SEGMENT_REGISTER, ME_REGISTER_KEY);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_CLAIMS + "/" + SEGMENT_LIST, CLAIMS_LIST);
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


    public static Uri getClaimsUri(int teamId) {
        return new Uri.Builder()
                .authority(AUTHORITY)
                .appendEncodedPath(SEGMENT_CLAIMS)
                .appendEncodedPath(SEGMENT_LIST)
                .appendQueryParameter(KEY_TEAM_ID, Integer.toString(teamId))
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

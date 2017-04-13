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
    private static final String SEGMENT_LIST = "list";
    private static final String SEGMENT_ONE = "one";


    static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static final int TEAMMATES_LIST = 1;
    static final int TEAMMATES_ONE = 2;


    static {
        sUriMatcher.addURI(AUTHORITY, SEGMENT_TEAM + "/#/" + SEGMENT_LIST, TEAMMATES_LIST);
        sUriMatcher.addURI(AUTHORITY, SEGMENT_TEAM + "/#/" + SEGMENT_ONE + "/#", TEAMMATES_ONE);
    }


    /**
     * Get team Uri
     *
     * @param teamId team ID
     * @return uri
     */
    public static Uri getTeamUri(int teamId) {
        return new Uri.Builder().authority(AUTHORITY).appendEncodedPath(SEGMENT_TEAM)
                .appendEncodedPath(Integer.toString(teamId)).appendEncodedPath(SEGMENT_LIST).build();
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

    static int getTeamId(Uri uri) {
        return Integer.parseInt(uri.getPathSegments().get(1));
    }


    static Pair<Integer, String> getTeamAndTeammateId(Uri uri) {
        List<String> segments = uri.getPathSegments();
        return new Pair<>(Integer.parseInt(segments.get(1)), segments.get(3));
    }
}

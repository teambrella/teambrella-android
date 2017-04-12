package com.teambrella.android.api.server;

import android.content.UriMatcher;
import android.net.Uri;

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
     * Get teammates Uri
     *
     * @param teamId team ID
     * @return uri
     */
    public static Uri getTeammatesUri(int teamId) {
        return new Uri.Builder().authority(AUTHORITY).appendEncodedPath(SEGMENT_TEAM)
                .appendEncodedPath(Integer.toString(teamId)).appendEncodedPath(SEGMENT_LIST).build();
    }

    /**
     * Get team ID form specified Uri
     *
     * @param uri Uri
     * @return team id
     */
    public static int getTeamId(Uri uri) {
        return Integer.parseInt(uri.getPathSegments().get(1));
    }


}

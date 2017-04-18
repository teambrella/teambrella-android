package com.teambrella.android.content;

import android.content.UriMatcher;
import android.net.Uri;

/**
 * Teambrella Content
 */
public class TeambrellaRepository {


    static String AUTHORITY = "com.teambrella.android.provider";

    static final String BTC_ADDRESS_TABLE = "BTCAddress";
    static final String CONNECTION_TABLE = "Connection";
    static final String COSIGNER_TABLE = "Cosigner";
    static final String PAY_TO_TABLE = "PayTo";
    static final String TEAMMATE_TABLE = "Teammate";
    static final String TEAM_TABLE = "Team";


    static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static final int BTC_ADDRESS = 1;
    static final int CONNECTION = 2;
    static final int COSIGNER = 3;
    static final int PAY_TO = 4;
    static final int TEAMMATE = 5;
    static final int TEAM = 6;


    static {
        sUriMatcher.addURI(AUTHORITY, BTC_ADDRESS_TABLE, BTC_ADDRESS);
        sUriMatcher.addURI(AUTHORITY, CONNECTION_TABLE, CONNECTION);
        sUriMatcher.addURI(AUTHORITY, COSIGNER_TABLE, COSIGNER);
        sUriMatcher.addURI(AUTHORITY, PAY_TO_TABLE, PAY_TO);
        sUriMatcher.addURI(AUTHORITY, TEAMMATE_TABLE, TEAMMATE);
        sUriMatcher.addURI(AUTHORITY, TEAM_TABLE, TEAM);
    }

    public static final class BTCAddress {
        public static final Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(BTC_ADDRESS_TABLE).build();

        public static final String ADDRESS = "Address";
        public static final String STATUS = "Status";
        public static final String DATE_CREATED = "DataCreated";


    }

    public static final class Connection {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(CONNECTION_TABLE).build();
        public static final String LAST_CONNECTED = "LastConnected";
        public static final String LAST_UPDATED = "LastUpdated";
    }

    public static class Cosigner {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(COSIGNER_TABLE).build();
        public static final String KEY_ORDER = "KeyOrder";
    }


    public static class PayTo {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(PAY_TO_TABLE).build();
        public static final String KNOWN_SINCE = "KnownSince";
        public static final String ADDRESS = "Address";
        public static final String IS_DEFAULT = "IsDefault";
    }

    public static class Teammate {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(TEAMMATE_TABLE).build();
        public static final String NAME = "Name";
        public static final String FB_NAME = "FBName";
        public static final String PUBLIC_KEY = "PublicKey";
    }

    public static class Team {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(TEAM_TABLE).build();

        public static final String NAME = "Name";
        public static final String TESTNET = "Testnet";

    }

}


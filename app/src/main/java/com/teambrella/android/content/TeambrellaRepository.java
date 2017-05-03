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
    static final String TX_TABLE = "Tx";
    static final String TX_INPUT_TABLE = "TxInput";
    static final String TX_OUTPUT_TABLE = "TxOutput";
    static final String TX_SIGNATURE_TABLE = "TxSignature";


    static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static final int BTC_ADDRESS = 1;
    static final int CONNECTION = 2;
    static final int COSIGNER = 3;
    static final int PAY_TO = 4;
    static final int TEAMMATE = 5;
    static final int TEAM = 6;
    static final int TX = 7;
    static final int TX_INPUT = 8;
    static final int TX_OUTPUT = 9;
    static final int TX_SIGNATURE = 10;


    static {
        sUriMatcher.addURI(AUTHORITY, BTC_ADDRESS_TABLE, BTC_ADDRESS);
        sUriMatcher.addURI(AUTHORITY, CONNECTION_TABLE, CONNECTION);
        sUriMatcher.addURI(AUTHORITY, COSIGNER_TABLE, COSIGNER);
        sUriMatcher.addURI(AUTHORITY, PAY_TO_TABLE, PAY_TO);
        sUriMatcher.addURI(AUTHORITY, TEAMMATE_TABLE, TEAMMATE);
        sUriMatcher.addURI(AUTHORITY, TEAM_TABLE, TEAM);
        sUriMatcher.addURI(AUTHORITY, TX_TABLE, TX);
        sUriMatcher.addURI(AUTHORITY, TX_INPUT_TABLE, TX_INPUT);
        sUriMatcher.addURI(AUTHORITY, TX_OUTPUT_TABLE, TX_OUTPUT);
        sUriMatcher.addURI(AUTHORITY, TX_SIGNATURE_TABLE, TX_SIGNATURE);

    }

    public static final class BTCAddress {
        public static final Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(BTC_ADDRESS_TABLE).build();

        public static final String ADDRESS = "Address";
        public static final String TEAMMATE_ID = "TeammateID";
        public static final String STATUS = "Status";
        public static final String DATE_CREATED = "DateCreated";


    }

    public static final class Connection {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(CONNECTION_TABLE).build();
        public static final String ID = "Id";
        public static final String LAST_CONNECTED = "LastConnected";
        public static final String LAST_UPDATED = "LastUpdated";
    }

    public static class Cosigner {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(COSIGNER_TABLE).build();
        public static final String ADDRESS_ID = "AddressId";
        public static final String TEAMMATE_ID = "TeammateId";
        public static final String KEY_ORDER = "KeyOrder";
    }


    public static class PayTo {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(PAY_TO_TABLE).build();
        public static final String ID = "Id";
        public static final String TEAMMATE_ID = "TeammateId";
        public static final String KNOWN_SINCE = "KnownSince";
        public static final String ADDRESS = "Address";
        public static final String IS_DEFAULT = "IsDefault";
    }

    public static class Teammate {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(TEAMMATE_TABLE).build();
        public static final String ID = "Id";
        public static final String TEAM_ID = "TeamId";
        public static final String NAME = "Name";
        public static final String FB_NAME = "FBName";
        public static final String PUBLIC_KEY = "PublicKey";
    }

    public static class Tx {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(TX_TABLE).build();
        public static final String ID = "Id";
        public static final String TEAMMATE_ID = "TeammateId";
        public static final String AMOUNT_BTC = "AmountBTC";
        public static final String FEE_BTC = "FeeBTC";
        public static final String CLAIM_ID = "ClaimId";
        public static final String CLAIM_TEAMMATE_ID = "ClaimTeammateId";
        public static final String WITHDRAW_REQ_ID = "WithdrawReqId";
        public static final String KIND = "Kind";
        public static final String STATE = "State";
        public static final String INITIATED_TIME = "InitiatedTime";
        public static final String MOVE_TO_ADDRESS_ID = "MoveToAddressId";
        public static final String UPDATE_TIME = "UpdateTime";
        public static final String RECEIVED_TIME = "ReceivedTime";
        public static final String RESOLUTION_TIME = "ResolutionTime";
        public static final String PROCESSED_TIME = "ProcessedTime";
        public static final String NEED_UPDATE_SERVER = "NeedUpdateServer";
        public static final String RESOLUTION = "Resolution";
        public static final String CLIENT_RESOLUTION_TIME = "ClientResolutionTime";
    }

    public static final class TXInput {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(TX_INPUT_TABLE).build();
        public static final String ID = "Id";
        public static final String TX_ID = "TxId";
        public static final String PREV_TX_ID = "PrevTxId";
        public static final String PREV_TX_INDEX = "PrevTxIndex";
        public static final String AMMOUNT_BTC = "AmountBTC";
    }

    public static final class TXOutput {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(TX_OUTPUT_TABLE).build();
        public static final String ID = "Id";
        public static final String TX_ID = "TxId";
        public static final String PAY_TO_ID = "PayToId";
        public static final String AMOUNT_BTC = "AmountBTC";
    }

    public static final class TXSignature {
        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(TX_SIGNATURE_TABLE).build();
        public static final String ID = "Id";
        public static final String TX_INPUT_ID = "TxInputId";
        public static final String TEAMMATE_ID = "TeammateId";
        public static final String SIGNATURE = "Signature";
        public static final String NEED_UPDATE_SERVER = "NeedUpdateServer";
    }

    public static class Team {

        public static Uri CONTENT_URI = new Uri.Builder().encodedAuthority(AUTHORITY)
                .encodedPath(TEAM_TABLE).build();

        public static final String ID = "Id";
        public static final String NAME = "Name";
        public static final String TESTNET = "Testnet";

        public static final String AUTO_APPROVAL_MY_GODD_ADDRESS = "AutoApprovalMyGoodAddress";
        public static final String AUTO_APPROVAL_MY_NEW_ADDRESS = "AudioApprovalMyNewAddress";
        public static final String AUTO_APPROVAL_COSIGN_GOOD_ADDRESS = "AutoApprovalCosignGoodAddress";
        public static final String AUTO_APPROVAL_COSIGN_NEW_ADDRESS = "AutoApprovalCosignNewAddress";
        public static final String PAY_TO_ADDRESS_OK_AGE = "PayToAddressOkAge";

    }

}


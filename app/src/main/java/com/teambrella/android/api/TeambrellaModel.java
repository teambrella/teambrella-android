package com.teambrella.android.api;

/**
 * Model
 */
public class TeambrellaModel {

    /*
     * Tx  Kind
     */
    public static final int TX_KIND_PAYOUT = 0;
    public static final int TX_KIND_WITHDRAW = 1;
    public static final int TX_KIND_MOVE_TO_NEXT_WALLET = 2;
    public static final int TX_KIND_SAVE_FROM_PREV_WALLLET = 3;


    /*
     * Tx State
     */
    public static final int TX_STATE_CREATED = 0;
    public static final int TX_STATE_APPROVED_MASTER = 1;
    public static final int TX_STATE_APPROVED_COSIGNERS = 2;
    public static final int TX_STATE_APPREOVED_ALL = 3;
    public static final int TX_STATE_BLOCKED_MASTER = 4;
    public static final int TX_STATE_BLOCKED_COSIGNERS = 5;
    public static final int TX_STATE_SELECTED_FOR_COSIGNING = 6;
    public static final int TX_STATE_BEING_COSIGNED = 7;
    public static final int TX_STATE_COSIGNED = 8;
    public static final int TX_STATE_PUBLISHED = 9;
    public static final int TX_STATE_CONFIRMED = 10;
    public static final int TX_STATE_ERROR_COSIGNERS_TIMEOUT = 100;
    public static final int TX_STATE_ERROR_SUBMIT_TO_BLOCKCHAIN = 101;
    public static final int TX_STATE_ERROR_BAD_REQUEST = 102;
    public static final int TX_STATE_ERROR_OUT_OF_FOUNDS = 103;
    public static final int TX_STATE_ERROR_TOO_MANY_UTXOS = 104;


    /*
     *  Client resolution
     */
    public static final int TX_CLIENT_RESOLUTION_NONE = 0;
    public static final int TX_CLIENT_RESOLUTION_RECEIVED = 1;
    public static final int TX_CLIENT_RESOLUTON_APPROVED = 2;
    public static final int TX_CLIENT_RESOLUTION_BLOCKED = 3;
    public static final int TX_CLIENT_RESOLUTION_SIGNED = 4;
    public static final int TX_CLIENT_RESOLUTION_PUBLISHED = 5;
    public static final int TX_CLIENT_RESOLUTION_ERROR_COSIGNERS_TIMEOUT = 100;
    public static final int TX_CLIENT_RESOLUTION_ERROR_SUBMITT_TO_BLOCKCHAIN = 101;
    public static final int TX_CLIENT_RESOLUTION_ERROR_BAD_REQUEST = 102;
    public static final int TX_CLIENT_RESOLUTION_ERROR_OUT_OF_FUNDS = 103;


    /* Tx signing state*/
    public static final int TX_SIGNING_STATE_CREATED = 0;
    public static final int TX_SIGNING_STATE_TAKEN_FOR_APPROVAL = 1;
    public static final int TX_SIGNING_STATE_APPROVED = 2;
    public static final int TX_SIGNING_STATE_BLOCKED = 3;
    public static final int TX_SIGNING_STATE_NEEDS_SIGNING = 4;
    public static final int TX_SIGNING_STATE_SIGNED = 5;


    /*Request*/
    public static final String ATTR_REQUEST_TIMESTAMP = "Timestamp";
    public static final String ATTR_REQUEST_SIGNATURE = "Signature";
    public static final String ATTR_REQUEST_PUBLIC_KEY = "PublicKey";
    public static final String ATTR_REQUEST_TEAM_ID = "TeamId";
    public static final String ATTR_REQUEST_USER_ID = "UserId";


    /*Response*/
    public static final String ATTR_STATUS = "Status";
    public static final String ATTR_DATA = "Data";


    /*Status*/
    public static final String ATTR_STATUS_TIMESTAMP = "Timestamp";
    public static final String ATTR_STATUS_RESULT_CODE = "ResultCode";
    public static final String ATTR_STATUS_ERROR_MESSAGE = "ErrorMessage";

    public static final int VALUE_STATUS_RESULT_CODE_SUCCESS = 0;
    public static final int VALUE_STATUS_RESULT_CODE_FATAL = 1;
    public static final int VALUE_STATUS_RESULT_CODE_AUTH = 2;


    /*Data*/
    public static final String ATTR_DATA_MY_TEAMMATE_ID = "MyTeammateID";
    public static final String ATTR_DATA_TEAM_ID = "TeamId";
    public static final String ATTR_DATA_TEAMMATES = "Teammates";
    public static final String ATTR_DATA_ID = "Id";
    public static final String ATTR_DATA_VER = "Ver";
    public static final String ATTR_DATA_USER_ID = "UserId";
    public static final String ATTR_DATA_AVATAR = "Avatar";
    public static final String ATTR_DATA_NAME = "Name";
    public static final String ATTR_DATA_FB_NAME = "FBName";
    public static final String ATTR_DATA_MODEL = "Model";
    public static final String ATTR_DATA_YEAR = "Year";
    public static final String ATTR_DATA_UNREAD = "Unread";
    public static final String ATTR_DATA_CLAIM_LIMIT = "ClaimLimit";
    public static final String ATTR_DATA_RISK = "Risk";
    public static final String ATTR_DATA_RISK_VOTED = "RiskVoted";
    public static final String ATTR_DATA_TOTALLY_PAID = "TotallyPaid";
    public static final String ATTR_DATA_IS_JOINING = "IsJoining";
    public static final String ATTR_DATA_IS_VOTING = "IsVoting";
    public static final String ATTR_DATA_CLAIMS_COUNT = "ClaimsCount";
    public static final String ATTR_DATA_TEST_NET = "Testnet";
    public static final String ATTR_DATA_PUBLIC_KEY = "PublicKey";
    public static final String ATTR_DATA_ADDRESS = "Address";
    public static final String ATTR_DATA_TEAMMATE_ID = "TeammateId";
    public static final String ATTR_DATA_STATUS = "Status";
    public static final String ATTR_DATA_DATE_CREATED = "DateCreated";
    public static final String ATTR_DATA_KEY_ORDER = "KeyOrder";
    public static final String ATTR_DATA_BTC_AMOUNT = "AmountBTC";
    public static final String ATTR_DATA_CLAIM_ID = "ClaimId";
    public static final String ATTR_DATA_CLAIM_TEAMMATE_ID = "ClaimTeammateId";
    public static final String ATTR_DATA_KIND = "kind";
    public static final String ATTR_DATA_STATE = "state";
    public static final String ATTR_DATA_INITIATED_TIME = "InitiatedTime";
    public static final String ATTR_DATA_TEAMS = "Teams";
    public static final String ATTR_DATA_KNOWN_SINCE = "KnownSince";
    public static final String ATTR_DATA_IS_DEFAULT = "IsDefault";
    public static final String ATTR_DATA_PAY_TOS = "PayTos";
    public static final String ATTR_DATA_BTC_ADDRESSES = "BTCAddresses";
    public static final String ATTR_DATA_COSIGNERS = "Cosigners";
}

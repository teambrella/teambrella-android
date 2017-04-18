package com.teambrella.android.api;

/**
 * Model
 */
public class TeambrellaModel {


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


}

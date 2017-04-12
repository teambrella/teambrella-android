package com.teambrella.android.api;

/**
 * Model
 */
public class TeambrellaModel {


    /*Request*/
    static final String ATTR_REQUEST_TIMESTAMP = "Timestamp";
    static final String ATTR_REQUEST_SIGNATURE = "Signature";
    static final String ATTR_REQUEST_PUBLIC_KEY = "PublicKey";
    static final String ATTR_REQUEST_TEAM_ID = "TeamId";


    /*Response*/
    static final String ATTR_STATUS = "Status";
    static final String ATTR_DATA = "Data";


    /*Status*/
    static final String ATTR_STATUS_TIMESTAMP = "Timestamp";
    static final String ATTR_STATUS_RESULT_CODE = "ResultCode";
    static final String ATTR_STATUS_ERROR_MESSAGE = "ErrorMessage";

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
}

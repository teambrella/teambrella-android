package com.teambrella.android.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

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
    public static final int TX_CLIENT_RESOLUTION_APPROVED = 2;
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


    /* User address status*/
    public static final int USER_ADDRESS_STATUS_PREVIOUS = 0;
    public static final int USER_ADDRESS_STATUS_CURRENT = 1;
    public static final int USER_ADDRESS_STATUS_NEXT = 3;
    public static final int USER_ADDRESS_STATUS_ARCHIVE = 4;


    /*Request*/
    public static final String ATTR_REQUEST_TEAM_ID = "TeamId";
    public static final String ATTR_REQUEST_USER_ID = "UserId";
    public static final String ATTR_REQUEST_OFFSET = "Offset";
    public static final String ATTR_REQUEST_LIMIT = "Limit";
    public static final String ATTR_REQUEST_SINCE = "Since";
    public static final String ATTR_REQUEST_ADD = "add";
    public static final String ATTR_REQUEST_POSITION = "Position";
    public static final String ATTR_REQUEST_OPT_INTO = "OptInto";

    public static final String ATTR_REQUEST_TOPIC_ID = "TopicId";
    public static final String ATTR_REQUEST_TEXT = "Text";
    public static final String ATTR_REQUEST_ID = "Id";
    public static final String ATTR_REQUEST_CLAIM_ID = "claimId";
    public static final String ATTR_REQUEST_TEAMMATE_ID_FILTER = "TeammateIdFilter";
    public static final String ATTR_REQUEST_MY_VOTE = "MyVote";
    public static final String ATTR_REQUEST_TEAMMATE_ID = "TeammateId";


    /*Response*/
    public static final String ATTR_STATUS = "Status";
    public static final String ATTR_DATA = "Data";
    public static final String ATTR_METADATA_ = "MetaData";


    /* Metadata */
    public static final String ATTR_METADATA_ORIGINAL_SIZE = "OriginalSize";
    public static final String ATTR_METADATA_DIRECTION = "direction";
    public static final String ATTR_METADATA_FORCE = "force";
    public static final String ATTR_METADATA_RELOAD = "reload";
    public static final String ATTR_METADATA_NEXT_DIRECTION = "next";
    public static final String ATTR_METADATA_PREVIOUS_DIRECTION = "previous";
    public static final String ATTR_METADATA_SIZE = "size";

    /*Status*/
    public static final String ATTR_STATUS_TIMESTAMP = "Timestamp";
    public static final String ATTR_STATUS_RESULT_CODE = "ResultCode";
    public static final String ATTR_STATUS_ERROR_MESSAGE = "ErrorMessage";
    public static final String ATTR_STATUS_URI = "uri";


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
    public static final String ATTR_DATA_COVER_ME = "TheyCoverMeAmount";
    public static final String ATTR_DATA_COVER_THEM = "ICoverThemAmount";
    public static final String ATTR_DATA_SMALL_PHOTOS = "SmallPhotos";
    public static final String ATTR_DATA_BIG_PHOTOS = "BigPhotos";
    public static final String ATTR_DATA_SMALL_PHOTO = "SmallPhoto";
    public static final String ATTR_DATA_NAME = "Name";
    public static final String ATTR_DATA_WEIGHT = "Weight";
    public static final String ATTR_DATA_FB_NAME = "FBName";
    public static final String ATTR_DATA_MODEL = "Model";
    public static final String ATTR_DATA_OBJECT_NAME = "ObjectName";
    public static final String ATTR_DATA_ORIGINAL_POST_TEXT = "OriginalPostText";
    public static final String ATTR_DATA_UNREAD_COUNT = "UnreadCount";
    public static final String ATTR_DATA_CLAIM_AMOUNT = "ClaimAmount";
    public static final String ATTR_DATA_YEAR = "Year";
    public static final String ATTR_DATA_UNREAD = "Unread";
    public static final String ATTR_DATA_CLAIM_LIMIT = "ClaimLimit";
    public static final String ATTR_DATA_RISK = "Risk";
    public static final String ATTR_DATA_PROXY_RANK = "ProxyRank";
    public static final String ATTR_DATA_RISK_VOTED = "RiskVoted";
    public static final String ATTR_DATA_TOTALLY_PAID = "TotallyPaid";
    public static final String ATTR_DATA_TOTALLY_PAID_AMOUNT = "TotallyPaidAmount";
    public static final String ATTR_DATA_IS_JOINING = "IsJoining";
    public static final String ATTR_DATA_IS_VOTING = "IsVoting";
    public static final String ATTR_DATA_CLAIMS_COUNT = "ClaimsCount";
    public static final String ATTR_DATA_TEST_NET = "Testnet";
    public static final String ATTR_DATA_PUBLIC_KEY = "PublicKey";
    public static final String ATTR_DATA_ADDRESS = "Address";
    public static final String ATTR_DATA_ADDRESS_ID = "AddressId";
    public static final String ATTR_DATA_TEAMMATE_ID = "TeammateId";
    public static final String ATTR_DATA_STATUS = "Status";
    public static final String ATTR_DATA_DATE_CREATED = "DateCreated";
    public static final String ATTR_DATA_KEY_ORDER = "KeyOrder";
    public static final String ATTR_DATA_BTC_AMOUNT = "AmountBTC";
    public static final String ATTR_DATA_CLAIM_ID = "ClaimId";
    public static final String ATTR_DATA_CLAIM_TEAMMATE_ID = "ClaimTeammateId";
    public static final String ATTR_DATA_KIND = "Kind";
    public static final String ATTR_DATA_STATE = "State";
    public static final String ATTR_DATA_INITIATED_TIME = "InitiatedTime";
    public static final String ATTR_DATA_TEAMS = "Teams";
    public static final String ATTR_DATA_KNOWN_SINCE = "KnownSince";
    public static final String ATTR_DATA_IS_DEFAULT = "IsDefault";
    public static final String ATTR_DATA_PAY_TOS = "PayTos";
    public static final String ATTR_DATA_BTC_ADDRESSES = "BTCAddresses";
    public static final String ATTR_DATA_COSIGNERS = "Cosigners";
    public static final String ATTR_DATA_WITHDRAW_REQ_ID = "WithdrawReqId";
    public static final String ATTR_DATA_TXS = "Txs";
    public static final String ATTR_DATA_TX_ID = "TxId";
    public static final String ATTR_DATA_PREVIOUS_TX_ID = "PrevTxId";
    public static final String ATTR_DATA_PREVIOUS_TX_INDEX = "PrevTxIndex";
    public static final String ATTR_DATA_PAY_TO_ID = "PayToId";
    public static final String ATTR_DATA_SIGNATURE = "Signature";
    public static final String ATTR_DATA_TX_INPUTS = "TxInputs";
    public static final String ATTR_DATA_TX_OUTPUTS = "TxOutputs";
    public static final String ATTR_DATA_TX_SIGNATURES = "TxSignatures";
    public static final String ATTR_DATA_LAST_UPDATED = "Since";
    public static final String ATTR_DATA_RESOLUTION = "Resolution";
    public static final String ATTR_DATA_RESOLUTION_TIME = "ResolutionTime";
    public static final String ATTR_DATA_TX_INFOS = "TxInfos";
    public static final String ATTR_DATA_TX_INPUT_ID = "TxInputId";
    public static final String ATTR_DATA_TOPIC_ID = "TopicId";


    public static final String ATTR_DATA_ONE_VOTING = "VotingPart";
    public static final String ATTR_DATA_ONE_BASIC = "BasicPart";
    public static final String ATTR_DATA_ONE_DISCUSSION = "DiscussionPart";
    public static final String ATTR_DATA_ONE_OBJECT = "ObjectPart";
    public static final String ATTR_DATA_ONE_STATS = "StatsPart";
    public static final String ATTR_DATA_ONE_RISK_SCALE = "RiskScalePart";

    public static final String ATTR_DATA_ESTIMATED_EXPENSES = "EstimatedExpenses";
    public static final String ATTR_DATA_DEDUCTIBLE = "Deductible";
    public static final String ATTR_DATA_COVERAGE = "Coverage";
    public static final String ATTR_DATA_INCIDENT_DATE = "IncidentDate";
    public static final String ATTR_DATA_CHAT = "Chat";
    public static final String ATTR_DATA_TEXT = "Text";
    public static final String ATTR_DATA_TEAMMATE_PART = "TeammatePart";
    public static final String ATTR_DATA_CREATED = "Created";
    public static final String ATTR_DATA_LAST_READ = "LastRead";
    public static final String ATTR_DATA_IMAGES = "Images";


    public static final String ATTR_DATA_RANGES = "Ranges";
    public static final String ATTR_DATA_LEFT_RANGE = "LeftRange";
    public static final String ATTR_DATA_RIGHT_RANGE = "RightRange";
    public static final String ATTR_DATA_COUNT = "Count";
    public static final String ATTR_DATA_TEAMMTES_IN_RANGE = "TeammatesInRange";
    public static final String ATTR_DATA_PROXY_AVATAR = "ProxyAvatar";
    public static final String ATTR_DATA_PROXY_NAME = "ProxyName";


    public static final String ATTR_DATA_RATIO_VOTED = "RatioVoted";
    public static final String ATTR_DATA_MY_VOTE = "MyVote";

    public static final String ATTR_DATA_MY_TEAMS = "MyTeams";


    public static final String ATTR_DATA_ITEM_TYPE = "ItemType";
    public static final String ATTR_DATA_AVG_RISK = "AverageRisk";
    public static final String ATTR_DATA_OTHER_AVATARS = "OtherAvatars";
    public static final String ATTR_DATA_CARDS = "Cards";
    public static final String ATTR_DATA_SMALL_PHOTO_OR_AVATAR = "SmallPhotoOrAvatar";
    public static final String ATTR_DATA_AMOUNT = "Amount";
    public static final String ATTR_DATA_TEAM_VOTE = "TeamVote";
    public static final String ATTR_DATA_ITEM_ID = "ItemId";
    public static final String ATTR_DATA_ITEM_DATE = "ItemDate";
    public static final String ATTR_DATA_CHAT_TITLE = "ChatTitle";
    public static final String ATTR_DATA_TOP_POSTER_AVATARS = "TopPosterAvatars";
    public static final String ATTR_DATA_MODEL_OR_NAME = "ModelOrName";
    public static final String ATTR_DATA_ITEM_USER_ID = "ItemUserId";
    public static final String ATTR_DATA_IS_MY_PROXY = "IsMyProxy";
    public static final String ATTR_DATA_AM_I_PROXY = "AmIProxy";
    public static final String ATTR_DATA_LOCATION = "Location";
    public static final String ATTR_DATA_DECISION_FREQUENCY = "DecisionFreq";
    public static final String ATTR_DATA_DISCUSSION_FREQUENCY = "DiscussionFreq";
    public static final String ATTR_DATA_VOTING_FREQUENCY = "VotingFreq";
    public static final String ATTR_DATA_COMMISSION = "Commission";
    public static final String ATTR_DATA_MEMBERS = "Members";
    public static final String ATTR_DATA_POSITION = "Position";


    public static final int FEED_ITEM_TEAMMATE = 0;
    public static final int FEED_ITEM_CLAIM = 1;
    public static final int FEED_ITEM_RULE = 2;
    public static final int FEED_ITEM_TEAM_CHART = 3;
    public static final int FEED_ITEM_TEAM_NOTIFICATION = 100;


    public static final int ATTR_DATA_ITEM_TYPE_SECTION_NEW_MEMBERS = 10;
    public static final int ATTR_DATA_ITEM_TYPE_SECTION_TEAMMATES = 20;
    public static final int ATTR_DATA_ITEM_TYPE_TEAMMATE = 30;


    /**
     * Get Images
     */
    public static ArrayList<String> getImages(String authority, JsonObject object, String property) {
        ArrayList<String> list = new ArrayList<>();

        JsonElement element = object.get(property);

        if (element != null && element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                list.add(authority + item.getAsString());
            }
        }

        return list;
    }

    public static String getImage(String authority, JsonObject object, String property) {
        JsonElement element = property != null ? object.get(property) : object;
        if (element != null && element.isJsonPrimitive()) {
            return authority + element.getAsString();
        }
        return null;
    }


}

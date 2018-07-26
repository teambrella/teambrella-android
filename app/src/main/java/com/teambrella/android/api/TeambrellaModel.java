package com.teambrella.android.api;

import android.support.annotation.StringRes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.R;

import java.util.ArrayList;

/**
 * Model
 */
@SuppressWarnings("ALL")
public class TeambrellaModel {


    public static final class InsuranceType {
        public static final int BICYCLE = 40;
        public static final int CAR_COLLISION_DEDUCTIBLE = 100;
        public static final int CAR_COLLISION = 101;
        public static final int CAR_COMPREHENSIVE = 102;
        public static final int CAR_COLLISION_AND_COMPREHENSIVE = 104;
        public static final int THIRDPARTY = 103;
        public static final int DRONE = 140;
        public static final int MOBILE = 200;
        public static final int HOME_APPLIANCES = 220;
        public static final int DOG = 240;
        public static final int CAT = 241;
        public static final int UNEMPLOYMENT = 260;
        public static final int HEALTH_DENTAL = 280;
        public static final int HEALTH_OTHER = 290;
        public static final int BUSINESS_BEES = 400;
        public static final int BUSINESS_CRIME = 440;
        public static final int BUSINESS_LIABILITY = 460;
    }


    public static final class ClaimStates {
        public static final int VOTING = 0;
        public static final int REVOTING = 10;
        public static final int VOTED = 15;
        public static final int DECLINED = 20;
        public static final int IN_PAYMENT = 30;
        public static final int PROCESSEED = 40;
    }


    public static final class ClaimsListItemType {
        public static final String ITEM_VOTING_HEADER = "voting_header";
        public static final String ITEM_VOTING = "voting";
        public static final String ITEM_VOTED_HEADER = "voted_header";
        public static final String ITEM_VOTED = "voted";
        public static final String ITEM_IN_PAYMENT_HEADER = "in_payment_header";
        public static final String ITEM_IN_PAYMENT = "in_payment";
        public static final String ITEM_PROCESSED_HEADER = "processed_header";
        public static final String ITEM_PROCESSED = "processed";
    }

    public static final class PostStatus {
        public static final String POST_PENDING = "post_pending";
        public static final String POST_SYNCED = "post_synced";
        public static final String POST_ERROR = "post_error";
    }


    public static final class WithdrawlsItemType {
        public static final String ITEM_QUEUED_HEADER = "queued_header";
        public static final String ITEM_QUEDUED = "queued";
        public static final String ITEM_IN_PROCESS_HEADER = "in_process_header";
        public static final String ITEM_IN_PROCESS = "in_procees";
        public static final String ITEM_HISTORY_HEADER = "history_header";
        public static final String ITEM_HISTORY = "history";
    }


    public static final class TeamAccessLevel {
        public static final int NO_ACCESS = 0;
        public static final int HIDDEN_DETAILS_AND_EDIT_MINE = 1;
        public static final int READ_ONLY = 2;
        public static final int READ_ALL_AND_EDIT_MINE = 3;
        public static final int FULL_ACCESS = 4;
        public static final int HIDDEN_DETAILS_AND_STEALTH = -10;
        public static final int READ_ONLY_ALL_AND_STEALTH = -20;
    }


    public static final class Gender {
        public static final int UNKNOWN = 0;
        public static final int MALE = 1;
        public static final int FEMALE = 2;
        public static final int OTHER = 100;
    }


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


    /* User multisig (address) status*/
    public static final int USER_MULTISIG_STATUS_PREVIOUS = 0;
    public static final int USER_MULTISIG_STATUS_CURRENT = 1;
    public static final int USER_MULTISIG_STATUS_NEXT = 2;
    public static final int USER_MULTISIG_STATUS_ARCHIVE = 3;

    public static final int USER_MULTISIG_STATUS_CREATION_FAILED = -400;


    /*Request*/
    public static final String ATTR_REQUEST_TEAM_ID = "TeamId";
    public static final String ATTR_REQUEST_USER_ID = "UserId";
    public static final String ATTR_REQUEST_OFFSET = "Offset";
    public static final String ATTR_REQUEST_LIMIT = "Limit";
    public static final String ATTR_REQUEST_SINCE = "Since";
    public static final String ATTR_REQUEST_ADD = "add";
    public static final String ATTR_REQUEST_POSITION = "Position";
    public static final String ATTR_REQUEST_OPT_INTO = "OptInto";
    public static final String ATTR_REQUEST_DATE = "Date";
    public static final String ATTR_REQUEST_INCIDENT_DATE = "IncidentDate";
    public static final String ATTR_REQUEST_EXPENSES = "Expenses";
    public static final String ATTR_REQUEST_MESSAGE = "Message";
    public static final String ATTR_REQUEST_IMAGES = "Images";
    public static final String ATTR_REQUEST_ADDRESS = "Address";
    public static final String ATTR_REQUEST_SORTED_BY_RISK = "OrderByRisk";
    public static final String ATTR_REQUEST_TO_ADDRESS = "ToAddress";
    public static final String ATTR_REQUEST_AMOUNT = "Amount";
    public static final String ATTR_REQUEST_NEW_MESSAGE_ID = "NewMessageId";


    public static final String ATTR_REQUEST_TOPIC_ID = "TopicId";
    public static final String ATTR_REQUEST_TITLE = "Title";
    public static final String ATTR_REQUEST_TEXT = "Text";
    public static final String ATTR_REQUEST_ID = "Id";
    public static final String ATTR_REQUEST_CLAIM_ID = "ClaimId";
    public static final String ATTR_REQUEST_TEAMMATE_ID_FILTER = "TeammateIdFilter";
    public static final String ATTR_REQUEST_MY_VOTE = "MyVote";
    public static final String ATTR_REQUEST_TEAMMATE_ID = "TeammateId";
    public static final String ATTR_REQUEST_TO_USER_ID = "ToUserId";
    public static final String ATTR_REQUEST_IS_MUTED = "IsMuted";
    public static final String ATTR_REQUEST_NEW_POST_ID = "NewPostId";

    /*Response*/
    public static final String ATTR_STATUS = "Status";
    public static final String ATTR_DATA = "Data";
    public static final String ATTR_METADATA_ = "MetaData";


    /* Metadata */
    public static final String ATTR_METADATA_ORIGINAL_SIZE = "OriginalSize";
    public static final String ATTR_METADATA_DIRECTION = "direction";
    public static final String ATTR_METADATA_FORCE = "force";
    public static final String ATTR_METADATA_RELOAD = "reload";
    public static final String ATTR_METADATA_ITEMS_UPDATED = "ItemsUpdated";
    public static final String ATTR_METADATA_NEXT_DIRECTION = "next";
    public static final String ATTR_METADATA_PREVIOUS_DIRECTION = "previous";
    public static final String ATTR_METADATA_SIZE = "size";

    /*Status*/
    public static final String ATTR_STATUS_TIMESTAMP = "Timestamp";
    public static final String ATTR_STATUS_RESULT_CODE = "ResultCode";
    public static final String ATTR_STATUS_ERROR_MESSAGE = "ErrorMessage";
    public static final String ATTR_STATUS_URI = "uri";
    public static final String ATTR_STATUS_RECOMMENDING_VERSION = "RecommendedVersion";


    public static final int VALUE_STATUS_RESULT_CODE_SUCCESS = 0;
    public static final int VALUE_STATUS_RESULT_CODE_FATAL = 1;
    public static final int VALUE_STATUS_RESULT_CODE_AUTH = 2;
    public static final int VALUE_STATUS_RESULT_USER_HAS_ANOTHER_KEY = 5;
    public static final int VALUE_STATUS_RESULT_USER_HAS_NO_TEAM = 6;
    public static final int VALUE_STATUS_RESULT_USER_HAS_NO_TEAM_BUT_APPLICTION_PENDING = 8;
    public static final int VALUE_STATUS_RESULT_USER_HAS_NO_TEAM_BUT_APPLICTION_APPROVED = 9;
    public static final int VALUE_STATUS_RESULT_NOT_SUPPORTED_CLIENT_VERSION = 12;
    public static final int VALUE_STATUS_RESULT_USER_HAS_NO_TEAM_BUT_APPLICATION_STARTED = 13;


    /*Data*/
    public static final String ATTR_DATA_MY_TEAMMATE_ID = "MyTeammateId";
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
    public static final String ATTR_DATA_WEIGHT_COMBINED = "WeightCombined";
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
    public static final String ATTR_DATA_LIMIT_ANOUNT = "LimitAmount";
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
    public static final String ATTR_DATA_PUBLIC_KEY_ADDRESS = "CryptoAddress";
    public static final String ATTR_DATA_ADDRESS = "Address";
    public static final String ATTR_DATA_CREATION_TX = "BlockchainTxId";
    public static final String ATTR_DATA_MULTISIG_ID = "MultisigId";
    public static final String ATTR_DATA_TEAMMATE_ID = "TeammateId";
    public static final String ATTR_DATA_STATUS = "Status";
    public static final String ATTR_DATA_DATE_CREATED = "DateCreated";
    public static final String ATTR_DATA_KEY_ORDER = "KeyOrder";
    public static final String ATTR_DATA_CRYPTO_AMOUNT = "AmountCrypto";
    public static final String ATTR_DATA_CLAIM_ID = "ClaimId";
    public static final String ATTR_DATA_ONE_CLAIM_ID = "OneClaimId";
    public static final String ATTR_DATA_CLAIM_COUNT = "ClaimCount";
    public static final String ATTR_DATA_CLAIM_TEAMMATE_ID = "ClaimTeammateId";
    public static final String ATTR_DATA_KIND = "Kind";
    public static final String ATTR_DATA_STATE = "State";
    public static final String ATTR_DATA_INITIATED_TIME = "InitiatedTime";
    public static final String ATTR_DATA_TEAMS = "Teams";
    public static final String ATTR_DATA_KNOWN_SINCE = "KnownSince";
    public static final String ATTR_DATA_IS_DEFAULT = "IsDefault";
    public static final String ATTR_DATA_PAY_TOS = "PayTos";
    public static final String ATTR_DATA_MULTISIGS = "Multisigs";
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
    public static final String ATTR_DATA_SINCE = "Since";
    public static final String ATTR_DATA_RESOLUTION = "Resolution";
    public static final String ATTR_DATA_RESOLUTION_TIME = "ResolutionTime";
    public static final String ATTR_DATA_TX_INFOS = "TxInfos";
    public static final String ATTR_DATA_TX_INPUT_ID = "TxInputId";
    public static final String ATTR_DATA_TX_Hash = "TxHash";
    public static final String ATTR_DATA_BLOCKCHAIN_TX_ID = "BlockchainTxId";
    public static final String ATTR_DATA_CRYPTO_CONTRACT = "CryptoContracts";
    public static final String ATTR_DATA_TOPIC_ID = "TopicId";
    public static final String ATTR_DATA_VOTERS = "Voters";
    public static final String ATTR_DATA_ME = "Me";
    public static final String ATTR_DATA_CRYPTO_BALANCE = "CryptoBalance";
    public static final String ATTR_DATA_CRYPTO_RESERVED = "CryptoReserved";
    public static final String ATTR_DATA_CURRENCY_RATE = "CurrencyRate";
    public static final String ATTR_DATA_FUND_ADDRESS = "FundAddress";
    public static final String ATTR_DATA_NEED_CRYPTO = "NeedCrypto";
    public static final String ATTR_DATA_RECOMMENDED_CRYPTO = "RecommendedCrypto";
    public static final String ATTR_DATA_GENDER = "Gender";
    public static final String ATTR_DATA_REIMBURSEMENT = "Reimbursement";


    public static final String ATTR_DATA_ONE_VOTING = "VotingPart";
    public static final String ATTR_DATA_ONE_BASIC = "BasicPart";
    public static final String ATTR_DATA_ONE_DISCUSSION = "DiscussionPart";
    public static final String ATTR_DATA_ONE_OBJECT = "ObjectPart";
    public static final String ATTR_DATA_ONE_STATS = "StatsPart";
    public static final String ATTR_DATA_ONE_RISK_SCALE = "RiskScalePart";
    public static final String ATTR_DATA_ONE_TEAM = "TeamPart";
    public static final String ATTR_DATA_ONE_TEAMMATE = "TeammatePart";
    public static final String ATTR_DATA_ONE_COVERAGE = "CoveragePart";

    public static final String ATTR_DATA_ESTIMATED_EXPENSES = "EstimatedExpenses";
    public static final String ATTR_DATA_DEDUCTIBLE = "Deductible";
    public static final String ATTR_DATA_COVERAGE = "Coverage";
    public static final String ATTR_DATA_INCIDENT_DATE = "IncidentDate";
    public static final String ATTR_DATA_CHAT = "Chat";
    public static final String ATTR_DATA_TEXT = "Text";
    public static final String ATTR_DATA_TEAMMATE_PART = "TeammatePart";
    public static final String ATTR_DATA_CREATED = "Created";
    public static final String ATTR_DATA_ADDED = "Added";
    public static final String ATTR_DATA_LAST_READ = "LastRead";
    public static final String ATTR_DATA_IMAGES = "Images";
    public static final String ATTR_DATA_LOCAL_IMAGES = "LocalImages";
    public static final String ATTR_DATA_SMALL_IMAGES = "SmallImages";
    public static final String ATTR_DATA_IMAGE_RATIOS = "ImageRatios";
    public static final String ATTR_DATA_DIMENSION_RATIO = "DimensionRatio";
    public static final String ATTR_DATA_MESSAGES = "Messages";


    public static final String ATTR_DATA_RANGES = "Ranges";
    public static final String ATTR_DATA_LEFT_RANGE = "LeftRange";
    public static final String ATTR_DATA_RIGHT_RANGE = "RightRange";
    public static final String ATTR_DATA_COUNT = "Count";
    public static final String ATTR_DATA_TEAMMTES_IN_RANGE = "TeammatesInRange";
    public static final String ATTR_DATA_PROXY_AVATAR = "ProxyAvatar";
    public static final String ATTR_DATA_PROXY_NAME = "ProxyName";
    public static final String ATTR_DATA_SINCE_LAST_POST_MINUTES = "SinceLastPostMinutes";
    public static final String ATTR_DATA_SINCE_LAST_MESSAGE_MINUTES = "SinceLastMessageMinutes";


    public static final String ATTR_DATA_RATIO_VOTED = "RatioVoted";
    public static final String ATTR_DATA_MY_VOTE = "MyVote";
    public static final String ATTR_DATA_CAN_VOTE = "CanVote";

    public static final String ATTR_DATA_MY_TEAMS = "MyTeams";


    public static final String ATTR_DATA_ITEM_TYPE = "ItemType";
    public static final String ATTR_DATA_ITEM_USER_NAME = "ItemUserName";
    public static final String ATTR_DATA_ITEM_USER_AVATAR = "ItemUserAvatar";
    public static final String ATTR_DATA_AVG_RISK = "AverageRisk";
    public static final String ATTR_DATA_VOTE = "Vote";
    public static final String ATTR_DATA_OTHER_AVATARS = "OtherAvatars";
    public static final String ATTR_DATA_CARDS = "Cards";
    public static final String ATTR_DATA_SMALL_PHOTO_OR_AVATAR = "SmallPhotoOrAvatar";
    public static final String ATTR_DATA_AMOUNT = "Amount";
    public static final String ATTR_DATA_TEAM_VOTE = "TeamVote";
    public static final String ATTR_DATA_ITEM_ID = "ItemId";
    public static final String ATTR_DATA_ITEM_DATE = "ItemDate";
    public static final String ATTR_DATA_CHAT_TITLE = "ChatTitle";
    public static final String ATTR_DATA_TOP_POSTER_AVATARS = "TopPosterAvatars";
    public static final String ATTR_DATA_POSTER_COUNT = "PosterCount";
    public static final String ATTR_DATA_MODEL_OR_NAME = "ModelOrName";
    public static final String ATTR_DATA_ITEM_USER_ID = "ItemUserId";
    public static final String ATTR_DATA_IS_MY_PROXY = "IsMyProxy";
    public static final String ATTR_DATA_AM_I_PROXY = "AmIProxy";
    public static final String ATTR_DATA_LOCATION = "Location";
    public static final String ATTR_DATA_DECISION_FREQUENCY = "DecisionFreq";
    public static final String ATTR_DATA_DISCUSSION_FREQUENCY = "DiscussionFreq";
    public static final String ATTR_DATA_VOTING_FREQUENCY = "VotingFreq";
    public static final String ATTR_DATA_OTHER_COUNT = "OtherCount";
    public static final String ATTR_DATA_COMMISSION = "Commission";
    public static final String ATTR_DATA_MEMBERS = "Members";
    public static final String ATTR_DATA_POSITION = "Position";
    public static final String ATTR_DATA_TEAM_LOGO = "TeamLogo";
    public static final String ATTR_DATA_TEAM_NAME = "TeamName";
    public static final String ATTR_DATA_COVERAGE_TYPE = "CoverageType";
    public static final String ATTR_DATA_CURRENCY = "Currency";
    public static final String ATTR_DATA_VOTING_RES_CRYPTO = "VotingRes_Crypto";
    public static final String ATTR_DATA_PAYMENT_RES_CRYPTO = "PaymentRes_Crypto";
    public static final String ATTR_DATA_REMAINED_MINUTES = "RemainedMinutes";
    public static final String ATTR_DATA_TEAM_ACCESS_LEVEL = "TeamAccessLevel";
    public static final String ATTR_DATA_VOTING_ENDS_IN = "VotingEndsIn";
    public static final String ATTR_DATA_OBJECT_COVERAGE = "ObjectCoverage";
    public static final String ATTR_DATA_VOTED_BY_PROXY_USER_ID = "VotedByProxyUserId";
    public static final String ATTR_DATA_DEFAULT_WITHDRAW_ADDRESS = "DefaultWithdrawAddress";
    public static final String ATTR_DATA_SERVER_TX_STATE = "ServerTxState";
    public static final String ATTR_DATA_WITHDRAWAL_DATE = "WithdrawalDate";
    public static final String ATTR_DATA_IS_NEW = "IsNew";
    public static final String ATTR_DATA_IS_MUTED = "IsMuted";
    public static final String ATTR_DATA_MESSAGE_STATUS = "MesageStatus";
    public static final String ATTR_DATA_TOTAL_COMISSION = "TotalCommission";
    public static final String ATTR_DATA_VOTED_PART = "VotedPart";
    public static final String ATTR_DATA_DATE_JOINED = "DateJoined";


    public static final String ATTR_DATA_HE_COVERS_ME02 = "HeCoversMeIf02";
    public static final String ATTR_DATA_HE_COVERS_ME_IF1 = "HeCoversMeIf1";
    public static final String ATTR_DATA_HE_COVERS_ME_IF499 = "HeCoversMeIf499";
    public static final String ATTR_DATA_MY_RISK = "MyRisk";
    public static final String ATTR_DATA_CITY = "City";
    public static final String ATTR_DATA_IS_NEXT_DAY = "isNextDay";
    public static final String ATTR_DATA_INVITE_FRIENDS_TEXT = "InviteFriendsText";
    public static final String ATTR_DATA_LAST_VOTED = "LastVoted";


    public static final int FEED_ITEM_TEAMMATE = 0;
    public static final int FEED_ITEM_CLAIM = 1;
    public static final int FEED_ITEM_RULE = 2;
    public static final int FEED_ITEM_TEAM_CHAT = 3;
    public static final int FEED_ITEM_TEAM_NOTIFICATION = 100;
    public static final int FEED_ITEM_PAY_TO_JOIN = 200;


    public static final int ATTR_DATA_ITEM_TYPE_SECTION_NEW_MEMBERS = 10;
    public static final int ATTR_DATA_ITEM_TYPE_SECTION_TEAMMATES = 20;
    public static final int ATTR_DATA_ITEM_TYPE_TEAMMATE = 30;
    public static final int ATTR_DATA_ITEM_TYPE_SECTION_RISK = 40;


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


    @StringRes
    public static int getInsuranceTypeName(int type) {
        switch (type) {
            case InsuranceType.CAR_COLLISION_DEDUCTIBLE:
                return R.string.collision_deductible_insurance;
            case InsuranceType.DOG:
            case InsuranceType.CAT:
                return R.string.pet_insurance;
            case InsuranceType.BICYCLE:
                return R.string.bike_insurance;
            default:
                return R.string.other_insurance;
        }
    }


    @StringRes
    public static int getObjectType(int type) {
        switch (type) {
            case InsuranceType.CAT:
            case InsuranceType.DOG:
                return R.string.object_pet;
            case InsuranceType.BICYCLE:
                return R.string.object_bike;
            case InsuranceType.CAR_COLLISION:
            case InsuranceType.CAR_COLLISION_AND_COMPREHENSIVE:
            case InsuranceType.CAR_COLLISION_DEDUCTIBLE:
            case InsuranceType.CAR_COMPREHENSIVE:
                return R.string.object_car;
            default:
                return R.string.object;
        }
    }


    @StringRes
    public static int getObjectNameWithOwner(int type, int gender) {
        switch (type) {
            case InsuranceType.BICYCLE:
                switch (gender) {
                    case Gender.MALE:
                        return R.string.his_bike;
                    case Gender.FEMALE:
                        return R.string.her_bike;
                    default:
                        return R.string.object_bike;
                }
            case InsuranceType.CAT:
                switch (gender) {
                    case Gender.MALE:
                        return R.string.his_cat;
                    case Gender.FEMALE:
                        return R.string.her_cat;
                    default:
                        return R.string.object_cat;
                }

            case InsuranceType.DOG:
                switch (gender) {
                    case Gender.MALE:
                        return R.string.his_dog;
                    case Gender.FEMALE:
                        return R.string.her_dog;
                    default:
                        return R.string.object_dog;
                }
            case InsuranceType.CAR_COLLISION:
            case InsuranceType.CAR_COLLISION_AND_COMPREHENSIVE:
            case InsuranceType.CAR_COLLISION_DEDUCTIBLE:
            case InsuranceType.CAR_COMPREHENSIVE:
                switch (gender) {
                    case Gender.MALE:
                        return R.string.his_car;
                    case Gender.FEMALE:
                        return R.string.her_car;
                    default:
                        return R.string.object_car;
                }
            default:
                return R.string.object;
        }
    }


    @StringRes
    public static int getMyObjectNamer(int type) {
        switch (type) {
            case InsuranceType.BICYCLE:
                return R.string.my_bike;
            case InsuranceType.CAT:
                return R.string.my_cat;
            case InsuranceType.DOG:
                return R.string.my_dog;
            case InsuranceType.CAR_COLLISION:
            case InsuranceType.CAR_COLLISION_AND_COMPREHENSIVE:
            case InsuranceType.CAR_COLLISION_DEDUCTIBLE:
            case InsuranceType.CAR_COMPREHENSIVE:
                return R.string.my_car;
            default:
                return R.string.object;
        }
    }


}

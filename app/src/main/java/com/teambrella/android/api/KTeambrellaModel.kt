package com.teambrella.android.api

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject


const val TO = "To"
const val USER_NAME = "UserName"
const val FACEBOOK_URL = "FacebookUrl"
const val CLAIM_AMOUNT = "ClaimAmount"
const val CMD = "Cmd"
const val POST_ID = "PostId"
const val CONTENT = "Content"
const val BALANCE_CRYPTO = "BalanceCrypto"
const val BALANCE_FIAT = "BalanceFiat"
const val MESSAGE = "Message"
const val TOPIC_NAME = "TopicName"
const val TEAMMATE = "Teammate"
const val CLAIM = "Claim"
const val DISCUSSION = "Discussion"
const val IS_MY_TOPIC = "MyTopic"
const val USER_GENDER = "UserGender"
const val IMAGE_INDEX = "ImageIndex"
const val DATE_PAYMENT_FINISHED = "DatePaymentFinished"
const val SHARED_URL = "SharedUrl"


object ChatItems {
    const val CHAT_ITEM_MESSAGE = "message"
    const val CHAT_ITEM_MY_MESSAGE = "my_message"
    const val CHAT_ITEM_IMAGE = "image"
    const val CHAT_ITEM_MY_IMAGE = "my_image"
    const val CHAT_ITEM_DATE = "date"
    const val CHAT_ITEM_PAID_CLAIM = "paid_claim"
}


/**
 * Teambrella Kotlin Model
 */

var JsonObject?.data: JsonObject?
    get() = getObject(TeambrellaModel.ATTR_DATA)
    set(value) {
        this?.add(TeambrellaModel.ATTR_DATA, value)
    }


var JsonObject?.status: JsonObject?
    get() = getObject(TeambrellaModel.ATTR_STATUS)
    set(value) {
        this?.add(TeambrellaModel.ATTR_STATUS, value)
    }

val JsonObject?.objectPart: JsonObject?
    get() = getObject(TeambrellaModel.ATTR_DATA_ONE_OBJECT)

val JsonObject?.teamPart: JsonObject?
    get() = getObject(TeambrellaModel.ATTR_DATA_ONE_TEAM)

var JsonObject?.uri: String?
    get() = getString(TeambrellaModel.ATTR_STATUS_URI)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_STATUS_URI, value)
    }

val JsonObject?.intId: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_ID)

var JsonObject?.stringId: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_ID)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_ID, value)
    }

val JsonObject?.gender: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_GENDER)

val JsonObject?.smallPhotos: JsonArray?
    get() = this?.get(TeambrellaModel.ATTR_DATA_SMALL_PHOTOS)?.asJsonArray

val JsonObject?.smallImages: JsonArray?
    get() = this?.get(TeambrellaModel.ATTR_DATA_SMALL_IMAGES)?.asJsonArray

var JsonObject?.localImages: JsonArray?
    get() = this?.get(TeambrellaModel.ATTR_DATA_LOCAL_IMAGES)?.asJsonArray
    set(value) {
        this?.add(TeambrellaModel.ATTR_DATA_LOCAL_IMAGES, value)
    }

val JsonObject?.datePaymentFinished: String?
    get() = this?.getString(DATE_PAYMENT_FINISHED)

val JsonObject?.imageRatios: JsonArray?
    get() = this?.get(TeambrellaModel.ATTR_DATA_IMAGE_RATIOS)?.asJsonArray


val JsonObject?.images: JsonArray?
    get() = this?.get(TeambrellaModel.ATTR_DATA_IMAGES)?.takeIf { it.isJsonArray }?.asJsonArray

val JsonObject?.coverageType: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_COVERAGE_TYPE)

val JsonObject?.claimLimit: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_CLAIM_LIMIT)

val JsonObject?.oneClaimId: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_ONE_CLAIM_ID)

val JsonObject?.claimCount: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_CLAIM_COUNT)

val JsonObject?.totallyPaidAmount: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_TOTALLY_PAID_AMOUNT)

val JsonObject?.stats: JsonObject?
    get() = getObject(TeambrellaModel.ATTR_DATA_ONE_STATS)

val JsonObject?.basic: JsonObject?
    get() = getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC)

val JsonObject?.weight: Float?
    get() = getFloat(TeambrellaModel.ATTR_DATA_WEIGHT)

val JsonObject?.proxyRank: Float?
    get() = getFloat(TeambrellaModel.ATTR_DATA_PROXY_RANK)

val JsonObject?.decisionFreq: Float?
    get() = getFloat(TeambrellaModel.ATTR_DATA_DECISION_FREQUENCY)

val JsonObject?.discussionFreq: Float?
    get() = getFloat(TeambrellaModel.ATTR_DATA_DISCUSSION_FREQUENCY)

val JsonObject?.votingFreq: Float?
    get() = getFloat(TeambrellaModel.ATTR_DATA_VOTING_FREQUENCY)

val JsonObject?.isMyProxy: Boolean?
    get() = getBoolean(TeambrellaModel.ATTR_DATA_IS_MY_PROXY)

var JsonObject?.avatar: String?
    get() = getString(TeambrellaModel.ATTR_DATA_AVATAR)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_AVATAR, value)
    }

val JsonObject?.name: String?
    get() = getString(TeambrellaModel.ATTR_DATA_NAME)

val JsonObject?.model: String?
    get() = getString(TeambrellaModel.ATTR_DATA_MODEL)

val JsonObject?.year: String?
    get() = getString(TeambrellaModel.ATTR_DATA_YEAR)

var JsonObject?.userId: String?
    get() = getString(TeambrellaModel.ATTR_DATA_USER_ID)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_USER_ID, value)
    }

val JsonObject?.totallyPaid: Double?
    get() = getDouble(TeambrellaModel.ATTR_DATA_TOTALLY_PAID)

val JsonObject?.risk: Float?
    get() = getFloat(TeambrellaModel.ATTR_DATA_RISK)

val JsonObject?.votingEndsIn: Long?
    get() = getLong(TeambrellaModel.ATTR_DATA_VOTING_ENDS_IN)

val JsonObject?.claimId: Int?
    get() = getInt(TeambrellaModel.ATTR_DATA_CLAIM_ID)

val JsonObject?.To: JsonArray?
    get() = this?.get(TO)?.asJsonArray

val JsonObject?.voting: JsonObject?
    get() = this?.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING)

val JsonObject?.voted: JsonObject?
    get() = this?.getObject(TeambrellaModel.ATTR_DATA_VOTED_PART)

val JsonObject?.remainedMinutes: Long?
    get() = this?.getLong(TeambrellaModel.ATTR_DATA_REMAINED_MINUTES)

var JsonObject?.userName: String?
    get() = getString(USER_NAME)
    set(value) {
        this?.addProperty(USER_NAME, value)
    }

val JsonObject?.objectName: String?
    get() = getString(TeambrellaModel.ATTR_DATA_OBJECT_NAME)

val JsonObject?.objectPhoto: String?
    get() = getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO)

val JsonObject?.message: String?
    get() = getString(MESSAGE)

var JsonObject?.amount: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_AMOUNT)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_AMOUNT, value)
    }

val JsonObject?.teamVote: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_TEAM_VOTE)

val JsonObject?.cards: JsonArray?
    get() = this?.getAsJsonArray(TeambrellaModel.ATTR_DATA_CARDS)

var JsonObject?.unreadCount: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_UNREAD_COUNT)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_UNREAD_COUNT, value)
    }

val JsonObject?.amountStr: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_AMOUNT)

var JsonObject?.kind: Int?
    get() = this.getInt(TeambrellaModel.ATTR_DATA_KIND)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_KIND, value)
    }

val JsonObject?.averageRisk: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_AVG_RISK)

val JsonObject?.serverTxState: Int?
    get() = this.getInt(TeambrellaModel.ATTR_DATA_SERVER_TX_STATE)

val JsonObject?.riskVoted: Double?
    get() = this?.getDouble(TeambrellaModel.ATTR_DATA_RISK_VOTED)

val JsonObject?.myVote: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE)

val JsonObject?.vote: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_VOTE)

val JsonObject?.proxyName: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_PROXY_NAME)

val JsonObject?.proxyAvatar: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_PROXY_AVATAR)

val JsonObject?.riskScale: JsonObject?
    get() = this?.getObject(TeambrellaModel.ATTR_DATA_ONE_RISK_SCALE)

val JsonObject?.avgRisk: Double?
    get() = this?.getDouble(TeambrellaModel.ATTR_DATA_AVG_RISK)

val JsonObject?.otherAvatars: JsonArray?
    get() = this?.get(TeambrellaModel.ATTR_DATA_OTHER_AVATARS)?.asJsonArray

val JsonObject?.otherCount: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_OTHER_COUNT)

val JsonObject?.fbName: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_FB_NAME)

val JsonObject?.facebookUrl: String?
    get() = this?.getString(FACEBOOK_URL)

val JsonObject?.claimAmount: Float?
    get() = this?.getFloat(CLAIM_AMOUNT)

val JsonObject?.itemType: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_ITEM_TYPE)

val JsonObject?.smallPhoto: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO)

val JsonObject?.coverage: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_COVERAGE)

val JsonObject?.smallPhotoOrAvatar: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO_OR_AVATAR)

val JsonObject?.chatTitle: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_CHAT_TITLE)

val JsonObject?.itemUserName: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_ITEM_USER_NAME)

val JsonObject?.itemUserAvatar: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_ITEM_USER_AVATAR)

val JsonObject?.isVoting: Boolean?
    get() = this?.getBoolean(TeambrellaModel.ATTR_DATA_IS_VOTING)

val JsonObject?.modelOrName: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_MODEL_OR_NAME)

var JsonObject?.text: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_TEXT)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_TEXT, value)
    }

val JsonObject?.posterCount: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_POSTER_COUNT)

val JsonObject?.topPosterAvatars: JsonArray?
    get() = this?.get(TeambrellaModel.ATTR_DATA_TOP_POSTER_AVATARS)?.asJsonArray

val JsonObject?.itemIdInt: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_ITEM_ID)

val JsonObject?.itemDate: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_ITEM_DATE)

val JsonObject?.itemUserId: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_ITEM_USER_ID);

val JsonObject?.team: JsonObject?
    get() = this?.getObject(TeambrellaModel.ATTR_DATA_ONE_TEAM)

val JsonObject?.currency: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_CURRENCY)

val JsonObject?.ratioVoted: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_RATIO_VOTED)

val JsonObject?.cryptoBalance: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_CRYPTO_BALANCE)

val JsonObject?.currencyRate: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_CURRENCY_RATE)

val JsonObject?.cmd: Int?
    get() = this?.getInt(CMD)

val JsonObject?.timeStamp: Long?
    get() = this?.getLong(TeambrellaModel.ATTR_STATUS_TIMESTAMP)

var JsonObject?.created: Long?
    get() = this?.getLong(TeambrellaModel.ATTR_DATA_CREATED)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_CREATED, value)
    }

var JsonObject?.added: Long?
    get() = this?.getLong(TeambrellaModel.ATTR_DATA_ADDED)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_ADDED, value)
    }

val JsonObject?.teamId: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID)

val JsonObject?.topicId: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID)

val JsonObject?.topicName: String?
    get() = this?.getString(TOPIC_NAME)

val JsonObject?.postId: String?
    get() = this?.getString(POST_ID)

val JsonObject?.userImage: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_AVATAR)

val JsonObject?.content: String?
    get() = this?.getString(CONTENT)

val JsonObject?.teamLogo: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_TEAM_LOGO)

val JsonObject?.teamName: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_TEAM_NAME)

val JsonObject?.count: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_COUNT)

val JsonObject?.balanceCrypto: String?
    get() = this?.getString(BALANCE_CRYPTO)

val JsonObject?.balanceFiat: String?
    get() = this?.getString(BALANCE_FIAT)

val JsonObject?.teammate: JsonObject?
    get() = this?.getObject(TEAMMATE)

val JsonObject?.claim: JsonObject?
    get() = this?.getObject(CLAIM)

var JsonObject?.chat: JsonArray?
    get() = this?.getAsJsonArray(TeambrellaModel.ATTR_DATA_CHAT)
    set(value) {
        this?.add(TeambrellaModel.ATTR_DATA_CHAT, value)
    }

val JsonObject?.teamAccessLevel: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_DATA_TEAM_ACCESS_LEVEL)

val JsonObject?.coverMe: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_COVER_ME)

val JsonObject?.coverThem: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_COVER_THEM)

val JsonObject?.city: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_CITY)

val JsonObject?.dateJoined: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_DATE_JOINED)

val JsonObject?.originalPostText: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_ORIGINAL_POST_TEXT)

val JsonObject?.sinceLastPostMinutes: Long?
    get() = this?.getLong(TeambrellaModel.ATTR_DATA_SINCE_LAST_POST_MINUTES)


val JsonObject?.heCoversMeIf1: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_HE_COVERS_ME_IF1)

val JsonObject?.hetCoversMeIf02: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_HE_COVERS_ME02)

val JsonObject?.heCoversMeIf499: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_HE_COVERS_ME_IF499)

val JsonObject?.myRisk: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_MY_RISK)

val JsonObject?.teammatePart: JsonObject?
    get() = this?.getObject(TeambrellaModel.ATTR_DATA_TEAMMATE_PART)

var JsonObject?.chatItemType: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_ITEM_TYPE)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, value)
    }

var JsonObject?.sharedUrl: String?
    get() = this?.getString(SHARED_URL)
    set(value) {
        this?.addProperty(SHARED_URL, value)
    }

var JsonObject?.imageIndex: Int?
    get() = this?.getInt(IMAGE_INDEX)
    set(value) {
        this?.addProperty(IMAGE_INDEX, value)
    }

val JsonObject?.discussion: JsonObject?
    get() = this?.getObject(DISCUSSION)

var JsonObject?.discussionPart: JsonObject?
    get() = this?.getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION)
    set(value) {
        this?.add(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION, value)
    }

val JsonObject?.isMyTopic: Boolean?
    get() = this?.getBoolean(IS_MY_TOPIC)

val JsonObject?.userGender: String?
    get() = this?.getString(USER_GENDER)


var JsonObject?.reload: Boolean?
    get() = this?.getBoolean(TeambrellaModel.ATTR_METADATA_RELOAD)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_METADATA_RELOAD, value)
    }

var JsonObject?.forced: Boolean?
    get() = this?.getBoolean(TeambrellaModel.ATTR_METADATA_FORCE)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_METADATA_FORCE, value)
    }
var JsonObject?.direction: String?
    get() = this?.getString(TeambrellaModel.ATTR_METADATA_DIRECTION)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_METADATA_DIRECTION, value)
    }

var JsonObject?.originalSize: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_METADATA_ORIGINAL_SIZE)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_METADATA_ORIGINAL_SIZE, value)
    }

var JsonObject?.size: Int?
    get() = this?.getInt(TeambrellaModel.ATTR_METADATA_SIZE)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_METADATA_SIZE, value)
    }

var JsonObject?.itemsUpdated: Boolean?
    get() = this?.getBoolean(TeambrellaModel.ATTR_METADATA_ITEMS_UPDATED)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_METADATA_ITEMS_UPDATED, value)
    }

var JsonObject?.metadata: JsonObject?
    get() = this?.getObject(TeambrellaModel.ATTR_METADATA_)
    set(value) {
        this?.add(TeambrellaModel.ATTR_METADATA_, value)
    }

var JsonObject?.isNextDay: Boolean?
    get() = this?.getBoolean(TeambrellaModel.ATTR_DATA_IS_NEXT_DAY)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_IS_NEXT_DAY, value)
    }

var JsonObject?.messageStatus: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_MESSAGE_STATUS)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_MESSAGE_STATUS, value)
    }

val JsonObject?.lastRead: Long?
    get() = this?.getLong(TeambrellaModel.ATTR_DATA_LAST_READ)


val JsonObject?.reimbursement: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_REIMBURSEMENT)

val JsonObject?.withdrawalDate: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_WITHDRAWAL_DATE)

val JsonObject?.toAddress: String?
    get() = this?.getString(TeambrellaModel.ATTR_REQUEST_TO_ADDRESS)

val JsonObject?.isNew: Boolean?
    get() = this?.getBoolean(TeambrellaModel.ATTR_DATA_IS_NEW)

private fun JsonObject?.getFloat(key: String): Float? {
    return this?.getJsonElement(key)?.asFloat
}

private fun JsonObject?.getBoolean(key: String): Boolean? {
    return this?.getJsonElement(key)?.asBoolean
}

private fun JsonObject?.getDouble(key: String): Double? {
    return this?.getJsonElement(key)?.asDouble
}

private fun JsonObject?.getLong(key: String): Long? {
    return this?.getJsonElement(key)?.asLong
}

private fun JsonObject?.getInt(key: String): Int? {
    return this?.getJsonElement(key)?.asInt
}


private fun JsonObject?.getJsonElement(key: String): JsonElement? {
    var element = this?.get(key)
    if (element == null || element.isJsonNull) {
        element = null
    }
    return element
}

private fun JsonObject?.getString(key: String): String? = this?.getJsonElement(key)?.asString

private fun JsonObject?.getObject(key: String): JsonObject? {
    val element = this?.getJsonElement(key)
    if (element?.isJsonObject == true) {
        return element.asJsonObject
    }
    return null
}



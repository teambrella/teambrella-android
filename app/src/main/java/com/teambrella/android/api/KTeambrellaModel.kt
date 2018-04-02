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
const val MESSAGE = "Message";
const val TOPIC_NAME = "TopicName";

/**
 * Teambrella Kotlin Model
 */

val JsonObject?.data: JsonObject?
    get() = getObject(TeambrellaModel.ATTR_DATA)


val JsonObject?.status: JsonObject?
    get() = getObject(TeambrellaModel.ATTR_STATUS)

val JsonObject?.uri: String?
    get() = getString(TeambrellaModel.ATTR_STATUS_URI)

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

val JsonObject?.message: String?
    get() = getString(MESSAGE)

var JsonObject?.amount: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_AMOUNT)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_AMOUNT, value)
    }

val  JsonObject?.amountStr: String?
    get() = this?.getString(TeambrellaModel.ATTR_DATA_AMOUNT)

var JsonObject?.kind: Int?
    get() = this.getInt(TeambrellaModel.ATTR_DATA_KIND)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_KIND, value)
    }

val JsonObject?.serverTxState: Int?
    get() = this.getInt(TeambrellaModel.ATTR_DATA_SERVER_TX_STATE)

val JsonObject?.riskVoted: Double?
    get() = this?.getDouble(TeambrellaModel.ATTR_DATA_RISK_VOTED)

val JsonObject?.myVote: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE)

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

val JsonObject?.cmd : Int?
    get() = this?.getInt(CMD)

val JsonObject?.timeStamp : Long?
    get() = this?.getLong(TeambrellaModel.ATTR_STATUS_TIMESTAMP)

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



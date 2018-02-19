package com.teambrella.android.api

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject


const val TO = "To"
const val USER_NAME = "UserName"
const val FACEBOOK_URL = "FacebookUrl"

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


var JsonObject?.amount: Float?
    get() = this?.getFloat(TeambrellaModel.ATTR_DATA_AMOUNT)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_AMOUNT, value)
    }

var JsonObject?.kind: Int?
    get() = this.getInt(TeambrellaModel.ATTR_DATA_KIND)
    set(value) {
        this?.addProperty(TeambrellaModel.ATTR_DATA_KIND, value)
    }

val JsonObject?.serverTxState: Int?
    get() = this.getInt(TeambrellaModel.ATTR_DATA_SERVER_TX_STATE)

val JsonObject?.riskVoted: Double?
    get() = this?.getDouble(TeambrellaModel.ATTR_DATA_RISK_VOTED)

val JsonObject?.myVote: Double?
    get() = this?.getDouble(TeambrellaModel.ATTR_DATA_MY_VOTE)

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

private fun JsonObject?.getObject(key: String): JsonObject? = this?.getJsonElement(key)?.asJsonObject



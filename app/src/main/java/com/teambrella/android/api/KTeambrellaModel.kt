@file:JvmName("KTeambrellaModel")

package com.teambrella.android.api

import com.google.gson.JsonObject

/**
 * Teambrella Kotlin Model
 */
class DataModelObject(private val jsonObject: JsonObject?) {

    val data: DataModelObject?
        get() = DataModelObject(getObject(TeambrellaModel.ATTR_DATA))

    val status: DataModelObject?
        get() = DataModelObject(getObject(TeambrellaModel.ATTR_STATUS))

    val uri: String?
        get() = getString(TeambrellaModel.ATTR_STATUS_URI)

    val stats: DataModelObject?
        get() {
            val obj = getObject(TeambrellaModel.ATTR_DATA_ONE_STATS)
            return if (obj != null) DataModelObject(obj) else null
        }

    val basic: DataModelObject?
        get() = DataModelObject(getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC))

    val weight: Float?
        get() = getFloat(TeambrellaModel.ATTR_DATA_WEIGHT)

    val proxyRank: Float?
        get() = getFloat(TeambrellaModel.ATTR_DATA_PROXY_RANK)

    val decisionFreq: Float?
        get() = getFloat(TeambrellaModel.ATTR_DATA_DECISION_FREQUENCY)

    val discussionFreq: Float?
        get() = getFloat(TeambrellaModel.ATTR_DATA_DISCUSSION_FREQUENCY)

    val votingFreq: Float?
        get() = getFloat(TeambrellaModel.ATTR_DATA_VOTING_FREQUENCY)

    val isMyProxy: Boolean?
        get() = getBoolean(TeambrellaModel.ATTR_DATA_IS_MY_PROXY)

    val avatar: String?
        get() = getString(TeambrellaModel.ATTR_DATA_AVATAR)

    val name: String?
        get() = getString(TeambrellaModel.ATTR_DATA_NAME)

    val model: String?
        get() = getString(TeambrellaModel.ATTR_DATA_MODEL)

    val year: String?
        get() = getString(TeambrellaModel.ATTR_DATA_YEAR)

    val userId: String?
        get() = getString(TeambrellaModel.ATTR_DATA_USER_ID)

    val totallyPaid: Double?
        get() = getDouble(TeambrellaModel.ATTR_DATA_TOTALLY_PAID)

    val risk: Float?
        get() = getFloat(TeambrellaModel.ATTR_DATA_RISK)

    val votingEndsIn: Long?
        get() = getLong(TeambrellaModel.ATTR_DATA_VOTING_ENDS_IN)


    private fun getFloat(key: String): Float? {
        return jsonObject?.get(key)?.asFloat
    }

    private fun getBoolean(key: String): Boolean? {
        return jsonObject?.get(key)?.asBoolean
    }

    private fun getDouble(key: String): Double? {
        return jsonObject?.get(key)?.asDouble
    }

    private fun getLong(key: String): Long? {
        return jsonObject?.get(key)?.asLong
    }

    private fun getInt(key: String): Int? {
        return jsonObject?.get(key)?.asInt
    }

    private fun getString(key: String): String? = jsonObject?.get(key)?.asString

    private fun getObject(key: String): JsonObject? = jsonObject?.get(key)?.asJsonObject
}


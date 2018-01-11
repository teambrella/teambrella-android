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
        get() = DataModelObject(getObject(TeambrellaModel.ATTR_DATA_ONE_STATS))

    val basic: DataModelObject?
        get() = DataModelObject(getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC))

    val weight: Float
        get() = getFloat(TeambrellaModel.ATTR_DATA_WEIGHT, -1f)

    val proxyRank: Float
        get() = getFloat(TeambrellaModel.ATTR_DATA_PROXY_RANK, -1f)

    val decisionFreq: Float
        get() = getFloat(TeambrellaModel.ATTR_DATA_DECISION_FREQUENCY, -1f)

    val discussionFreq: Float
        get() = getFloat(TeambrellaModel.ATTR_DATA_DISCUSSION_FREQUENCY, -1f)

    val votingFreq: Float
        get() = getFloat(TeambrellaModel.ATTR_DATA_VOTING_FREQUENCY, -1f)

    val isMyProxy: Boolean
        get() = getBoolean(TeambrellaModel.ATTR_DATA_IS_MY_PROXY, false)


    private fun getFloat(key: String, defaultValue: Float): Float {
        val value = jsonObject?.get(key)?.asFloat
        return value ?: defaultValue
    }

    private fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val value = jsonObject?.get(key)?.asBoolean
        return value ?: defaultValue
    }

    private fun getString(key: String): String? = jsonObject?.get(key)?.asString

    private fun getObject(key: String): JsonObject? = jsonObject?.get(key)?.asJsonObject
}


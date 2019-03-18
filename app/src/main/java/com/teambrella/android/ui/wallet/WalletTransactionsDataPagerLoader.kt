package com.teambrella.android.ui.wallet

import android.net.Uri
import com.google.gson.JsonArray
import com.teambrella.android.api.*
import com.teambrella.android.data.base.TeambrellaDataPagerLoader
import com.teambrella.android.util.TimeUtils

/**
 * Wallet transactions Data Pager Loader
 */
class WalletTransactionsDataPagerLoader(uri: Uri) : TeambrellaDataPagerLoader(uri, null) {

    override fun onAddNewData(newData: JsonArray) {
        for (element in newData) {
            val item = element.asJsonObject
            val tos = item.To
            item.remove(TO)
            tos?.let {
                for (toElement in tos) {
                    val tosItem = toElement.asJsonObject
                    val newItem = item.deepCopy()
                    newItem.userId = tosItem.userId
                    newItem.userName = tosItem.userName
                    newItem.amount = tosItem.amount
                    newItem.amountFiat = tosItem.amountFiat
                    newItem.amountFiatMonth = item.amountFiatMonth
                    newItem.amountFiatYear = item.amountFiatYear
                    newItem.kind = tosItem.kind
                    newItem.avatar = tosItem.avatar
                    newItem.itemType = TeambrellaModel.ATTR_DATA_ITEM_TYPE_ENTRY
                    val updated = TimeUtils.getDateFromTicks(item.lastUpdated)
                    newItem.itemMonth = updated.month + updated.year * 12
                    if (array.count() == 0 || array.count() > 0 && array.last().asJsonObject.itemMonth != newItem.itemMonth) {
                        val sectionHeader = newItem.deepCopy()
                        sectionHeader.itemType = TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_MONTH
                        array.add(sectionHeader)
                    }

                    array.add(newItem)
                }
            }
        }
    }
}
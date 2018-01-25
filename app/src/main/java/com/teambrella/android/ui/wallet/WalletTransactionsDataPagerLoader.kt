package com.teambrella.android.ui.wallet

import android.net.Uri
import com.google.gson.JsonArray
import com.teambrella.android.api.*
import com.teambrella.android.data.base.TeambrellaDataPagerLoader

/**
 * Wallet transactions Data Pager Loader
 */
class WalletTransactionsDataPagerLoader(uri: Uri?) : TeambrellaDataPagerLoader(uri, null) {

    override fun onAddNewData(newData: JsonArray) {
        val items = JsonArray()
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
                    newItem.kind = tosItem.kind
                    items.add(newItem)
                }
            }
        }
        mArray.addAll(items)
    }
}
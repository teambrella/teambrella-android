package com.teambrella.android.ui.user.wallet

import com.google.gson.JsonArray
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter

/**
 * Wallet Transaction Adapter
 */
class WalletTransactionsAdapter(val pager: IDataPager<JsonArray>, val listener: OnStartActivityListener?)
    : TeambrellaDataPagerAdapter(pager, listener) {

}
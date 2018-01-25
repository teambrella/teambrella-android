package com.teambrella.android.ui.wallet

import android.os.Bundle
import com.google.gson.JsonArray
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.data.base.TeambrellaDataPagerFragment
import com.teambrella.android.ui.base.TeambrellaDataHostActivity

/**
 * Wallet transactions Data Pager fragment
 */
class WalletTransactionsDataPagerFragment : TeambrellaDataPagerFragment() {

    override fun createLoader(args: Bundle?): IDataPager<JsonArray> {
        val loader = WalletTransactionsDataPagerLoader(args?.getParcelable(EXTRA_URI))
        (context as TeambrellaDataHostActivity).component.inject(loader)
        return loader
    }
}
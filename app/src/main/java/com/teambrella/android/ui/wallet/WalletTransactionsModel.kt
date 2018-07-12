package com.teambrella.android.ui.wallet

import android.os.Bundle
import com.teambrella.android.data.base.TeambrellaDataPagerLoader
import com.teambrella.android.ui.base.TeambrellaPagerViewModel
import com.teambrella.android.ui.base.uri

class WalletTransactionsModel : TeambrellaPagerViewModel() {
    override fun getDataPagerLoader(config: Bundle?): TeambrellaDataPagerLoader =
            WalletTransactionsDataPagerLoader(config?.uri ?: throw IllegalArgumentException())
}
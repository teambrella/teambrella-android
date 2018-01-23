package com.teambrella.android.ui.wallet

import com.teambrella.android.data.base.IDataHost
import com.teambrella.android.ui.base.ADataPagerProgressFragment
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter

/**
 * Wallet Transactions Fragment
 */
class WalletTransactionsFragment : ADataPagerProgressFragment<IDataHost>() {

    override fun getAdapter(): ATeambrellaDataPagerAdapter {
        return WalletTransactionsAdapter(mDataHost.getPager(mTag), null)
    }
}
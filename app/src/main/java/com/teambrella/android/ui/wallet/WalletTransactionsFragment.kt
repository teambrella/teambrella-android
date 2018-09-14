package com.teambrella.android.ui.wallet

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.teambrella.android.R
import com.teambrella.android.ui.base.ADataPagerProgressFragment
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter
import com.teambrella.android.ui.widget.DividerItemDecoration

/**
 * Wallet Transactions Fragment
 */
class WalletTransactionsFragment : ADataPagerProgressFragment<IWalletTransactionActivity>() {

    override fun createAdapter(): ATeambrellaDataPagerAdapter {
        return WalletTransactionsAdapter(dataHost.getPager(tags[0]), dataHost.teamId, dataHost.currency, dataHost.cryptoRate,
                ATeambrellaDataPagerAdapter.OnStartActivityListener {
                    startActivity(it)
                })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dividerItemDecoration = object : DividerItemDecoration(context!!.resources.getDrawable(R.drawable.divder)) {
            override fun canDrawChild(view: View, parent: RecyclerView): Boolean {
                var position = parent.getChildAdapterPosition(view)
                var drawDivider = canDrawChild(position, parent)
                if (drawDivider && ++position < parent.adapter?.itemCount ?: 0) {
                    drawDivider = canDrawChild(position, parent)
                }
                return drawDivider
            }

            private fun canDrawChild(position: Int, parent: RecyclerView): Boolean {
                var drawDivider = true
                when (parent.adapter?.getItemViewType(position)) {
                    WalletTransactionsAdapter.ViewType.VIEW_TYPE_HEADER,
                    TeambrellaDataPagerAdapter.VIEW_TYPE_LOADING,
                    TeambrellaDataPagerAdapter.VIEW_TYPE_ERROR,
                    TeambrellaDataPagerAdapter.VIEW_TYPE_BOTTOM -> drawDivider = false
                }
                return drawDivider
            }
        }

        list?.addItemDecoration(dividerItemDecoration)
    }
}
package com.teambrella.android.ui.wallet

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.teambrella.android.R
import com.teambrella.android.data.base.IDataHost
import com.teambrella.android.ui.base.ADataPagerProgressFragment
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter
import com.teambrella.android.ui.widget.DividerItemDecoration

/**
 * Wallet Transactions Fragment
 */
class WalletTransactionsFragment : ADataPagerProgressFragment<IDataHost>() {

    override fun getAdapter(): ATeambrellaDataPagerAdapter {
        return WalletTransactionsAdapter(mDataHost.getPager(mTag), null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dividerItemDecoration = object : DividerItemDecoration(context.resources.getDrawable(R.drawable.divder)) {
            override fun canDrawChild(view: View, parent: RecyclerView): Boolean {
                var position = parent.getChildAdapterPosition(view)
                var drawDivider = canDrawChild(position, parent)
                if (drawDivider && ++position < parent.adapter.itemCount) {
                    drawDivider = canDrawChild(position, parent)
                }
                return drawDivider
            }

            private fun canDrawChild(position: Int, parent: RecyclerView): Boolean {
                var drawDivider = true
                when (parent.adapter.getItemViewType(position)) {
                    WalletTransactionsAdapter.ViewType.VIEW_TYPE_HEADER,
                    TeambrellaDataPagerAdapter.VIEW_TYPE_LOADING,
                    TeambrellaDataPagerAdapter.VIEW_TYPE_ERROR,
                    TeambrellaDataPagerAdapter.VIEW_TYPE_BOTTOM -> drawDivider = false
                }
                return drawDivider
            }
        }

        mList.addItemDecoration(dividerItemDecoration)
    }
}
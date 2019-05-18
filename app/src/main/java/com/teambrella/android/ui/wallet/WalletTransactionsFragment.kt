package com.teambrella.android.ui.wallet

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.amountFiatMonth
import com.teambrella.android.api.amountFiatYear
import com.teambrella.android.api.itemMonth
import com.teambrella.android.api.lastUpdated
import com.teambrella.android.ui.base.ADataPagerProgressFragment
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter
import com.teambrella.android.ui.widget.DividerItemDecoration
import com.teambrella.android.util.AmountCurrencyUtil
import com.teambrella.android.util.TimeUtils
import io.reactivex.Notification
import java.text.SimpleDateFormat
import java.util.*

/**
 * Wallet Transactions Fragment
 */
class WalletTransactionsFragment : ADataPagerProgressFragment<IWalletTransactionActivity>() {

    private val perMonth: TextView? by ViewHolder(R.id.perMonth)
    private val perMonthValue: TextView? by ViewHolder(R.id.perMonthValue)
    private val perYear: TextView? by ViewHolder(R.id.perYear)
    private val perYearValue: TextView? by ViewHolder(R.id.perYearValue)
    private val topContainer: View? by ViewHolder(R.id.top_container)

    override fun createAdapter(): ATeambrellaDataPagerAdapter {
        return WalletTransactionsAdapter(dataHost.getPager(tags[0]), dataHost.teamId, dataHost.currency, dataHost.cryptoRate, this,
                ATeambrellaDataPagerAdapter.OnStartActivityListener {
                    startActivity(it)
                })
    }

    override val contentLayout = R.layout.fragment_transactions_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dividerItemDecoration = object : DividerItemDecoration(context!!.resources.getDrawable(R.drawable.divder)) {
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

        list?.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateHeaderStats()
            }
        })

        list?.addItemDecoration(dividerItemDecoration)
        topContainer?.visibility = View.GONE
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        super.onDataUpdated(notification)
        if (notification.isOnNext) {
            updateHeaderStats()
        }
    }

    private fun updateHeaderStats() {
        val currencySign: String = AmountCurrencyUtil.getCurrencySign(dataHost.currency)
        val firstVisibleItemPos = (list?.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
        val items = dataHost.getPager(tags[0]).loadedData
        require(firstVisibleItemPos != null && firstVisibleItemPos >= 0 && firstVisibleItemPos < items.count()) {return}

        val dateFormatMonth = SimpleDateFormat("LLLL", Locale.getDefault())
        var item = items[firstVisibleItemPos!!].asJsonObject
        item?.amountFiatMonth?.let {
            topContainer?.visibility = View.VISIBLE
            AmountCurrencyUtil.setSignedAmount(this.perMonthValue, it.toInt(), currencySign)
            item?.itemMonth?.let {
                val date = TimeUtils.getDateFromTicks(item.lastUpdated ?: 0L)
                this.perMonth?.text = view!!.context.getString(if (it > 0) R.string.income_for_period else R.string.expenses_for_period, dateFormatMonth.format(date).capitalize())
            }
        }

        item?.amountFiatYear?.let {
            AmountCurrencyUtil.setSignedAmount(this.perYearValue, it.toInt(), currencySign)
            item?.itemMonth?.let {
                this.perYear?.text = view!!.context.getString(if (it > 0) R.string.income_for_year else R.string.expenses_for_year, it/12 + 1900)
            }
        }

//        if (lastItemVisible == (adapter?.itemCount ?: 0) - 2) {
//            needScrollDown = true
//        }
    }

    public fun updatePadding(itemPadding: Int) {
        list?.setPadding(0,0,0,(list?.height ?: 0) - itemPadding)
    }
}
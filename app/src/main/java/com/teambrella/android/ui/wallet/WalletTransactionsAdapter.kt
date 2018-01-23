package com.teambrella.android.ui.wallet

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.To
import com.teambrella.android.api.amount
import com.teambrella.android.api.claimId
import com.teambrella.android.api.userName
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter


/**
 * Wallet Transaction Adapter
 */
class WalletTransactionsAdapter(val pager: IDataPager<JsonArray>, val listener: OnStartActivityListener?)
    : TeambrellaDataPagerAdapter(pager, listener) {

    object ViewType {
        const val VIEW_TYPE_HEADER = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 1
    }

    override fun getItemViewType(position: Int): Int {
        var viewType = super.getItemViewType(position)
        if (viewType == VIEW_TYPE_REGULAR && position == 0) {
            viewType = ViewType.VIEW_TYPE_HEADER
        }
        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        var holder = super.onCreateViewHolder(parent, viewType)
        if (holder == null) {
            holder = when (viewType) {
                ViewType.VIEW_TYPE_HEADER -> Header(parent, R.string.to_address, R.string.milli_ethereum, R.drawable.list_item_header_background_top)
                VIEW_TYPE_REGULAR -> TransactionViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.list_item_transaction, parent, false))
                else -> null
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int, payloads: MutableList<Any>?) {
        super.onBindViewHolder(holder, position, payloads)
        if (holder is TransactionViewHolder) {
            holder.onBind(mPager.loadedData[position - headersCount].asJsonObject)
        } else if (holder is Header) {
            holder.itemView.findViewById<TextView>(R.id.status_subtitle)?.setAllCaps(false)
        }
    }

    override fun getHeadersCount(): Int = 1


    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val to: TextView? = view.findViewById(R.id.to)
        val type: TextView? = view.findViewById(R.id.type)
        val amount: TextView? = view.findViewById(R.id.amount)
        val status: TextView? = view.findViewById(R.id.status)


        fun onBind(item: JsonObject) {
            val claimId = item.claimId
            val to = item.To
            to?.let {
                this.to?.text = to.get(0).asJsonObject?.userName
                val amount = to.get(0).asJsonObject?.amount
                amount?.let {
                    this.amount?.text = itemView.resources.getString(R.string.eth_amount_short_format_string, -it * 1000)
                }
                this.status?.text = "Pending"
            }

            claimId?.let {
                this.type?.text = itemView.resources.getString(R.string.claim_title_format_string, claimId)
            }

            if (claimId == null) {
                this.type?.text = "Withdrawal"
            }
        }
    }
}
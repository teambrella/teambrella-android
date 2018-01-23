package com.teambrella.android.ui.user.wallet

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.claimId
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter

/**
 * Wallet Transaction Adapter
 */
class WalletTransactionsAdapter(val pager: IDataPager<JsonArray>, val listener: OnStartActivityListener?)
    : TeambrellaDataPagerAdapter(pager, listener) {


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        var holder = super.onCreateViewHolder(parent, viewType)
        if (holder == null) {
            holder = TransactionViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.list_item_transaction, parent, false))
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int, payloads: MutableList<Any>?) {
        super.onBindViewHolder(holder, position, payloads)
        if (holder is TransactionViewHolder) {
            holder.onBind(mPager.loadedData[position].asJsonObject)
        }
    }


    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val to: TextView? = view.findViewById(R.id.to)

        fun onBind(item: JsonObject) {
            val claimId = item.claimId
            claimId?.let {
                this.to?.text = Integer.toString(claimId)
            }

        }
    }
}
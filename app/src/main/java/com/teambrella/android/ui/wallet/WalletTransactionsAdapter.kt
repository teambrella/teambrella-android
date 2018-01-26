package com.teambrella.android.ui.wallet

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.image.glide.GlideApp
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter
import com.teambrella.android.ui.claim.ClaimActivity
import com.teambrella.android.ui.withdraw.WithdrawActivity
import kotlinx.android.synthetic.main.list_item_transaction.view.*


/**
 * Wallet Transaction Adapter
 */
class WalletTransactionsAdapter(val pager: IDataPager<JsonArray>, val teamId: Int, val listener: OnStartActivityListener?)
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


    override fun createEmptyViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return DefaultEmptyViewHolder(parent?.context, parent, R.string.no_transactions_yet)
    }

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView? = view.findViewById(R.id.icon)
        val to: TextView? = view.findViewById(R.id.to)
        val type: TextView? = view.findViewById(R.id.type)
        val amount: TextView? = view.findViewById(R.id.amount)
        val status: TextView? = view.findViewById(R.id.status)


        fun onBind(item: JsonObject) {
            val claimId = item.claimId
            this.to?.text = item.userName
            val amount = item.amount
            amount?.let {
                this.amount?.text = itemView.resources.getString(R.string.eth_amount_short_format_string, it * 1000)
            }

            val state = item.serverTxState
            state?.let {
                setStatus(state)
            }

            claimId?.let {
                this.type?.text = itemView.resources.getString(R.string.claim_title_format_string, claimId)
                itemView.setOnClickListener({
                    startActivity(ClaimActivity.getLaunchIntent(itemView.context, claimId, null, teamId))
                })
            }

            if (claimId == null) {
                this.type?.text = itemView.resources.getString(R.string.withdrawal)
                itemView.setOnClickListener({
                    startActivity(WithdrawActivity.getIntent(itemView.context, teamId))
                })
            }

            GlideApp.with(itemView).load(imageLoader.getImageUrl(item.avatar)).apply(RequestOptions().circleCrop())
                    .into(itemView.icon)
        }

        private fun setStatus(state: Int) {
            when (state) {
                TeambrellaModel.TX_STATE_CREATED,
                TeambrellaModel.TX_STATE_APPREOVED_ALL,
                TeambrellaModel.TX_STATE_APPROVED_COSIGNERS,
                TeambrellaModel.TX_STATE_BEING_COSIGNED,
                TeambrellaModel.TX_STATE_SELECTED_FOR_COSIGNING,
                TeambrellaModel.TX_STATE_COSIGNED,
                TeambrellaModel.TX_STATE_PUBLISHED,
                TeambrellaModel.TX_STATE_APPROVED_MASTER -> {
                    this.status?.text = itemView.resources.getText(R.string.transaction_pending)
                    this.status?.visibility = View.VISIBLE
                }
                TeambrellaModel.TX_STATE_BLOCKED_COSIGNERS,
                TeambrellaModel.TX_STATE_ERROR_COSIGNERS_TIMEOUT,
                TeambrellaModel.TX_STATE_ERROR_BAD_REQUEST,
                TeambrellaModel.TX_STATE_ERROR_OUT_OF_FOUNDS,
                TeambrellaModel.TX_STATE_ERROR_SUBMIT_TO_BLOCKCHAIN,
                TeambrellaModel.TX_STATE_ERROR_TOO_MANY_UTXOS,
                TeambrellaModel.TX_STATE_BLOCKED_MASTER -> {
                    this.status?.text = itemView.resources.getText(R.string.transaction_cancelled)
                    this.status?.visibility = View.VISIBLE
                }
                TeambrellaModel.TX_STATE_CONFIRMED -> {
                    this.status?.text = null
                    this.status?.visibility = View.GONE
                }
            }
        }
    }
}
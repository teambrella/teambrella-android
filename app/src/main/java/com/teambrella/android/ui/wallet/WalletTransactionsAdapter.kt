package com.teambrella.android.ui.wallet

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.api.model.json.JsonWrapper
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter
import com.teambrella.android.ui.claim.ClaimActivity
import com.teambrella.android.ui.util.setAvatar
import com.teambrella.android.ui.util.setImage
import com.teambrella.android.ui.withdraw.WithdrawActivity
import com.teambrella.android.util.AmountCurrencyUtil
import com.teambrella.android.util.TimeUtils
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * Wallet Transaction Adapter
 */
class WalletTransactionsAdapter(val pager: IDataPager<JsonArray>,
                                val teamId: Int,
                                val currency: String,
                                val cryptoRate: Float,
                                val fragment: WalletTransactionsFragment,
                                listener: OnStartActivityListener?)
    : TeambrellaDataPagerAdapter(pager, listener) {

    object ViewType {
        const val VIEW_TYPE_HEADER = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 1
    }

    private val dateFormat = SimpleDateFormat("LLLL yyyy", Locale.getDefault())

    override fun getItemViewType(position: Int): Int {
        var viewType = super.getItemViewType(position)

        if (viewType == TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR) {
            val item = JsonWrapper(mPager.loadedData.get(position).asJsonObject)
            when (item.getInt(TeambrellaModel.ATTR_DATA_ITEM_TYPE)) {
                TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_MONTH -> return ViewType.VIEW_TYPE_HEADER
                TeambrellaModel.ATTR_DATA_ITEM_TYPE_ENTRY -> return VIEW_TYPE_REGULAR
            }
        }
        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var holder: RecyclerView.ViewHolder? = super.onCreateViewHolder(parent, viewType)
        if (holder == null) {
            holder = when (viewType) {
                ViewType.VIEW_TYPE_HEADER -> Header(parent, -1, -1, R.drawable.list_item_header_background_middle)
                VIEW_TYPE_REGULAR -> TransactionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_transaction, parent, false))
                else -> null
            }
        }
        return holder!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (holder is TransactionViewHolder) {
            holder.onBind(mPager.loadedData[position].asJsonObject)
            holder.itemWrapper?.measure(0,0)
            fragment.updatePadding(holder.itemWrapper?.measuredHeight ?: 0)
        } else if (holder is Header && getItemViewType(position) == ViewType.VIEW_TYPE_HEADER) {
            holder.itemView.findViewById<TextView>(R.id.status_subtitle)?.setAllCaps(false)
            val item = mPager.loadedData[position].asJsonObject
            val date = TimeUtils.getDateFromTicks(item.lastUpdated ?: 0L)
            //holder.setTitle(java.text.DateFormatSymbols().months[date.month] + " " + (1900+date.year))
            holder.setTitle(dateFormat.format(date))
        }
    }

    override fun createEmptyViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return DefaultEmptyViewHolder(parent?.context, parent, R.string.no_transactions_yet, -1)
    }

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemWrapper: ConstraintLayout? = view.findViewById(R.id.itemWrapper)
        val icon: ImageView? = view.findViewById(R.id.icon)
        val to: TextView? = view.findViewById(R.id.to)
        val type: TextView? = view.findViewById(R.id.type)
        val currencySign: String = AmountCurrencyUtil.getCurrencySign(currency)
        val amountFiat: TextView? = view.findViewById(R.id.amount)
        val amountCrypto: TextView? = view.findViewById(R.id.amount_crypto)

        private fun getPositiveNetString(net: Float, currencySign: String): String {
            return itemView.context.getString(R.string.teammate_net_format_string_plus, currencySign, DecimalFormat("0.##").format(Math.abs(net)))
        }

        private fun getNegativeNetString(net: Float, currencySign: String): String {
            return itemView.context.getString(R.string.teammate_net_format_string_minus, currencySign, DecimalFormat("0.##").format(Math.abs(net)))
        }

        fun onBind(item: JsonObject) {
            val claimId = item.claimId
            item.amount?.let {
                this.amountCrypto?.text = DecimalFormat("0.##").format(Math.abs(it * 1000)) + " mETH"
            }
            item.amountFiat?.let {
                val amount = -it
                this.amountFiat?.text = when {
                    amount > 0 -> Html.fromHtml(getPositiveNetString(amount, currencySign))
                    amount < 0 -> Html.fromHtml(getNegativeNetString(amount, currencySign))
                    else -> itemView.context.getString(R.string.teammate_net_format_string_zero, currencySign)
                }
            }

            val state = item.serverTxState
            state?.let {
                setStatus(state)
            }

            if (claimId != null) {
//                val date = TeambrellaDateUtils.getDate(item.incidentDate)
//                val current = Date()
//                val isTheSameYear = date != null && date.year == current.year
//                this.type?.text = itemView.resources.getString(R.string.claim_title_date_format_string, TeambrellaDateUtils.getDatePresentation(itemView.context, if (isTheSameYear) TeambrellaDateUtils.TEAMBRELLA_UI_DATE_CHAT_SHORT else TeambrellaDateUtils.TEAMBRELLA_UI_DATE, item.incidentDate))
                this.type?.text = itemView.resources.getString(R.string.payout_for_claim)
                itemView.setOnClickListener {
                    startActivity(ClaimActivity.getLaunchIntent(itemView.context, claimId, null, teamId))
                }
                this.to?.text = itemView.resources.getString(R.string.object_format_string, item.modelOrName, item.year)
                icon?.setImage(imageLoader.getImageUrl(item.smallPhoto), R.dimen.rounded_corners_2dp)
            } else {
                this.type?.text = itemView.resources.getString(R.string.withdrawal)
                itemView.setOnClickListener {
                    startActivity(WithdrawActivity.getIntent(itemView.context, teamId, currency, cryptoRate))
                }
                this.to?.text = item.userName
                icon?.setAvatar(imageLoader.getImageUrl(item.avatar))
            }

        }

        private fun setStatus(state: Int) {
            // TODO: Show status graphically
            when (state) {
                TeambrellaModel.TX_STATE_CREATED,
                TeambrellaModel.TX_STATE_APPREOVED_ALL,
                TeambrellaModel.TX_STATE_APPROVED_COSIGNERS,
                TeambrellaModel.TX_STATE_BEING_COSIGNED,
                TeambrellaModel.TX_STATE_SELECTED_FOR_COSIGNING,
                TeambrellaModel.TX_STATE_COSIGNED,
                TeambrellaModel.TX_STATE_PUBLISHED,
                TeambrellaModel.TX_STATE_APPROVED_MASTER -> {
//                    this.status?.text = itemView.resources.getText(R.string.transaction_pending)
//                    this.status?.visibility = View.VISIBLE
                }
                TeambrellaModel.TX_STATE_BLOCKED_COSIGNERS,
                TeambrellaModel.TX_STATE_ERROR_COSIGNERS_TIMEOUT,
                TeambrellaModel.TX_STATE_ERROR_BAD_REQUEST,
                TeambrellaModel.TX_STATE_ERROR_OUT_OF_FOUNDS,
                TeambrellaModel.TX_STATE_ERROR_SUBMIT_TO_BLOCKCHAIN,
                TeambrellaModel.TX_STATE_ERROR_TOO_MANY_UTXOS,
                TeambrellaModel.TX_STATE_BLOCKED_MASTER -> {
//                    this.status?.text = itemView.resources.getText(R.string.transaction_cancelled)
//                    this.status?.visibility = View.VISIBLE
                }
                TeambrellaModel.TX_STATE_CONFIRMED -> {
//                    this.status?.text = null
//                    this.status?.visibility = View.GONE
                }
            }
        }
    }
}
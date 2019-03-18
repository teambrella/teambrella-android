package com.teambrella.android.ui.withdraw

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter
import com.teambrella.android.util.AmountCurrencyUtil
import com.teambrella.android.util.TeambrellaDateUtils
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.regex.Pattern


class WithdrawalsAdapter(pager: IDataPager<JsonArray>
                         , private val mWithdrawActivity: IWithdrawActivity) : TeambrellaDataPagerAdapter(pager) {

    companion object {
        const val VIEW_TYPE_SUBMIT_WITHDRAWAL = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 1
        const val VIEW_TYPE_QUEUED_HEADER = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 2
        const val VIEW_TYPE_IN_PROCESS_HEADER = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 3
        const val VIEW_TYPE_HISTORY_HEADER = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 4
        private const val VIEW_TYPE_WITHDRAWAL = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 5

        private const val MIN_VALUE = 0.00001
        private val HINT_FORMAT_STRING = DecimalFormat("0.00")
        private const val MILLIS = 1000
    }

    private var mDefaultWithdrawAddress: String? = null

    private var mAvailableValue: Double = 0.0
    private var mBalanceValue: Double = 0.0
    private var mReservedValue: Double = 0.0
    private var mCurrency: String? = null
    private var mRate: Double = 0.0


    fun setDefaultWithdrawAddress(address: String) {
        mDefaultWithdrawAddress = address
        notifyItemChanged(0)
    }

    fun setBalanceValue(balance: Double, reserved: Double, currency: String, rate: Double) {
        mBalanceValue = balance
        mReservedValue = reserved
        mCurrency = currency
        mRate = rate
        mAvailableValue = mBalanceValue - mReservedValue - 0.000005
        if (mAvailableValue < MIN_VALUE) {
            mAvailableValue = 0.0
        }
        notifyItemChanged(0)
    }


    override fun getItemViewType(position: Int): Int {
        var viewType: Int
        if (position == 0) {
            viewType = VIEW_TYPE_SUBMIT_WITHDRAWAL
        } else {
            viewType = super.getItemViewType(position)
            if (viewType == TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR) {
                val item = mPager.loadedData.get(position - headersCount).asJsonObject
                when (item.get(TeambrellaModel.ATTR_DATA_ITEM_TYPE).asString) {
                    TeambrellaModel.WithdrawlsItemType.ITEM_QUEUED_HEADER -> viewType = VIEW_TYPE_QUEUED_HEADER
                    TeambrellaModel.WithdrawlsItemType.ITEM_IN_PROCESS_HEADER -> viewType = VIEW_TYPE_IN_PROCESS_HEADER
                    TeambrellaModel.WithdrawlsItemType.ITEM_HISTORY_HEADER -> viewType = VIEW_TYPE_HISTORY_HEADER

                    TeambrellaModel.WithdrawlsItemType.ITEM_HISTORY,
                    TeambrellaModel.WithdrawlsItemType.ITEM_IN_PROCESS,
                    TeambrellaModel.WithdrawlsItemType.ITEM_QUEDUED -> viewType = VIEW_TYPE_WITHDRAWAL
                }
            }
        }
        return viewType
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return super.onCreateViewHolder(parent, viewType) ?: when (viewType) {
            VIEW_TYPE_SUBMIT_WITHDRAWAL -> return SubmitWithdrawViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_withdraw_request, parent, false))
            VIEW_TYPE_QUEUED_HEADER -> return Header(parent, R.string.deferred_withdrawals, R.string.milli_ethereum)
            VIEW_TYPE_IN_PROCESS_HEADER -> return Header(parent, R.string.withdrawals_in_progress, R.string.milli_ethereum, R.drawable.list_item_header_background_top)
            VIEW_TYPE_HISTORY_HEADER -> return Header(parent, R.string.history_withdrawals, R.string.milli_ethereum)
            VIEW_TYPE_WITHDRAWAL -> return WithdrawalViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_withdrawal, parent, false))
            else -> null
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is SubmitWithdrawViewHolder) {
            holder.setAddress(mDefaultWithdrawAddress)
            holder.setBalance(mBalanceValue, mReservedValue, mAvailableValue, mCurrency)
        } else if (holder is WithdrawalViewHolder) {
            holder.onBind(mPager.loadedData.get(position - headersCount).asJsonObject)
        } else if (holder is TeambrellaDataPagerAdapter.Header && itemCount > 2 && getItemViewType(position) != TeambrellaDataPagerAdapter.VIEW_TYPE_BOTTOM) {
            holder.setBackgroundDrawable(if (position == 1) R.drawable.list_item_header_background_top else R.drawable.list_item_header_background_middle)
            val subtitleView = holder.itemView.findViewById<TextView>(R.id.status_subtitle)
            subtitleView.setAllCaps(false)
        }
    }

    override fun createEmptyViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(View(parent.context)) {

        }
    }

    override fun getHeadersCount(): Int {
        return 1
    }


    private inner class SubmitWithdrawViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val addressView: TextView? = itemView.findViewById(R.id.eth_address_input)
        private val amountView: TextView? = itemView.findViewById(R.id.amount_input)
        private val submitView: TextView? = itemView.findViewById(R.id.submit)
        private val cryptoAvailableView: TextView? = itemView.findViewById(R.id.crypto_available)
        private val currencyView: TextView? = itemView.findViewById(R.id.currency)
        private val availableView: TextView? = itemView.findViewById(R.id.currency_available)
        private val infoView: View? = itemView.findViewById(R.id.info)

        init {
            submitView?.setOnClickListener { v ->
                val address = addressView?.text.toString()
                if (!checkEthereum(address)) {
                    addressView?.error = itemView.context.getString(R.string.invalid_ethereum_address_error)
                    return@setOnClickListener
                }

                val amountString = amountView?.text.toString()
                val value = if (!TextUtils.isEmpty(amountString))
                    (BigDecimal(amountView
                            ?.text.toString()).divide(BigDecimal(MILLIS.toString()), MathContext(5, RoundingMode.DOWN)))
                else BigDecimal.ZERO


                val amount = value.toDouble()


                if (amount <= 0.0 || amount > mAvailableValue) {
                    amountView?.error = itemView.context.resources.getString(R.string.invalid_withdraw_amount_error, mAvailableValue.asMillis)
                    return@setOnClickListener
                }
                mWithdrawActivity.requestWithdraw(address, amount)
                amountView?.text = null

            }
            infoView?.setOnClickListener { mWithdrawActivity.showWithdrawInfo() }
        }

        fun setAddress(address: String?) {
            if (TextUtils.isEmpty(addressView?.text)) {
                addressView?.text = address
            }
        }

        @SuppressLint("SetTextI18n")
        fun setBalance(cryptoBalance: Double, reserved: Double, available: Double, currency: String?) {
            amountView?.hint = HINT_FORMAT_STRING.format(available.asMillis)
            submitView?.isEnabled = mAvailableValue > 0
            amountView?.isEnabled = mAvailableValue > 0

            val cryptoCurrency = itemView.context.getString(R.string.milli_ethereum)
            cryptoAvailableView?.text = DecimalFormat("0.##").format(available.asMillis)
            currencyView?.text = cryptoCurrency
            val currencySign = AmountCurrencyUtil.getCurrencySign(mCurrency)
            availableView?.text = currencySign + DecimalFormat("0.##").format(available * mRate)
        }

        private fun checkEthereum(address: String): Boolean {
            return Pattern.matches("^0x[a-fA-F0-9]{40}$", address)
        }
    }

    class WithdrawalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var dateView: TextView? = itemView.findViewById(R.id.date)
        private var addressView: TextView? = itemView.findViewById(R.id.address)
        private var amount: TextView? = itemView.findViewById(R.id.amount)
        private var newIndicator: View? = itemView.findViewById(R.id.new_indicator)

        fun onBind(item: JsonObject) {
            dateView?.text = TeambrellaDateUtils.getDatePresentation(itemView.context, TeambrellaDateUtils.TEAMBRELLA_UI_DATE_SHORT, item.withdrawalDate)
            addressView?.text = item.toAddress
            amount?.text = itemView.context.getString(R.string.eth_amount_short_format_string, 1000 * (item.amount
                    ?: 0f))
            newIndicator?.visibility = if (item.isNew == true) View.VISIBLE else View.INVISIBLE
        }
    }


    private val Double.asMillis: Double
        get() = this * MILLIS
}
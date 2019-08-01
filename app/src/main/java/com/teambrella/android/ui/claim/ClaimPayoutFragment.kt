package com.teambrella.android.ui.claim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.ui.base.ADataFragment
import com.teambrella.android.util.AmountCurrencyUtil
import com.teambrella.android.util.TeambrellaDateUtils
import io.reactivex.Notification
import java.text.DecimalFormat

/**
 * Claims Details Fragment
 */
class ClaimPayoutFragment : ADataFragment<IClaimActivity>() {

    private val cryptoRateView: TextView? by ViewHolder(R.id.claim_crypto_rate)
    private val toPayView: TextView? by ViewHolder(R.id.to_pay)
    private val paidView: TextView? by ViewHolder(R.id.paid)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_claim_payout, container, false)


    override fun onDataUpdated(notification: Notification<JsonObject>) {
        notification.takeIf { it.isOnNext }?.value?.data?.let { _data ->
            val basic = _data.basic
            val sign = _data.team?.currency?.let { AmountCurrencyUtil.getCurrencySign(it) }

            if (basic != null && sign != null && (basic.votingRes ?: 0.0) > 0.0001) {
    
                val rate = basic.claimAmount!! * basic.reimbursement!! / basic.votingRes!!
                val rateFormatted = (if (rate > 1000) "%.0f" else "%.2f").format(rate)
                val paidPart = Math.min(100.0, basic.paymentRes!! * 100 / basic.votingRes!! + 0.5)
    
                cryptoRateView?.text = getString(R.string.rate_format_string, rateFormatted, sign, "ETH")
                toPayView?.text = "%.4f".format(basic.paymentRes) + " ETH"
                paidView?.text = getString(R.string.percentage_format_string, Math.round(paidPart))
            }
        }
    }
}

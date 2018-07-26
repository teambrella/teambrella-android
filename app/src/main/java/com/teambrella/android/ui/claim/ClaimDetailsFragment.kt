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
class ClaimDetailsFragment : ADataFragment<IClaimActivity>() {

    private val claimAmountView: TextView? by ViewHolder(R.id.claim_amount)
    private val expensesView: TextView? by ViewHolder(R.id.estimated_expenses)
    private val deductibleView: TextView? by ViewHolder(R.id.deductible)
    private val coverageView: TextView? by ViewHolder(R.id.coverage)
    private val incidentDateView: TextView? by ViewHolder(R.id.incident_date)
    private val decimalFormat = DecimalFormat.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_claim_details, container, false)


    override fun onDataUpdated(notification: Notification<JsonObject>) {
        notification.takeIf { it.isOnNext }?.value?.data?.let { _data ->
            val basic = _data.basic
            val sign = _data.team?.currency?.let { AmountCurrencyUtil.getCurrencySign(it) }

            if (basic != null && sign != null) {
                claimAmountView?.text = getString(R.string.amount_format_string, sign
                        , decimalFormat.format(Math.round(basic.claimAmount ?: 0f)))
                expensesView?.text = getString(R.string.amount_format_string, sign
                        , decimalFormat.format(Math.round(basic.estimatedExpenses ?: 0.0)))
                deductibleView?.text = getString(R.string.amount_format_string, sign
                        , decimalFormat.format(Math.round(basic.deductible ?: 0.0)))
                coverageView?.text = getString(R.string.percentage_format_string
                        , Math.round((basic.coverage ?: 0f) * 100))

                val date = TeambrellaDateUtils.getDatePresentation(context, TeambrellaDateUtils.TEAMBRELLA_UI_DATE, basic.incidentDate)
                dataHost.setSubtitle(date)
                incidentDateView?.text = date

            }
        }
    }
}

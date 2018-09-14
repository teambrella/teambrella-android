package com.teambrella.android.ui.user.coverage

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.claimLimit
import com.teambrella.android.api.coverage
import com.teambrella.android.api.coveragePart
import com.teambrella.android.api.data
import com.teambrella.android.ui.IMainDataHost
import com.teambrella.android.ui.QRCodeActivity
import com.teambrella.android.ui.base.ADataFragment
import com.teambrella.android.util.AmountCurrencyUtil
import io.reactivex.Notification

/**
 * Coverage fragment
 */
class CoverageFragment : ADataFragment<IMainDataHost>() {

    private val mCoverageView: TextView? by ViewHolder(R.id.coverage)
    private val mCoverageIcon: ImageView? by ViewHolder(R.id.coverage_icon)
    private val mMaxExpenses: TextView? by ViewHolder(R.id.max_expenses_value)
    private val mPossibleExpenses: TextView? by ViewHolder(R.id.possible_expenses_value)
    private val mTeamPay: TextView? by ViewHolder(R.id.team_pay_value)
    private val mCoverageSlider: SeekBar? by ViewHolder(R.id.coverage_slider)
    private val mCoverageProgress: ProgressBar? by ViewHolder(R.id.coverage_progress)
    private val mFundButton: View? by ViewHolder(R.id.fund_wallet)
    private var mIsShown: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_coverage, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataHost.load(tags[0])
        mCoverageSlider?.max = 100
        mCoverageSlider?.progress = 70
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        val data = notification.value?.data
        val coveragePart = data?.coveragePart
        val coverage = coveragePart?.coverage ?: 0f
        val limit = coveragePart?.claimLimit ?: 4500f

        AmountCurrencyUtil.setAmount(mMaxExpenses, Math.round(limit), dataHost.currency)
        AmountCurrencyUtil.setAmount(mPossibleExpenses, Math.round(limit * 0.7f), dataHost.currency)
        AmountCurrencyUtil.setAmount(mTeamPay, Math.round(coverage * limit), dataHost.currency)


        updateCoverageView(coverage)


        mCoverageSlider?.max = Math.round(limit)
        mCoverageSlider?.progress = Math.round(limit * 0.7f)
        mCoverageProgress?.max = mCoverageSlider?.max ?: 0
        mCoverageProgress?.progress = mCoverageSlider?.progress ?: 0


        mCoverageSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                AmountCurrencyUtil.setAmount(mPossibleExpenses, Math.round(i.toFloat()), dataHost.currency)
                AmountCurrencyUtil.setAmount(mTeamPay, Math.round(coverage * i), dataHost.currency)
                mCoverageProgress?.progress = i
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        val fundAddress = dataHost.fundAddress
        if (fundAddress != null) {
            mFundButton?.isEnabled = true
            mFundButton?.setOnClickListener { QRCodeActivity.startQRCode(context, fundAddress) }
        } else {
            mFundButton?.isEnabled = false
        }

        mIsShown = true
    }

    private fun updateCoverageView(coverage: Float) {

        val coverageString = Integer.toString(Math.round(coverage * 100))
        val coveragePercent = SpannableString("$coverageString%")

        coveragePercent.setSpan(ForegroundColorSpan(context?.resources?.getColor(R.color.darkSkyBlue)
                ?: 0), coverageString.length, coverageString.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        coveragePercent.setSpan(RelativeSizeSpan(0.2f)
                , coverageString.length
                , coverageString.length + 1
                , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        mCoverageView?.text = coveragePercent

        when {
            coverage > 0.97f -> mCoverageIcon?.setImageResource(R.drawable.cover_sunny)
            coverage > 0.90f -> mCoverageIcon?.setImageResource(R.drawable.cover_lightrain)
            else -> mCoverageIcon?.setImageResource(R.drawable.cover_rain)
        }
    }
}

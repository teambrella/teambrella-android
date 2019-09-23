package com.teambrella.android.ui.user.coverage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.ui.IMainDataHost
import com.teambrella.android.ui.QRCodeActivity
import com.teambrella.android.ui.base.ADataFragment
import com.teambrella.android.util.AmountCurrencyUtil
import com.teambrella.android.util.StatisticHelper
import io.reactivex.Notification

/**
 * Coverage fragment
 */
class CoverageFragment : ADataFragment<IMainDataHost>() {

    private val mCoverageView: TextView? by ViewHolder(R.id.coverage)
    private val mCoverageIcon: ImageView? by ViewHolder(R.id.coverage_icon)
    private val mDesiredLimitView: TextView? by ViewHolder(R.id.desirable_limit_value)
    private val mMaxPaymentView: TextView? by ViewHolder(R.id.possible_payment_value)
    private val mTeammatesCoveredView: TextView? by ViewHolder(R.id.teammates_pay_value)
    private val mRealCoverageView: TextView? by ViewHolder(R.id.real_coverage_value)
    private val mCoverageSlider: SeekBar? by ViewHolder(R.id.coverage_slider)
    private val mCoverageProgress: ProgressBar? by ViewHolder(R.id.coverage_progress)
    
    private val mRealCoverageExplanaionBlock: View? by ViewHolder(R.id.real_coverage_explanation_block)
    private val mRealCoverageExplanaionView: TextView? by ViewHolder(R.id.real_coverage_explanation)
    
    private val mFundBlock: View? by ViewHolder(R.id.fund_wallet_block)
    private val mFundButton: View? by ViewHolder(R.id.fund_wallet)
    private val mInviteBlock: View? by ViewHolder(R.id.invite_friends_block)
    private val mInviteButton: View? by ViewHolder(R.id.invite_friends)
    private val mCheckConfigBlock: View? by ViewHolder(R.id.check_config_block)
    private val mCheckConfigButton: View? by ViewHolder(R.id.check_config)
    private val mWarningsBlock: View? by ViewHolder(R.id.coverage_general_warnings)
    private val mCoverageDecreaseWarning: View? by ViewHolder(R.id.coverage_decrease_warning)
    private val mCoverageDecreaseButton: View? by ViewHolder(R.id.confirm_coverage_decrease)

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
        var selectedLimit = 0
        val effectivelimit = coveragePart?.claimLimit ?: 0
        val desiredlimit = coveragePart?.desiredLimit ?: 0
        val nextlimit = coveragePart?.nextLimit ?: 0
        val maxPayment = coveragePart?.maxPayment ?: 0f
        val teammatesAtEffLimit = coveragePart?.teammatesAtEffLimit ?: 0
        val teammatesAtLimit = coveragePart?.teammatesAtLimit ?: 0
        val teamLimit = coveragePart?.teamClaimLimit ?: 0
        var mSystemText = coveragePart?.text ?: ""
        var settingsUrl = coveragePart?.settingsUrl ?: ""
        val wasCoverageSuppressed = coveragePart?.wasCoverageSuppressed ?: false

        AmountCurrencyUtil.setAmount(mDesiredLimitView, desiredlimit, dataHost.currency)
        AmountCurrencyUtil.setAmount(mMaxPaymentView, Math.round(maxPayment), dataHost.currency)
        mTeammatesCoveredView?.text = (coveragePart?.teammatesAtEffLimit ?: 0).toString()

        updateCoverageView(effectivelimit * 1f / desiredlimit, desiredlimit * 1f)

        mCoverageSlider?.max = teamLimit
        mCoverageSlider?.progress = desiredlimit
        mCoverageProgress?.max = mCoverageSlider?.max ?: 0
        mCoverageProgress?.progress = mCoverageSlider?.progress ?: 0
    
        var showCheckSettings = false
        var showLimitIncrease = false
        var showInvite = false
        var showFund = false

        if (coverage <= 0 && wasCoverageSuppressed) {
            showCheckSettings = true
        }
        else if (nextlimit > effectivelimit * 1.2f) {
            showLimitIncrease = true
        }
        else if (teammatesAtEffLimit > teammatesAtLimit) {
            showInvite = true
        }
        else if (nextlimit * 1.01 < desiredlimit) {
            showFund = true
        }
        else if (nextlimit > effectivelimit * 1.01) {
            showLimitIncrease = true
        }
    

        mCheckConfigBlock?.visibility = View.GONE
        mInviteBlock?.visibility = if (showInvite && dataHost.inviteFriendsText != "") View.VISIBLE else View.GONE
        mFundBlock?.visibility = if (showFund) View.VISIBLE else View.GONE
        mRealCoverageExplanaionBlock?.visibility = if (showFund || showInvite || showLimitIncrease || showCheckSettings) View.VISIBLE else View.GONE
        
        if (mSystemText != "" && (showFund || showInvite || showCheckSettings)) {
            mRealCoverageExplanaionView?.text = mSystemText
        }
        else if (showLimitIncrease) {
            mRealCoverageExplanaionView?.text = getString(R.string.coverage_explanation_increase, nextlimit, AmountCurrencyUtil.getLocalizedCurrency(context, dataHost.currency))
        }
        else if (showFund) {
            mRealCoverageExplanaionView?.text = getString(R.string.coverage_explanation_fund)
        }
        else if (showCheckSettings) {
            mRealCoverageExplanaionView?.text = getString(R.string.coverage_explanation_settings)
        }
        else if (showInvite) {
            mRealCoverageExplanaionView?.text = getString(R.string.coverage_explanation_need_more_teammates, teammatesAtLimit)
        }
    
        mCoverageView?.alpha = 1f
        mCoverageIcon?.alpha = 1f
        mMaxPaymentView?.alpha = 1f
        mTeammatesCoveredView?.alpha = 1f
        mRealCoverageView?.alpha = 1f
    
        mWarningsBlock?.visibility = View.VISIBLE
        mCoverageDecreaseWarning?.visibility = View.GONE
    
    
        mCoverageSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                val scale = mCoverageSlider!!.max / 100
                selectedLimit = Math.round(i.toFloat()/scale) * scale
                AmountCurrencyUtil.setAmount(mDesiredLimitView, selectedLimit, dataHost.currency)
                mCoverageProgress?.progress = i
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (selectedLimit < effectivelimit) {
                    mWarningsBlock?.visibility = View.GONE
                    mCoverageDecreaseWarning?.visibility = View.VISIBLE
                }
                else
                {
                    mWarningsBlock?.visibility = View.VISIBLE
                    mCoverageDecreaseWarning?.visibility = View.GONE
                    sendSetLimitRequest(selectedLimit)
                }
            }
        })

        val fundAddress = dataHost.fundAddress
        if (fundAddress != null) {
            mFundButton?.isEnabled = true
            mFundButton?.setOnClickListener { QRCodeActivity.startQRCode(context, fundAddress, QRCodeActivity.QRTYPE_ADDRESS) }
        } else {
            mFundButton?.isEnabled = false
        }
    
        mInviteButton?.setOnClickListener {
            startActivity(Intent.createChooser(Intent().setAction(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, dataHost.inviteFriendsText)
                                                       .setType("text/plain"), context?.getString(R.string.invite_friends)))
            StatisticHelper.onInviteFriends(context, dataHost.teamId)
        }
    
        mCoverageDecreaseButton?.setOnClickListener {
            sendSetLimitRequest(selectedLimit)
        }

        if (settingsUrl != "") {
            mCheckConfigBlock?.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(settingsUrl))
                startActivity(browserIntent)
            }
            mCheckConfigBlock?.visibility = View.VISIBLE
        }
        
        mIsShown = true
    }
    
    private fun sendSetLimitRequest(selectedLimit: Int) {
        mCheckConfigBlock?.visibility = View.GONE
        mInviteBlock?.visibility = View.GONE
        mFundBlock?.visibility = View.GONE
        mRealCoverageExplanaionBlock?.visibility = View.GONE
        mCoverageView?.alpha = 0.3f
        mCoverageIcon?.alpha = 0.3f
        mMaxPaymentView?.alpha = 0.3f
        mTeammatesCoveredView?.alpha = 0.3f
        mRealCoverageView?.alpha = 0.3f
    
        dataHost.setClaimLimit(selectedLimit)
    }

    private fun updateCoverageView(coverage: Float, limit: Float) {

//        val coverageString = Integer.toString(Math.round(coverage * limit))
//        val coveragePercent = SpannableString("$coverageString${dataHost.currency}")
        AmountCurrencyUtil.setAmount(mCoverageView, Math.round(coverage * limit), dataHost.currency)
        AmountCurrencyUtil.setAmount(mRealCoverageView, Math.round(coverage * limit), dataHost.currency)
//
//        coveragePercent.setSpan(ForegroundColorSpan(context?.resources?.getColor(R.color.darkSkyBlue)
//                ?: 0), coverageString.length, coverageString.length + dataHost.currency.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//        coveragePercent.setSpan(RelativeSizeSpan(0.2f)
//                , coverageString.length
//                , coverageString.length + dataHost.currency.length
//                , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//        mCoverageView?.text = coveragePercent

        when {
            coverage > 0.90f -> mCoverageIcon?.setImageResource(R.drawable.cover_sunny)
            coverage > 0.80f -> mCoverageIcon?.setImageResource(R.drawable.cover_lightrain)
            else -> mCoverageIcon?.setImageResource(R.drawable.cover_rain)
        }
    }
}

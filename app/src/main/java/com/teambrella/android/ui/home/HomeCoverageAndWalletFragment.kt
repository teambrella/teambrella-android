package com.teambrella.android.ui.home

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.ui.IMainDataHost
import com.teambrella.android.ui.base.ADataFragment
import com.teambrella.android.ui.claim.ReportClaimActivity
import com.teambrella.android.ui.util.setImage
import com.teambrella.android.util.AmountCurrencyUtil
import io.reactivex.Notification

class HomeCoverageAndWalletFragment : ADataFragment<IMainDataHost>() {

    private val objectImage: ImageView? by ViewHolder(R.id.object_picture)
    private val objectModel: TextView? by ViewHolder(R.id.model)
    private val coverage: TextView? by ViewHolder(R.id.coverage)
    private val coverType: TextView? by ViewHolder(R.id.coverage_type)
    private val walletAmount: TextView? by ViewHolder(R.id.wallet_amount)
    private val submitClaim: TextView? by ViewHolder(R.id.submit_claim)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_home_coverage_and_wallet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.coverageInfo)?.setOnClickListener { dataHost.showCoverage() }
        view.findViewById<View>(R.id.walletInfo)?.setOnClickListener { dataHost.showWallet() }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        val data = notification.takeIf { it.isOnNext }?.value?.data
        data?.let { _data ->
            val objectName = _data.objectName
            objectModel?.text = objectName
            objectImage?.setImage(imageLoader.getImageUrl(_data.smallPhoto), R.dimen.rounded_corners_3dp)

            coverage?.text = Html.fromHtml(getString(R.string.coverage_format_string
                    , Math.round((data.coverage ?: 0f) * 100)))

            submitClaim?.visibility = View.VISIBLE
            submitClaim?.isEnabled = dataHost.isFullTeamAccess
            submitClaim?.setOnClickListener {
                ReportClaimActivity.start(context, _data.smallPhoto, objectName, dataHost.teamId
                        , dataHost.currency, dataHost.userCity)
            }
            coverType?.setText(TeambrellaModel.getInsuranceTypeName(dataHost.teamType))
            AmountCurrencyUtil.setCryptoAmount(walletAmount, data.cryptoBalance ?: 0f)
        }
    }
}
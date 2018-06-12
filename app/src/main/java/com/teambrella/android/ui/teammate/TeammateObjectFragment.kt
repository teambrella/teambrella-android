package com.teambrella.android.ui.teammate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.ui.base.AKDataFragment
import com.teambrella.android.ui.claim.ClaimActivity
import com.teambrella.android.ui.image.ImageViewerActivity
import com.teambrella.android.ui.team.claims.ClaimsActivity
import com.teambrella.android.ui.util.setAVGDifference
import com.teambrella.android.ui.util.setImage
import com.teambrella.android.util.AmountCurrencyUtil
import io.reactivex.Notification
import java.util.*

class TeammateObjectFragment : AKDataFragment<ITeammateActivity>() {

    private val objectModel: TextView? by ViewHolder(R.id.model)
    private val objectPicture: ImageView? by ViewHolder(R.id.object_picture)
    private val limit: TextView? by ViewHolder(R.id.limit)
    private val net: TextView? by ViewHolder(R.id.net)
    private val risk: TextView? by ViewHolder(R.id.risk)
    private val seeClaims: TextView? by ViewHolder(R.id.see_claims)
    private val coverageType: TextView? by ViewHolder(R.id.coverage_type)
    private val objectTitle: TextView? by ViewHolder(R.id.object_title)
    private val avgDifference: TextView? by ViewHolder(R.id.avg_difference)


    private var teammateId: Int = 0
    private var teamId: Int = 0
    private var claimId: Int = 0
    private var claimCount: Int = 0
    private var model: String? = null
    private var currency: String? = null
    private var gender: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_teammate_object, container, false)
    }

    override fun onDataUpdated(notification: Notification<JsonObject>?) {
        val data = notification?.takeIf { it.isOnNext }?.value?.data
        data?.let { _data ->
            teammateId = _data.intId ?: teammateId
            gender = _data.basic?.gender ?: gender
        }

        data?.team?.let { _team ->
            coverageType?.setText(TeambrellaModel.getInsuranceTypeName(_team.coverageType ?: 0))
            currency = _team.currency ?: currency
            objectTitle?.setText(
                    if (mDataHost.isItMe)
                        TeambrellaModel.getMyObjectNamer(_team.coverageType ?: 0)
                    else
                        TeambrellaModel.getObjectNameWithOwner(_team.coverageType ?: 0, gender))
        }

        data.objectPart?.let { _object ->
            model = _object.model ?: model
            objectModel?.text = model
            AmountCurrencyUtil.setAmount(limit, Math.round(_object.claimLimit ?: 0f), currency)
            claimId = _object.oneClaimId ?: claimId
            claimCount = _object.claimCount ?: claimCount

            val photos = ArrayList<String>().apply {
                _object.smallPhotos?.let {
                    addAll(it.map { it.asString })
                }
            }
            if (photos.size > 0) {
                objectPicture?.setImage(imageLoader.getImageUrl(photos[0]), R.dimen.rounded_corners_4dp, R.drawable.picture_background_round_4dp)
                objectPicture?.setOnClickListener({ startActivity(ImageViewerActivity.getLaunchIntent(context, photos, 0)) })
            }

        }

        data.basic?.let { _basic ->
            AmountCurrencyUtil.setAmount(net, Math.round(_basic.totallyPaidAmount ?: 0f), currency)
            risk?.text = String.format(Locale.US, "%.2f", _basic.risk ?: 0f)
            avgDifference?.setAVGDifference(_basic.risk ?: 0.2f, _basic.averageRisk ?: 1f)
            teamId = _basic.teamId ?: teamId
        }


        seeClaims?.isEnabled = claimCount > 0
        seeClaims?.text = getString(R.string.see_claims_format_string, claimCount)
        seeClaims?.setOnClickListener({
            if (claimId > 0) {
                ClaimActivity.start(context!!, claimId, model, teamId)
            } else {
                ClaimsActivity.start(context!!, teamId, teammateId, currency)
            }
        })
    }
}
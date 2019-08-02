package com.teambrella.android.ui.claim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.ui.base.ADataProgressFragment
import com.teambrella.android.ui.base.createDataFragment
import com.teambrella.android.ui.chat.ChatActivity
import com.teambrella.android.ui.util.setImage
import com.teambrella.android.ui.widget.ImagePager
import com.teambrella.android.util.ConnectivityUtils
import com.teambrella.android.util.TeambrellaDateUtils
import io.reactivex.Notification
import java.util.*

/**
 * Claim fragment
 */
class ClaimFragment : ADataProgressFragment<IClaimActivity>() {

    companion object {
        private const val DETAILS_FRAGMENT_TAG = "details"
        private const val PAYOUT_FRAGMENT_TAG = "payout"
        private const val VOTING_FRAGMENT_TAG = "voting"
    }

    private val claimPicturesView: ImagePager? by ViewHolder(R.id.image_pager)
    private val originalObjectPictureView: ImageView? by ViewHolder(R.id.object_picture)
    private val messageTitleView: TextView? by ViewHolder(R.id.message_title)
    private val messageTextView: TextView? by ViewHolder(R.id.message_text)
    private val unreadCountView: TextView? by ViewHolder(R.id.unread)
    private val whenView: TextView? by ViewHolder(R.id.`when`)
    private val discussionForeground: View? by ViewHolder(R.id.discussion_foreground)
    private val detailsContainer: View? by ViewHolder(R.id.details_container)
    private val payoutContainer: View? by ViewHolder(R.id.payout_container)

    private var isShown: Boolean = false
    private var teamAccessLevel = TeambrellaModel.TeamAccessLevel.FULL_ACCESS

    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_claim, container, false)
        view.findViewById<View>(R.id.swipe_to_refresh).isEnabled = false
        dataHost.load(tags[0])
        setContentShown(false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.apply {
            val transaction = beginTransaction()
            if (findFragmentByTag(DETAILS_FRAGMENT_TAG) == null) {
                transaction.add(R.id.details_container
                        , createDataFragment(tags, ClaimDetailsFragment::class.java)
                        , DETAILS_FRAGMENT_TAG)
            }
    
            if (findFragmentByTag(PAYOUT_FRAGMENT_TAG) == null) {
                transaction.add(R.id.payout_container
                                , createDataFragment(tags, ClaimPayoutFragment::class.java)
                                , PAYOUT_FRAGMENT_TAG)
            }

            if (findFragmentByTag(VOTING_FRAGMENT_TAG) == null) {
                transaction.add(R.id.voting_container
                        , getInstance(tags, MODE_CLAIM)
                        , VOTING_FRAGMENT_TAG)
            }

            if (!transaction.isEmpty) {
                transaction.commit()
            }
        }
        isShown = false
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        if (notification.isOnNext) {
            notification.value?.data?.let {
                val basic = it.basic
                val team = it.team
                val discussion = it.discussionPart
                val photos = basic.bigPhotos?.mapTo(ArrayList()) { it.asString }
                val smallPhoto = if (photos?.isNotEmpty() == true) photos[0] else null

                if (photos != null && photos.isNotEmpty()) {
                    claimPicturesView?.init(childFragmentManager, photos)
                }

                messageTitleView?.text = getString(R.string.claim_title_format_string, it.intId)

                discussion?.originalPostText?.let {
                    messageTextView?.text = it.replace("<p>".toRegex(), "").replace("</p>".toRegex(), "")
                }

                val unreadCount = discussion?.unreadCount ?: 0
                unreadCountView?.text = unreadCount.toString()
                unreadCountView?.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE

                discussion?.smallPhoto?.let {
                    originalObjectPictureView?.setImage(imageLoader.getImageUrl(it), R.dimen.rounded_corners_4dp)
                }

                whenView?.text = TeambrellaDateUtils.getRelativeTime(-(discussion.sinceLastPostMinutes
                        ?: 0L))

                val claimId = it.intId ?: 0
                val uri = if (claimId > 0) TeambrellaUris.getClaimChatUri(claimId) else null
                teamAccessLevel = team?.teamAccessLevel ?: teamAccessLevel

                if (uri != null) {
                    discussionForeground?.setOnClickListener {
                        dataHost.launchActivity(ChatActivity.getClaimChat(context, dataHost.teamId, claimId, basic.model
                                , smallPhoto, discussion?.topicId, teamAccessLevel, basic.incidentDate))
                    }
                }
                
                val showPayout = (basic.votingRes ?: 0.0) > 0.0001
                payoutContainer?.visibility = if (showPayout) View.VISIBLE else View.GONE
                detailsContainer?.setBackgroundResource(if (showPayout) R.drawable.block else R.drawable.block_last)
            }
            setContentShown(true)
            isShown = true
        } else {
            setContentShown(true, !isShown)
            dataHost.showSnackBar(if (ConnectivityUtils.isNetworkAvailable(context)) R.string.something_went_wrong_error else R.string.no_internet_connection)
        }
    }
}


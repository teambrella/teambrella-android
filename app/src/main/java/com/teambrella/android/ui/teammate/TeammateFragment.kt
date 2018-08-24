package com.teambrella.android.ui.teammate

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.ui.base.ADataProgressFragment
import com.teambrella.android.ui.base.createDataFragment
import com.teambrella.android.ui.chat.ChatActivity
import com.teambrella.android.ui.util.setAvatar
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets
import com.teambrella.android.ui.widget.VoterBar
import com.teambrella.android.util.AmountCurrencyUtil
import com.teambrella.android.util.ConnectivityUtils
import com.teambrella.android.util.TeambrellaDateUtils
import io.reactivex.Notification

class TeammateFragment : ADataProgressFragment<ITeammateActivity>(), VoterBar.VoterBarListener {

    private companion object {
        private const val OBJECT_FRAGMENT_TAG = "object_tag"
        private const val VOTING_TAG = "voting_tag"
        private const val VOTING_STATS_FRAGMENT_TAG = "voting_stats_tag"
        private const val VOTING_RESULT_TAG = "voting_result_tag"
        private const val CONTACTS_TAG = "contacts_tag"

        private fun getLinearPoint(x: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
            return (x - x1) * (y2 - y1) / (x2 - x1) + y1
        }
    }


    private val userPictureView: ImageView? by ViewHolder(R.id.user_picture)
    private val smallImagePictureView: ImageView? by ViewHolder(R.id.small_teammate_icon)
    private val teammateIconView: ImageView? by ViewHolder(R.id.teammate_icon)
    private val avatarsView: TeambrellaAvatarsWidgets? by ViewHolder(R.id.avatars)
    private val scrollViewView: NestedScrollView? by ViewHolder(R.id.scroll_view)
    private val userNameView: TextView? by ViewHolder(R.id.user_name)
    private val messageView: TextView? by ViewHolder(R.id.message)
    private val unreadView: TextView? by ViewHolder(R.id.unread)
    private val whenView: TextView? by ViewHolder(R.id.`when`)
    private val coverMeSectionView: View? by ViewHolder(R.id.cover_me_section)
    private val coverThemSectionView: View? by ViewHolder(R.id.cover_them_section)
    private val wouldCoverPanelView: View? by ViewHolder(R.id.would_cover_panel)
    private val coverMeView: TextView? by ViewHolder(R.id.cover_me)
    private val coverThemView: TextView? by ViewHolder(R.id.cover_them)
    private val wouldCoverMeView: TextView? by ViewHolder(R.id.would_cover_me)
    private val wouldCoverThemView: TextView? by ViewHolder(R.id.would_cover_them)
    private val coversMeTitleView: TextView? by ViewHolder(R.id.covers_me_title)
    private val coversThemTitleView: TextView? by ViewHolder(R.id.covers_them_title)
    private val wouldCoverThemTitleView: TextView? by ViewHolder(R.id.would_cover_them_title)
    private val cityView: TextView? by ViewHolder(R.id.city)
    private val memberSinceView: TextView? by ViewHolder(R.id.member_since)
    private val votingContainerView: View? by ViewHolder(R.id.voting_container)
    private val objectInfoContainerView: View? by ViewHolder(R.id.object_info_container)
    private val votingResultContainerView: View? by ViewHolder(R.id.voting_result_container)


    private var currency: String? = null
    private var teamAccessLevel: Int = TeambrellaModel.TeamAccessLevel.FULL_ACCESS
    private var wouldCoverMeValue: Float? = null
    private var wouldCoverThemValue: Float? = null
    private var teamId: Int? = null
    private var userId: String? = null
    private var mGender: Int? = null
    private var topicId: String? = null

    private var heCoversMeIf02: Float = 0f
    private var heCoversMeIf1: Float = 0f
    private var heCoversMeIf499: Float = 0f
    private var myRisk: Float = 0f
    private var isShown = false


    private var mCoverageUpdateStarted: Boolean = false


    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_teammate, container, false)
                    ?: throw RuntimeException()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.let {
            val transaction = it.beginTransaction()

            if (it.findFragmentByTag(OBJECT_FRAGMENT_TAG) == null) {
                transaction.add(R.id.object_info_container
                        , createDataFragment(tags, TeammateObjectFragment::class.java)
                        , OBJECT_FRAGMENT_TAG)

                if (it.findFragmentByTag(VOTING_STATS_FRAGMENT_TAG) == null) {
                    transaction.add(R.id.voting_statistics_container
                            , getFragmentInstance(tags)
                            , VOTING_STATS_FRAGMENT_TAG)
                }


                if (it.findFragmentByTag(VOTING_TAG) == null) {
                    transaction.add(R.id.voting_container
                            , createDataFragment(tags, TeammateVotingFragment::class.java)
                            , VOTING_TAG)
                }

                if (it.findFragmentByTag(VOTING_RESULT_TAG) == null) {
                    transaction.add(R.id.voting_result_container
                            , createDataFragment(tags, TeammateVotingResultFragment::class.java)
                            , VOTING_RESULT_TAG)
                }

                if (it.findFragmentByTag(CONTACTS_TAG) == null) {
                    transaction.add(R.id.contacts_container
                            , createDataFragment(tags, TeammateContactsFragment::class.java)
                            , CONTACTS_TAG)
                }

            }

            if (!transaction.isEmpty) {
                transaction.commit()
            }
        }

        if (savedInstanceState == null) {
            dataHost.load(tags[0])
        }

        setContentShown(false)

        view.findViewById<View>(R.id.discussion_foreground).setOnClickListener {
            dataHost.launchActivity(ChatActivity.getTeammateChat(context, teamId
                    ?: 0, userId, null, null, topicId, teamAccessLevel))
        }

        isShown = false
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        if (notification.isOnNext) {
            val data = notification.value?.data
            val basic = data?.basic

            data?.teamPart?.let {
                currency = it.currency ?: currency
                teamAccessLevel = it.teamAccessLevel ?: teamAccessLevel
            }

            basic?.let { _basic ->

                AmountCurrencyUtil.setAmount(coverMeView, wouldCoverMeValue
                        ?: _basic.coverMe ?: 0f, currency)

                AmountCurrencyUtil.setAmount(coverThemView, wouldCoverThemValue
                        ?: _basic.coverThem ?: 0f, currency)

                AmountCurrencyUtil.setAmount(wouldCoverMeView, wouldCoverMeValue
                        ?: _basic.coverMe ?: 0f, currency)

                AmountCurrencyUtil.setAmount(wouldCoverThemView, wouldCoverThemValue
                        ?: _basic.coverThem ?: 0f, currency)

                userNameView?.text = _basic.name

                teamId = _basic.teamId
                userId = _basic.userId
                mGender = _basic.gender ?: mGender

                cityView?.text = _basic.city

                _basic.dateJoined?.let {
                    memberSinceView?.text = getString(R.string.member_since_format_string
                            , TeambrellaDateUtils.getDatePresentation(context, TeambrellaDateUtils.TEAMBRELLA_UI_DATE, it))
                }


                val avatarString = _basic.avatar

                avatarString?.let {
                    val url = imageLoader.getImageUrl(it)
                    userPictureView?.setAvatar(url)
                    smallImagePictureView?.setAvatar(url)
                    teammateIconView?.setAvatar(url)
                }

            }

            val voting = data?.voting

            if (voting != null) {
                votingContainerView?.visibility = View.VISIBLE
                objectInfoContainerView?.setBackgroundResource(R.drawable.block)
                coversMeTitleView?.setText(R.string.would_cover_me)

                when (mGender) {
                    TeambrellaModel.Gender.MALE -> {
                        coversThemTitleView?.setText(R.string.would_cover_him)
                        wouldCoverThemTitleView?.setText(R.string.would_cover_him)
                    }
                    TeambrellaModel.Gender.FEMALE -> {
                        coversThemTitleView?.setText(R.string.would_cover_her)
                        wouldCoverThemTitleView?.setText(R.string.would_cover_her)
                    }
                    else -> {
                        coversThemTitleView?.setText(R.string.would_cover_them)
                        wouldCoverThemTitleView?.setText(R.string.would_cover_them)
                    }
                }
            } else {
                when (mGender) {
                    TeambrellaModel.Gender.MALE -> {
                        coversThemTitleView?.setText(R.string.cover_him)
                    }
                    TeambrellaModel.Gender.FEMALE -> {
                        coversThemTitleView?.setText(R.string.cover_her)
                    }
                    else -> {
                        coversThemTitleView?.setText(R.string.cover_them)
                    }
                }
            }

            data?.voted?.let {
                votingResultContainerView?.visibility = View.VISIBLE
            }

            data?.discussionPart?.let { _discussionPart ->
                unreadView?.text = _discussionPart.unreadCount?.toString()
                unreadView?.visibility = if (_discussionPart.unreadCount ?: 0 > 0) View.VISIBLE else View.GONE

                _discussionPart.originalPostText?.let { _text ->
                    @Suppress("DEPRECATION")
                    messageView?.text = Html.fromHtml(_text)
                }

                topicId = _discussionPart.topicId ?: topicId
                whenView?.text = TeambrellaDateUtils.getRelativeTime(-(_discussionPart.sinceLastPostMinutes
                        ?: 0L))

                val posterCount = _discussionPart.posterCount ?: 0
                _discussionPart.topPosterAvatars?.map { it.asString }?.let {
                    avatarsView?.setAvatars(imageLoader, it, posterCount)
                }

            }

            coverThemSectionView?.visibility = if (dataHost.isItMe) View.GONE else View.VISIBLE
            coverMeSectionView?.visibility = if (dataHost.isItMe) View.GONE else View.VISIBLE

            data?.riskScale?.let {
                heCoversMeIf1 = it.heCoversMeIf1 ?: heCoversMeIf1
                heCoversMeIf02 = it.hetCoversMeIf02 ?: heCoversMeIf02
                heCoversMeIf499 = it.heCoversMeIf499 ?: heCoversMeIf499
                myRisk = it.myRisk ?: myRisk
            }

            isShown = true
            setContentShown(true)
        } else {
            setContentShown(true, !isShown)
            dataHost.showSnackBar(if (ConnectivityUtils.isNetworkAvailable(context)) R.string.something_went_wrong_error else R.string.no_internet_connection)
        }
    }


    override fun onVoteChanged(vote: Float, fromUser: Boolean) {
        val scrollBounds = Rect()
        scrollViewView?.getHitRect(scrollBounds)
        if (userPictureView?.getLocalVisibleRect(scrollBounds) == false && wouldCoverPanelView?.visibility == View.INVISIBLE
                && fromUser && !dataHost.isItMe) {
            val animator = ObjectAnimator.ofFloat(wouldCoverPanelView, "translationY", -(wouldCoverPanelView?.height?.toFloat()
                    ?: 0f), 0f).setDuration(300)
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    wouldCoverPanelView?.visibility = View.VISIBLE
                }
            })
            animator.start()
        }

        val value = Math.pow(25.0, vote.toDouble()).toFloat() / 5

        if (value in 0.2f..1f) {
            wouldCoverMeValue = getLinearPoint(value, 0.2f, heCoversMeIf02, 1f, heCoversMeIf1)
            wouldCoverThemValue = wouldCoverMeValue!! * myRisk / value
        } else {
            wouldCoverMeValue = getLinearPoint(value, 1f, heCoversMeIf1, 4.99f, heCoversMeIf499)
            wouldCoverThemValue = wouldCoverMeValue!! * myRisk / value
        }

        if (!mCoverageUpdateStarted) {
            wouldCoverPanelView?.post(mCoverageUpdate)
            mCoverageUpdateStarted = true
        }

        wouldCoverPanelView?.removeCallbacks(mHideWouldCoverPanelRunnable)
    }

    override fun onVoterBarReleased(vote: Float, fromUser: Boolean) {
        if (wouldCoverPanelView?.visibility == View.VISIBLE) {
            wouldCoverPanelView?.postDelayed(mHideWouldCoverPanelRunnable, 1000)
        }
        wouldCoverPanelView?.removeCallbacks(mCoverageUpdate)
        mCoverageUpdateStarted = false
    }


    private val mHideWouldCoverPanelRunnable = {
        val animator = ObjectAnimator.ofFloat(wouldCoverPanelView, "translationY", 0f, -(wouldCoverPanelView?.height?.toFloat()
                ?: 0f)).setDuration(300)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                wouldCoverPanelView?.visibility = View.INVISIBLE
            }
        })

        animator.start()
    }

    private val mCoverageUpdate = object : Runnable {
        override fun run() {
            if (wouldCoverPanelView != null) {
                AmountCurrencyUtil.setAmount(wouldCoverMeView, wouldCoverMeValue ?: 0f, currency)
                AmountCurrencyUtil.setAmount(coverMeView, wouldCoverMeValue ?: 0f, currency)
                AmountCurrencyUtil.setAmount(wouldCoverThemView, wouldCoverThemValue
                        ?: 0f, currency)
                AmountCurrencyUtil.setAmount(coverThemView, wouldCoverThemValue ?: 0f, currency)
                wouldCoverPanelView?.postDelayed(this, 100)
            }

        }
    }
}
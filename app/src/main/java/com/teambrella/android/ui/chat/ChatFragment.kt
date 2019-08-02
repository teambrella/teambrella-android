package com.teambrella.android.ui.chat

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.ui.base.ADataPagerProgressFragment
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter
import com.teambrella.android.ui.claim.ClaimActivity
import com.teambrella.android.ui.claim.MODE_CHAT
import com.teambrella.android.ui.teammate.getTeammateIntent
import com.teambrella.android.ui.util.setAvatar
import com.teambrella.android.ui.util.setImage
import com.teambrella.android.util.AmountCurrencyUtil
import io.reactivex.Notification
import java.text.DecimalFormat
import java.util.*

class KChatFragment : ADataPagerProgressFragment<IChatActivity>() {


    private companion object {
        const val VOTING_FRAGMENT_TAG = "voting_fragment_tag"

        private fun fadeIn(view: View?) {
            view?.let { _view ->
                val animator = ObjectAnimator.ofFloat(_view, "alpha", 0f, 1f)
                animator.duration = 300
                _view.visibility = View.VISIBLE
                animator.start()
            }

        }

        private fun fadeOut(view: View?) {
            view?.let { _view ->
                val animator = ObjectAnimator.ofFloat(_view, "alpha", 1f, 0f)
                animator.duration = 300
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        _view.visibility = View.INVISIBLE
                    }
                })
                animator.start()
            }
        }
    }


    private val votingPanelView: View? by ViewHolder(R.id.voting_panel)
    private val titleView: TextView? by ViewHolder(R.id.title)
    private val subtitleView: TextView? by ViewHolder(R.id.subtitle)
    private val voteValueView: TextView? by ViewHolder(R.id.vote_value)
    private val voteButtonView: View? by ViewHolder(R.id.vote)
    private val voteAction: View? by ViewHolder(R.id.vote_action)
    private val voteTitleView: TextView? by ViewHolder(R.id.vote_title)
    private val iconView: ImageView? by ViewHolder(R.id.image)
    private val votingContainerView: View? by ViewHolder(R.id.voting_container)
    private val votingSectionView: View? by ViewHolder(R.id.voting_section)
    private val dividerView: View? by ViewHolder(R.id.divider)
    private val hideButtonView: View? by ViewHolder(R.id.hide)

    private val decimalFormat = DecimalFormat.getInstance()


    private val votingContainerBehaviour = VotingContainerBehaviour(object : AVotingViewBehaviour.OnHideShowListener {
        override fun onHide() {
            voteButtonView?.visibility = View.VISIBLE
            hideButtonView?.visibility = View.INVISIBLE
            fadeIn(votingSectionView)
            fadeIn(dividerView)
        }

        override fun onShow() {
            voteButtonView?.visibility = View.INVISIBLE
            hideButtonView?.visibility = View.VISIBLE
            fadeOut(votingSectionView)
            fadeOut(dividerView)
        }
    })


    private val votingPanelBehaviour = VotingPanelBehaviour()


    private var userName: String? = null
    private var lastRead: Long? = null

    val layoutManager; get() = list?.layoutManager as LinearLayoutManager
    val scrollY; get() = layoutManager.findFirstVisibleItemPosition()
    val scrollYOffset: Int
        get() {
            val v = layoutManager.getChildAt(0)
            val top = if (v == null) 0 else (v.top - layoutManager.paddingTop)
            return top
        }

    override fun createAdapter(): ATeambrellaDataPagerAdapter {
        return KChatAdapter(dataHost.getPager(tags[0]), context!!, dataHost.teamId,
                when (TeambrellaUris.sUriMatcher.match(dataHost.chatUri)) {
                    TeambrellaUris.CLAIMS_CHAT -> MODE_CLAIM
                    TeambrellaUris.TEAMMATE_CHAT -> MODE_APPLICATION
                    TeambrellaUris.CONVERSATION_CHAT -> MODE_CONVERSATION
                    else -> MODE_DISCUSSION
                })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        when (TeambrellaUris.sUriMatcher.match(dataHost.chatUri)) {
            TeambrellaUris.CLAIMS_CHAT ->
                dataHost.getObservable(ChatActivity.VOTE_DATA_TAG).observe(lifecycleOwner,
                        android.arch.lifecycle.Observer { it?.let { onChatDataUpdated(it) } })
        }

        super.onViewCreated(view, savedInstanceState)
        isRefreshable = false
        list?.setBackgroundColor(Color.TRANSPARENT)
        layoutManager.stackFromEnd = true


        when (TeambrellaUris.sUriMatcher.match(dataHost.chatUri)) {
            TeambrellaUris.CLAIMS_CHAT -> {
                votingPanelView?.visibility = View.VISIBLE
                votingPanelView?.setOnClickListener(this::onClaimClickListener)
                voteButtonView?.setOnClickListener(this::onClaimClickListener)
                hideButtonView?.setOnClickListener(this::onClaimClickListener)
                childFragmentManager.apply {
                    if (findFragmentByTag(VOTING_FRAGMENT_TAG) == null) {
                        beginTransaction().add(R.id.voting_container, com.teambrella.android.ui.claim.getInstance(
                                arrayOf(ChatActivity.CLAIM_DATA_TAG
                                        , ChatActivity.VOTE_DATA_TAG), MODE_CHAT)
                                , VOTING_FRAGMENT_TAG)
                                .commit()
                    }
                }
                dataHost.load(ChatActivity.CLAIM_DATA_TAG)
            }
            TeambrellaUris.TEAMMATE_CHAT -> {
                votingPanelView?.visibility = View.VISIBLE
                votingPanelView?.setOnClickListener(this::onTeammateClickListener)
                voteButtonView?.setOnClickListener(this::onTeammateClickListener)
                votingContainerView?.visibility = View.GONE
            }
            TeambrellaUris.CONVERSATION_CHAT -> {
                votingPanelView?.visibility = View.GONE
                list?.apply {
                    setPadding(paddingLeft, 0, paddingRight, paddingBottom)
                }
                votingContainerView?.visibility = View.GONE
            }
            TeambrellaUris.FEED_CHAT -> {
                votingPanelView?.visibility = View.GONE
                list?.apply {
                    setPadding(paddingLeft, 0, paddingRight, paddingBottom)
                }
                votingContainerView?.visibility = View.GONE
            }
        }

        list?.itemAnimator = null


        votingContainerView?.layoutParams = votingContainerView?.layoutParams?.asCoordinatorParams
                ?.apply {
                    behavior = votingContainerBehaviour
                }

        votingPanelView?.layoutParams = votingPanelView?.layoutParams.asCoordinatorParams
                ?.apply {
                    behavior = votingPanelBehaviour
                }


    }

    override val contentLayout = R.layout.fragment_chat


    override fun onDataUpdated(notification: Notification<JsonObject>) {
        super.onDataUpdated(notification)
        if (notification.isOnNext) {
            val uriString = notification.value.status?.uri
            val uri = if (uriString != null) Uri.parse(uriString) else null
            if (uri != null) {
                when (TeambrellaUris.sUriMatcher.match(Uri.parse(uriString))) {
                    TeambrellaUris.CLAIMS_CHAT,
                    TeambrellaUris.FEED_CHAT,
                    TeambrellaUris.CONVERSATION_CHAT,
                    TeambrellaUris.TEAMMATE_CHAT -> onChatDataUpdated(notification)

                    TeambrellaUris.SET_CLAIM_VOTE -> onVoteDataUpdated(notification)
                }
            }
            val scroll = notification.value.data?.scroll
            if (scroll != null) {
                if (scroll >= 0) {
                    layoutManager.scrollToPositionWithOffset(scroll, notification.value.data?.scrollOffset!!)
                } else {
                    if (!scrollToLastRead()) {
                        layoutManager.scrollToPosition((adapter?.itemCount ?: 0) - 1)
                    }
                }
            }
        }
    }

    
    public fun forceUpdate() {
        list?.recycledViewPool?.clear();
        adapter?.notifyDataSetChanged();
    }
    
    
    private fun onChatDataUpdated(notification: Notification<JsonObject>) {
        if (notification.isOnNext) {
            val data = notification.value.data
            val metadata = notification.value.metadata
            var needScrollDown = metadata?.reload == true && metadata.size ?: 0 > 0
            if (metadata?.forced == true && metadata.size ?: 0 > 0) {
                val lastItemVisible = (list?.layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition()
                if (lastItemVisible == (adapter?.itemCount ?: 0) - 2) {
                    needScrollDown = true
                }
            }
            if (needScrollDown) {
                list?.layoutManager?.scrollToPosition((adapter?.itemCount ?: 0) - 1)
            }

            if (lastRead == null) {
                lastRead = data?.discussionPart?.lastRead ?: -1L
            }


            val basicPart = data?.basic
            val votingPart = data?.voting

            if (basicPart != null) {

                when (TeambrellaUris.sUriMatcher.match(dataHost.chatUri)) {
                    TeambrellaUris.CLAIMS_CHAT -> {
                        iconView?.setImage(imageLoader.getImageUrl(basicPart.smallPhoto), R.dimen.rounded_corners_4dp, R.drawable.picture_background_round_4dp)
                        titleView?.text = basicPart.model

                        subtitleView?.text = Html.fromHtml(getString(R.string.claim_amount_format_string, decimalFormat.format(Math.round(basicPart.claimAmount
                                ?: 0f)), AmountCurrencyUtil.getLocalizedCurrency(context, data.teamPart?.currency
                                ?: "")))


                        if (votingPart == null) {
                            val votingCrypto = basicPart.votingResCrypto
                            val paymentCrypto: Double? = basicPart.paymentResCrypto

                            if (votingCrypto != null && paymentCrypto != null && votingCrypto != 0.0) {
                                if ((paymentCrypto / votingCrypto).toFloat() < 0.995) {
                                    voteTitleView?.setText(R.string.being_paid_title)
                                    setClaimVoteValue((paymentCrypto / votingCrypto).toFloat())
                                } else {
                                    voteTitleView?.setText(R.string.paid_title)
                                    setClaimVoteValue(1f)
                                }
                            } else {
                                voteTitleView?.setText(R.string.team_vote)
                                setClaimVoteValue(basicPart.reimbursement ?: -1f)
                            }

                        }
                    }
                    TeambrellaUris.TEAMMATE_CHAT -> {
                        iconView?.setAvatar(imageLoader.getImageUrl(basicPart.avatar))
                        userName = basicPart.name
                        titleView?.text = userName
                        subtitleView?.text = getString(R.string.object_format_string, basicPart.model, basicPart.year)
                        subtitleView?.setAllCaps(true)

                        if (votingPart == null) {
                            voteTitleView?.setText(R.string.risk)
                            setTeammateVoteValue(basicPart.risk ?: 0f)
                        }
                    }
                }
            }

            if (votingPart != null) {
                when (TeambrellaUris.sUriMatcher.match(dataHost.chatUri)) {
                    TeambrellaUris.CLAIMS_CHAT -> setClaimVoteValue(votingPart.myVote
                            ?: votingPart.ratioVoted?.takeIf { !votingPart.canVote } ?: -1f)
                    TeambrellaUris.TEAMMATE_CHAT -> {
                        setTeammateVoteValue(votingPart.myVote
                                ?: votingPart.ratioVoted.takeIf { !votingPart.canVote } ?: -1f)
                    }
                }

                val proxyName = votingPart.proxyName

                if (votingPart.canVote) {
                    voteTitleView?.setText(if (proxyName != null) R.string.proxy_vote_title else R.string.your_vote)
                    voteAction?.visibility = View.VISIBLE
                } else {
                    voteTitleView?.setText(if (proxyName != null) R.string.proxy_vote_title else R.string.team_vote)
                    voteAction?.visibility = View.GONE
                }

            } else {
                voteAction?.visibility = View.GONE
            }
        }
    
        scrollToLastRead()
    }

    
    private fun scrollToLastRead(): Boolean {
        if (lastRead != null && lastRead!! >= 0L) {
            val pager = dataHost.getPager(tags[0])
        
            var moveTo = pager.loadedData.size() - 1
            for (i in 0 until pager.loadedData.size()) {
                val item = pager.loadedData.get(i).asJsonObject
                val created = item.created ?: -1
                if (created >= lastRead!!) {
                    moveTo = i
                    break
                }
            }
            layoutManager.scrollToPositionWithOffset(moveTo, 0)
            lastRead = -1L
            return true
        }
        return false
    }
    
    private fun onVoteDataUpdated(notification: Notification<JsonObject>) {
        setClaimVoteValue(notification.value?.data?.voting?.myVote ?: -1f)
    }


    private fun onTeammateClickListener(v: View) {
        when (v.id) {
            R.id.voting_panel, R.id.vote ->
                startActivityForResult(getTeammateIntent(context!!, dataHost.teamId, dataHost.userId, userName, dataHost.imageUri), 10)
        }
    }

    private fun onClaimClickListener(v: View) {
        when (v.id) {
            R.id.voting_panel -> startActivityForResult(
                    ClaimActivity.getLaunchIntent(v.context, dataHost.claimId, dataHost.objectName, dataHost.teamId), 10
            )
            R.id.vote -> votingContainerBehaviour.show(votingContainerView)
            R.id.hide -> votingContainerBehaviour.hide(votingContainerView)
        }
    }

    private fun setClaimVoteValue(value: Float) {
        if (value >= 0) {
            voteValueView?.text = Html.fromHtml(getString(R.string.vote_in_percent_format_string, (value * 100).toInt()))
        } else {
            voteValueView?.setText(R.string.no_teammate_vote_value)
        }
    }

    private fun setTeammateVoteValue(value: Float) {
        if (value >= 0) {
            voteValueView?.text = String.format(Locale.US, "%.2f", value)
        } else {
            voteValueView?.setText(R.string.no_teammate_vote_value)
        }
    }


    private val ViewGroup.LayoutParams?.asCoordinatorParams: CoordinatorLayout.LayoutParams?
        get() = this as CoordinatorLayout.LayoutParams


}

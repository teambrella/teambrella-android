@file:JvmName("KTeammate")

package com.teambrella.android.ui.teammate

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.DataModelObject
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.ui.base.ADataFragment
import com.teambrella.android.ui.widget.PercentageWidget
import io.reactivex.Notification

fun getFragmentInstance(tags: Array<String>): KTeammateVotingStatsFragment {
    val fragment = KTeammateVotingStatsFragment()
    val bundle = Bundle()
    bundle.putStringArray(ADataFragment.EXTRA_DATA_FRAGMENT_TAG, tags)
    fragment.arguments = bundle
    return fragment
}

/**
 * Voting Stats Fragment
 */
class KTeammateVotingStatsFragment : ADataFragment<ITeammateActivity>() {

    private val weight: TextView?
        get() = view?.findViewById(R.id.weight)

    private val proxyRank: TextView?
        get() = view?.findViewById(R.id.proxy_rank)

    private val setProxy: TextView?
        get() = view?.findViewById(R.id.add_to_proxies)

    private val decisionView: PercentageWidget?
        get() = view?.findViewById(R.id.decision_stats)

    private val discussionView: PercentageWidget?
        get() = view?.findViewById(R.id.discussion_stats)

    private val votingView: PercentageWidget?
        get() = view?.findViewById(R.id.voting_stats)

    private var voting: Float = -1f
    private var decision: Float = -1f
    private var discussion: Float = -1f


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_teammate_voting_stats, container, false)
    }

    /**
     *
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.findViewById<View>(R.id.add_to_proxies)?.setOnClickListener({ v -> mDataHost.setAsProxy(!(v?.tag as Boolean)) })
        view?.findViewById<View>(R.id.add_to_proxies)?.tag = true
    }

    /**
     *
     */
    override fun onDataUpdated(notification: Notification<JsonObject>?) {
        if (notification != null) {
            if (notification.isOnNext) {
                this.setProxy?.visibility = if (mDataHost.isItMe) View.GONE else View.VISIBLE
                val value = DataModelObject(notification.value)
                val uri = Uri.parse(value.status?.uri)
                val matchId = TeambrellaUris.sUriMatcher.match(uri)
                when (matchId) {
                    TeambrellaUris.SET_MY_PROXY -> {
                        val add = java.lang.Boolean.parseBoolean(uri.getQueryParameter(TeambrellaUris.KEY_ADD))
                        this.setProxy?.text = getString(if (add) R.string.remove_from_my_proxies else R.string.add_to_my_proxies)
                        this.setProxy?.tag = add
                    }
                    else -> {

                        val stats = value.data?.stats
                        this.voting = (stats?.votingFreq) ?: this.voting
                        this.decision = (stats?.decisionFreq) ?: this.decision
                        this.discussion = (stats?.discussionFreq) ?: this.discussion
                        this.weight?.text = getString(R.string.risk_format_string, stats?.weight)
                        this.proxyRank?.text = getString(R.string.risk_format_string, stats?.proxyRank)


                        val basic = value.data?.basic
                        val isMyProxy = (basic?.isMyProxy) ?: false
                        this.setProxy?.text = getString(if (isMyProxy) R.string.remove_from_my_proxies else R.string.add_to_my_proxies)
                        this.setProxy?.tag = isMyProxy

                    }
                }

                this.votingView?.setPercentage(this.voting)
                this.votingView?.setDescription(getString(getVotingStatsString(this.voting)))
                this.decisionView?.setPercentage(this.decision)
                this.decisionView?.setDescription(getString(getDecisionStatsString(this.decision)))
                this.discussionView?.setPercentage(this.discussion)
                this.discussionView?.setDescription(getString(getDiscussionStatsString(this.discussion)))

            }
        }
    }


    private fun getVotingStatsString(value: Float): Int {
        return when {
            value >= 0.95f -> R.string.voting_always
            value >= 0.6f -> R.string.voting_regularly
            value >= 0.3f -> R.string.voting_often
            value >= 0.15f -> R.string.voting_frequently
            value >= 0.05f -> R.string.voting_rarely
            else -> R.string.voting_never
        }
    }

    private fun getDecisionStatsString(value: Float): Int {
        return when {
            value >= 0.7f -> R.string.decision_harsh
            value >= 0.55f -> R.string.decision_severe
            value >= 0.45f -> R.string.decision_moderate
            value >= 0.3f -> R.string.decision_mild
            else -> R.string.decision_generous
        }
    }


    private fun getDiscussionStatsString(value: Float): Int {
        return when {
            value >= 0.5f -> R.string.discussion_chatty
            value >= 0.25f -> R.string.discussion_sociable
            value >= 0.1f -> R.string.discussion_moderate
            value >= 0.03f -> R.string.discussion_reserved
            else -> R.string.discussion_quite
        }
    }
}
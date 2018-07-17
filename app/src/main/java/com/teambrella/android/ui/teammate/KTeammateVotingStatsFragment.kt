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
import com.teambrella.android.api.*
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.ui.base.ADataFragment
import com.teambrella.android.ui.base.TeambrellaBroadcastManager
import com.teambrella.android.ui.base.createDataFragment
import com.teambrella.android.ui.widget.PercentageWidget
import io.reactivex.Notification
import java.util.*

fun getFragmentInstance(tags: Array<String>) = createDataFragment(tags, KTeammateVotingStatsFragment::class.java)

/**
 * Voting Stats Fragment
 */
class KTeammateVotingStatsFragment : ADataFragment<ITeammateActivity>() {

    private val weight: TextView? by ViewHolder(R.id.weight)
    private val proxyRank: TextView? by ViewHolder(R.id.proxy_rank)
    private val setProxy: TextView? by ViewHolder(R.id.add_to_proxies)
    private val decisionView: PercentageWidget? by ViewHolder(R.id.decision_stats)
    private val discussionView: PercentageWidget? by ViewHolder(R.id.discussion_stats)
    private val votingView: PercentageWidget? by ViewHolder(R.id.voting_stats)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_teammate_voting_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setProxy?.setOnClickListener { v -> dataHost.setAsProxy(!(v?.tag as Boolean)) }
        setProxy?.tag = true
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        if (notification.isOnNext) {
            this.setProxy?.visibility = if (dataHost.isItMe) View.GONE else View.VISIBLE
            val value = notification.value
            val uri = Uri.parse(value.status?.uri)
            val matchId = TeambrellaUris.sUriMatcher.match(uri)
            when (matchId) {
                TeambrellaUris.SET_MY_PROXY -> {
                    val add = java.lang.Boolean.parseBoolean(uri.getQueryParameter(TeambrellaUris.KEY_ADD))
                    this.setProxy?.text = getString(if (add) R.string.remove_from_my_proxies else R.string.add_to_my_proxies)
                    this.setProxy?.tag = add
                    context?.let {
                        TeambrellaBroadcastManager(it).notifyProxyListChanged()
                    }
                }
                else -> {
                    val stats = value.data?.stats
                    stats?.let {

                        val votingFreq = stats.votingFreq
                        votingFreq?.let {
                            this.votingView?.setPercentage(votingFreq)
                            this.votingView?.setDescription(getString(getVotingStatsString(votingFreq)))
                        }

                        val decisionFreq = stats.decisionFreq
                        decisionFreq?.let {
                            this.decisionView?.setPercentage(decisionFreq)
                            this.decisionView?.setDescription(getString(getDecisionStatsString(decisionFreq)))
                        }

                        val discussionFreq = stats.discussionFreq
                        discussionFreq?.let {
                            this.discussionView?.setPercentage(discussionFreq)
                            this.discussionView?.setDescription(getString(getDiscussionStatsString(discussionFreq)))
                        }

                        val weightValue = stats.weight
                        weightValue?.let {
                            this.weight?.text = String.format(Locale.US, if (it >= 0.1) "%.1f" else "%.2f", it)
                        }

                        val proxyRankValue = stats.proxyRank
                        proxyRankValue?.let {
                            val rank = when {
                                it < 0.005 -> 0f
                                it >= 0.005 && it < 0.01 -> 0.1f
                                else -> it
                            }
                            this.proxyRank?.text = String.format(Locale.US, if (rank >= 0.1 || rank == 0f) "%.1f" else "%.2f", rank)
                        }
                    }
                    val basic = value.data?.basic
                    basic?.isMyProxy?.let {
                        val isMyProxy = (basic.isMyProxy) ?: false
                        this.setProxy?.text = getString(if (isMyProxy) R.string.remove_from_my_proxies else R.string.add_to_my_proxies)
                        this.setProxy?.tag = isMyProxy
                    }
                }
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
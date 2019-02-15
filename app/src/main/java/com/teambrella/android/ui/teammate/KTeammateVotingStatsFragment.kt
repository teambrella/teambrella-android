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
import com.teambrella.android.ui.votes.AllVotesActivity
import io.reactivex.Notification
import java.util.*
import kotlin.math.roundToInt

fun getFragmentInstance(tags: Array<String>) = createDataFragment(tags, KTeammateVotingStatsFragment::class.java)

/**
 * Voting Stats Fragment
 */
class KTeammateVotingStatsFragment : ADataFragment<ITeammateActivity>() {

    private val risksVotes: TextView? by ViewHolder(R.id.risks_votes)
    private val claimsVotes: TextView? by ViewHolder(R.id.claims_votes)
    private val setProxy: TextView? by ViewHolder(R.id.add_to_proxies)
    private val header: TextView? by ViewHolder(R.id.header)

    private var risksVoteAsTeamOrBetter: Float = 0f
    private var risksVoteAsTeam: Float = 0f
    private var claimsVoteAsTeamOrBetter: Float = 0f
    private var claimsVoteAsTeam: Float = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_teammate_voting_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setProxy?.setOnClickListener { v -> dataHost.setAsProxy(!(v?.tag as Boolean)) }
        setProxy?.tag = true

        view.findViewById<View>(R.id.stats_risks)?.setOnClickListener {
            AllVotesActivity.startTeammateRisksVotes(it.context, dataHost.teamId, dataHost.teammateId, dataHost.teammateName, dataHost.isItMe, risksVoteAsTeam, risksVoteAsTeamOrBetter)
        }
        view.findViewById<View>(R.id.stats_claims)?.setOnClickListener {
            AllVotesActivity.startTeammateClaimsVotes(it.context, dataHost.teamId, dataHost.teammateId,dataHost.teammateName, dataHost.isItMe, claimsVoteAsTeam, claimsVoteAsTeamOrBetter)
        }

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

                        stats.risksVoteAsTeamOrBetter?.let {
                            risksVoteAsTeamOrBetter = it
                            this.risksVotes?.text = if (it < 0) "-" else String.format(Locale.US, "%d%%", (it*100).roundToInt())
                        }

                        stats.risksVoteAsTeam?.let {
                            risksVoteAsTeam = it
                        }

                        stats.claimsVoteAsTeamOrBetter?.let {
                            claimsVoteAsTeamOrBetter = it
                            this.claimsVotes?.text = if (it < 0) "-" else String.format(Locale.US, "%d%%", (it*100).roundToInt())
                        }

                        stats.claimsVoteAsTeam?.let {
                            claimsVoteAsTeam = it
                        }
                    }
                    val basic = value.data?.basic
                    basic?.isMyProxy?.let {
                        val isMyProxy = (basic.isMyProxy) ?: false
                        this.setProxy?.text = getString(if (isMyProxy) R.string.remove_from_my_proxies else R.string.add_to_my_proxies)
                        this.setProxy?.tag = isMyProxy
                    }
                    val shortName = dataHost.teammateName?.substringBefore(' ')
                    this.header?.text = if (dataHost.isItMe) context?.getString(R.string.how_i_vote) else context?.getString(R.string.how_x_votes, shortName)
                }
            }
        }
    }


}
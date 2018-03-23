@file:Suppress("ProtectedInFinal")

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
import com.teambrella.android.image.glide.GlideApp
import com.teambrella.android.ui.base.AKDataFragment
import com.teambrella.android.ui.votes.AllVotesActivity
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets
import com.teambrella.android.util.TeambrellaDateUtils
import io.reactivex.Notification
import java.util.*

/**
 * Base Teammate Voting Result Fragment
 */

class TeammateVotingResultFragment : AKDataFragment<ITeammateActivity>() {

    protected val teamVoteRisk: TextView? by ViewHolder(R.id.team_vote_risk)
    protected val myVoteRisk: TextView? by ViewHolder(R.id.your_vote_risk)
    protected val avgDifferenceTeamVote: TextView? by ViewHolder(R.id.team_vote_avg_difference)
    protected val avgDifferenceMyVote: TextView? by ViewHolder(R.id.your_vote_avg_difference)
    protected val proxyName: TextView? by ViewHolder(R.id.proxy_name)
    protected val proxyAvatar: ImageView? by ViewHolder(R.id.proxy_avatar)
    protected val allVotes: View? by ViewHolder(R.id.all_votes)
    protected val whenDate: TextView? by ViewHolder(R.id.`when`)
    protected val yourVoteTitle: TextView? by ViewHolder(R.id.your_vote_title)
    protected val avatarWidget: TeambrellaAvatarsWidgets? by ViewHolder(R.id.team_avatars)

    protected var avgRiskValue: Double? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_teammate_voting, container, false)
        view?.findViewById<View>(R.id.voting_panel)?.visibility = View.GONE
        view?.findViewById<View>(R.id.reset_vote_btn)?.visibility = View.GONE
        view?.findViewById<View>(R.id.clock)?.visibility = View.GONE
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allVotes?.setOnClickListener({
            AllVotesActivity.startTeammateAllVotes(context, mDataHost.teamId, mDataHost.teammateId)
        })
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        if (notification.isOnNext) {
            val response = notification.value
            val data = response?.data
            val voted = data?.voted

            voted?.let {

                this.avgRiskValue = it.avgRisk
                setTeamVote(it.riskVoted)
                setMyVote(it.myVote)

                val proxyName = it.proxyName
                val proxyAvatar = it.proxyAvatar

                if (proxyName != null && proxyAvatar != null) {
                    this.proxyName?.let {
                        it.text = proxyName
                        it.visibility = View.VISIBLE
                    }
                    this.proxyAvatar?.let {
                        GlideApp.with(this).load(imageLoader.getImageUrl(proxyAvatar)).into(it)
                        it.visibility = View.VISIBLE
                    }

                    this.yourVoteTitle?.text = getString(R.string.proxy_vote_title)

                } else {
                    this.proxyName?.visibility = View.INVISIBLE
                    this.proxyAvatar?.visibility = View.INVISIBLE
                    this.yourVoteTitle?.text = getString(R.string.your_vote)
                }

                this.whenDate?.text = getString(R.string.ended_ago, TeambrellaDateUtils.getRelativeTimeLocalized(context
                        , Math.abs(it.remainedMinutes ?: 0)))


                val otherCount = voted.otherCount ?: 0
                it.otherAvatars?.map { it.asString }.let {
                    this.avatarWidget?.setAvatars(imageLoader, it, otherCount)
                }

            }


        }
    }

    private fun getAVGDifference(vote: Double, average: Double): String {
        val percent = Math.round((100 * (vote - average)) / average)
        return when {
            percent > 0 -> getString(R.string.vote_avg_difference_bigger_format_string, percent)
            percent < 0 -> getString(R.string.vote_avg_difference_smaller_format_string, percent)
            else -> getString(R.string.vote_avg_difference_same)
        }
    }

    private fun setMyVote(vote: Double?) {
        if (vote != null) {
            if (vote > 0) {
                this.myVoteRisk?.text = String.format(Locale.US, "%.2f", vote)
                this.avgDifferenceMyVote?.visibility = View.VISIBLE
                this.avgDifferenceMyVote?.text = getAVGDifference(vote, avgRiskValue
                        ?: vote)
            } else {
                this.myVoteRisk?.text = getString(R.string.no_teammate_vote_value)
                this.avgDifferenceMyVote?.visibility = View.INVISIBLE
            }
        } else {
            setMyVote(-1.0)
        }
    }

    private fun setTeamVote(vote: Double?) {
        if (vote != null) {
            if (vote > 0) {
                this.teamVoteRisk?.text = String.format(Locale.US, "%.2f", vote)
                this.avgDifferenceTeamVote?.visibility = View.VISIBLE
                this.avgDifferenceTeamVote?.text = getAVGDifference(vote, avgRiskValue
                        ?: vote)
            } else {
                this.teamVoteRisk?.text = getString(R.string.no_teammate_vote_value)
                this.avgDifferenceTeamVote?.visibility = View.INVISIBLE
            }
        } else {
            setTeamVote(-1.0)
        }
    }
}
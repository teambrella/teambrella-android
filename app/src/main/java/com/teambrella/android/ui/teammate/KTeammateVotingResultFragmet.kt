@file:Suppress("ProtectedInFinal")

package com.teambrella.android.ui.teammate

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.image.glide.GlideApp
import com.teambrella.android.ui.base.AKDataFragment
import com.teambrella.android.ui.votes.AllVotesActivity
import com.teambrella.android.ui.widget.CountDownClock
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets
import com.teambrella.android.ui.widget.VoterBar
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
    protected val clock: CountDownClock? by ViewHolder(R.id.clock)
    protected val yourVoteTitle: TextView? by ViewHolder(R.id.your_vote_title)
    protected val avatarWidget: TeambrellaAvatarsWidgets? by ViewHolder(R.id.team_avatars)
    protected val voterBar: VoterBar? by ViewHolder(R.id.voter_bar)
    protected val resetVote: View? by ViewHolder(R.id.reset_vote_btn)

    protected var avgRiskValue: Double? = null


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allVotes?.setOnClickListener({
            AllVotesActivity.startTeammateAllVotes(context, mDataHost.teamId, mDataHost.teammateId)
        })
        voterBar?.setVoterBarListener(voterBarListener)
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        if (notification.isOnNext) {
            val response = notification.value
            val data = response?.data
            val voting = data?.voting
            val riskScale = data?.riskScale

            riskScale?.let {
                this.avgRiskValue = riskScale.avgRisk
            }

            voting?.let {
                it.riskVoted?.let {
                    if (it > 0) {
                        this.teamVoteRisk?.text = String.format(Locale.US, "%.2f", it)
                        this.avgDifferenceTeamVote?.visibility = View.VISIBLE
                        this.avgDifferenceTeamVote?.text = getAVGDifference(it, avgRiskValue
                                ?: it)
                    } else {
                        this.teamVoteRisk?.text = getString(R.string.no_teammate_vote_value)
                        this.avgDifferenceTeamVote?.visibility = View.INVISIBLE
                    }
                }
                it.myVote?.let {
                    setMyVote(it)
                }

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

                    this.yourVoteTitle?.text = getString(R.string.proxy_vote)

                } else {
                    this.proxyName?.visibility = View.INVISIBLE
                    this.proxyAvatar?.visibility = View.INVISIBLE
                    this.yourVoteTitle?.text = getString(R.string.your_vote)
                }

                this.whenDate?.text = getString(R.string.ends_in, TeambrellaDateUtils.getRelativeTimeLocalized(context
                        , it.remainedMinutes ?: 0))

                val otherCount = voting.otherCount ?: 0
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

    private fun setVoting(isVoting: Boolean) {
        this.myVoteRisk?.alpha = if (isVoting) 0.3f else 1f
        this.resetVote?.alpha = if (isVoting) 0.3f else 1f
        this.resetVote?.isEnabled = !isVoting
    }

    private fun setMyVote(vote: Double) {
        if (vote > 0) {
            this.myVoteRisk?.text = String.format(Locale.US, "%.2f", vote)
            this.avgDifferenceMyVote?.visibility = View.VISIBLE
            this.avgDifferenceMyVote?.text = getAVGDifference(vote, avgRiskValue
                    ?: vote)
        } else {
            this.myVoteRisk?.text = getString(R.string.no_teammate_vote_value)
            this.avgDifferenceMyVote?.visibility = View.INVISIBLE
        }
    }


    private val voterBarListener = object : VoterBar.VoterBarListener {

        override fun onVoteChanged(vote: Float, fromUser: Boolean) {
            val value = Math.pow(25.0, vote.toDouble()) / 5
            setVoting(true)
            if (fromUser) {
                setMyVote(value)
            }
        }

        override fun onVoterBarReleased(vote: Float, fromUser: Boolean) {
            if (fromUser) {
                mDataHost.postVote(Math.pow(25.0, vote.toDouble()) / 5)
            }
            parentFragment?.let {
                if (it is VoterBar.VoterBarListener) {
                    it.onVoterBarReleased(vote, fromUser)
                }
            }
        }
    }
}
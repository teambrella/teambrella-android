@file:Suppress("ProtectedInFinal", "DEPRECATION")

package com.teambrella.android.ui.claim

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
import com.teambrella.android.image.glide.GlideApp
import com.teambrella.android.ui.base.AKDataFragment
import com.teambrella.android.ui.votes.AllVotesActivity
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets
import com.teambrella.android.util.AmountCurrencyUtil
import com.teambrella.android.util.TeambrellaDateUtils
import io.reactivex.Notification


class ClaimVotingResultFragment : AKDataFragment<IClaimActivity>() {

    protected val teamVotePercents: TextView? by ViewHolder(R.id.team_vote_percent)
    protected val yourVotePercents: TextView? by ViewHolder(R.id.your_vote_percent)
    protected val teamVoteCurrency: TextView? by ViewHolder(R.id.team_vote_currency)
    protected val yourVoteCurrency: TextView? by ViewHolder(R.id.your_vote_currency)
    protected val proxyName: TextView? by ViewHolder(R.id.proxy_name)
    protected val proxyAvatar: ImageView? by ViewHolder(R.id.proxy_avatar)
    protected val avatarWidget: TeambrellaAvatarsWidgets? by ViewHolder(R.id.team_avatars)
    protected val allVotes: TextView? by ViewHolder(R.id.all_votes)
    protected val whenDate: TextView? by ViewHolder(R.id.`when`)
    protected val clock: TextView? by ViewHolder(R.id.clock)
    protected val yourVoteTitle: TextView? by ViewHolder(R.id.your_vote_title)


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_claim_voting, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.findViewById<View>(R.id.voting_panel)?.visibility = View.GONE
        allVotes?.setOnClickListener({
            AllVotesActivity.startClaimAllVotes(context, mDataHost.teamId, mDataHost.claimId)
        })
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        if (notification.isOnNext) {
            val data = notification.value?.data
            val basic = data?.basic
            val team = data?.team
            val voted = data?.voted
            val claimAmount = basic?.claimAmount
            val currency = team?.currency

            voted?.let {
                val teamVote = it.ratioVoted
                val myVote = it.myVote
                val proxyName = it.proxyName
                val proxyAvatar = it.proxyAvatar

                setTeamVote(teamVote, currency ?: "", claimAmount)
                setMyVote(myVote?.toFloat(), currency ?: "", claimAmount)

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

                this.whenDate?.text = getString(R.string.ended_ago, TeambrellaDateUtils.getRelativeTimeLocalized(context
                        , Math.abs(it.remainedMinutes ?: 0)))


                val otherCount = voted.otherCount ?: 0
                it.otherAvatars?.map { it.asString }.let {
                    this.avatarWidget?.setAvatars(imageLoader, it, otherCount)
                }

            }

        }
    }


    private fun setTeamVote(vote: Float?, currency: String, claimAmount: Float?) {
        if (vote != null) {
            if (claimAmount != null) {
                if (vote >= 0) {
                    this.teamVotePercents?.text = Html.fromHtml(getString(R.string.vote_in_percent_format_string, (vote * 1000).toInt()))
                    AmountCurrencyUtil.setAmount(this.teamVoteCurrency, (claimAmount * vote), currency)
                    this.teamVoteCurrency?.visibility = View.VISIBLE
                } else {
                    this.teamVotePercents?.text = getString(R.string.no_teammate_vote_value)
                    this.teamVoteCurrency?.visibility = View.INVISIBLE
                }
            } else {
                setTeamVote(vote, currency, 0f)
            }
        } else {
            setTeamVote(-1f, currency, claimAmount)
        }
    }


    private fun setMyVote(vote: Float?, currency: String, claimAmount: Float?) {
        if (vote != null) {
            if (claimAmount != null) {
                if (vote >= 0) {
                    this.yourVotePercents?.text = Html.fromHtml(getString(R.string.vote_in_percent_format_string, (vote * 1000).toInt()))
                    AmountCurrencyUtil.setAmount(this.yourVoteCurrency, (claimAmount * vote), currency)
                    this.yourVoteCurrency?.visibility = View.VISIBLE
                } else {
                    this.yourVotePercents?.text = getString(R.string.no_teammate_vote_value)
                    this.yourVoteCurrency?.visibility = View.INVISIBLE
                }
            } else {
                setTeamVote(vote, currency, 0f)
            }
        } else {
            setTeamVote(-1f, currency, claimAmount)
        }
    }


}

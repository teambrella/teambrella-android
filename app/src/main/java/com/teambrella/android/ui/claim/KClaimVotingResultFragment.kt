@file:Suppress("ProtectedInFinal", "DEPRECATION")

package com.teambrella.android.ui.claim

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.image.glide.GlideApp
import com.teambrella.android.ui.base.ADataFragment
import com.teambrella.android.ui.base.AKDataFragment
import com.teambrella.android.ui.votes.AllVotesActivity
import com.teambrella.android.ui.widget.CountDownClock
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets
import com.teambrella.android.util.AmountCurrencyUtil
import com.teambrella.android.util.TeambrellaDateUtils
import io.reactivex.Notification
import kotlin.math.roundToInt


const val MODE_CLAIM = 1
const val MODE_CHAT = 2
const val EXTRA_MODE = "mode"

fun getInstance(tags: Array<String>, mode: Int): ClaimVotingResultFragment {
    val fragment = ClaimVotingResultFragment()
    val args = Bundle()
    args.putStringArray(ADataFragment.EXTRA_DATA_FRAGMENT_TAG, tags)
    args.putInt(EXTRA_MODE, mode)
    fragment.arguments = args
    return fragment
}

class ClaimVotingResultFragment : AKDataFragment<IClaimActivity>() {


    protected val teamVotePercents: TextView? by ViewHolder(R.id.team_vote_percent)
    protected val yourVotePercents: TextView? by ViewHolder(R.id.your_vote_percent)
    protected val teamVoteCurrency: TextView? by ViewHolder(R.id.team_vote_currency)
    protected val yourVoteCurrency: TextView? by ViewHolder(R.id.your_vote_currency)
    protected val proxyName: TextView? by ViewHolder(R.id.proxy_name)
    protected val proxyAvatar: ImageView? by ViewHolder(R.id.proxy_avatar)
    protected val avatarWidget: TeambrellaAvatarsWidgets? by ViewHolder(R.id.team_avatars)
    protected val allVotes: View? by ViewHolder(R.id.all_votes)
    protected val whenDate: TextView? by ViewHolder(R.id.`when`)
    protected val clock: CountDownClock? by ViewHolder(R.id.clock)
    protected val yourVoteTitle: TextView? by ViewHolder(R.id.your_vote_title)
    protected val resetVoteButton: TextView? by ViewHolder(R.id.reset_vote_btn)
    protected val votingControl: SeekBar? by ViewHolder(R.id.voting_control)
    protected val votingProgress: ProgressBar? by ViewHolder(R.id.voting_progress)
    protected val votingPanel: View? by ViewHolder(R.id.voting_panel)

    protected var currency: String? = null
    protected var claimAmount: Float? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_claim_voting, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allVotes?.setOnClickListener({
            AllVotesActivity.startClaimAllVotes(context, mDataHost.teamId, mDataHost.claimId)
        })

        resetVoteButton?.setOnClickListener({
            yourVotePercents?.alpha = 0.3f
            yourVoteCurrency?.alpha = 0.3f
            resetVoteButton?.alpha = 0.3f
            mDataHost.postVote(-1)
        })

        component.inject(avatarWidget)

        view?.findViewById<View>(R.id.header_line)?.visibility =
                if ((arguments?.getInt(EXTRA_MODE, MODE_CLAIM)
                                ?: MODE_CLAIM) == MODE_CLAIM) View.VISIBLE else View.GONE

        votingControl?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    yourVotePercents?.text = Html.fromHtml(getString(R.string.vote_in_percent_format_string, progress))
                    claimAmount?.let {
                        AmountCurrencyUtil.setAmount(yourVoteCurrency, ((it * progress) / 100).roundToInt(), currency)
                    }
                    yourVoteCurrency?.visibility = View.VISIBLE
                    yourVotePercents?.alpha = 0.3f
                    yourVoteCurrency?.alpha = 0.3f
                    resetVoteButton?.alpha = 0.3f
                }
                votingProgress?.progress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mDataHost.postVote(seekBar?.progress ?: -1)
            }
        })
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        if (notification.isOnNext) {
            val data = notification.value?.data
            val basic = data?.basic
            val team = data?.team
            val voting = data?.voting

            claimAmount = basic?.claimAmount ?: claimAmount
            currency = team?.currency ?: currency

            voting?.let {
                setVotes(it, true)
            }

            data?.voted?.let {
                setVotes(it, false)
            }

        }
    }


    private fun setVotes(voteData: JsonObject, voting: Boolean) {
        val teamVote = voteData.ratioVoted
        val myVote = voteData.myVote
        val proxyName = voteData.proxyName
        val proxyAvatar = voteData.proxyAvatar

        setTeamVote(teamVote, currency ?: "", claimAmount)
        setMyVote(myVote?.toFloat(), currency ?: "", claimAmount)

        this.votingControl?.progress = Math.round((myVote ?: 0.0) * 100).toInt()

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
            this.resetVoteButton?.visibility = View.GONE

        } else {
            this.proxyName?.visibility = View.INVISIBLE
            this.proxyAvatar?.visibility = View.INVISIBLE
            this.yourVoteTitle?.text = getString(R.string.your_vote)
            this.resetVoteButton?.visibility = if (myVote ?: -1.0 >= 0) View.VISIBLE else View.GONE
        }

        val remainedMinutes = voteData.remainedMinutes ?: 0
        this.whenDate?.text = getString(if (remainedMinutes < 0) R.string.ended_ago else R.string.ends_in
                , TeambrellaDateUtils.getRelativeTimeLocalized(context
                , Math.abs(remainedMinutes)))

        if (remainedMinutes >= 0) {
            clock?.setRemainedMinutes(remainedMinutes)
        }

        val otherCount = voteData.otherCount ?: 0
        voteData.otherAvatars?.map { it.asString }.let {
            this.avatarWidget?.setAvatars(imageLoader, it, otherCount)
        }

        votingPanel?.visibility = if (voting) View.VISIBLE else View.GONE
    }


    private fun setTeamVote(vote: Float?, currency: String, claimAmount: Float?) {
        if (vote != null) {
            if (claimAmount != null) {
                if (vote >= 0) {
                    this.teamVotePercents?.text = Html.fromHtml(getString(R.string.vote_in_percent_format_string, (vote * 100).toInt()))
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
                    this.yourVotePercents?.text = Html.fromHtml(getString(R.string.vote_in_percent_format_string, (vote * 100).toInt()))
                    AmountCurrencyUtil.setAmount(this.yourVoteCurrency, (claimAmount * vote), currency)
                    this.yourVoteCurrency?.visibility = View.VISIBLE
                } else {
                    this.yourVotePercents?.text = getString(R.string.no_teammate_vote_value)
                    this.yourVoteCurrency?.visibility = View.INVISIBLE
                }
                yourVotePercents?.alpha = 1f
                yourVoteCurrency?.alpha = 1f
                resetVoteButton?.alpha = 1f
            } else {
                setMyVote(vote, currency, 0f)
            }
        } else {
            setMyVote(-1f, currency, claimAmount)
        }
    }


}

package com.teambrella.android.ui.votes

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.gson.JsonArray
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.api.model.json.JsonWrapper
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter
import com.teambrella.android.ui.teammate.getTeammateIntent
import com.teambrella.android.ui.util.setAvatar
import com.teambrella.android.ui.util.setTeamVoteDifference
import java.util.*
import kotlin.math.roundToInt

/**
 * All Votes Adapter
 */
class AllVotesAdapter(pager: IDataPager<JsonArray>, private val mAllVotesActivity: IAllVoteActivity, private val mMode: Int) : TeambrellaDataPagerAdapter(pager) {

    private val mTeamId: Int = mAllVotesActivity.teamId

    companion object {

        const val VIEW_TYPE_ME = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 1
        const val VIEW_TYPE_HEADER = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 2
        const val VIEW_TYPE_TEAMMATE = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 3
        const val VIEW_TYPE_STATS_TOP = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 4
        const val VIEW_TYPE_RISK_VOTE = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 5
        const val VIEW_TYPE_CLAIM_VOTE = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 6

        const val MODE_CLAIM = 1
        const val MODE_APPLICATION = 2
        const val MODE_TEAMMATE_RISKS = 3
        const val MODE_TEAMMATE_CLAIMS = 4
    }


    private var mMyVote: JsonWrapper? = null


    init {
        setHasStableIds(true)
    }

    fun setMyVote(vote: JsonWrapper) {
        if (mMode != MODE_CLAIM && mMode != MODE_APPLICATION) {
            return
        }
        mMyVote = vote
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        var holder = super.onCreateViewHolder(parent, viewType)
        if (holder == null) {
            val inflater = LayoutInflater.from(parent.context)
            val shortName = mAllVotesActivity.userName?.substringBefore(' ')
            holder = when (viewType) {
                VIEW_TYPE_ME -> MyVoteViewHolder(inflater.inflate(R.layout.list_item_vote, parent, false))
                VIEW_TYPE_HEADER -> TeambrellaDataPagerAdapter.Header(parent,
                        parent.context.getString(when(mMode) {
                            MODE_TEAMMATE_RISKS -> R.string.team_vote
                            MODE_TEAMMATE_CLAIMS -> R.string.team_vote
                            else -> R.string.all_votes
                        }),
                        when(mMode) {
                            MODE_TEAMMATE_RISKS,
                            MODE_TEAMMATE_CLAIMS -> if (mAllVotesActivity.isItMe) parent.context.getString(R.string.your_vote) else parent.context.getString(R.string.x_votes, shortName)
                            else -> parent.context.getString(R.string.votes)
                        },
                        if (headersCount == 2) R.drawable.list_item_header_background_middle else R.drawable.list_item_header_background_top)
                VIEW_TYPE_RISK_VOTE -> RisksVotesViewHolder(inflater.inflate(R.layout.list_item_teammate, parent, false))
                VIEW_TYPE_STATS_TOP -> StatsViewHolder(inflater.inflate(R.layout.list_item_stats_top, parent, false))
                VIEW_TYPE_CLAIM_VOTE -> ClaimsVotesViewHolder(inflater.inflate(R.layout.list_item_claim_being_paid, parent, false))
                VIEW_TYPE_TEAMMATE -> VoteViewHolder(inflater.inflate(R.layout.list_item_vote, parent, false))
                else -> null
            }
        }
        return holder
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is MyVoteViewHolder) {
            mMyVote?.let {
                holder.onBind(it)
            }
        } else if (holder is VoteViewHolder) {
            holder.onBind(JsonWrapper(mPager.loadedData.get(position - headersCount).asJsonObject))
        } else if (holder is RisksVotesViewHolder) {
            holder.onBind(JsonWrapper(mPager.loadedData.get(position - headersCount).asJsonObject))
        } else if (holder is ClaimsVotesViewHolder) {
            holder.onBind(JsonWrapper(mPager.loadedData.get(position - headersCount).asJsonObject))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val viewType = super.getItemViewType(position)
        val itemType = when (mMode) {
            MODE_TEAMMATE_RISKS -> VIEW_TYPE_RISK_VOTE
            MODE_TEAMMATE_CLAIMS -> VIEW_TYPE_CLAIM_VOTE
            else -> VIEW_TYPE_TEAMMATE
        }
        val topType = when (mMode) {
            MODE_TEAMMATE_RISKS -> VIEW_TYPE_STATS_TOP
            MODE_TEAMMATE_CLAIMS -> VIEW_TYPE_STATS_TOP
            else -> VIEW_TYPE_ME
        }
        if (viewType == TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR) {
            when (position) {
                0 -> return if (headersCount == 2) topType else VIEW_TYPE_HEADER
                1 -> return if (headersCount == 2) VIEW_TYPE_HEADER else itemType
                else -> return itemType
            }
        }
        return viewType
    }

    override fun getHeadersCount() = when (mMode) {
        MODE_TEAMMATE_RISKS -> 2
        MODE_TEAMMATE_CLAIMS -> 2
        else -> mMyVote?.let {
            return if (it.obj.votedByProxy != null || (it.obj.vote?:0f) <= 0) 1 else 2
        } ?: 1
    }


    private open inner class VoteViewHolder(itemView: View) : TeambrellaDataPagerAdapter.AMemberViewHolder(itemView, mTeamId) {

        private val voteView: TextView? = itemView.findViewById(R.id.vote)
        private val weightView: TextView? = itemView.findViewById(R.id.weight)

        @SuppressLint("SetTextI18n")
        override fun onBind(item: JsonWrapper) {
            super.onBind(item)
            var valueDif = (((item.obj.vote?:0f) - mAllVotesActivity.teamVote) * 100).roundToInt();
            when (mMode) {
                MODE_CLAIM -> {
                    voteView?.text = ((item.obj.vote?:0f) * 100).roundToInt().toString() + "%"
                }
                MODE_APPLICATION -> {
                    voteView?.text = itemView.context.getString(R.string.risk_vote_format_string, item.obj.vote?:0f)
                    valueDif = - valueDif;
                }
            }
            setVoteColor(voteView, valueDif)

            val weight = item.getFloat(TeambrellaModel.ATTR_DATA_WEIGHT_COMBINED)
            weightView?.text = itemView.context.getString(if (weight >= 0.1) R.string.float_format_string_1 else R.string.float_format_string_2, weight)
        }
    }

    private open inner class StatsViewHolder : RecyclerView.ViewHolder {
        private val lowNameView: TextView? = itemView.findViewById(R.id.stat_name_low)
        private val highNameView: TextView? = itemView.findViewById(R.id.stat_name_high)
        private val statLowView: TextView? = itemView.findViewById(R.id.stat_low)
        private val statMidView: TextView? = itemView.findViewById(R.id.stat_middle)
        private val statHighView: TextView? = itemView.findViewById(R.id.stat_high)

        constructor(itemView: View): super(itemView) {
            val statLow = ((1f - mAllVotesActivity.statAsTeamOrBetter) * 100).roundToInt()
            val statMid = (mAllVotesActivity.statAsTeam * 100).roundToInt()
            val statHigh = ((mAllVotesActivity.statAsTeamOrBetter - mAllVotesActivity.statAsTeam) * 100).roundToInt()
            statLowView?.text = statLow.toString() + "%"
            statMidView?.text = statMid.toString() + "%"
            statHighView?.text = statHigh.toString() + "%"

            if (mMode == MODE_TEAMMATE_RISKS){
                highNameView?.text = itemView.context.getString(R.string.lower_than_team) // lower risks are better
                lowNameView?.text = itemView.context.getString(R.string.higher_than_team)
            }
        }
    }

    private open inner class RisksVotesViewHolder(itemView: View) : TeambrellaDataPagerAdapter.AMemberViewHolder(itemView, mTeamId) {

        val icon: ImageView? = itemView.findViewById(R.id.icon)
        val title: TextView? = itemView.findViewById(R.id.teammate)
        val objName: TextView? = itemView.findViewById(R.id.`object`)
        val resultView: TextView = itemView.findViewById(R.id.result)
        val voteValueView: TextView = itemView.findViewById(R.id.vote_value)
        val voteTeamView: TextView? = itemView.findViewById(R.id.indicator)

        override fun onBind(item: JsonWrapper?) {
            item?.obj.avatar?.let {
                icon?.setAvatar(imageLoader.getImageUrl(item?.obj.avatar))
            }

            title?.text = item?.obj.name
            objName?.text = itemView.context.getString(R.string.object_format_string, item?.obj.model, item?.obj.year)

            itemView.setOnClickListener {
                startActivity(getTeammateIntent(itemView.context, mTeamId, item?.obj.userId!!,
                        item?.obj.name, item?.obj.avatar))
            }

            val titleParams = title?.layoutParams as RelativeLayout.LayoutParams
            titleParams.addRule(RelativeLayout.START_OF, voteValueView.id)

            if (item?.obj.votedByProxy != null) {
                resultView.text = itemView.context.getString(R.string.proxy_vote)
                resultView.alpha = 0.5f
                val params = objName?.layoutParams as RelativeLayout.LayoutParams
                params.addRule(RelativeLayout.START_OF, resultView.id)
                titleParams.addRule(RelativeLayout.START_OF, resultView.id)
            } else {
                resultView.visibility = View.GONE
            }
            voteValueView.visibility = View.VISIBLE
            voteValueView.text = String.format(Locale.US, "%.1f", item?.obj.vote?:0f)
            val percentDif = voteTeamView?.setTeamVoteDifference(item?.obj.vote?:0f, item?.obj.teamVote?:0f) ?: 0
            voteTeamView?.text = String.format(Locale.US, "%.1f", item?.obj.teamVote)
            setVoteColor(voteValueView, - percentDif)
        }
    }

    private open inner class ClaimsVotesViewHolder(itemView: View) : TeambrellaDataPagerAdapter.ClaimViewHolder(itemView, mTeamId, "") {

        private val voteView: TextView? = itemView.findViewById(R.id.vote)
        private val weightView: TextView? = itemView.findViewById(R.id.weight)
        private val voteValueView: TextView? = itemView.findViewById(R.id.vote_value)
        private val voteTeamView: TextView? = itemView.findViewById(R.id.vote_team)

        @SuppressLint("SetTextI18n")
        override fun onBind(item: JsonWrapper) {
            super.onBind(item)

            val weight = item.getFloat(TeambrellaModel.ATTR_DATA_WEIGHT_COMBINED)
            weightView?.text = itemView.context.getString(if (weight >= 0.1) R.string.float_format_string_1 else R.string.float_format_string_2, weight)

            mClaimAmount.visibility = View.GONE
            mPaymentProgress.visibility = View.GONE

            if (item.obj.proxyName != null) {
                mResultView.text = itemView.context.getString(R.string.proxy_vote)
                mResultView.alpha = 0.5f
            } else {
                mResultView.visibility = View.GONE
            }
            voteValueView?.visibility = View.VISIBLE
            voteValueView?.text = ((item.obj.myVote?:0f) * 100).roundToInt().toString() + "%"
            voteTeamView?.visibility = View.VISIBLE
            val percentDif = voteTeamView?.setTeamVoteDifference(item.obj.myVote?:0f, item.obj.reimbursement?:0f) ?: 0
            voteTeamView?.text = ((item.obj.reimbursement?:0f) * 100).roundToInt().toString() + "%"
            setVoteColor(voteValueView, percentDif)
        }
    }

    private inner class MyVoteViewHolder internal constructor(itemView: View) : VoteViewHolder(itemView)

    fun setVoteColor(voteView: TextView?, value: Int)
    {
        voteView?.setTextColor(voteView.context.resources.getColor(when {
            value < 0 -> R.color.perrywinkle
            value > 0 -> R.color.darkSkyBlue
            else -> R.color.blueGrey
        }))
    }
}

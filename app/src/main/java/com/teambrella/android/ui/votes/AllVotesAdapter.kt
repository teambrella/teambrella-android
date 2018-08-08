package com.teambrella.android.ui.votes

import android.annotation.SuppressLint
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.JsonArray
import com.teambrella.android.R
import com.teambrella.android.api.TeambrellaModel
import com.teambrella.android.api.model.json.JsonWrapper
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter

/**
 * All Votes Adapter
 */
class AllVotesAdapter(pager: IDataPager<JsonArray>, private val mTeamId: Int, private val mMode: Int) : TeambrellaDataPagerAdapter(pager) {

    companion object {

        const val VIEW_TYPE_ME = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 1
        const val VIEW_TYPE_HEADER = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 2
        const val VIEW_TYPE_TEAMMATE = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 3

        const val MODE_CLAIM = 1
        const val MODE_APPLICATION = 2
    }


    private var mMyVote: JsonWrapper? = null


    init {
        setHasStableIds(true)
    }

    fun setMyVote(vote: JsonWrapper) {
        mMyVote = vote
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        var holder = super.onCreateViewHolder(parent, viewType)
        if (holder == null) {
            val inflater = LayoutInflater.from(parent.context)
            when (viewType) {
                VIEW_TYPE_ME -> holder = MyVoteViewHolder(inflater.inflate(R.layout.list_item_vote, parent, false))
                VIEW_TYPE_HEADER -> holder = TeambrellaDataPagerAdapter.Header(parent, R.string.all_votes, R.string.votes, if (headersCount == 2) R.drawable.list_item_header_background_middle else R.drawable.list_item_header_background_top)
                else -> holder = VoteViewHolder(inflater.inflate(R.layout.list_item_vote, parent, false))
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
        }
    }

    override fun getItemViewType(position: Int): Int {
        val viewType = super.getItemViewType(position)
        if (viewType == TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR) {
            when (position) {
                0 -> return if (headersCount == 2)
                    VIEW_TYPE_ME
                else
                    VIEW_TYPE_HEADER
                1 -> return if (headersCount == 2)
                    VIEW_TYPE_HEADER
                else
                    VIEW_TYPE_TEAMMATE
                else -> return VIEW_TYPE_TEAMMATE
            }
        }
        return viewType
    }

    override fun getHeadersCount() = mMyVote?.let {
        if (it.getString(TeambrellaModel.ATTR_DATA_VOTED_BY_PROXY_USER_ID) != null
                || it.getFloat(TeambrellaModel.ATTR_DATA_VOTE, 0f) <= 0) 1 else 2
    } ?: 1


    private open inner class VoteViewHolder(itemView: View) : TeambrellaDataPagerAdapter.AMemberViewHolder(itemView, mTeamId) {

        private val voteView: TextView? = itemView.findViewById(R.id.vote)
        private val weightView: TextView? = itemView.findViewById(R.id.weight)

        @SuppressLint("SetTextI18n")
        override fun onBind(item: JsonWrapper) {
            super.onBind(item)
            when (mMode) {
                MODE_CLAIM -> voteView?.text = Html.fromHtml("" + (item.getFloat(TeambrellaModel.ATTR_DATA_VOTE) * 100).toInt()).toString() + "%"
                MODE_APPLICATION -> voteView?.text = itemView.context.getString(R.string.risk_vote_format_string, item.getFloat(TeambrellaModel.ATTR_DATA_VOTE))
            }

            val weight = item.getFloat(TeambrellaModel.ATTR_DATA_WEIGHT_COMBINED)
            weightView?.text = itemView.context.getString(if (weight >= 0.1)
                R.string.float_format_string_1
            else
                R.string.float_format_string_2, weight)
        }
    }

    private inner class MyVoteViewHolder internal constructor(itemView: View) : VoteViewHolder(itemView)

}

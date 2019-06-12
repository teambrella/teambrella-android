package com.teambrella.android.ui.team.feed

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.ui.IMainDataHost
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter
import com.teambrella.android.ui.chat.ChatActivity
import com.teambrella.android.ui.util.*
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets


const val VIEW_TYPE_HEADER = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 1
const val VIEW_TYPE_ITEM_FEED = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 2

class KFeedAdapter(val dataHost: IMainDataHost, val teamId: Int
                   , pager: IDataPager<JsonArray>, listener: OnStartActivityListener) : TeambrellaDataPagerAdapter(pager, listener) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getHeadersCount() = if (dataHost.isFullTeamAccess) 1 else 0

    override fun getItemViewType(position: Int): Int {
        val viewType = super.getItemViewType(position)
        return if (viewType == VIEW_TYPE_REGULAR) {
            if (position == 0 && dataHost.isFullTeamAccess) {
                VIEW_TYPE_HEADER
            } else {
                VIEW_TYPE_ITEM_FEED
            }
        } else {
            viewType
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder = super.onCreateViewHolder(parent, viewType)
        if (viewHolder == null) {
            val inflater = LayoutInflater.from(parent.context)
            when (viewType) {
                VIEW_TYPE_ITEM_FEED -> viewHolder = FeedItemViewHolder(inflater.inflate(R.layout.list_item_feed_claim, parent, false))
                VIEW_TYPE_HEADER -> viewHolder = FeedHeader(inflater.inflate(R.layout.list_item_feed_header, parent, false))
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is FeedItemViewHolder) {
            mPager.loadedData[position - headersCount]?.asJsonObject?.let {
                holder.onBind(it)
            }
        }
    }

    override fun onPagerItemChanged(item: Int) {
        super.onPagerItemChanged(item)
        //notifyItemChanged(headersCount + item) // see https://stackoverflow.com/questions/30220771/recyclerview-inconsistency-detected-invalid-item-position
        notifyDataSetChanged();
    }

    private inner class FeedItemViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView? = itemView.findViewById(R.id.icon)
        private val title: TextView? = itemView.findViewById(R.id.title)
        private val `when`: TextView? = itemView.findViewById(R.id.`when`)
        private val message: TextView? = itemView.findViewById(R.id.message)
        private val avatars: TeambrellaAvatarsWidgets? = itemView.findViewById(R.id.avatars)
        private val unread: TextView? = itemView.findViewById(R.id.unread)
        private val type: TextView? = itemView.findViewById(R.id.type)
        private val pinned: ImageView? = itemView.findViewById(R.id.pinned)

        fun onBind(item: JsonObject) {
            if (item.text == null) {
                return
            }
            setImage(item)
            setType(item)
            setTitle(item)
            message?.text = Html.fromHtml(item.text?.replace("<p>", "")?.replace("</p>", ""))
            unread?.setUnreadCount(item.unreadCount ?: 0)
            pinned?.visibility = if ((item.unreadCount ?:0) <= 0 && (item.teamPin ?:0F) > 0F) View.VISIBLE else View.INVISIBLE

            item.itemDate?.let {
                `when`?.setRelativeTimeSpan(it)
            }

            item.topPosterAvatars?.map { it.asString }?.let {
                avatars?.setAvatars(imageLoader, it, item.posterCount ?: 0)
            }
            setClickAction(item)

        }


        private fun setImage(item: JsonObject) {
            item.smallPhotoOrAvatar?.let { imageLoader.getImageUrl(it) }?.let {
                when (item.itemType) {
                    TeambrellaModel.FEED_ITEM_TEAMMATE,
                    TeambrellaModel.FEED_ITEM_TEAM_CHAT -> {
                        icon?.setAvatar(it)
                    }
                    else -> {
                        icon?.setImage(it, R.dimen.rounded_corners_2dp)
                    }
                }
            }
        }

        private fun setType(item: JsonObject) {
            when (item.itemType) {
                TeambrellaModel.FEED_ITEM_CLAIM -> {
                    type?.setText(R.string.claim)
                    type?.setRightCompoundDrawable(R.drawable.ic_claim)
                }
                TeambrellaModel.FEED_ITEM_TEAM_CHAT -> {
                    type?.setRightCompoundDrawable(R.drawable.ic_discussion)
                    type?.setText(R.string.discussion)
                }
                else -> {
                    //type?.setRightCompoundDrawable(R.drawable.ic_application)
                    //type?.setText(R.string.application)
                    type?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    type?.setText("")
                }
            }
        }

        private fun setTitle(item: JsonObject) {
            when (item.itemType) {
                TeambrellaModel.FEED_ITEM_TEAM_CHAT -> title?.text = item.chatTitle
                TeambrellaModel.FEED_ITEM_TEAMMATE -> title?.text = item.itemUserName
                else -> title?.text = item.modelOrName
            }
        }

        private fun setClickAction(item: JsonObject) {
            itemView.setOnClickListener {
                when (item.itemType) {
                    TeambrellaModel.FEED_ITEM_CLAIM -> {
                        startActivity(ChatActivity.getClaimChat(itemView.context
                                , teamId
                                , item.itemIdInt ?: 0
                                , item.modelOrName
                                , item.smallPhotoOrAvatar
                                , item.topicId
                                , dataHost.teamAccessLevel
                                , item.itemDate))
                    }

                    TeambrellaModel.FEED_ITEM_TEAM_CHAT -> {
                        startActivity(ChatActivity.getFeedChat(itemView.context
                                , item.chatTitle
                                , item.topicId
                                , teamId
                                , dataHost.teamAccessLevel))
                    }
                    else -> {
                        startActivity(ChatActivity.getTeammateChat(itemView.context, teamId
                                , item.itemUserId
                                , item.itemUserName
                                , item.smallPhotoOrAvatar
                                , item.topicId
                                , dataHost.teamAccessLevel))
                    }
                }
            }

        }
    }

    private inner class FeedHeader internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.findViewById<View>(R.id.start_new_discussion).setOnClickListener {
                dataHost.startNewDiscussion()
            }
        }
    }
}
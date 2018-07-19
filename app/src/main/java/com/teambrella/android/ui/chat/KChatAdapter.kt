package com.teambrella.android.ui.chat

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.image.glide.GlideApp
import com.teambrella.android.ui.base.ChatDataPagerAdapter
import com.teambrella.android.ui.image.ImageViewerActivity
import com.teambrella.android.ui.teammate.startTeammateActivity
import com.teambrella.android.ui.util.setAvatar
import com.teambrella.android.ui.util.setImage
import com.teambrella.android.util.StatisticHelper
import com.teambrella.android.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val MODE_CLAIM = 1
const val MODE_APPLICATION = 2
const val MODE_DISCUSSION = 3
const val MODE_CONVERSATION = 4

class KChatAdapter(pager: IDataPager<JsonArray>, private val context: Context, private val teamId: Int,
                   private val mode: Int) : ChatDataPagerAdapter(pager), ListPreloader.PreloadModelProvider<JsonObject>, ListPreloader.PreloadSizeProvider<JsonObject> {

    private companion object {
        private const val VIEW_TYPE_MESSAGE_ME = VIEW_TYPE_REGULAR + 1
        private const val VIEW_TYPE_MESSAGE_THEM = VIEW_TYPE_REGULAR + 2
        private const val VIEW_TYPE_IMAGE_ME = VIEW_TYPE_REGULAR + 3
        private const val VIEW_TYPE_IMAGE_THEM = VIEW_TYPE_REGULAR + 4
        private const val VIEW_TYPE_DATE = VIEW_TYPE_REGULAR + 5
        private const val VIEW_TYPE_PAID_CLAIM = VIEW_TYPE_REGULAR + 6
        private const val FILE_PREFIX = "file:"

    }

    private val timeFormat: SimpleDateFormat = SimpleDateFormat(if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a", Locale.ENGLISH)
    private val dateFormat = SimpleDateFormat("d MMMM", Locale.getDefault())


    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int): Int {
        var type = super.getItemViewType(position)
        if (type == VIEW_TYPE_REGULAR) {
            val item = mPager.loadedData[position]?.asJsonObject
            type = when (item?.chatItemType) {
                ChatItems.CHAT_ITEM_MESSAGE -> VIEW_TYPE_MESSAGE_THEM
                ChatItems.CHAT_ITEM_MY_MESSAGE -> VIEW_TYPE_MESSAGE_ME
                ChatItems.CHAT_ITEM_IMAGE -> VIEW_TYPE_IMAGE_THEM
                ChatItems.CHAT_ITEM_MY_IMAGE -> VIEW_TYPE_IMAGE_ME
                ChatItems.CHAT_ITEM_DATE -> VIEW_TYPE_DATE
                ChatItems.CHAT_ITEM_PAID_CLAIM -> VIEW_TYPE_PAID_CLAIM
                else -> VIEW_TYPE_REGULAR
            }
        }

        return type
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_MESSAGE_ME -> ChatMessageViewHolder(inflater.inflate(R.layout.list_item_my_message, parent, false))
            VIEW_TYPE_MESSAGE_THEM -> ChatMessageViewHolder(inflater.inflate(R.layout.list_item_message, parent, false))
            VIEW_TYPE_IMAGE_ME -> ChatImageViewHolder(inflater.inflate(R.layout.list_item_my_message_image, parent, false))
            VIEW_TYPE_IMAGE_THEM -> ChatImageViewHolder(inflater.inflate(R.layout.list_item_message_image, parent, false))
            VIEW_TYPE_DATE -> ChatDateViewHolder(inflater.inflate(R.layout.list_item_date, parent, false))
            VIEW_TYPE_PAID_CLAIM -> ChatPaidClaimViewHolder(inflater.inflate(R.layout.list_item_chat_paid_claim, parent, false))
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)



        if (holder is ChatViewHolder) {
            mPager.loadedData[position]?.asJsonObject?.let {
                holder.onBind(it)
            }
        }

        if (holder is ChatDateViewHolder) {
            mPager.loadedData[position]?.asJsonObject?.let {
                holder.onBind(it)
            }
        }

        if (holder is ChatPaidClaimViewHolder) {
            mPager.loadedData[position]?.asJsonObject?.let {
                holder.onBind(it)
            }
        }
    }

    override fun createErrorViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(View(parent.context)) {

        }
    }


    override fun createLoadingViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(View(parent.context)) {

        }
    }


    override fun getPreloadItems(position: Int): MutableList<JsonObject> {
        return when (getItemViewType(position)) {
            VIEW_TYPE_IMAGE_THEM,
            VIEW_TYPE_IMAGE_ME -> {
                mutableListOf(mPager.loadedData[position]!!.asJsonObject)
            }
            else -> mutableListOf()
        }
    }

    override fun getPreloadRequestBuilder(item: JsonObject): RequestBuilder<*>? {

        var smallImages = item.localImages?.mapTo(ArrayList()) { FILE_PREFIX + it.asString }
        if (smallImages == null) {
            smallImages = item.smallImages?.mapTo(ArrayList()) { it.asString }
        }

        var requestBuilder: RequestBuilder<*>? = null

        smallImages?.let { _smallImages ->
            item.imageIndex?.let { _index ->
                requestBuilder = if (_smallImages[_index].startsWith(FILE_PREFIX)) {
                    GlideApp.with(context).load(_smallImages[_index])
                            .apply(RequestOptions().transform(RoundedCorners(context.resources.getDimensionPixelSize(R.dimen.rounded_corners_4dp))))
                } else {
                    GlideApp.with(context).load(imageLoader.getImageUrl(_smallImages[_index]))
                            .apply(RequestOptions().transform(RoundedCorners(context.resources.getDimensionPixelSize(R.dimen.rounded_corners_4dp))))
                }
            }
        }
        return requestBuilder
    }


    override fun getPreloadSize(item: JsonObject, adapterPosition: Int, perItemPosition: Int): IntArray? {
        var size: IntArray? = null
        item.imageRatios?.get(item.imageIndex!!)?.asFloat?.let {
            val width = context.resources.getDimensionPixelSize(R.dimen.chat_image_width)
            size = intArrayOf(width, Math.round(width * it))
        }
        return size
    }

    private inner class ChatDateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val date: TextView? = itemView.findViewById(R.id.date)

        fun onBind(item: JsonObject) {
            date?.text = when (item.messageStatus) {
                TeambrellaModel.PostStatus.POST_PENDING -> dateFormat.format(Date(item.added ?: 0L))
                else -> dateFormat.format(TimeUtils.getDateFromTicks(item.created ?: 0L))
            }
        }
    }

    private inner class ChatPaidClaimViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val action: View? = itemView.findViewById(R.id.action)

        fun onBind(item: JsonObject) {
            action?.setOnClickListener {
                val text = when (item.coverageType) {
                    TeambrellaModel.InsuranceType.CAR_COLLISION_DEDUCTIBLE -> context.getString(R.string.paid_claim_shareable_collision_deductible, item.sharedUrl)
                    TeambrellaModel.InsuranceType.CAT,
                    TeambrellaModel.InsuranceType.DOG -> context.getString(R.string.paid_claim_shareable_pet, item.sharedUrl)
                    TeambrellaModel.InsuranceType.BICYCLE -> context.getString(R.string.paid_claim_shareable_bike, item.sharedUrl)
                    else -> item.sharedUrl
                }
                context.startActivity(Intent.createChooser(Intent().setAction(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, text)
                        .setType("text/plain"), itemView.context.getString(R.string.share_with_friends)))

                StatisticHelper.sharePaidClaim(context, item.claimId ?: 0)
            }
        }
    }

    private open inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected val userPicture: ImageView? = itemView.findViewById(R.id.user_picture)
        private val time: TextView? = itemView.findViewById(R.id.time)
        private val status: View? = itemView.findViewById(R.id.status)

        open fun onBind(item: JsonObject) {
            item.teammatePart?.let { teammate ->
                teammate.avatar?.let { avatar ->
                    userPicture?.setAvatar(imageLoader.getImageUrl(avatar))
                }
                userPicture?.setOnClickListener {
                    if (teamId > 0) {
                        startTeammateActivity(itemView.context, teamId, item.userId!!, teammate.name, teammate.avatar.toString())
                    }
                }
            }

            time?.text = timeFormat.format(TimeUtils.getDateFromTicks(item.created ?: 0L))
            status?.visibility = if (item.messageStatus == TeambrellaModel.PostStatus.POST_PENDING) View.VISIBLE else View.GONE
            time?.visibility = if (status == null || status.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    private inner class ChatMessageViewHolder(itemView: View) : ChatViewHolder(itemView) {

        private val context = itemView.context
        private val message: TextView? = itemView.findViewById(R.id.message)
        private val teammateName: TextView? = itemView.findViewById(R.id.teammate_name)
        private val vote: TextView? = itemView.findViewById(R.id.vote)
        private val header: View? = itemView.findViewById(R.id.header)

        init {
            message?.movementMethod = LinkMovementMethod.getInstance()
            message?.linksClickable = true
        }

        override fun onBind(item: JsonObject) {
            super.onBind(item)
            message?.text = item.text
            teammateName?.text = item.teammatePart?.name
            val voteValue = item.teammatePart?.vote ?: -1f
            when (mode) {
                MODE_DISCUSSION -> {
                    vote?.visibility = View.INVISIBLE
                }
                MODE_CLAIM -> {
                    vote?.visibility = View.VISIBLE
                    vote?.text = if (voteValue > 0) String.format(Locale.US, "%s:%d%%", context.getString(R.string.chat_voted), Math.round(voteValue * 100)) else
                        context.getString(R.string.chat_not_voted_yet)
                }
                MODE_APPLICATION -> {
                    vote?.visibility = View.VISIBLE
                    vote?.text = if (voteValue > 0) String.format(Locale.US, "%s:%.2f", context.getString(R.string.chat_voted), voteValue) else
                        context.getString(R.string.chat_not_voted_yet)
                }
                MODE_CONVERSATION -> {
                    vote?.visibility = View.INVISIBLE
                    userPicture?.visibility = View.GONE
                    header?.visibility = View.GONE
                }
            }
        }
    }

    private inner class ChatImageViewHolder(itemView: View) : ChatViewHolder(itemView) {


        private val context = itemView.context
        private val image: ImageView? = itemView.findViewById(R.id.image)
        private val width = itemView.context.resources.getDimensionPixelSize(R.dimen.chat_image_width)

        override fun onBind(item: JsonObject) {
            super.onBind(item)
            var smallImages = item.localImages?.mapTo(ArrayList()) { FILE_PREFIX + it.asString }
            if (smallImages == null) {
                smallImages = item.smallImages?.mapTo(ArrayList()) { it.asString }
            }

            smallImages?.let { _smallImages ->
                item.imageIndex?.let { _index ->
                    val ratio = item.imageRatios?.get(_index)?.asFloat
                    ratio?.let {
                        val params = image?.layoutParams as ConstraintLayout.LayoutParams
                        params.dimensionRatio = "" + Math.round(width * it) + ":" + width
                        image.layoutParams = params
                    }

                    if (_smallImages[_index].startsWith(FILE_PREFIX)) {
                        image?.setImage(_smallImages[_index], R.dimen.rounded_corners_4dp, false)
                    } else {
                        image?.setImage(imageLoader.getImageUrl(_smallImages[_index]), R.dimen.rounded_corners_4dp, false)
                    }


                    if (item.messageStatus != TeambrellaModel.PostStatus.POST_PENDING) {
                        item.images?.mapTo(ArrayList()) { it.asString }?.let { _images ->
                            image?.setOnClickListener {
                                context.startActivity(ImageViewerActivity.getLaunchIntent(context, _images, _index))
                            }
                        }
                    }
                }
            }
        }

    }
}
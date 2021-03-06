package com.teambrella.android.ui.team.teammates

import android.content.Intent
import android.support.v7.widget.RecyclerView
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
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter
import com.teambrella.android.ui.teammate.getTeammateIntent
import com.teambrella.android.ui.util.setAvatar
import com.teambrella.android.ui.widget.CountDownClock
import com.teambrella.android.util.AmountCurrencyUtil
import com.teambrella.android.util.StatisticHelper
import com.teambrella.android.util.TeambrellaDateUtils
import java.text.DecimalFormat
import java.util.*

const val VIEW_TYPE_TEAMMATE = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR
const val VIEW_TYPE_NEW_MEMBER = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 1
const val VIEW_TYPE_HEADER_TEAMMATES = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 2
const val VIEW_TYPE_HEADER_NEW_MEMBERS = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 3
const val VIEW_TYPE_INVITES_FRIENDS = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 4


class KTeammateAdapter(pager: IDataPager<JsonArray>,
                       teamId: Int,
                       currency: String,
                       coverage: Double,
                       inviteText: String?,
                       listener: OnStartActivityListener) : TeambrellaDataPagerAdapter(pager, listener) {
    val mTeamId = teamId
    val mCurrency = currency
    val mInviteText = inviteText
    val mCoverage = coverage


    companion object {
        private val decimalFormat = DecimalFormat.getInstance()
    }


    override fun getItemViewType(position: Int): Int {
        var type = super.getItemViewType(position)
        if (type == TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR) {
            type = if (position == 0 && headersCount > 0) VIEW_TYPE_INVITES_FRIENDS
            else {
                var dataPosition = position - headersCount
                if (dataPosition < 0 || dataPosition >= mPager.loadedData.count()) {
                    return TeambrellaDataPagerAdapter.VIEW_TYPE_ERROR
                }
                val jsonObject = mPager.loadedData.get(dataPosition).asJsonObject
                when (jsonObject.get(TeambrellaModel.ATTR_DATA_ITEM_TYPE).asInt) {
                    TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_NEW_MEMBERS -> VIEW_TYPE_HEADER_NEW_MEMBERS
                    TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_TEAMMATES -> VIEW_TYPE_HEADER_TEAMMATES
                    TeambrellaModel.ATTR_DATA_ITEM_TYPE_ENTRY -> {
                        if (jsonObject.get(TeambrellaModel.ATTR_DATA_IS_VOTING).asBoolean) VIEW_TYPE_NEW_MEMBER
                        else VIEW_TYPE_TEAMMATE
                    }
                    else -> VIEW_TYPE_TEAMMATE
                }
            }
        }
        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = super.onCreateViewHolder(parent, viewType)
        val inflater = LayoutInflater.from(parent.context)
        if (viewHolder == null)
            viewHolder = when (viewType) {
                VIEW_TYPE_INVITES_FRIENDS -> InviteFriendsViewHolder(inflater.inflate(R.layout.list_item_invite_friends, parent, false))
                VIEW_TYPE_TEAMMATE -> TeammateViewHolder(inflater.inflate(R.layout.list_item_teammate, parent, false))
                VIEW_TYPE_HEADER_TEAMMATES -> Header(parent, R.string.teammates, if (mCoverage < 0.0001) R.string.to_cover_for else R.string.covering, R.drawable.list_item_header_background_middle)
                VIEW_TYPE_HEADER_NEW_MEMBERS -> Header(parent, R.string.new_teammates, R.string.voting_ends_title, R.drawable.list_item_header_background_middle)
                VIEW_TYPE_NEW_MEMBER -> NewMemberViewHolder(inflater.inflate(R.layout.list_item_new_teamate, parent, false))
                else -> null
            }

        return viewHolder!!
    }

    override fun getHeadersCount(): Int = if (mInviteText != null) 1 else 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is ATeammateViewHolder) {
            holder.onBind(mPager.loadedData.get(position - headersCount).asJsonObject)
        }
    }

    open inner class ATeammateViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val icon: ImageView? = view.findViewById(R.id.icon)
        val title: TextView? = view.findViewById(R.id.teammate)
        val `object`: TextView? = view.findViewById(R.id.`object`)

        open fun onBind(item: JsonObject?) {
            item.avatar?.let {
                icon?.setAvatar(imageLoader.getImageUrl(item.avatar))
            }

            title?.text = item.name
            `object`?.text = getObjectString(item.model, item.year)

            itemView.setOnClickListener {
                startActivity(getTeammateIntent(itemView.context, mTeamId, item.userId!!,
                        item.name, item.avatar))
            }
        }

        private fun getObjectString(model: String?, year: String?): String {
            return itemView.context.getString(R.string.object_format_string, model, year)
        }
    }

    inner class TeammateViewHolder(view: View) : ATeammateViewHolder(view) {

        private val coversMe: TextView? = view.findViewById(R.id.net)
        private val risk: TextView? = view.findViewById(R.id.indicator)
        private val currencySign: String = AmountCurrencyUtil.getCurrencySign(mCurrency)

        override fun onBind(item: JsonObject?) {
            super.onBind(item)
            item.coversMe?.let { _coversMe ->
                val net = Math.round(_coversMe as Double).toInt()
                if (net > 0) {
                    AmountCurrencyUtil.setSignedAmount(this.coversMe, net, currencySign)
                }
                else {
                    this.coversMe?.text = "-"
                }
            }
            this.risk?.text = String.format(Locale.US, "%.1f", item.risk)
        }
    }


    inner class NewMemberViewHolder(view: View) : ATeammateViewHolder(view) {
        private val endsIn: TextView? = view.findViewById(R.id.ends_in)
        private val clock: CountDownClock? = view.findViewById(R.id.clock)
        override fun onBind(item: JsonObject?) {
            super.onBind(item)
            val endsInValue = item.votingEndsIn
            endsInValue?.let {
                endsIn?.text = TeambrellaDateUtils.getRelativeTimeLocalized(itemView.context, endsInValue)
                clock?.setRemainedMinutes(endsInValue)
            }
        }
    }

    inner class InviteFriendsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val inviteButton: View? = view.findViewById(R.id.invite_friends)

        init {
            inviteButton?.setOnClickListener {
                startActivity(Intent.createChooser(Intent().setAction(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, mInviteText)
                        .setType("text/plain"), itemView.context.getString(R.string.invite_friends)))
                StatisticHelper.onInviteFriends(itemView.context, mTeamId)
            }
        }
    }

}

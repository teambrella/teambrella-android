package com.teambrella.android.ui.team.teammates

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.image.glide.GlideApp
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter
import com.teambrella.android.ui.teammate.TeammateActivity
import com.teambrella.android.util.AmountCurrencyUtil
import com.teambrella.android.util.TeambrellaDateUtils

const val VIEW_TYPE_TEAMMATE = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR
const val VIEW_TYPE_NEW_MEMBER = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 1
const val VIEW_TYPE_HEADER_TEAMMATES = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 2
const val VIEW_TYPE_HEADER_NEW_MEMBERS = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 3
const val VIEW_TYPE_INVITES_FRIENDS = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 4

class KTeammateAdapter(pager: IDataPager<JsonArray>
                       , teamId: Int
                       , currency: String
                       , inviteText: String?
                       , listener: OnStartActivityListener) : TeambrellaDataPagerAdapter(pager, listener) {
    val mTeamId = teamId
    val mCurrency = currency
    val mInviteText = inviteText


    override fun getItemViewType(position: Int): Int {
        var type = super.getItemViewType(position)
        if (type == TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR) {
            type = if (position == 0 && headersCount > 0) VIEW_TYPE_INVITES_FRIENDS
            else {
                val jsonObject = mPager.loadedData.get(position - headersCount).asJsonObject
                when (jsonObject.get(TeambrellaModel.ATTR_DATA_ITEM_TYPE).asInt) {
                    TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_NEW_MEMBERS -> VIEW_TYPE_HEADER_NEW_MEMBERS
                    TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_TEAMMATES -> VIEW_TYPE_HEADER_TEAMMATES
                    TeambrellaModel.ATTR_DATA_ITEM_TYPE_TEAMMATE -> {
                        if (jsonObject.get(TeambrellaModel.ATTR_DATA_IS_VOTING).asBoolean) VIEW_TYPE_NEW_MEMBER
                        else VIEW_TYPE_TEAMMATE
                    }
                    else -> VIEW_TYPE_TEAMMATE
                }
            }
        }
        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder = super.onCreateViewHolder(parent, viewType)
        val inflater = LayoutInflater.from(parent?.context)
        if (viewHolder == null)
            viewHolder = when (viewType) {
                VIEW_TYPE_INVITES_FRIENDS -> InviteFriendsViewHolder(inflater.inflate(R.layout.list_item_invite_friends, parent, false))
                VIEW_TYPE_TEAMMATE -> TeammateViewHolder(inflater.inflate(R.layout.list_item_teammate, parent, false))
                VIEW_TYPE_HEADER_TEAMMATES -> Header(parent, R.string.teammates, R.string.net, R.drawable.list_item_header_background_middle)
                VIEW_TYPE_HEADER_NEW_MEMBERS -> Header(parent, R.string.new_teammates, R.string.voting_ends_title, R.drawable.list_item_header_background_middle)
                VIEW_TYPE_NEW_MEMBER -> NewMemberViewHolder(inflater.inflate(R.layout.list_item_new_teamate, parent, false))
                else -> null
            }

        return viewHolder
    }

    override fun getHeadersCount(): Int = if (mInviteText != null) 1 else 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
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
                icon?.let {
                    GlideApp.with(itemView).load(imageLoader.getImageUrl(item.avatar))
                            .apply(RequestOptions().circleCrop()).into(icon)
                }
            }

            title?.text = item.name
            `object`?.text = getObjectString(item.model, item.year)

            itemView.setOnClickListener({
                startActivity(TeammateActivity.getIntent(itemView.context, mTeamId, item.userId,
                        item.name, item.avatar))
            })
        }

        private fun getObjectString(model: String?, year: String?): String {
            return itemView.context.getString(R.string.object_format_string, model, year)
        }
    }

    inner class TeammateViewHolder(view: View) : ATeammateViewHolder(view) {

        private val net: TextView? = view.findViewById(R.id.net)
        private val risk: TextView? = view.findViewById(R.id.indicator)
        private val currencySign: String = AmountCurrencyUtil.getCurrencySign(mCurrency)

        override fun onBind(item: JsonObject?) {
            super.onBind(item)
            item.totallyPaid?.let {
                val net = Math.round(item.totallyPaid as Double)
                when {
                    net > 0 -> this.net?.text = Html.fromHtml(getPositiveNetString(net))
                    net < 0 -> this.net?.text = Html.fromHtml(getNegativeNetString(net))
                    else -> this.net?.text = itemView.context.getString(R.string.teammate_net_format_string_zero, currencySign)
                }
            }
            this.risk?.text = itemView.context.getString(R.string.risk_format_string, item.risk)
        }

        private fun getPositiveNetString(net: Long): String {
            return itemView.context.getString(R.string.teammate_net_format_string_plus, currencySign, Math.abs(net))
        }

        private fun getNegativeNetString(net: Long): String {
            return itemView.context.getString(R.string.teammate_net_format_string_minus, currencySign, Math.abs(net))
        }
    }


    inner class NewMemberViewHolder(view: View) : ATeammateViewHolder(view) {
        private val endsIn: TextView? = view.findViewById(R.id.ends_in)
        override fun onBind(item: JsonObject?) {
            super.onBind(item)
            val endsInValue = item.votingEndsIn
            endsInValue?.let {
                endsIn?.text = TeambrellaDateUtils.getRelativeTimeLocalized(itemView.context, endsInValue)
            }
        }
    }

    inner class InviteFriendsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val inviteButton: View? = view.findViewById(R.id.invite_friends)

        init {
            inviteButton?.setOnClickListener({
                startActivity(Intent.createChooser(Intent().setAction(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, mInviteText)
                        .setType("text/plain"), itemView.context.getString(R.string.invite_friends)))
            })
        }
    }

}

@file:JvmName("KTeammateAdapter")

package com.teambrella.android.ui.team.teammates

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.DataModelObject
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.image.glide.GlideApp
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter
import com.teambrella.android.ui.teammate.TeammateActivity
import com.teambrella.android.util.AmountCurrencyUtil

const val VIEW_TYPE_TEAMMATE = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR
const val VIEW_TYPE_NEW_MEMBER = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 1
const val VIEW_TYPE_HEADER_TEAMMATES = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 2
const val VIEW_TYPE_HEADER_NEW_MEMBERS = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 3
const val VIEW_TYPE_INVITE_FRIENDS = TeambrellaDataPagerAdapter.VIEW_TYPE_REGULAR + 4

class KTeammateAdapter(pager: IDataPager<JsonArray>
                       , teamId: Int
                       , currency: String
                       , listener: OnStartActivityListener) : TeambrellaDataPagerAdapter(pager, listener) {
    val mTeamId = teamId
    val mCurrency = currency


    open inner class ATeammateViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val icon: ImageView? = view.findViewById(R.id.icon)
        val title: TextView? by lazy(LazyThreadSafetyMode.NONE, { itemView.findViewById<TextView>(R.id.teammate) })
        val `object`: TextView? by lazy(LazyThreadSafetyMode.NONE, { itemView.findViewById<TextView>(R.id.`object`) })

        open fun onBind(item: JsonObject?) {
            val data = DataModelObject(item)
            data.avatar?.let {
                icon?.let {
                    GlideApp.with(itemView).load(imageLoader.getImageUrl(data.avatar))
                            .apply(RequestOptions().circleCrop()).into(icon as ImageView)
                }
            }

            title?.text = data.name
            `object`?.text = itemView.context.getString(R.string.object_format_string, data.model, data.year)

            itemView.setOnClickListener({
                startActivity(TeammateActivity.getIntent(itemView.context, mTeamId, data.userId, data.name, data.avatar))
            })
        }
    }

    inner class TeammateViewHolder(view: View) : ATeammateViewHolder(view) {
        val net: TextView? by lazy(LazyThreadSafetyMode.NONE, { itemView.findViewById<TextView>(R.id.icon) })
        val risk: TextView? by lazy(LazyThreadSafetyMode.NONE, { itemView.findViewById<TextView>(R.id.indicator) })

        override fun onBind(item: JsonObject?) {
            super.onBind(item)
            val data = DataModelObject(item)
            data.totallyPaid?.let {
                val net = Math.round(data.totallyPaid as Double)
                when {
                    net > 0 -> this.net?.text = Html.fromHtml(itemView.context.getString(R.string.teammate_net_format_string_plus, AmountCurrencyUtil.getCurrencySign(mCurrency), Math.abs(net)))
                    net < 0 -> this.net?.text = Html.fromHtml(itemView.context.getString(R.string.teammate_net_format_string_minus, AmountCurrencyUtil.getCurrencySign(mCurrency), Math.abs(net)))
                    else -> this.net?.text = itemView.context.getString(R.string.teammate_net_format_string_zero, AmountCurrencyUtil.getCurrencySign(mCurrency))
                }
            }
            this.risk?.text = itemView.context.getString(R.string.risk_format_string, data.risk)

        }
    }

}

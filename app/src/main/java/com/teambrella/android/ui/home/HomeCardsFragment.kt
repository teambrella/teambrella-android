package com.teambrella.android.ui.home

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.ui.IMainDataHost
import com.teambrella.android.ui.base.ADataFragment
import com.teambrella.android.ui.base.createDataFragment
import com.teambrella.android.ui.chat.ChatActivity
import com.teambrella.android.ui.util.setAvatar
import com.teambrella.android.ui.util.setImage
import com.teambrella.android.ui.util.setUnreadCount
import com.teambrella.android.util.AmountCurrencyUtil
import io.reactivex.Notification
import java.util.*

class HomeCardsFragment : ADataFragment<IMainDataHost>() {

    private val header: TextView? by ViewHolder(R.id.home_header)
    private val subHeader: TextView? by ViewHolder(R.id.home_sub_header)
    private val cardsPager: ViewPager? by ViewHolder(R.id.cards_pager)
    private var pagerAdapter: CardsAdapter? = null
    private val pagerIndicator: LinearLayout? by ViewHolder(R.id.page_indicator)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_home_cards, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cardsPager?.pageMargin = 20
        val adapter = CardsAdapter()
        cardsPager?.adapter = adapter
        pagerAdapter = adapter
        super.onViewCreated(view, savedInstanceState)
        dataHost.getObservable(tags[0]).observe(this, adapter)
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        val data = notification.takeIf { it.isOnNext }?.value?.data
        data?.let { _data ->
            header?.text = getString(R.string.welcome_user_format_string, _data.name?.trim()?.split(" ".toRegex())!![0])
            subHeader?.visibility = View.VISIBLE
            val inflater = LayoutInflater.from(context)
            pagerIndicator?.removeAllViews()
            _data.cards?.forEachIndexed { index, _ ->
                val view = inflater.inflate(R.layout.home_card_pager_indicator, pagerIndicator, false)
                view.isSelected = cardsPager?.currentItem ?: 0 == index
                pagerIndicator?.addView(view)
            }

            cardsPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    (parentFragment as KHomeFragment).refreshingEnabled = state == ViewPager.SCROLL_STATE_IDLE
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

                override fun onPageSelected(position: Int) {
                    for (i in 0..(pagerIndicator?.childCount ?: 0)) {
                        pagerIndicator?.getChildAt(i)?.isSelected =
                                (i == position)
                    }
                }
            })
        }
    }

    inner class CardsAdapter : FragmentStatePagerAdapter(childFragmentManager), Observer<Notification<JsonObject>> {

        private var cards: JsonArray? = null

        override fun getItem(position: Int): Fragment =
                when (cards?.get(position)?.asJsonObject.itemType) {
                    TeambrellaModel.FEED_ITEM_PAY_TO_JOIN -> createCardFragment(PayToJoinCardFragment::class.java, position
                            , cards?.get(position)?.asJsonObject?.itemType ?: 0
                            , tags)

                    TeambrellaModel.FEED_ITEM_UPDATE_PROFILE -> createCardFragment(UpdateProfileCardFragment::class.java, position
                            , cards?.get(position)?.asJsonObject?.itemType ?: 0
                            , tags)

                    else -> createCardFragment(ClaimCardFragment::class.java, position
                            , cards?.get(position)?.asJsonObject?.itemType ?: 0
                            , tags)
                }


        override fun getCount() = cards?.size() ?: 0
        override fun getItemPosition(fragment: Any): Int {
            if (fragment is CardFragment) {
                return if (fragment.type == cards?.get(fragment.position)?.asJsonObject?.itemType) PagerAdapter.POSITION_UNCHANGED else
                    PagerAdapter.POSITION_NONE
            }
            return PagerAdapter.POSITION_NONE
        }

        override fun onChanged(notification: Notification<JsonObject>?) {
            if (notification?.isOnNext == true) {
                cards = notification.value?.data?.cards
                notifyDataSetChanged()
            }
        }
    }
}

private const val EXTRA_POSITION = "position"
private const val EXTRA_TYPE = "type"

private fun <T : CardFragment> createCardFragment(clazz: Class<T>, position: Int, type: Int, tags: Array<String>) = createDataFragment(tags, clazz).apply {
    arguments?.putInt(EXTRA_POSITION, position)
    arguments?.putInt(EXTRA_TYPE, type)
}


abstract class CardFragment : ADataFragment<IMainDataHost>() {
    val position: Int by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getInt(EXTRA_POSITION, 0) ?: 0
    }
    val type: Int by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getInt(EXTRA_TYPE, 0) ?: 0
    }
}

class PayToJoinCardFragment : CardFragment() {

    private val titleView: TextView? by ViewHolder(R.id.title)
    private val textView: TextView? by ViewHolder(R.id.text)
    private val subtitle: TextView? by ViewHolder(R.id.subtitle)
    private val icon: ImageView? by ViewHolder(R.id.icon)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.home_cards_action, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener { _ ->
            dataHost.showWallet()
        }
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        notification.takeIf { it.isOnNext }?.value?.data?.cards?.get(position)?.asJsonObject?.let {
            titleView?.text = it.chatTitle
            textView?.text = it.text
            subtitle?.text = it.subTitle
            icon?.setImage(imageLoader.getImageUrl(it.smallPhotoOrAvatar), R.dimen.rounded_corners_3dp)
        }
    }
}


class UpdateProfileCardFragment : CardFragment() {

    private val titleView: TextView? by ViewHolder(R.id.title)
    private val textView: TextView? by ViewHolder(R.id.text)
    private val subtitle: TextView? by ViewHolder(R.id.subtitle)
    private val icon: ImageView? by ViewHolder(R.id.icon)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.home_cards_action, container, false)


    override fun onDataUpdated(notification: Notification<JsonObject>) {
        notification.takeIf { it.isOnNext }?.value?.data?.cards?.get(position)?.asJsonObject?.let {
            titleView?.text = it.chatTitle
            textView?.text = it.text
            subtitle?.text = it.subTitle
            icon?.setImage(imageLoader.getImageUrl(it.smallPhotoOrAvatar), R.dimen.rounded_corners_3dp)


            view?.setOnClickListener { _ ->
                startActivity(ChatActivity.getTeammateChat(context, dataHost.teamId
                        , it.itemUserId
                        , it.itemUserName
                        , null
                        , it.topicId
                        , dataHost.teamAccessLevel))
            }
        }
    }
}


class ClaimCardFragment : CardFragment() {

    private val icon: ImageView? by ViewHolder(R.id.icon)
    private val teammatePicture: ImageView? by ViewHolder(R.id.teammate_picture)
    private val unread: TextView? by ViewHolder(R.id.unread)
    private val amountWidget: TextView? by ViewHolder(R.id.amount_widget)
    private val teamVote: TextView? by ViewHolder(R.id.team_vote)
    private val title: TextView? by ViewHolder(R.id.title)
    private val subtitle: TextView? by ViewHolder(R.id.subtitle)
    private val leftTile: TextView? by ViewHolder(R.id.left_title)
    private val votingLabel: TextView? by ViewHolder(R.id.voting_label)
    private val message: TextView? by ViewHolder(R.id.message_text)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.home_card_claim, container, false)


    override fun onDataUpdated(notification: Notification<JsonObject>) {
        val card = notification.takeIf { it.isOnNext }?.value?.data?.cards?.get(position)?.asJsonObject
        card?.let { _card ->
            when (_card.itemType) {
                TeambrellaModel.FEED_ITEM_TEAMMATE -> {
                    leftTile?.setText(R.string.coverage)
                    icon?.setAvatar(imageLoader.getImageUrl(_card.smallPhotoOrAvatar))
                    title?.text = _card.itemUserName
                    teamVote?.text = String.format(Locale.US, "%.1f", _card.teamVote ?: 0f)
                    subtitle?.text = getString(R.string.object_format_string, _card.modelOrName, _card.year)
                    teammatePicture?.visibility = View.GONE
                    subtitle?.layoutParams = (subtitle?.layoutParams as ConstraintLayout.LayoutParams).apply {
                        marginStart = 0
                    }
                }
                TeambrellaModel.FEED_ITEM_CLAIM -> {
                    leftTile?.setText(R.string.claimed)
                    icon?.setImage(imageLoader.getImageUrl(_card.smallPhotoOrAvatar), R.dimen.rounded_corners_3dp)
                    title?.text = _card.modelOrName
                    teamVote?.text = Html.fromHtml(getString(R.string.home_team_vote_format_string, Math.round((_card.teamVote
                            ?: 0f) * 100)))
                    subtitle?.text = _card.itemUserName
                    teammatePicture?.visibility = View.VISIBLE
                    teammatePicture?.setAvatar(imageLoader.getImageUrl(_card.itemUserAvatar))
                    subtitle?.layoutParams = (subtitle?.layoutParams as ConstraintLayout.LayoutParams).apply {
                        marginStart = resources.getDimensionPixelOffset(R.dimen.margin_4)
                    }
                }
            }

            message?.text = Html.fromHtml(_card.text)
            message?.post { message?.maxLines = if ((message?.length() ?: 0) > 64) 2 else 1 }


            unread?.setUnreadCount(_card.unreadCount ?: 0)

            AmountCurrencyUtil.setAmount(amountWidget, _card.amount ?: 0f, dataHost.currency)

            votingLabel?.visibility = if (_card.isVoting == true) View.VISIBLE else View.GONE

            view?.setOnClickListener {
                when (_card.itemType) {
                    TeambrellaModel.FEED_ITEM_CLAIM -> {
                        startActivity(ChatActivity.getClaimChat(context
                                , dataHost.teamId
                                , _card.itemIdInt ?: 0
                                , _card.modelOrName
                                , _card.smallPhotoOrAvatar
                                , _card.topicId
                                , dataHost.teamAccessLevel
                                , _card.itemDate))
                    }
                    else -> {
                        startActivity(ChatActivity.getTeammateChat(context, dataHost.teamId
                                , _card.itemUserId
                                , _card.itemUserName
                                , _card.smallPhotoOrAvatar
                                , _card.topicId
                                , dataHost.teamAccessLevel))
                    }
                }
            }
        }
    }
}
package com.teambrella.android.ui.home

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.ui.IMainDataHost
import com.teambrella.android.ui.base.ADataFragment
import com.teambrella.android.ui.base.AKDataFragment
import com.teambrella.android.ui.chat.ChatActivity
import com.teambrella.android.ui.util.setAvatar
import com.teambrella.android.ui.util.setImage
import com.teambrella.android.ui.util.setUnreadCount
import com.teambrella.android.util.AmountCurrencyUtil
import io.reactivex.Notification
import java.util.*

class HomeCardsFragment : AKDataFragment<IMainDataHost>() {

    private val header: TextView? by ViewHolder(R.id.home_header)
    private val subHeader: TextView? by ViewHolder(R.id.home_sub_header)
    private val cardsPager: ViewPager? by ViewHolder(R.id.cards_pager)
    private var adapter: CardsAdapter? = null
    private val pagerIndicator: LinearLayout? by ViewHolder(R.id.page_indicator)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_home_cards, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardsPager?.pageMargin = 20
    }

    override fun onDataUpdated(notification: Notification<JsonObject>?) {
        val data = notification?.takeIf { it.isOnNext }?.value?.data
        data?.let { _data ->
            header?.text = getString(R.string.welcome_user_format_string, _data.name?.trim()?.split(" ".toRegex())!![0])
            subHeader?.visibility = View.VISIBLE

            if (adapter == null) {

                adapter = CardsAdapter(_data.cards?.size() ?: 0)
                cardsPager?.adapter = adapter

                val inflater = LayoutInflater.from(context)
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
            } else {
                adapter?.notifyDataSetChanged()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    inner class CardsAdapter(val size: Int) : FragmentStatePagerAdapter(childFragmentManager) {
        override fun getItem(position: Int): Fragment = createCardFragment(position, mTags)
        override fun getCount() = size
        override fun getItemPosition(`object`: Any) = PagerAdapter.POSITION_UNCHANGED
    }
}

private const val EXTRA_POSITION = "position"

private fun createCardFragment(position: Int, tags: Array<String>): KCardFragment {
    return KCardFragment().apply {
        arguments = Bundle().apply {
            putInt(EXTRA_POSITION, position)
            putStringArray(ADataFragment.EXTRA_DATA_FRAGMENT_TAG, tags)
        }
    }
}

class KCardFragment : AKDataFragment<IMainDataHost>() {

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
    private var position: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt(EXTRA_POSITION, 0) ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.home_card_claim, container, false)


    override fun onDataUpdated(notification: Notification<JsonObject>?) {
        val card = notification?.takeIf { it.isOnNext }?.value?.data?.cards?.get(position)?.asJsonObject
        card?.let { _card ->

            when (_card.itemType) {
                TeambrellaModel.FEED_ITEM_TEAMMATE -> {
                    leftTile?.setText(R.string.coverage)
                    icon?.setAvatar(imageLoader.getImageUrl(_card.smallPhotoOrAvatar))
                    title?.text = _card.itemUserName
                    teamVote?.text = String.format(Locale.US, "%.1f", _card.teamVote ?: 0f);
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
                            ?: 0f) * 100)));
                    subtitle?.text = _card.itemUserName
                    teammatePicture?.visibility = View.VISIBLE
                    teammatePicture?.setAvatar(imageLoader.getImageUrl(_card.itemUserAvatar))
                    subtitle?.layoutParams = (subtitle?.layoutParams as ConstraintLayout.LayoutParams).apply {
                        marginStart = resources.getDimensionPixelOffset(R.dimen.margin_4)
                    }
                }
            }

            message?.text = Html.fromHtml(_card.text)
            message?.post({ message?.maxLines = if ((message?.length() ?: 0) > 64) 2 else 1 });


            unread?.setUnreadCount(_card.unreadCount ?: 0)

            AmountCurrencyUtil.setAmount(amountWidget, _card.amount ?: 0f, mDataHost.currency);

            votingLabel?.visibility = if (_card.isVoting == true) View.VISIBLE else View.GONE;

            view?.setOnClickListener {
                when (_card.itemType) {
                    TeambrellaModel.FEED_ITEM_CLAIM -> {
                        startActivity(ChatActivity.getClaimChat(context
                                , mDataHost.teamId
                                , _card.itemIdInt ?: 0
                                , _card.modelOrName
                                , _card.smallPhotoOrAvatar
                                , _card.topicId
                                , mDataHost.teamAccessLevel
                                , _card.itemDate))
                    }
                    else -> {
                        startActivity(ChatActivity.getTeammateChat(context, mDataHost.teamId
                                , _card.itemUserId
                                , _card.itemUserName
                                , _card.smallPhotoOrAvatar
                                , _card.topicId
                                , mDataHost.teamAccessLevel))
                    }
                }
            }
        }
    }
}
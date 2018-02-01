package com.teambrella.android.ui.home;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.base.ITeambrellaDaggerActivity;
import com.teambrella.android.ui.base.dagger.IDaggerActivity;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.util.AmountCurrencyUtil;
import com.teambrella.android.util.TeambrellaDateUtils;

import io.reactivex.Notification;

/**
 * Home Cards Fragment.
 */
public class HomeCardsFragment extends ADataFragment<IMainDataHost> {


    private TextView mHeader;
    private ViewPager mCardsPager;
    private CardAdapter mAdapter;
    private LinearLayout mPagerIndicator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_cards, container, false);
        mHeader = view.findViewById(R.id.home_header);
        mCardsPager = view.findViewById(R.id.cards_pager);
        mPagerIndicator = view.findViewById(R.id.page_indicator);
        mCardsPager.setPageMargin(20);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IDaggerActivity) {
            ((ITeambrellaDaggerActivity) context).getComponent().inject(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            mHeader.setText(getString(R.string.welcome_user_format_string, data.getString(TeambrellaModel.ATTR_DATA_NAME).trim().split(" ")[0]));
            JsonArray cards = data.getJsonArray(TeambrellaModel.ATTR_DATA_CARDS);
            if (mAdapter == null) {
                mCardsPager.setAdapter(mAdapter = new CardAdapter(cards));
                LayoutInflater inflater = LayoutInflater.from(getContext());
                for (int i = 0; i < cards.size(); i++) {
                    View view = inflater.inflate(R.layout.home_card_pager_indicator, mPagerIndicator, false);
                    view.setSelected(mCardsPager.getCurrentItem() == i);
                    mPagerIndicator.addView(view);
                }

                mCardsPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        for (int i = 0; i < mPagerIndicator.getChildCount(); i++) {
                            mPagerIndicator.getChildAt(i).setSelected(position == i);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                        ((HomeFragment) getParentFragment()).setRefreshingEnable(state == ViewPager.SCROLL_STATE_IDLE);
                    }
                });
            } else {
                mAdapter.setData(cards);
            }
        }
    }


    public class CardAdapter extends FragmentStatePagerAdapter {

        private JsonArray mCards;

        CardAdapter(JsonArray cards) {
            super(HomeCardsFragment.this.getChildFragmentManager());
            mCards = cards;
        }

        @Override
        public Fragment getItem(int position) {
            return CardsFragment.getInstance(position, mTags);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        public void setData(JsonArray cards) {
            mCards = cards;
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_UNCHANGED;
        }
    }


    public static final class CardsFragment extends ADataFragment<IMainDataHost> {

        private static final String EXTRA_POSITION = "position";
        ImageView icon;
        TextView message;
        TextView unread;
        TextView amountWidget;
        TextView teamVote;
        TextView title;
        TextView subtitle;
        TextView leftTitle;
        int mPosition;

        public static CardsFragment getInstance(int position, String[] tags) {
            CardsFragment fragment = new CardsFragment();
            Bundle args = new Bundle();
            args.putInt(EXTRA_POSITION, position);
            args.putStringArray(EXTRA_DATA_FRAGMENT_TAG, tags);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPosition = getArguments().getInt(EXTRA_POSITION);
        }

        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.home_card_claim, container, false);
            icon = view.findViewById(R.id.icon);
            message = view.findViewById(R.id.message_text);
            unread = view.findViewById(R.id.unread);
            amountWidget = view.findViewById(R.id.amount_widget);
            teamVote = view.findViewById(R.id.team_vote);
            title = view.findViewById(R.id.title);
            subtitle = view.findViewById(R.id.subtitle);
            leftTitle = view.findViewById(R.id.left_title);
            return view;
        }

        @Override
        protected void onDataUpdated(Notification<JsonObject> notification) {

            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonArray cards = data.getJsonArray(TeambrellaModel.ATTR_DATA_CARDS);
            JsonWrapper card = new JsonWrapper(cards.get(mPosition).getAsJsonObject());

            int itemType = card.getInt(TeambrellaModel.ATTR_DATA_ITEM_TYPE);

            leftTitle.setText(itemType == TeambrellaModel.FEED_ITEM_TEAMMATE ? R.string.limit : R.string.claimed);


            RequestBuilder requestCreator = GlideApp.with(this).load(getImageLoader().getImageUrl(card.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO_OR_AVATAR)));

            if (itemType == TeambrellaModel.FEED_ITEM_TEAMMATE
                    || itemType == TeambrellaModel.FEED_ITEM_TEAM_CHAT) {
                requestCreator = requestCreator.apply(RequestOptions.circleCropTransform());
            } else {
                Resources resources = getContext().getResources();
                requestCreator = requestCreator.apply(new RequestOptions()
                        .transforms(new CenterCrop(), new RoundedCorners(resources.getDimensionPixelOffset(R.dimen.rounded_corners_2dp))));
            }

            requestCreator.into(icon);

            message.setText(Html.fromHtml(card.getString(TeambrellaModel.ATTR_DATA_TEXT, "")));
            message.post(() -> message.setMaxLines(message.length() > 64 ? 2 : 1));

            int unreadCount = card.getInt(TeambrellaModel.ATTR_DATA_UNREAD_COUNT);

            unread.setVisibility(unreadCount > 0 ? View.VISIBLE : View.INVISIBLE);
            unread.setText(card.getString(TeambrellaModel.ATTR_DATA_UNREAD_COUNT));

            AmountCurrencyUtil.setAmount(amountWidget, card.getFloat(TeambrellaModel.ATTR_DATA_AMOUNT), mDataHost.getCurrency());
            if (itemType == TeambrellaModel.FEED_ITEM_TEAMMATE) {
                teamVote.setText(getString(R.string.risk_format_string, card.getFloat(TeambrellaModel.ATTR_DATA_TEAM_VOTE)));
            } else {
                teamVote.setText(Html.fromHtml(getString(R.string.home_team_vote_format_string, Math.round(card.getFloat(TeambrellaModel.ATTR_DATA_TEAM_VOTE) * 100))));
            }


            switch (itemType) {
                case TeambrellaModel.FEED_ITEM_CLAIM:
                    title.setText(getString(R.string.claim_title_format_string, card.getInt(TeambrellaModel.ATTR_DATA_ITEM_ID)));
                    break;
                case TeambrellaModel.FEED_ITEM_TEAM_CHAT:
                    title.setText(card.getString(TeambrellaModel.ATTR_DATA_CHAT_TITLE));
                    break;
                case TeambrellaModel.FEED_ITEM_TEAMMATE:
                    title.setText(R.string.application);
                    break;
            }

            subtitle.setText(TeambrellaDateUtils.getDatePresentation(getContext()
                    , TeambrellaDateUtils.TEAMBRELLA_UI_DATE
                    , card.getString(TeambrellaModel.ATTR_DATA_ITEM_DATE)));

            View view = getView();
            if (view != null) {
                view.setOnClickListener(v -> {
                    Context context = getContext();
                    IMainDataHost dataHost = (IMainDataHost) getContext();
                    switch (itemType) {
                        case TeambrellaModel.FEED_ITEM_CLAIM:
                            dataHost.launchActivity(ChatActivity.getClaimChat(context
                                    , mDataHost.getTeamId()
                                    , card.getInt(TeambrellaModel.ATTR_DATA_ITEM_ID)
                                    , card.getString(TeambrellaModel.ATTR_DATA_MODEL_OR_NAME)
                                    , card.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO_OR_AVATAR)
                                    , card.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID)
                                    , mDataHost.getTeamAccessLevel()));
                            break;
                        case TeambrellaModel.FEED_ITEM_TEAM_CHAT:
                            dataHost.launchActivity(ChatActivity.getFeedChat(context
                                    , card.getString(TeambrellaModel.ATTR_DATA_CHAT_TITLE)
                                    , card.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID)
                                    , dataHost.getTeamId()
                                    , mDataHost.getTeamAccessLevel()));
                            break;
                        default:
                            dataHost.launchActivity(ChatActivity.getTeammateChat(context, mDataHost.getTeamId()
                                    , card.getString(TeambrellaModel.ATTR_DATA_ITEM_USER_ID)
                                    , null
                                    , card.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO_OR_AVATAR)
                                    , card.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID)
                                    , mDataHost.getTeamAccessLevel()));
                            break;
                    }
                });
            }
        }
    }

}

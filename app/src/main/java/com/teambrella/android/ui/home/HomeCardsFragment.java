package com.teambrella.android.ui.home;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.util.AmountCurrencyUtil;
import com.teambrella.android.util.TeambrellaDateUtils;

import io.reactivex.Notification;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.MaskTransformation;

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
        mCardsPager.setPageMargin(40);
        return view;
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


    public final class CardAdapter extends FragmentStatePagerAdapter {

        private JsonArray mCards;

        CardAdapter(JsonArray cards) {
            super(HomeCardsFragment.this.getChildFragmentManager());
            mCards = cards;
        }

        @Override
        public Fragment getItem(int position) {
            return CardsFragment.getInstance(mCards.get(position).toString(), mDataHost.getTeamId(), mDataHost.getCurrency());
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
            return POSITION_NONE;
        }
    }


    public static final class CardsFragment extends Fragment {

        private static final String EXTRA_DATA = "data";
        private static final String EXTRA_CURRENCY = "currency";
        private static final String EXTRA_TEAM_ID = "team_id";

        private JsonWrapper mCard;
        private int mTeamId;
        private String mCurrency;

        public static CardsFragment getInstance(String data, int teamId, String currency) {
            CardsFragment fragment = new CardsFragment();
            Bundle args = new Bundle();
            args.putString(EXTRA_DATA, data);
            args.putString(EXTRA_CURRENCY, currency);
            args.putInt(EXTRA_TEAM_ID, teamId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Gson gson = new GsonBuilder().create();
            mCard = new JsonWrapper(gson.fromJson(getArguments().getString(EXTRA_DATA), JsonObject.class));
            mTeamId = getArguments().getInt(EXTRA_TEAM_ID);
            mCurrency = getArguments().getString(EXTRA_CURRENCY);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            Picasso picasso = TeambrellaImageLoader.getInstance(getContext()).getPicasso();

            View view = inflater.inflate(R.layout.home_card_claim, container, false);

            ImageView icon = view.findViewById(R.id.icon);
            TextView message = view.findViewById(R.id.message_text);
            TextView unread = view.findViewById(R.id.unread);
            TextView amountWidget = view.findViewById(R.id.amount_widget);
            TextView teamVote = view.findViewById(R.id.team_vote);
            TextView title = view.findViewById(R.id.title);
            TextView subtitle = view.findViewById(R.id.subtitle);
            TextView leftTitle = view.findViewById(R.id.left_title);


            int itemType = mCard.getInt(TeambrellaModel.ATTR_DATA_ITEM_TYPE);


            leftTitle.setText(itemType == TeambrellaModel.FEED_ITEM_TEAMMATE ? R.string.limit : R.string.claimed);


            RequestCreator requestCreator = picasso.load(TeambrellaModel.getImage(TeambrellaServer.BASE_URL, mCard.getObject(), TeambrellaModel.ATTR_DATA_SMALL_PHOTO_OR_AVATAR));

            if (itemType == TeambrellaModel.FEED_ITEM_TEAMMATE
                    || itemType == TeambrellaModel.FEED_ITEM_TEAM_CHAT) {
                requestCreator.transform(new CropCircleTransformation());
            } else {
                Resources resources = getContext().getResources();
                requestCreator.resize(resources.getDimensionPixelSize(R.dimen.image_size_48), resources.getDimensionPixelSize(R.dimen.image_size_48))
                        .centerCrop().transform(new MaskTransformation(getContext(), R.drawable.teammate_object_mask));
            }

            requestCreator.into(icon);

            message.setText(Html.fromHtml(mCard.getString(TeambrellaModel.ATTR_DATA_TEXT)));

            int unreadCount = mCard.getInt(TeambrellaModel.ATTR_DATA_UNREAD_COUNT);

            unread.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
            unread.setText(mCard.getString(TeambrellaModel.ATTR_DATA_UNREAD_COUNT));

            AmountCurrencyUtil.setAmount(amountWidget, mCard.getFloat(TeambrellaModel.ATTR_DATA_AMOUNT), getArguments().getString(EXTRA_CURRENCY));
            if (itemType == TeambrellaModel.FEED_ITEM_TEAMMATE) {
                teamVote.setText(getString(R.string.risk_format_string, mCard.getFloat(TeambrellaModel.ATTR_DATA_TEAM_VOTE)));
            } else {
                teamVote.setText(Html.fromHtml(getString(R.string.home_team_vote_format_string, Math.round(mCard.getFloat(TeambrellaModel.ATTR_DATA_TEAM_VOTE) * 100))));
            }


            switch (itemType) {
                case TeambrellaModel.FEED_ITEM_CLAIM:
                    title.setText(getString(R.string.claim_title_format_string, mCard.getInt(TeambrellaModel.ATTR_DATA_ITEM_ID)));
                    break;
                case TeambrellaModel.FEED_ITEM_TEAM_CHAT:
                    title.setText(mCard.getString(TeambrellaModel.ATTR_DATA_CHAT_TITLE));
                    break;
                case TeambrellaModel.FEED_ITEM_TEAMMATE:
                    title.setText(R.string.application);
                    break;
            }

            subtitle.setText(TeambrellaDateUtils.getDatePresentation(getContext()
                    , TeambrellaDateUtils.TEAMBRELLA_UI_DATE
                    , mCard.getString(TeambrellaModel.ATTR_DATA_ITEM_DATE)));

            view.setOnClickListener(v -> {
                Context context = getContext();
                switch (itemType) {
                    case TeambrellaModel.FEED_ITEM_CLAIM:
                        ChatActivity.startClaimChat(context
                                , mTeamId
                                , mCard.getInt(TeambrellaModel.ATTR_DATA_ITEM_ID)
                                , mCard.getString(TeambrellaModel.ATTR_DATA_MODEL_OR_NAME)
                                , TeambrellaImageLoader.getImageUri(mCard.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO_OR_AVATAR))
                                , mCard.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID)
                                , mCurrency);
                        break;
                    case TeambrellaModel.FEED_ITEM_TEAM_CHAT:
                        ChatActivity.startFeedChat(context
                                , mCard.getString(TeambrellaModel.ATTR_DATA_CHAT_TITLE)
                                , mCard.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID));
                        break;
                    default:
                        ChatActivity.startTeammateChat(context, mTeamId
                                , mCard.getString(TeambrellaModel.ATTR_DATA_ITEM_USER_ID)
                                , null
                                , TeambrellaImageLoader.getImageUri(mCard.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO_OR_AVATAR))
                                , mCard.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID)
                                , mCurrency);
                        break;
                }
            });

            return view;

        }
    }

}

package com.teambrella.android.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
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
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.widget.AmountWidget;

import java.text.SimpleDateFormat;
import java.util.Locale;

import io.reactivex.Notification;

/**
 * Home Cards Fragment.
 */
public class HomeCardsFragment extends ADataFragment<IDataHost> {


    private TextView mHeader;
    private ViewPager mCardsPager;
    private CardAdapter mAdapter;
    private LinearLayout mPagerIndicator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_cards, container, false);
        mHeader = (TextView) view.findViewById(R.id.home_header);
        mCardsPager = (ViewPager) view.findViewById(R.id.cards_pager);
        mPagerIndicator = (LinearLayout) view.findViewById(R.id.page_indicator);
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

            if (mAdapter == null) {
                JsonArray cards = data.getJsonArray(TeambrellaModel.ATTR_DATA_CARDS);
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
            }
        }
    }


    public final class CardAdapter extends FragmentPagerAdapter {

        private JsonArray mCards;

        public CardAdapter(JsonArray cards) {
            super(HomeCardsFragment.this.getFragmentManager());
            mCards = cards;
        }

        @Override
        public Fragment getItem(int position) {
            return CardsFragment.getInstance(mCards.get(position).toString());
        }

        @Override
        public int getCount() {
            return mCards.size();
        }
    }


    public static final class CardsFragment extends Fragment {

        private static String EXTRA_DATA = "data";
        private static SimpleDateFormat mDateFormat = new SimpleDateFormat("d LLLL yyyy \'at\' HH:mm ", Locale.ENGLISH);
        private static SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        private JsonWrapper mCard;

        public static CardsFragment getInstance(String data) {
            CardsFragment fragment = new CardsFragment();
            Bundle args = new Bundle();
            args.putString(EXTRA_DATA, data);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Gson gson = new GsonBuilder().create();
            mCard = new JsonWrapper(gson.fromJson(getArguments().getString(EXTRA_DATA), JsonObject.class));
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            Picasso picasso = TeambrellaImageLoader.getInstance(getContext()).getPicasso();

            View view = inflater.inflate(R.layout.home_card_claim, container, false);


            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            TextView message = (TextView) view.findViewById(R.id.message_text);
            TextView unread = (TextView) view.findViewById(R.id.unread);
            AmountWidget amountWidget = (AmountWidget) view.findViewById(R.id.amount_widget);
            TextView teamVote = (TextView) view.findViewById(R.id.team_vote);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
            TextView leftTitle = (TextView) view.findViewById(R.id.left_title);


            int itemType = mCard.getInt(TeambrellaModel.ATTR_DATA_ITEM_TYPE);


            leftTitle.setText(itemType == TeambrellaModel.FEED_ITEM_TEAMMATE ? R.string.limit : R.string.claimed);


            picasso.load(TeambrellaModel.getImage(TeambrellaServer.AUTHORITY, mCard.getObject(), TeambrellaModel.ATTR_DATA_SMALL_PHOTO_OR_AVATAR))
                    .into(icon);
            message.setText(Html.fromHtml(mCard.getString(TeambrellaModel.ATTR_DATA_TEXT)));

            int unreadCount = mCard.getInt(TeambrellaModel.ATTR_DATA_UNREAD_COUNT);

            unread.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
            unread.setText(mCard.getString(TeambrellaModel.ATTR_DATA_UNREAD_COUNT));

            amountWidget.setAmount(mCard.getFloat(TeambrellaModel.ATTR_DATA_AMOUNT));
            if (itemType == TeambrellaModel.FEED_ITEM_TEAMMATE) {
                teamVote.setText(getString(R.string.risk_format_string, mCard.getFloat(TeambrellaModel.ATTR_DATA_TEAM_VOTE)));
            } else {
                teamVote.setText(Html.fromHtml(getString(R.string.home_team_vote_format_string, Math.round(mCard.getFloat(TeambrellaModel.ATTR_DATA_TEAM_VOTE) * 100))));
            }
            title.setText(itemType == TeambrellaModel.FEED_ITEM_TEAMMATE ? getString(R.string.application)
                    : getString(R.string.claim_title_format_string, mCard.getInt(TeambrellaModel.ATTR_DATA_ITEM_ID)));

            try {
                subtitle.setText(mDateFormat.format(mSDF.parse(mCard.getString(TeambrellaModel.ATTR_DATA_ITEM_DATE))));
            } catch (Exception e) {

            }


//            view.setOnClickListener(v -> {
//                switch (itemType) {
//                    case TeambrellaModel.FEED_ITEM_CLAIM:
//                        getContext().startActivity(ClaimActivity.getLaunchIntent(getContext()
//                                , TeambrellaUris.getClaimUri(mCard.getInt(TeambrellaModel.ATTR_DATA_ITEM_ID))
//                                , mCard.getString(TeambrellaModel.ATTR_DATA_MODEL_OR_NAME)
//                                , ));
//                        break;
//                    default:
//                        getContext().startActivity(TeammateActivity.getIntent(getContext()
//                                , TeambrellaUris.getTeammateUri(mTeamId, mCard.getString(TeambrellaModel.ATTR_DATA_ITEM_USER_ID)), null, null));
//                        break;
//                }
//            });

            return view;

        }
    }

}

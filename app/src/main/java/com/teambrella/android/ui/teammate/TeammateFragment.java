package com.teambrella.android.ui.teammate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets;
import com.teambrella.android.ui.widget.VoterBar;
import com.teambrella.android.util.AmountCurrencyUtil;
import com.teambrella.android.util.ConnectivityUtils;
import com.teambrella.android.util.TeambrellaDateUtils;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Teammate fragment.
 */
public class TeammateFragment extends ADataProgressFragment<ITeammateActivity> implements VoterBar.VoterBarListener {

    private static final String OBJECT_FRAGMENT_TAG = "object_tag";
    private static final String VOTING_TAG = "voting_tag";
    private static final String VOTING_STATS_FRAGMENT_TAG = "voting_stats_tag";


    private ImageView mUserPicture;
    private ImageView mSmallImagePicture;
    private ImageView mTeammateIcon;
    private TeambrellaAvatarsWidgets mAvatars;
    private NestedScrollView mScrollView;

    private TextView mUserName;
    private TextView mMessage;
    private TextView mUnread;
    private TextView mWhen;


    private View mCoverMeSection;
    private View mCoverThemSection;
    private View mWouldCoverPanel;
    private TextView mCoverMe;

    private TextView mCoverThem;
    private TextView mWouldCoverMe;
    private TextView mWouldCoverThem;
    private TextView mCoversMeTitle;
    private TextView mCoversThemTitle;
    private TextView mWouldCoverThemTitle;
    private TextView mCityView;


    private String mUserId;
    private int mTeamId;
    private String mTopicId;
    private String mCurrency;
    private int mTeamAccessLevel;

    private float mHeCoversMeIf02;
    private float mHeCoversMeIf1;
    private float mHeCoversMeIf499;
    private float mMyRisk;
    private int mGender;
    private int mPosterCount;


    private boolean mIsShown;

    private float mWouldCoverMeValue;
    private float mWouldCoverThemValue;
    private boolean mCoverageUpdateStarted;

    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teammate, container, false);
        mUserPicture = view.findViewById(R.id.user_picture);
        mSmallImagePicture = view.findViewById(R.id.small_teammate_icon);
        mUserName = view.findViewById(R.id.user_name);
        mCoverMe = view.findViewById(R.id.cover_me);
        mCoverThem = view.findViewById(R.id.cover_them);
        mTeammateIcon = view.findViewById(R.id.teammate_icon);
        mAvatars = view.findViewById(R.id.avatars);
        mMessage = view.findViewById(R.id.message);
        mUnread = view.findViewById(R.id.unread);
        mCoverMeSection = view.findViewById(R.id.cover_me_section);
        mCoverThemSection = view.findViewById(R.id.cover_them_section);
        mWhen = view.findViewById(R.id.when);
        mWouldCoverPanel = view.findViewById(R.id.would_cover_panel);
        mScrollView = view.findViewById(R.id.scroll_view);
        mWouldCoverMe = view.findViewById(R.id.would_cover_me);
        mWouldCoverThem = view.findViewById(R.id.would_cover_them);
        mCoversMeTitle = view.findViewById(R.id.covers_me_title);
        mCoversThemTitle = view.findViewById(R.id.covers_them_title);
        mWouldCoverThemTitle = view.findViewById(R.id.would_cover_them_title);
        mCityView = view.findViewById(R.id.city);
        mDataHost.load(mTags[0]);
        setContentShown(false);
        view.findViewById(R.id.discussion_foreground).setOnClickListener(v ->
                mDataHost.launchActivity(ChatActivity.getTeammateChat(getContext(), mTeamId, mUserId, null, null, mTopicId, mTeamAccessLevel)));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentManager fragmentManager = getChildFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentByTag(OBJECT_FRAGMENT_TAG) == null) {
            transaction.add(R.id.object_info_container, ADataFragment.getInstance(mTags, TeammateObjectFragment.class), OBJECT_FRAGMENT_TAG);
        }

        if (fragmentManager.findFragmentByTag(VOTING_STATS_FRAGMENT_TAG) == null) {
            transaction.add(R.id.voting_statistics_container, com.teambrella.android.ui.teammate.KTeammate.getFragmentInstance(mTags), VOTING_STATS_FRAGMENT_TAG);
        }


        if (fragmentManager.findFragmentByTag(VOTING_TAG) == null) {
            transaction.add(R.id.voting_container, ADataFragment.getInstance(mTags, TeammateVotingFragment.class), VOTING_TAG);
        }

        if (!transaction.isEmpty()) {
            transaction.commit();
        }
        mIsShown = false;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            Observable<JsonWrapper> responseObservable = Observable.just(notification.getValue())
                    .map(JsonWrapper::new);

            //noinspection unused
            final Integer matchId = responseObservable.map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_STATUS))
                    .map(jsonWrapper -> Uri.parse(jsonWrapper.getString(TeambrellaModel.ATTR_STATUS_URI)))
                    .map(TeambrellaUris.sUriMatcher::match)
                    .blockingFirst();

            Observable<JsonWrapper> dataObservable =
                    responseObservable.map(item -> item.getObject(TeambrellaModel.ATTR_DATA));

            Observable<JsonWrapper> basicObservable =
                    dataObservable.map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC));


            dataObservable.map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA_ONE_TEAM))
                    .doOnNext(jsonWrapper -> mCurrency = jsonWrapper.getString(TeambrellaModel.ATTR_DATA_CURRENCY, mCurrency))
                    .doOnNext(jsonWrapper -> mTeamAccessLevel = jsonWrapper.getInt(TeambrellaModel.ATTR_DATA_TEAM_ACCESS_LEVEL, mTeamAccessLevel))
                    .onErrorReturnItem(new JsonWrapper(new JsonObject())).blockingFirst();


            basicObservable.doOnNext(basic -> AmountCurrencyUtil.setAmount(mCoverMe, mWouldCoverMeValue > 0 ? mWouldCoverMeValue : basic.getFloat(TeambrellaModel.ATTR_DATA_COVER_ME), mCurrency))
                    .doOnNext(basic -> AmountCurrencyUtil.setAmount(mCoverThem, mWouldCoverThemValue > 0 ? mWouldCoverThemValue : basic.getFloat(TeambrellaModel.ATTR_DATA_COVER_THEM), mCurrency))
                    .doOnNext(basic -> AmountCurrencyUtil.setAmount(mWouldCoverMe, mWouldCoverMeValue > 0 ? mWouldCoverMeValue : basic.getFloat(TeambrellaModel.ATTR_DATA_COVER_ME), mCurrency))
                    .doOnNext(basic -> AmountCurrencyUtil.setAmount(mWouldCoverThem, mWouldCoverThemValue > 0 ? mWouldCoverThemValue : basic.getFloat(TeambrellaModel.ATTR_DATA_COVER_THEM), mCurrency))
                    .doOnNext(basic -> mUserName.setText(basic.getString(TeambrellaModel.ATTR_DATA_NAME)))
                    .doOnNext(basic -> mTeamId = basic.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID))
                    .doOnNext(basic -> mUserId = basic.getString(TeambrellaModel.ATTR_DATA_USER_ID))
                    .doOnNext(basic -> mCityView.setText(basic.getString(TeambrellaModel.ATTR_DATA_CITY)))
                    .doOnNext(basic -> mGender = basic.getInt(TeambrellaModel.ATTR_DATA_GENDER, mGender))
                    .subscribe(jsonWrapper -> {
                    }, throwable -> {
                    }, () -> {
                    });
            RequestManager manager = GlideApp.with(this);
            JsonWrapper basic = basicObservable.onErrorReturn(throwable ->
                    new JsonWrapper(new JsonObject())).blockingFirst();
            if (basic != null) {
                GlideUrl url = getImageLoader().getImageUrl(basic.getString(TeambrellaModel.ATTR_DATA_AVATAR));
                if (url != null) {
                    manager.load(url).apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.picture_background_round_4dp)).into(mUserPicture);
                    manager.load(url).apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.picture_background_round_4dp)).into(mTeammateIcon);
                    manager.load(url).apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.picture_background_round_4dp)).into(mSmallImagePicture);
                }
            }

            dataObservable.map(data -> data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING))
                    .doOnNext(voting -> {
                        View view = getView();
                        if (view != null) {
                            view.findViewById(R.id.voting_container).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.object_info_container).setBackgroundResource(R.drawable.block);
                            mCoversMeTitle.setText(R.string.would_cover_me);
                            switch (mGender) {
                                case TeambrellaModel.Gender.MALE:
                                    mCoversThemTitle.setText(R.string.would_cover_him);
                                    mWouldCoverThemTitle.setText(R.string.would_cover_him);
                                    break;
                                case TeambrellaModel.Gender.FEMALE:
                                    mCoversThemTitle.setText(R.string.would_cover_her);
                                    mWouldCoverThemTitle.setText(R.string.would_cover_her);
                                    break;
                                default:
                                    mCoversThemTitle.setText(R.string.would_cover_them);
                                    mWouldCoverThemTitle.setText(R.string.would_cover_them);
                            }
                        }
                    })
                    .doOnError(throwable -> {
                        switch (mGender) {
                            case TeambrellaModel.Gender.MALE:
                                mCoversThemTitle.setText(R.string.cover_him);
                                break;
                            case TeambrellaModel.Gender.FEMALE:
                                mCoversThemTitle.setText(R.string.cover_her);
                                break;
                            default:
                                mCoversThemTitle.setText(R.string.cover_them);
                        }
                    })
                    .onErrorReturnItem(new JsonWrapper(null)).blockingFirst();

            Observable<JsonWrapper> discussionsObservable = dataObservable.map(data -> data.getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION));

            discussionsObservable.doOnNext(discussion -> mUnread.setText(discussion.getString(TeambrellaModel.ATTR_DATA_UNREAD_COUNT)))
                    .doOnNext(discussion -> mUnread.setVisibility(discussion.getInt(TeambrellaModel.ATTR_DATA_UNREAD_COUNT) > 0 ? View.VISIBLE : View.INVISIBLE))
                    .doOnNext(discussion -> mMessage.setText(Html.fromHtml(discussion.getString(TeambrellaModel.ATTR_DATA_ORIGINAL_POST_TEXT, null))))
                    .doOnNext(discussion -> mTopicId = discussion.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID))
                    .doOnNext(discussion -> mWhen.setText(TeambrellaDateUtils.getRelativeTime(-discussion.getLong(TeambrellaModel.ATTR_DATA_SINCE_LAST_POST_MINUTES, 0))))
                    .doOnNext(discussion -> mPosterCount = discussion.getInt(TeambrellaModel.ATTR_DATA_POSTER_COUNT))
                    .onErrorReturnItem(new JsonWrapper(null)).blockingFirst();


            discussionsObservable.flatMap(discussion -> Observable.fromIterable(discussion.getJsonArray(TeambrellaModel.ATTR_DATA_TOP_POSTER_AVATARS)))
                    .map(JsonElement::getAsString)
                    .toList()
                    .subscribe(uris -> mAvatars.setAvatars(getImageLoader(), uris, mPosterCount), e -> {
                    });

            mCoverThemSection.setVisibility(mDataHost.isItMe() ? View.GONE : View.VISIBLE);
            mCoverMeSection.setVisibility(mDataHost.isItMe() ? View.GONE : View.VISIBLE);

            dataObservable.map(data -> data.getObject(TeambrellaModel.ATTR_DATA_ONE_RISK_SCALE))
                    .doOnNext(riskScale -> {
                        mHeCoversMeIf1 = riskScale.getFloat(TeambrellaModel.ATTR_DATA_HE_COVERS_ME_IF1, mHeCoversMeIf1);
                        mHeCoversMeIf02 = riskScale.getFloat(TeambrellaModel.ATTR_DATA_HE_COVERS_ME02, mHeCoversMeIf02);
                        mHeCoversMeIf499 = riskScale.getFloat(TeambrellaModel.ATTR_DATA_HE_COVERS_ME_IF499, mHeCoversMeIf499);
                        mMyRisk = riskScale.getFloat(TeambrellaModel.ATTR_DATA_MY_RISK, mMyRisk);
                    }).onErrorReturnItem(new JsonWrapper(null)).blockingFirst();


            setContentShown(true);
            mIsShown = true;
        } else {
            setContentShown(true, !mIsShown);
            //noinspection ConstantConditions
            mDataHost.showSnackBar(ConnectivityUtils.isNetworkAvailable(getContext()) ? R.string.something_went_wrong_error : R.string.no_internet_connection);
        }
    }


    @Override
    public void onVoteChanged(float vote, boolean fromUser) {
        Rect scrollBounds = new Rect();
        mScrollView.getHitRect(scrollBounds);
        if (!mUserPicture.getLocalVisibleRect(scrollBounds) && mWouldCoverPanel.getVisibility() == View.INVISIBLE
                && fromUser && !mDataHost.isItMe()) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mWouldCoverPanel, "translationY", -(float) mWouldCoverPanel.getHeight(), 0f).setDuration(300);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mWouldCoverPanel.setVisibility(View.VISIBLE);
                }
            });
            animator.start();
        }

        float value = (float) Math.pow(25, vote) / 5;

        if (value >= 0.2f && value <= 1f) {
            mWouldCoverMeValue = getLinearPoint(value, 0.2f, mHeCoversMeIf02, 1f, mHeCoversMeIf1);
            mWouldCoverThemValue = mWouldCoverMeValue * mMyRisk / value;
        } else {
            mWouldCoverMeValue = getLinearPoint(value, 1f, mHeCoversMeIf1, 4.99f, mHeCoversMeIf499);
            mWouldCoverThemValue = mWouldCoverMeValue * mMyRisk / value;
        }

        if (!mCoverageUpdateStarted) {
            mWouldCoverPanel.post(mCoverageUpdate);
            mCoverageUpdateStarted = true;
        }

        mWouldCoverPanel.removeCallbacks(mHideWouldCoverPanelRunnable);
    }

    @Override
    public void onVoterBarReleased(float vote, boolean fromUser) {
        if (mWouldCoverPanel.getVisibility() == View.VISIBLE) {
            mWouldCoverPanel.postDelayed(mHideWouldCoverPanelRunnable, 1000);
        }
        mWouldCoverPanel.removeCallbacks(mCoverageUpdate);
        mCoverageUpdateStarted = false;
    }


    private static float getLinearPoint(float x, float x1, float y1, float x2, float y2) {
        return ((x - x1) * (y2 - y1)) / (x2 - x1) + y1;
    }

    private Runnable mHideWouldCoverPanelRunnable = () -> {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mWouldCoverPanel, "translationY", 0f, -(float) mWouldCoverPanel.getHeight()).setDuration(300);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mWouldCoverPanel.setVisibility(View.INVISIBLE);
            }
        });

        animator.start();
    };

    private Runnable mCoverageUpdate = new Runnable() {
        @Override
        public void run() {
            AmountCurrencyUtil.setAmount(mWouldCoverMe, mWouldCoverMeValue, mCurrency);
            AmountCurrencyUtil.setAmount(mCoverMe, mWouldCoverMeValue, mCurrency);
            AmountCurrencyUtil.setAmount(mWouldCoverThem, mWouldCoverThemValue, mCurrency);
            AmountCurrencyUtil.setAmount(mCoverThem, mWouldCoverThemValue, mCurrency);
            mWouldCoverPanel.postDelayed(this, 100);
        }
    };
}

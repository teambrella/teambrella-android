package com.teambrella.android.ui.teammate;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.ui.widget.AmountWidget;
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets;

import io.reactivex.Notification;
import io.reactivex.Observable;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Teammate fragment.
 */
public class TeammateFragment extends ADataProgressFragment<IDataHost> {

    private static final String OBJECT_FRAGMENT_TAG = "object_tag";
    private static final String VOTING_TAG = "voting_tag";
    private static final String VOTING_STATS_FRAGMENT_TAG = "voting_stats_tag";


    private ImageView mUserPicture;
    private ImageView mTeammateIcon;
    private TeambrellaAvatarsWidgets mAvatars;

    private TextView mUserName;
    private TextView mMessage;
    private TextView mUnread;


    private View mDiscussion;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private AmountWidget mCoverMe;

    private AmountWidget mCoverThem;


    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teammate, container, false);
        mUserPicture = (ImageView) view.findViewById(R.id.user_picture);
        mUserName = (TextView) view.findViewById(R.id.user_name);
        mCoverMe = (AmountWidget) view.findViewById(R.id.cover_me);
        mCoverThem = (AmountWidget) view.findViewById(R.id.cover_them);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_to_refresh);
        mTeammateIcon = (ImageView) view.findViewById(R.id.teammate_icon);
        mAvatars = (TeambrellaAvatarsWidgets) view.findViewById(R.id.avatars);
        mMessage = (TextView) view.findViewById(R.id.message);
        mUnread = (TextView) view.findViewById(R.id.unread);
        mDiscussion = view.findViewById(R.id.discussion);
        mSwipeRefreshLayout.setEnabled(false);
        if (savedInstanceState == null) {
            mDataHost.load(mTags[0]);
            setContentShown(false);
        }
        mSwipeRefreshLayout.setOnRefreshListener(this::onRefresh);
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
            transaction.add(R.id.voting_statistics_container, ADataFragment.getInstance(mTags, TeammateVotingStatsFragment.class), VOTING_STATS_FRAGMENT_TAG);
        }


        if (fragmentManager.findFragmentByTag(VOTING_TAG) == null) {
            transaction.add(R.id.voting_container, ADataFragment.getInstance(mTags, TeammateVotingFragment.class), VOTING_TAG);
        }


        if (!transaction.isEmpty()) {
            transaction.commit();
        }
    }

    private void onRefresh() {
        mDataHost.load(mTags[0]);
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {

            Picasso picasso = TeambrellaImageLoader.getInstance(getContext()).getPicasso();

            JsonWrapper data = Observable.fromArray(notification.getValue())
                    .map(JsonWrapper::new)
                    .map(item -> item.getObject(TeambrellaModel.ATTR_DATA))
                    .blockingFirst();

            Observable.fromArray(data).map(item -> item.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC))
                    .doOnNext(basic -> {
                        if (basic != null) {
                            mCoverMe.setAmount(basic.getFloat(TeambrellaModel.ATTR_DATA_COVER_ME));
                            mCoverThem.setAmount(basic.getFloat(TeambrellaModel.ATTR_DATA_COVER_THEM));
                            mUserName.setText(basic.getString(TeambrellaModel.ATTR_DATA_NAME));
                        }
                    })
                    .map(jsonWrapper -> TeambrellaServer.BASE_URL + jsonWrapper.getString(TeambrellaModel.ATTR_DATA_AVATAR))
                    .doOnNext(uri -> {
                        picasso.load(uri).into(mUserPicture);
                        picasso.load(uri).transform(new CropCircleTransformation()).into(mTeammateIcon);
                    })
                    .subscribe(this::onSuccess, this::onError);

            Observable.fromArray(data).map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING))
                    .doOnNext(voting -> {
                        View view = getView();
                        if (view != null) {
                            view.findViewById(R.id.voting_container).setVisibility(View.VISIBLE);
                        }
                    }).subscribe(this::onSuccess, this::onError);

            Observable.fromArray(data).map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION))
                    .doOnNext(discussion -> {
                        if (discussion != null) {
                            int unreadCount = discussion.getInt(TeambrellaModel.ATTR_DATA_UNREAD_COUNT);
                            mUnread.setText(discussion.getString(TeambrellaModel.ATTR_DATA_UNREAD_COUNT));
                            mUnread.setVisibility(unreadCount > 0 ? View.VISIBLE : View.INVISIBLE);
                            mMessage.setText(Html.fromHtml(discussion.getString(TeambrellaModel.ATTR_DATA_ORIGINAL_POST_TEXT)));
                        }
                    })
                    .flatMap(discussion -> Observable.fromIterable(discussion.getJsonArray(TeambrellaModel.ATTR_DATA_TOP_POSTER_AVATARS)))
                    .map(jsonElement -> TeambrellaServer.BASE_URL + jsonElement.getAsString())
                    .toList()
                    .subscribe(mAvatars::setAvatars, this::onError);


            mDiscussion.setOnClickListener(v -> {
                Context context = getContext();
                Observable.fromArray(data)
                        .map(jsonWrapper -> data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC))
                        .doOnNext(basic -> context.startActivity(
                                ChatActivity.getLaunchIntent(context
                                        , TeambrellaUris.getTeammateChatUri(
                                                basic.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID)
                                                , basic.getString(TeambrellaModel.ATTR_DATA_USER_ID))
                                        , data.getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION).getString(TeambrellaModel.ATTR_DATA_TOPIC_ID))))
                        .subscribe(this::onSuccess, this::onError);
            });

        } else {
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        setContentShown(true);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void onSuccess(Object item) {

    }

    private void onError(Throwable e) {

    }
}

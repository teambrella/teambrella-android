package com.teambrella.android.ui.teammate;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

    private AmountWidget mCoverMe;

    private AmountWidget mCoverThem;


    private String mUserId;
    private int mTeamId;
    private String mTopicId;


    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teammate, container, false);
        mUserPicture = (ImageView) view.findViewById(R.id.user_picture);
        mUserName = (TextView) view.findViewById(R.id.user_name);
        mCoverMe = (AmountWidget) view.findViewById(R.id.cover_me);
        mCoverThem = (AmountWidget) view.findViewById(R.id.cover_them);
        mTeammateIcon = (ImageView) view.findViewById(R.id.teammate_icon);
        mAvatars = (TeambrellaAvatarsWidgets) view.findViewById(R.id.avatars);
        mMessage = (TextView) view.findViewById(R.id.message);
        mUnread = (TextView) view.findViewById(R.id.unread);
        if (savedInstanceState == null) {
            mDataHost.load(mTags[0]);
            setContentShown(false);
        }
        view.findViewById(R.id.discussion).setOnClickListener(v ->
                startActivity(ChatActivity.getLaunchIntent(getContext(), TeambrellaUris.getTeammateChatUri(mTeamId, mUserId), mTopicId)));
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


            Picasso picasso = TeambrellaImageLoader.getInstance(getContext()).getPicasso();


            Observable<JsonWrapper> dataObservable =
                    responseObservable.map(item -> item.getObject(TeambrellaModel.ATTR_DATA));

            Observable<JsonWrapper> basicObservable =
                    dataObservable.map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC));

            basicObservable.doOnNext(basic -> mCoverMe.setAmount(basic.getFloat(TeambrellaModel.ATTR_DATA_COVER_ME)))
                    .doOnNext(basic -> mCoverThem.setAmount(basic.getFloat(TeambrellaModel.ATTR_DATA_COVER_THEM)))
                    .doOnNext(basic -> mUserName.setText(basic.getString(TeambrellaModel.ATTR_DATA_NAME)))
                    .doOnNext(basic -> mTeamId = basic.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID))
                    .doOnNext(basic -> mUserId = basic.getString(TeambrellaModel.ATTR_DATA_USER_ID))
                    .onErrorReturnItem(new JsonWrapper(null)).blockingFirst();


            basicObservable.map(basic -> TeambrellaServer.BASE_URL + basic.getString(TeambrellaModel.ATTR_DATA_AVATAR))
                    .doOnNext(uri -> picasso.load(uri).into(mUserPicture))
                    .doOnNext(uri -> picasso.load(uri).transform(new CropCircleTransformation()).into(mTeammateIcon))
                    .onErrorReturnItem("").blockingFirst();


            dataObservable.map(data -> data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING))
                    .doOnNext(voting -> {
                        View view = getView();
                        if (view != null) {
                            view.findViewById(R.id.voting_container).setVisibility(View.VISIBLE);
                        }
                    })
                    .onErrorReturnItem(new JsonWrapper(null)).blockingFirst();

            Observable<JsonWrapper> discussionsObservable = dataObservable.map(data -> data.getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION));


            discussionsObservable.doOnNext(discussion -> mUnread.setText(discussion.getString(TeambrellaModel.ATTR_DATA_UNREAD_COUNT)))
                    .doOnNext(discussion -> mUnread.setVisibility(discussion.getInt(TeambrellaModel.ATTR_DATA_UNREAD_COUNT) > 0 ? View.VISIBLE : View.INVISIBLE))
                    .doOnNext(discussion -> mMessage.setText(Html.fromHtml(discussion.getString(TeambrellaModel.ATTR_DATA_ORIGINAL_POST_TEXT))))
                    .doOnNext(discussion -> mTopicId = discussion.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID))
                    .onErrorReturnItem(new JsonWrapper(null)).blockingFirst();


            discussionsObservable.flatMap(discussion -> Observable.fromIterable(discussion.getJsonArray(TeambrellaModel.ATTR_DATA_TOP_POSTER_AVATARS)))
                    .map(jsonElement -> TeambrellaServer.BASE_URL + jsonElement.getAsString())
                    .toList()
                    .subscribe(mAvatars::setAvatars, e -> {
                    });
        } else {
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        setContentShown(true);
    }


}

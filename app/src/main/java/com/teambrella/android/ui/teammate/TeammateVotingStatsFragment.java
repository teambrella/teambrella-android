package com.teambrella.android.ui.teammate;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.widget.PercentageWidget;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Voting statistics
 */
public class TeammateVotingStatsFragment extends ADataFragment<ITeammateActivity> {

    private TextView mWeight;
    private TextView mProxyRank;
    private TextView mSetProxy;
    private PercentageWidget mDecisionView;
    private PercentageWidget mDiscussionView;
    private PercentageWidget mVotingView;

    private float mDecision;
    private float mDiscussion;
    private float mVoting;


    public static TeammateVotingStatsFragment getInstance(String dataTag) {
        TeammateVotingStatsFragment fragment = new TeammateVotingStatsFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_DATA_FRAGMENT_TAG, dataTag);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teammate_voting_stats, container, false);
        mWeight = (TextView) view.findViewById(R.id.weight);
        mProxyRank = (TextView) view.findViewById(R.id.proxy_rank);
        mSetProxy = (TextView) view.findViewById(R.id.add_to_proxies);
        mDecisionView = (PercentageWidget) view.findViewById(R.id.decision_stats);
        mDiscussionView = (PercentageWidget) view.findViewById(R.id.discussion_stats);
        mVotingView = (PercentageWidget) view.findViewById(R.id.voting_stats);
        mSetProxy.setOnClickListener(this::onClick);
        return view;
    }


    private boolean onClick(View view) {
        switch (view.getId()) {
            case R.id.add_to_proxies:
                mDataHost.setAsProxy(!(Boolean) view.getTag());
                return true;
        }

        return false;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {

            Observable<JsonWrapper> responseObservable = Observable.just(notification.getValue())
                    .map(JsonWrapper::new);


            mSetProxy.setVisibility(mDataHost.isItMe() ? View.GONE : View.VISIBLE);


            final Observable<Uri> uriObservable = responseObservable.map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_STATUS))
                    .map(jsonWrapper -> Uri.parse(jsonWrapper.getString(TeambrellaModel.ATTR_STATUS_URI)));

            final Integer matchId = uriObservable.map(TeambrellaUris.sUriMatcher::match)
                    .blockingFirst();


            switch (matchId) {
                case TeambrellaUris.SET_MY_PROXY:
                    uriObservable.map(uri -> Boolean.parseBoolean(uri.getQueryParameter(TeambrellaUris.KEY_ADD)))
                            .doOnNext(add -> mSetProxy.setText(add ? R.string.remove_from_my_proxies : R.string.add_to_my_proxies))
                            .doOnNext(add -> mSetProxy.setTag(add))
                            .onErrorReturnItem(false).blockingFirst();
                    break;
                default:
                    Observable<JsonWrapper> dataObservable =
                            responseObservable.map(node -> node.getObject(TeambrellaModel.ATTR_DATA));

                    dataObservable.map(node -> node.getObject(TeambrellaModel.ATTR_DATA_ONE_STATS))
                            .doOnNext(node -> mWeight.setText(getString(R.string.risk_format_string, node.getFloat(TeambrellaModel.ATTR_DATA_WEIGHT))))
                            .doOnNext(node -> mProxyRank.setText(getString(R.string.risk_format_string, node.getFloat(TeambrellaModel.ATTR_DATA_PROXY_RANK))))
                            .doOnNext(node -> mDecision = node.getFloat(TeambrellaModel.ATTR_DATA_DECISION_FREQUENCY, mDecision))
                            .doOnNext(node -> mDiscussion = node.getFloat(TeambrellaModel.ATTR_DATA_DISCUSSION_FREQUENCY, mDiscussion))
                            .doOnNext(node -> mVoting = node.getFloat(TeambrellaModel.ATTR_DATA_VOTING_FREQUENCY, mVoting))
                            .onErrorReturnItem(new JsonWrapper(null)).blockingFirst();

                    dataObservable.map(node -> node.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC))
                            .map(node -> node.getBoolean(TeambrellaModel.ATTR_DATA_IS_MY_PROXY, false))
                            .doOnNext(isMyProxy -> mSetProxy.setText(isMyProxy ? R.string.remove_from_my_proxies : R.string.add_to_my_proxies))
                            .doOnNext(isMyProxy -> mSetProxy.setTag(isMyProxy))
                            .onErrorReturnItem(false).blockingFirst();
            }


            mVotingView.setPercentage(mVoting);
            mVotingView.setDescription(getString(getVotingStatsString(mVoting)));
            mDecisionView.setPercentage(mDecision);
            mDecisionView.setDescription(getString(getDecisionStatsString(mDecision)));
            mDiscussionView.setPercentage(mDiscussion);
            mDiscussionView.setDescription(getString(getDiscussionStatsString(mDiscussion)));
        }
    }


    static int getVotingStatsString(float value) {
        if (value >= 0.95f) {
            return R.string.voting_always;
        } else if (value >= 0.6f) {
            return R.string.voting_regularly;
        } else if (value >= 0.3f) {
            return R.string.voting_often;
        } else if (value >= 0.15f) {
            return R.string.voting_frequently;
        } else if (value >= 0.05f) {
            return R.string.voting_rarely;
        } else {
            return R.string.voting_never;
        }
    }

    static int getDecisionStatsString(float value) {
        if (value >= 0.7f) {
            return R.string.decision_harsh;
        } else if (value >= 0.55f) {
            return R.string.decision_severe;
        } else if (value >= 0.45f) {
            return R.string.decision_moderate;
        } else if (value >= 0.3f) {
            return R.string.decision_mild;
        } else {
            return R.string.decision_generous;
        }
    }


    static int getDiscussionStatsString(float value) {
        if (value >= 0.5f) {
            return R.string.discussion_chatty;
        } else if (value >= 0.25f) {
            return R.string.discussion_sociable;
        } else if (value >= 0.1f) {
            return R.string.discussion_moderate;
        } else if (value >= 0.03f) {
            return R.string.discussion_reserved;
        } else {
            return R.string.discussion_quite;
        }
    }


}

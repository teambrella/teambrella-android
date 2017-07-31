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

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Voting statistics
 */
public class TeammateVotingStatsFragment extends ADataFragment<ITeammateActivity> {

    private TextView mWeight;
    private TextView mProxyRank;
    private TextView mSetProxy;


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
                            .onErrorReturnItem(new JsonWrapper(null)).blockingFirst();

                    dataObservable.map(node -> node.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC))
                            .map(node -> node.getBoolean(TeambrellaModel.ATTR_DATA_IS_MY_PROXY, false))
                            .doOnNext(isMyProxy -> mSetProxy.setText(isMyProxy ? R.string.remove_from_my_proxies : R.string.add_to_my_proxies))
                            .doOnNext(isMyProxy -> mSetProxy.setTag(isMyProxy))
                            .onErrorReturnItem(false).blockingFirst();
            }

        }
    }
}

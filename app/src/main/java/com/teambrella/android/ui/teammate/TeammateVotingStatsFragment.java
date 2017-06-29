package com.teambrella.android.ui.teammate;

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
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataFragment;

import io.reactivex.Notification;

/**
 * Voting statistics
 */
public class TeammateVotingStatsFragment extends ADataFragment<IDataHost> {

    private TextView mWeight;
    private TextView mProxyRank;


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
        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper data = new JsonWrapper(notification.getValue().get(TeambrellaModel.ATTR_DATA).getAsJsonObject());
            JsonWrapper statsData = data.getObject(TeambrellaModel.ATTR_DATA_ONE_STATS);
            if (statsData != null) {
                mWeight.setText(getString(R.string.risk_format_string, statsData.getFloat(TeambrellaModel.ATTR_DATA_WEIGHT, 0f)));
                mProxyRank.setText(getString(R.string.risk_format_string, statsData.getFloat(TeambrellaModel.ATTR_DATA_PROXY_RANK, 0f)));
            }
        }
    }
}

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

import io.reactivex.Notification;

/**
 * Voting statistics
 */
public class TeammateVotingStatsFragment extends ATeammateFragment {

    private TextView mWeight;
    private TextView mProxyRank;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmnt_teammate_voting_stats, container, false);
        mWeight = (TextView) view.findViewById(R.id.weight);
        mProxyRank = (TextView) view.findViewById(R.id.proxy_rank);
        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonObject data = notification.getValue().get(TeambrellaModel.ATTR_DATA).getAsJsonObject();
            JsonObject statsData = data.get(TeambrellaModel.ATTR_DATA_ONE_STATS).getAsJsonObject();
            mWeight.setText(getString(R.string.risk_format_string, statsData.get(TeambrellaModel.ATTR_DATA_WEIGHT).getAsFloat()));
            mProxyRank.setText(getString(R.string.risk_format_string, statsData.get(TeambrellaModel.ATTR_DATA_PROXY_RANK).getAsFloat()));
        }
    }
}

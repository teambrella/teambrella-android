package com.teambrella.android.ui.proxies.myproxies;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * My Proxies Adapter
 */

public class MyProxiesAdapter extends TeambrellaDataPagerAdapter {

    private final int mTeamId;

    MyProxiesAdapter(IDataPager<JsonArray> pager, int teamId) {
        super(pager);
        mTeamId = teamId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);

        if (holder == null) {
            holder = new MyProxyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_my_proxy, parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof MyProxyViewHolder) {
            ((MyProxyViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void exchangeItems(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        ((MyProxyViewHolder) viewHolder).mPosition.setText(Integer.toString(target.getAdapterPosition() + 1));
        ((MyProxyViewHolder) target).mPosition.setText(Integer.toString(viewHolder.getAdapterPosition() + 1));
        super.exchangeItems(viewHolder, target);
    }

    private final class MyProxyViewHolder extends AMemberViewHolder {

        private TextView mLocation;
        private TextView mRank;
        private ProgressBar mDecision;
        private ProgressBar mDiscussion;
        private ProgressBar mVoting;
        private TextView mPosition;

        MyProxyViewHolder(View itemView) {
            super(itemView, mTeamId);
            mLocation = (TextView) itemView.findViewById(R.id.location);
            mRank = (TextView) itemView.findViewById(R.id.rank);
            mDecision = (ProgressBar) itemView.findViewById(R.id.decision_progress);
            mDiscussion = (ProgressBar) itemView.findViewById(R.id.discussion_progress);
            mVoting = (ProgressBar) itemView.findViewById(R.id.voting_progress);
            mPosition = (TextView) itemView.findViewById(R.id.position);
        }

        @SuppressLint("SetTextI18n")
        protected void onBind(JsonWrapper item) {
            super.onBind(item);
            mRank.setText(itemView.getContext().getString(R.string.risk_format_string, item.getFloat(TeambrellaModel.ATTR_DATA_PROXY_RANK)));
            String location = item.getString(TeambrellaModel.ATTR_DATA_LOCATION);
            mLocation.setText(TextUtils.isEmpty(location) ? "Unknown location" : location);

            mDecision.setProgress(Math.round(item.getFloat(TeambrellaModel.ATTR_DATA_DECISION_FREQUENCY) * 100));
            mDiscussion.setProgress(Math.round(item.getFloat(TeambrellaModel.ATTR_DATA_DISCUSSION_FREQUENCY) * 100));
            mVoting.setProgress(Math.round(item.getFloat(TeambrellaModel.ATTR_DATA_VOTING_FREQUENCY) * 100));
            mPosition.setText(Integer.toString(getAdapterPosition() + 1));
        }


    }

}
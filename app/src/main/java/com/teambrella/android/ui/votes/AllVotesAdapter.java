package com.teambrella.android.ui.votes;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * All Votes Adapter
 */
public class AllVotesAdapter extends TeambrellaDataPagerAdapter {

    private final int mTeamId;

    AllVotesAdapter(IDataPager<JsonArray> pager, int teamId) {
        super(pager);
        mTeamId = teamId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
        if (holder == null) {
            return new VoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vote, parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof VoteViewHolder) {
            ((VoteViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
        }
    }

    class VoteViewHolder extends AMemberViewHolder {

        private TextView mVoteView;
        private TextView mWeightView;

        VoteViewHolder(View itemView) {
            super(itemView, mTeamId);
            mVoteView = itemView.findViewById(R.id.vote);
            mWeightView = itemView.findViewById(R.id.weight);
        }


        @SuppressLint("SetTextI18n")
        @Override
        public void onBind(JsonWrapper item) {
            super.onBind(item);
            mVoteView.setText(Html.fromHtml("" + (int) (item.getFloat(TeambrellaModel.ATTR_DATA_VOTE) * 100)) + "%");
            mWeightView.setText(Html.fromHtml(itemView.getContext().getString(R.string.weight_format_string, item.getFloat(TeambrellaModel.ATTR_DATA_WEIGHT))));
        }
    }

}

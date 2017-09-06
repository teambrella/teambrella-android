package com.teambrella.android.ui.votes;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * All Votes Adapter
 */
public class AllVotesAdapter extends TeambrellaDataPagerAdapter {

    public AllVotesAdapter(IDataPager<JsonArray> pager) {
        super(pager);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
        if (holder == null) {
            return new RecyclerView.ViewHolder(new TextView(parent.getContext())) {
            };
        }
        return holder;
    }
}

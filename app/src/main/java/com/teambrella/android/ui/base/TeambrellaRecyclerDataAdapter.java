package com.teambrella.android.ui.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Teambrella Recycler Data Adapter
 */
public class TeambrellaRecyclerDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected static final int VIEW_TYPE_LOADING = 1;
    protected static final int VIEW_TYPE_ERROR = 2;
    protected static final int VIEW_TYPE_REGULAR = 3;


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    protected static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(Context context) {
            super(itemView);
        }
    }
}

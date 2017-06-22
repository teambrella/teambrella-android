package com.teambrella.android.ui.base;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonArray;
import com.teambrella.android.R;
import com.teambrella.android.data.base.IDataPager;

import io.reactivex.disposables.Disposable;

/**
 * Teambrella Data Pager Adapter
 */
public class TeambrellaDataPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @SuppressWarnings("WeakerAccess")
    protected static final int VIEW_TYPE_LOADING = 1;
    @SuppressWarnings("WeakerAccess")
    protected static final int VIEW_TYPE_ERROR = 2;
    @SuppressWarnings("WeakerAccess")
    protected static final int VIEW_TYPE_REGULAR = 3;


    protected final IDataPager<JsonArray> mPager;
    private final Disposable mDisposal;


    public TeambrellaDataPagerAdapter(IDataPager<JsonArray> pager) {
        mPager = pager;
        mDisposal = mPager.getObservable().subscribe(d -> notifyDataSetChanged());
    }

    @Override
    public int getItemViewType(int position) {

        int size = mPager.getLoadedData().size();

        if (position == size) {
            if (mPager.hasError()) {
                return VIEW_TYPE_ERROR;
            } else if (mPager.hasNext() || mPager.isLoading()) {
                return VIEW_TYPE_LOADING;
            } else {
                throw new RuntimeException();
            }
        }

        return VIEW_TYPE_REGULAR;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_LOADING:
                return new LoadingViewHolder(inflater.inflate(R.layout.list_item_loading, parent, false));
            case VIEW_TYPE_ERROR:
                return new ErrorViewHolder(inflater.inflate(R.layout.list_item_reload, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (mPager.hasNext() && !mPager.isLoading() && !mPager.hasError() && position > mPager.getLoadedData().size() - 10) {
            mPager.loadNext(false);
        }


        if (holder instanceof ErrorViewHolder) {
            holder.itemView.setOnClickListener(v -> {
                mPager.loadNext(false);
                notifyDataSetChanged();
            });
        }
    }

    public void destroy() {
        if (mDisposal != null && !mDisposal.isDisposed()) {
            mDisposal.dispose();
        }
    }

    @Override
    public int getItemCount() {
        return mPager.getLoadedData().size()
                + (mPager.hasError() || mPager.isLoading() || mPager.hasNext() ? 1 : 0);
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class ErrorViewHolder extends RecyclerView.ViewHolder {
        ErrorViewHolder(View itemView) {
            super(itemView);
        }
    }
}

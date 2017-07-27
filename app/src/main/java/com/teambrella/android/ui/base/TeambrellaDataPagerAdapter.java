package com.teambrella.android.ui.base;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
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
        mDisposal = mPager.getObservable().subscribe(notification -> {
            if (notification.isOnNext()) {
                JsonWrapper metadata = new JsonWrapper(notification.getValue()).getObject(TeambrellaModel.ATTR_METADATA_);
                if (metadata != null) {
                    if (metadata.getBoolean(TeambrellaModel.ATTR_METADATA_RELOAD, false)) {
                        notifyDataSetChanged();
                    } else {
                        int dataSize = mPager.getLoadedData().size();
                        int addedSize = metadata.getInt(TeambrellaModel.ATTR_METADATA_SIZE);
                        int shift = hasHeader() ? 1 : 0;

                        if (addedSize > 0) {
                            switch (metadata.getString(TeambrellaModel.ATTR_METADATA_DIRECTION)) {
                                case TeambrellaModel.ATTR_METADATA_NEXT_DIRECTION:
                                    notifyItemRangeInserted(dataSize - addedSize + shift, (mPager.hasNext() ? 0 : -1) + addedSize);
                                    break;

                                case TeambrellaModel.ATTR_METADATA_PREVIOUS_DIRECTION:
                                    notifyItemChanged(0);
                                    notifyItemRangeInserted(0, addedSize);
                                    break;
                            }
                        } else {
                            notifyDataSetChanged();
                        }
                    }
                }
            } else {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {

        int size = mPager.getLoadedData().size();

        if (position == 0) {
            if (mPager.hasPreviousError()) {
                return VIEW_TYPE_ERROR;
            } else if (mPager.hasPrevious() || mPager.isPreviousLoading()) {
                return VIEW_TYPE_LOADING;
            }

        } else if ((position == size) && !mPager.hasPrevious() && !mPager.isPreviousLoading() && !mPager.hasPreviousError()
                || ((mPager.hasPreviousError() || mPager.isPreviousLoading() || mPager.hasPrevious()) && position == size + 1)) {
            if (mPager.hasNextError()) {
                return VIEW_TYPE_ERROR;
            } else if (mPager.hasNext() || mPager.isNextLoading()) {
                return VIEW_TYPE_LOADING;
            } else {
                throw new RuntimeException();
            }
        }

        return VIEW_TYPE_REGULAR;
    }

    public void exchangeItems(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        JsonArray data = mPager.getLoadedData();
        int srcPosition = viewHolder.getAdapterPosition();
        int dstPosition = target.getAdapterPosition();
        JsonElement srcElement = data.get(srcPosition);
        JsonElement dstElement = data.get(dstPosition);
        data.set(srcPosition, dstElement);
        data.set(dstPosition, srcElement);
        notifyItemMoved(srcPosition, dstPosition);
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

        if (mPager.hasNext() && !mPager.isNextLoading() && !mPager.hasNextError() && position > mPager.getLoadedData().size() - 10) {
            mPager.loadNext(false);
        }

        if (mPager.hasPrevious() && !mPager.isNextLoading() && !mPager.hasNextError() && position < 10) {
            mPager.loadPrevious(false);
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
        return mPager.getLoadedData().size() + (hasFooter() ? 1 : 0) + (hasHeader() ? 1 : 0);

    }

    protected boolean hasHeader() {
        return mPager.hasPreviousError() || mPager.isPreviousLoading() || mPager.hasPrevious();
    }

    protected boolean hasFooter() {
        return mPager.hasNextError() || mPager.isNextLoading() || mPager.hasNext();
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

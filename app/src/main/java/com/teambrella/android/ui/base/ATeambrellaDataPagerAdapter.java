package com.teambrella.android.ui.base;

import android.support.v7.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.data.base.IDataPager;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Base Teambrella Data Pager Adapter
 */
public abstract class ATeambrellaDataPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected final IDataPager<JsonArray> mPager;
    private final Disposable mDisposal;

    public ATeambrellaDataPagerAdapter(IDataPager<JsonArray> pager) {
        mPager = pager;
        mDisposal = mPager.getObservable().subscribe(this::onPagerUpdated);
    }

    public void destroy() {
        if (mDisposal != null && !mDisposal.isDisposed()) {
            mDisposal.dispose();
        }
    }

    protected void onPagerUpdated(Notification<JsonObject> notification) {
        notifyDataSetChanged();
    }

    public abstract void exchangeItems(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);
}

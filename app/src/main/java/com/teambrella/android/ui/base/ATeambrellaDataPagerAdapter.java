package com.teambrella.android.ui.base;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.data.base.IDataPager;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Base Teambrella Data Pager Adapter
 */
public abstract class ATeambrellaDataPagerAdapter extends ATeambrellaAdapter {

    public interface OnStartActivityListener {
        void onStartActivity(Intent intent);
    }


    protected final IDataPager<JsonArray> mPager;
    private final Disposable mDisposal;


    private final OnStartActivityListener mStartActivityListener;

    public ATeambrellaDataPagerAdapter(IDataPager<JsonArray> pager) {
        this(pager, null);
    }

    public ATeambrellaDataPagerAdapter(IDataPager<JsonArray> pager, OnStartActivityListener listener) {
        mPager = pager;
        mDisposal = mPager.getObservable().subscribe(this::onPagerUpdated);
        mStartActivityListener = listener;
    }

    void destroy() {
        if (mDisposal != null && !mDisposal.isDisposed()) {
            mDisposal.dispose();
        }
    }

    protected boolean startActivity(Intent intent) {
        if (mStartActivityListener != null) {
            mStartActivityListener.onStartActivity(intent);
        }
        return mStartActivityListener != null;
    }

    protected void onPagerUpdated(Notification<JsonObject> notification) {
        notifyDataSetChanged();
    }

    public abstract void exchangeItems(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);
}

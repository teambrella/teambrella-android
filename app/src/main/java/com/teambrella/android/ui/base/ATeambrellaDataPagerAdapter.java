package com.teambrella.android.ui.base;

import androidx.lifecycle.Observer;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.data.base.IDataPager;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Base Teambrella Data Pager Adapter
 */
public abstract class ATeambrellaDataPagerAdapter extends ATeambrellaAdapter {

    public interface OnStartActivityListener {
        void onStartActivity(Intent intent);
    }


    protected final IDataPager<JsonArray> mPager;
    private final Disposable mItemChangedDisposal;


    private final OnStartActivityListener mStartActivityListener;

    public ATeambrellaDataPagerAdapter(IDataPager<JsonArray> pager) {
        this(pager, null);
    }

    public ATeambrellaDataPagerAdapter(IDataPager<JsonArray> pager, OnStartActivityListener listener) {
        mPager = pager;
        mPager.getDataObservable().observeForever(dataObserver);
        Observable<Integer> itemChangeObservable = mPager.getItemChangeObservable();
        mItemChangedDisposal = itemChangeObservable != null ? itemChangeObservable.subscribe(this::onPagerItemChanged) : null;
        mStartActivityListener = listener;
    }

    void destroy() {
        mPager.getDataObservable().removeObserver(dataObserver);
        if (mItemChangedDisposal != null && !mItemChangedDisposal.isDisposed()) {
            mItemChangedDisposal.dispose();
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

    protected void onPagerItemChanged(int item) {
        //nothing to do
    }


    public abstract void exchangeItems(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);


    private Observer<Notification<JsonObject>> dataObserver = this::onPagerUpdated;
}

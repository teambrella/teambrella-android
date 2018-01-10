package com.teambrella.android.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.teambrella.android.data.base.IDataHost;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Abstract Data Fragment
 */
public abstract class ADataFragment<T extends IDataHost> extends TeambrellaFragment {

    protected static final String EXTRA_DATA_FRAGMENT_TAG = "data_fragment_tag";

    protected T mDataHost;

    private Disposable[] mDisposals;

    protected String[] mTags;

    public static <T extends ADataFragment> T getInstance(String tag, Class<T> clazz) {
        T fragment;
        try {
            fragment = clazz.newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("unable to create fragment");
        }

        Bundle args = new Bundle();
        args.putStringArray(EXTRA_DATA_FRAGMENT_TAG, new String[]{tag});
        fragment.setArguments(args);
        return fragment;
    }


    public static <T extends ADataFragment> T getInstance(String[] tags, Class<T> clazz) {
        T fragment;
        try {
            fragment = clazz.newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("unable to create fragment");
        }

        Bundle args = new Bundle();
        args.putStringArray(EXTRA_DATA_FRAGMENT_TAG, tags);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTags = getArguments().getStringArray(EXTRA_DATA_FRAGMENT_TAG);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDataHost = (T) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        mDisposals = new Disposable[mTags.length];
        for (int i = 0; i < mDisposals.length; i++) {
            mDisposals[i] = mDataHost.getObservable(mTags[i]).subscribe(this::onDataUpdated);
        }
    }

    protected abstract void onDataUpdated(Notification<JsonObject> notification);

    @Override
    public void onStop() {
        super.onStop();
        for (Disposable disposable : mDisposals) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDataHost = null;
    }
}

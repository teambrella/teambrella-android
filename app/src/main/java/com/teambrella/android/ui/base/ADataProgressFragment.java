package com.teambrella.android.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.teambrella.android.data.base.IDataHost;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Abstract Data Progress Fragment
 */
public abstract class ADataProgressFragment<T extends IDataHost> extends ProgressFragment {

    protected static final String EXTRA_DATA_FRAGMENT_TAG = "data_fragment_tag";

    protected T mDataHost;

    private Disposable mDisposal;

    protected String mTag;


    public static <T extends ADataProgressFragment> T getInstance(String tag, Class<T> clazz) {
        T fragment;
        try {
            fragment = clazz.newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("unable to create fragment");
        }

        Bundle args = new Bundle();
        args.putString(EXTRA_DATA_FRAGMENT_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getArguments().getString(EXTRA_DATA_FRAGMENT_TAG);
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
        mDisposal = mDataHost.getObservable(mTag).subscribe(this::onDataUpdated);
    }

    protected abstract void onDataUpdated(Notification<JsonObject> notification);

    @Override
    public void onStop() {
        super.onStop();
        if (mDisposal != null && !mDisposal.isDisposed()) {
            mDisposal.dispose();
            mDisposal = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDataHost = null;
    }


}

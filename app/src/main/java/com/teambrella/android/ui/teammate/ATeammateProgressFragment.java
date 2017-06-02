package com.teambrella.android.ui.teammate;

import android.content.Context;

import com.google.gson.JsonObject;
import com.teambrella.android.ui.base.ProgressFragment;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Base Teammate Progress Fragment
 */
public abstract class ATeammateProgressFragment extends ProgressFragment {
    protected ITeammateDataHost mTeammateDataHost;
    private Disposable mDisposable;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mTeammateDataHost = (ITeammateDataHost) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        mDisposable = mTeammateDataHost.getTeammateObservable().subscribe(this::onDataUpdated);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    protected abstract void onDataUpdated(Notification<JsonObject> notification);

    @Override
    public void onDetach() {
        super.onDetach();
        mTeammateDataHost = null;
    }
}

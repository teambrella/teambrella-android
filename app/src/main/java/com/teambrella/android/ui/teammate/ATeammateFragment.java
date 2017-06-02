package com.teambrella.android.ui.teammate;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.google.gson.JsonObject;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Base Teammate Fragment
 */
public abstract class ATeammateFragment extends Fragment {


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

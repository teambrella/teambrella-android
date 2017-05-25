package com.teambrella.android.data;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.gson.JsonObject;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.ui.TeambrellaUser;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Main Data Fragment
 */
public class MainDataFragment extends Fragment {

    private static final String LOG_TAG = MainDataFragment.class.getSimpleName();


    private ConnectableObservable<JsonObject> mConnectableObservable;
    private PublishSubject<JsonObject> mPublisher = PublishSubject.create();
    private TeambrellaServer mServer;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mConnectableObservable = mPublisher.replay(1);
        mConnectableObservable.connect();
        mServer = new TeambrellaServer(getContext(), TeambrellaUser.get(getContext()).getPrivateKey());
    }


    public Observable<JsonObject> getTeamListObservable() {
        return mConnectableObservable;
    }

    public void requestTeamList(int teamId) {
        mServer.requestObservable(TeambrellaUris.getTeamUri(teamId), null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete);
    }


    private void onNext(JsonObject data) {
        mPublisher.onNext(data);
    }

    private void onError(Throwable throwable) {
        mPublisher.onError(throwable);
    }

    private void onComplete() {
        // nothing to do
    }

}

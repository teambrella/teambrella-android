package com.teambrella.android.data.base;

import android.content.Context;
import android.net.Uri;

import com.google.gson.JsonObject;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.ui.TeambrellaUser;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Teambrella data loader
 */
public class TeambrellaDataLoader {

    private ConnectableObservable<JsonObject> mConnectableObservable;
    private PublishSubject<JsonObject> mPublisher = PublishSubject.create();
    private TeambrellaServer mServer;


    public TeambrellaDataLoader(Context context) {
        mConnectableObservable = mPublisher.replay(1);
        mConnectableObservable.connect();
        mServer = new TeambrellaServer(context, TeambrellaUser.get(context).getPrivateKey());
    }


    public Observable<JsonObject> getObservable() {
        return mConnectableObservable;
    }


    public void load(Uri uri, JsonObject data) {
        mServer.requestObservable(uri, data)
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

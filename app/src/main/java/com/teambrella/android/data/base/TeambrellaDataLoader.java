package com.teambrella.android.data.base;

import android.annotation.SuppressLint;
import android.net.Uri;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.dagger.Dependencies;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Teambrella data loader
 */
public class TeambrellaDataLoader {

    private ConnectableObservable<Notification<JsonObject>> mConnectableObservable;
    private PublishSubject<Notification<JsonObject>> mPublisher = PublishSubject.create();

    @SuppressWarnings("WeakerAccess")
    @Inject
    @Named(Dependencies.TEAMBRELLA_SERVER)
    TeambrellaServer mServer;


    TeambrellaDataLoader() {
        mConnectableObservable = mPublisher.replay(1);
        mConnectableObservable.connect();
    }


    public Observable<Notification<JsonObject>> getObservable() {
        return mConnectableObservable;
    }


    @SuppressLint("CheckResult")
    public void load(Uri uri, JsonObject data) {
        mServer.requestObservable(uri, data)
                .map(jsonObject -> {
                    if (jsonObject != null) {
                        jsonObject.get(TeambrellaModel.ATTR_STATUS).getAsJsonObject().addProperty(TeambrellaModel.ATTR_STATUS_URI, uri.toString());
                    }
                    return jsonObject;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete);
    }


    private void onNext(JsonObject data) {
        mPublisher.onNext(Notification.createOnNext(data));
    }

    private void onError(Throwable throwable) {
        mPublisher.onNext(Notification.createOnError(throwable));
    }

    private void onComplete() {
        // nothing to do
    }


}

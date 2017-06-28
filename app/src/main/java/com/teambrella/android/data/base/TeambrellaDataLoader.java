package com.teambrella.android.data.base;

import android.content.Context;
import android.net.Uri;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.ui.TeambrellaUser;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Teambrella data loader
 */
public class TeambrellaDataLoader {

    private ConnectableObservable<Notification<JsonObject>> mConnectableObservable;
    private PublishSubject<Notification<JsonObject>> mPublisher = PublishSubject.create();
    private TeambrellaServer mServer;


    public TeambrellaDataLoader(Context context) {
        mConnectableObservable = mPublisher.replay(1);
        mConnectableObservable.connect();
        mServer = new TeambrellaServer(context, TeambrellaUser.get(context).getPrivateKey());
    }


    public Observable<Notification<JsonObject>> getObservable() {
        return mConnectableObservable;
    }


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

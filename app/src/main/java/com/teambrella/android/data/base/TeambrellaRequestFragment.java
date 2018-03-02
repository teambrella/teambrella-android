package com.teambrella.android.data.base;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.ui.TeambrellaUser;

import java.util.LinkedList;
import java.util.Queue;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Universal Data Fragment
 */
public class TeambrellaRequestFragment extends Fragment {


    /**
     * Tag for logging
     */
    private static final String LOG_TAG = TeambrellaRequestFragment.class.getSimpleName();

    /**
     * Is active
     */
    private boolean mIsActive;
    private Queue<Notification<JsonObject>> mBuffer = new LinkedList<>();
    private PublishSubject<Notification<JsonObject>> mPublisher = PublishSubject.create();
    private ConnectableObservable<Notification<JsonObject>> mConnectable = mPublisher.publish();
    private TeambrellaServer mServer;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        TeambrellaUser user = TeambrellaUser.get(getContext());
        if (user.getPrivateKey() != null) {
            mServer = new TeambrellaServer(getContext()
                    , user.getPrivateKey(), user.getDeviceCode()
                    , !user.isDemoUser() ? FirebaseInstanceId.getInstance().getToken() : null, user.getInfoMask());
        }
        mConnectable.connect();
    }


    /**
     * Start emitting events
     */
    public void start() {
        mIsActive = true;
        while (!mBuffer.isEmpty()) {
            mPublisher.onNext(mBuffer.poll());
        }
    }

    public void request(Uri uri) {
        if (mServer != null) {
            request(mServer, uri);
        }
    }


    public void request(Context context, String privateKey, Uri uri) {
        TeambrellaUser user = TeambrellaUser.get(getContext());
        request(new TeambrellaServer(context, privateKey, user.getDeviceCode(), null, user.getInfoMask()), uri);
    }


    private void request(TeambrellaServer server, Uri uri) {
        server.requestObservable(uri, null)
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


    /**
     * Stop emitting events
     */
    public void stop() {
        mIsActive = false;
    }


    void onNext(JsonObject value) {
        if (mIsActive) {
            mPublisher.onNext(Notification.createOnNext(value));
        } else {
            mBuffer.add(Notification.createOnNext(value));
        }
    }

    void onError(Throwable throwable) {
        if (mIsActive) {
            mPublisher.onNext(Notification.createOnError(throwable));
        } else {
            mBuffer.add(Notification.createOnError(throwable));
        }
    }

    void onComplete() {

    }

    public Observable<Notification<JsonObject>> getObservable() {
        return mConnectable;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

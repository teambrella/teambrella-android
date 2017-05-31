package com.teambrella.android.data.base;

import android.content.Context;
import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.ui.TeambrellaUser;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Data Pager Loader
 */
public class TeambrellaDataPagerLoader implements IDataPager<JsonArray> {

    private final static int LIMIT = 10;


    private final ConnectableObservable<Notification<JsonArray>> mConnectableObservable;
    private final PublishSubject<Notification<JsonArray>> mPublisher = PublishSubject.create();
    private final TeambrellaServer mServer;
    private final Uri mUri;
    private final String mProperty;
    private final JsonArray mArray = new JsonArray();

    private boolean mHasError = false;
    private boolean mIsLoading = false;
    private boolean mHasNext = true;


    public TeambrellaDataPagerLoader(Context context, Uri uri, String property) {
        mConnectableObservable = mPublisher.publish();
        mConnectableObservable.connect();
        mServer = new TeambrellaServer(context, TeambrellaUser.get(context).getPrivateKey());
        mUri = uri;
        mProperty = property;
    }


    @Override
    public void loadNext() {
        if (!mIsLoading && mHasNext) {
            mServer.requestObservable(TeambrellaUris.appendPagination(mUri, mArray.size(), LIMIT), null)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNext, this::onError, this::onComplete);
            mIsLoading = true;
            mHasError = false;
        }
    }

    @Override
    public Observable<Notification<JsonArray>> getObservable() {
        return mConnectableObservable;
    }

    @Override
    public JsonArray getLoadedData() {
        return mArray;
    }

    @Override
    public boolean hasNext() {
        return mHasNext;
    }

    @Override
    public boolean hasError() {
        return mHasError;
    }

    @Override
    public boolean isLoading() {
        return mIsLoading;
    }

    private void onNext(JsonObject data) {

        JsonArray newData;

        if (mProperty == null) {
            newData = data.get(TeambrellaModel.ATTR_DATA).getAsJsonArray();
        } else {
            newData = data.get(TeambrellaModel.ATTR_DATA)
                    .getAsJsonObject().get(mProperty).getAsJsonArray();
        }

        mArray.addAll(newData);
        mHasNext = newData.size() == LIMIT;
        mIsLoading = false;
        mPublisher.onNext(Notification.createOnNext(newData));
    }

    private void onError(Throwable throwable) {
        mPublisher.onNext(Notification.createOnError(throwable));
        mHasError = true;
        mIsLoading = false;
    }

    private void onComplete() {
        // nothing to do
    }
}

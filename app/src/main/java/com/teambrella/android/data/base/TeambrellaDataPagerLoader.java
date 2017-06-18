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

    protected final static int LIMIT = 10;


    private final ConnectableObservable<Notification<JsonArray>> mConnectableObservable;
    private final PublishSubject<Notification<JsonArray>> mPublisher = PublishSubject.create();
    private final TeambrellaServer mServer;
    protected final Uri mUri;
    private final String mProperty;
    private JsonArray mArray = new JsonArray();

    private boolean mHasError = false;
    private boolean mIsLoading = false;
    private boolean mHasNext = true;
    private boolean mHasPrevious = true;
    private int mNextIndex = 0;
    private int mPreviousIndex = 0;


    public TeambrellaDataPagerLoader(Context context, Uri uri, String property) {
        mConnectableObservable = mPublisher.publish();
        mConnectableObservable.connect();
        mServer = new TeambrellaServer(context, TeambrellaUser.get(context).getPrivateKey());
        mUri = uri;
        mProperty = property;
    }

    public TeambrellaDataPagerLoader(Context context, Uri uri, String property, int offset) {
        mConnectableObservable = mPublisher.publish();
        mConnectableObservable.connect();
        mNextIndex = mPreviousIndex = offset;
        mServer = new TeambrellaServer(context, TeambrellaUser.get(context).getPrivateKey());
        mUri = uri;
        mProperty = property;
    }


    @Override
    public void loadNext() {
        if (!mIsLoading && mHasNext) {
            mServer.requestObservable(TeambrellaUris.appendPagination(mUri, mNextIndex, LIMIT), null)
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

    @Override
    public boolean hasPrevious() {
        return mHasPrevious;
    }

    @Override
    public void loadPrevious() {
        if (!mIsLoading && mHasPrevious) {
            mServer.requestObservable(TeambrellaUris.appendPagination(mUri, mPreviousIndex, LIMIT), null)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onPrevious, this::onError, this::onComplete);
            mIsLoading = true;
            mHasError = false;
        }
    }

    private void onNext(JsonObject data) {
        JsonArray newData = getPageableData(data);
        mArray.addAll(newData);
        mHasNext = newData.size() == LIMIT;
        mNextIndex += newData.size();
        mIsLoading = false;
        mPublisher.onNext(Notification.createOnNext(newData));
    }

    private void onPrevious(JsonObject data) {
        JsonArray newData = getPageableData(data);
        newData.addAll(mArray);
        mHasNext = newData.size() == LIMIT;
        mPreviousIndex -= newData.size();
        mArray = newData;
        mIsLoading = false;
        mPublisher.onNext(Notification.createOnNext(newData));
    }

    protected JsonArray getPageableData(JsonObject src) {
        if (mProperty == null) {
            return src.get(TeambrellaModel.ATTR_DATA).getAsJsonArray();
        } else {
            return src.get(TeambrellaModel.ATTR_DATA)
                    .getAsJsonObject().get(mProperty).getAsJsonArray();
        }
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

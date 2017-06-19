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
 * Teambrella Chat Data Pager Loader
 */
public class TeambrellaChatDataPagerLoader implements IDataPager<JsonArray> {

    private final static int LIMIT = 10;

    private final ConnectableObservable<Notification<JsonArray>> mConnectableObservable;
    private final PublishSubject<Notification<JsonArray>> mPublisher = PublishSubject.create();
    private final TeambrellaServer mServer;
    private Uri mUri;
    private long mSince = Long.MIN_VALUE;

    private JsonArray mArray = new JsonArray();


    private boolean mHasError = false;
    private boolean mIsLoading = false;
    private boolean mHasNext = true;
    private boolean mHasPrevious = true;
    private int mNextIndex = 0;
    private int mPreviousIndex = 0;


    public TeambrellaChatDataPagerLoader(Context context, Uri uri) {
        mConnectableObservable = mPublisher.publish();
        mConnectableObservable.connect();
        mServer = new TeambrellaServer(context, TeambrellaUser.get(context).getPrivateKey());
        mUri = uri;
    }

    @Override
    public void loadNext(boolean force) {
        if (!mIsLoading && (mHasNext || force)) {
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
    public void loadPrevious(boolean force) {
        if (!mIsLoading && (mHasPrevious || force)) {
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
        if (mSince == Long.MIN_VALUE && newData != null && newData.size() > 0) {
            mSince = newData.get(0).getAsJsonObject().get(TeambrellaModel.ATTR_DATA_CREATED).getAsLong();
            mUri = TeambrellaUris.appendChatSince(mUri, mSince);
        }
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
        return null;
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

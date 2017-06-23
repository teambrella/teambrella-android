package com.teambrella.android.data.base;

import android.content.Context;
import android.net.Uri;
import android.util.Pair;

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


    private final ConnectableObservable<Notification<Pair<Integer, JsonArray>>> mConnectableObservable;
    private final PublishSubject<Notification<Pair<Integer, JsonArray>>> mPublisher = PublishSubject.create();
    private final TeambrellaServer mServer;
    protected final Uri mUri;
    private final String mProperty;
    private JsonArray mArray = new JsonArray();

    private boolean mHasError = false;
    private boolean mIsLoading = false;
    private boolean mHasNext = true;
    private int mNextIndex = 0;


    public TeambrellaDataPagerLoader(Context context, Uri uri, String property) {
        mConnectableObservable = mPublisher.publish();
        mConnectableObservable.connect();
        mServer = new TeambrellaServer(context, TeambrellaUser.get(context).getPrivateKey());
        mUri = uri;
        mProperty = property;
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
    public Observable<Notification<Pair<Integer, JsonArray>>> getObservable() {
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
    public boolean hasNextError() {
        return mHasError;
    }

    @Override
    public boolean isNextLoading() {
        return mIsLoading;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void loadPrevious(boolean force) {

    }

    @Override
    public boolean hasPreviousError() {
        return false;
    }

    @Override
    public boolean isPreviousLoading() {
        return false;
    }

    private void onNext(JsonObject data) {
        JsonArray newData = getPageableData(data);
        mArray.addAll(newData);
        mHasNext = newData.size() == LIMIT;
        mNextIndex += newData.size();
        mIsLoading = false;
        mPublisher.onNext(Notification.createOnNext(new Pair<>(newData.size(), newData)));
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

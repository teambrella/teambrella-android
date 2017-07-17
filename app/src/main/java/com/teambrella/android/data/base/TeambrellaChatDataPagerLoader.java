package com.teambrella.android.data.base;

import android.content.Context;
import android.net.Uri;
import android.util.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
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
    private final static int LIMIT = 100;

    private final ConnectableObservable<Notification<Pair<Integer, JsonArray>>> mConnectableObservable;
    private final PublishSubject<Notification<Pair<Integer, JsonArray>>> mPublisher = PublishSubject.create();
    private final TeambrellaServer mServer;
    private Uri mUri;
    private long mSince = -1;

    private JsonArray mArray = new JsonArray();


    private boolean mHasNextError = false;
    private boolean mIsNextLoading = false;
    private boolean mHasPreviousError = false;
    private boolean mIsPreviousLoading = false;
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
        if (!mIsNextLoading && (mHasNext || force)) {

            Uri uri = TeambrellaUris.appendPagination(mUri, mNextIndex, LIMIT);

            if (mSince != -1) {
                uri = TeambrellaUris.appendChatSince(uri, mSince);
            }

            mServer.requestObservable(uri, null)
                    .map(this::postProcess)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNext, this::onError, this::onComplete);
            mIsNextLoading = true;
            mHasNextError = false;
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
        return mHasNextError;
    }

    @Override
    public boolean isNextLoading() {
        return mIsNextLoading;
    }

    @Override
    public boolean hasPrevious() {
        return mHasPrevious;
    }

    @Override
    public void loadPrevious(boolean force) {
        if (!mIsPreviousLoading && (mHasPrevious || force)) {
            Uri uri = TeambrellaUris.appendPagination(mUri, mPreviousIndex - LIMIT, LIMIT);
            if (mSince != -1) {
                uri = TeambrellaUris.appendChatSince(uri, mSince);
            }
            mServer.requestObservable(uri, null)
                    .map(this::postProcess)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onPrevious, this::onError, this::onComplete);
            mIsPreviousLoading = true;
            mHasPreviousError = false;
        }
    }

    @Override
    public boolean hasPreviousError() {
        return mHasPreviousError;
    }

    @Override
    public boolean isPreviousLoading() {
        return mIsPreviousLoading;
    }

    private void onNext(JsonObject data) {
        JsonWrapper response = new JsonWrapper(data);
        int size = response.getObject(TeambrellaModel.ATTR_METADATA_).getInt(TeambrellaModel.ATTR_METADATA_ORIGINAL_SIZE);
        mIsNextLoading = false;
        JsonArray newData = getPageableData(data);

        if (mSince == -1) {
            mSince = response.getObject(TeambrellaModel.ATTR_DATA).getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION).getLong(TeambrellaModel.ATTR_DATA_LAST_READ, 0);
            if (newData.size() == 0) {
                mHasNext = false;
                loadPrevious(false);
                return;
            }
        }
        mArray.addAll(newData);
        mHasNext = size == LIMIT;
        mNextIndex += size;
        mPublisher.onNext(Notification.createOnNext(new Pair<>(size, newData)));

    }

    private void onPrevious(JsonObject data) {
        JsonWrapper response = new JsonWrapper(data);
        JsonArray newData = getPageableData(data);
        int size = response.getObject(TeambrellaModel.ATTR_METADATA_).getInt(TeambrellaModel.ATTR_METADATA_ORIGINAL_SIZE);
        newData.addAll(mArray);
        mHasPrevious = size == LIMIT;
        mPreviousIndex -= size;
        mArray = newData;
        mIsPreviousLoading = false;
        mPublisher.onNext(Notification.createOnNext(new Pair<>(-size, newData)));
    }

    protected JsonArray getPageableData(JsonObject src) {
        return null;
    }

    protected JsonObject postProcess(JsonObject object) {
        return object;
    }


    private void onError(Throwable throwable) {
        mPublisher.onNext(Notification.createOnError(throwable));
        mHasNextError = true;
        mIsNextLoading = false;
    }

    private void onComplete() {
        // nothing to do
    }
}

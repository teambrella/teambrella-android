package com.teambrella.android.data.base;

import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
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
 * Data Pager Loader
 */
public class TeambrellaDataPagerLoader implements IDataPager<JsonArray> {

    private final static int LIMIT = 50;


    private final ConnectableObservable<Notification<JsonObject>> mConnectableObservable;
    private final PublishSubject<Notification<JsonObject>> mPublisher = PublishSubject.create();
    private final Uri mUri;
    private final String mProperty;
    protected JsonArray mArray = new JsonArray();

    private boolean mHasError = false;
    private boolean mIsLoading = false;
    private boolean mHasNext = true;
    private int mNextIndex = 0;
    private int mLimit = LIMIT;


    @Inject
    @Named(Dependencies.TEAMBRELLA_SERVER)
    TeambrellaServer mServer;


    public TeambrellaDataPagerLoader(Uri uri, String property) {
        mConnectableObservable = mPublisher.publish();
        mConnectableObservable.connect();
        mUri = uri;
        mProperty = property;
    }

    public TeambrellaDataPagerLoader(Uri uri, String property, int limit) {
        mConnectableObservable = mPublisher.publish();
        mConnectableObservable.connect();
        mUri = uri;
        mProperty = property;
        mLimit = limit;
    }

    @Override
    public void loadNext(boolean force) {
        if (!mIsLoading && (mHasNext || force)) {
            mServer.requestObservable(TeambrellaUris.appendPagination(mUri, mNextIndex, mLimit), null)
                    .map(jsonObject -> {
                        if (jsonObject != null) {
                            jsonObject.get(TeambrellaModel.ATTR_STATUS).getAsJsonObject().addProperty(TeambrellaModel.ATTR_STATUS_URI, mUri.toString());
                        }
                        return jsonObject;
                    })
                    .subscribeOn(Schedulers.io())
                    .map(jsonObject -> {
                        JsonObject metadata = new JsonObject();
                        metadata.addProperty(TeambrellaModel.ATTR_METADATA_RELOAD, false);
                        metadata.addProperty(TeambrellaModel.ATTR_METADATA_FORCE, force);
                        metadata.addProperty(TeambrellaModel.ATTR_METADATA_DIRECTION, TeambrellaModel.ATTR_METADATA_NEXT_DIRECTION);
                        JsonArray array = getPageableData(jsonObject);
                        metadata.addProperty(TeambrellaModel.ATTR_METADATA_SIZE, array.size());
                        jsonObject.add(TeambrellaModel.ATTR_METADATA_, metadata);
                        return jsonObject;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNext, this::onError, this::onComplete);
            mIsLoading = true;
            mHasError = false;
        }
    }

    @Override
    public Observable<Notification<JsonObject>> getObservable() {
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


    @Override
    public void reload() {
        reload(mUri);
    }


    @Override
    public void reload(Uri uri) {
        mServer.requestObservable(TeambrellaUris.appendPagination(uri, 0, mLimit), null)
                .map(jsonObject -> {
                    if (jsonObject != null) {
                        jsonObject.get(TeambrellaModel.ATTR_STATUS).getAsJsonObject().addProperty(TeambrellaModel.ATTR_STATUS_URI, uri.toString());
                    }
                    return jsonObject;
                })
                .subscribeOn(Schedulers.io())
                .map(jsonObject -> {
                    JsonObject metadata = new JsonObject();
                    metadata.addProperty(TeambrellaModel.ATTR_METADATA_RELOAD, true);
                    metadata.addProperty(TeambrellaModel.ATTR_METADATA_FORCE, true);
                    metadata.addProperty(TeambrellaModel.ATTR_METADATA_DIRECTION, TeambrellaModel.ATTR_METADATA_NEXT_DIRECTION);
                    JsonArray array = getPageableData(jsonObject);
                    metadata.addProperty(TeambrellaModel.ATTR_METADATA_SIZE, array.size());
                    jsonObject.add(TeambrellaModel.ATTR_METADATA_, metadata);
                    return jsonObject;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(jsonObject -> {
                    mNextIndex = 0;
                    mArray = new JsonArray();
                })
                .subscribe(this::onNext, this::onError, this::onComplete);
        mIsLoading = true;
        mHasError = false;
    }

    private void onNext(JsonObject data) {
        JsonArray newData = getPageableData(data);
        onAddNewData(newData);
        mHasNext = newData.size() == mLimit;
        mNextIndex += newData.size();
        mIsLoading = false;
        mPublisher.onNext(Notification.createOnNext(data));
    }

    protected JsonArray getPageableData(JsonObject src) {
        if (mProperty == null) {
            return src.get(TeambrellaModel.ATTR_DATA).getAsJsonArray();
        } else {
            return src.get(TeambrellaModel.ATTR_DATA)
                    .getAsJsonObject().get(mProperty).getAsJsonArray();
        }
    }


    /**
     * On Add new Data
     *
     * @param newData new data
     */
    protected void onAddNewData(JsonArray newData) {
        mArray.addAll(newData);
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

package com.teambrella.android.data.base;

import com.google.gson.JsonObject;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Data Pager
 */
public interface IDataPager<T> {

    T getLoadedData();

    Observable<Notification<JsonObject>> getObservable();

    void loadNext(boolean force);

    boolean hasNext();

    boolean hasPrevious();

    void loadPrevious(boolean force);

    boolean hasNextError();

    boolean isNextLoading();

    boolean hasPreviousError();

    boolean isPreviousLoading();

    void reload();

}

package com.teambrella.android.data.base;

import android.util.Pair;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Data Pager
 */
public interface IDataPager<T> {

    T getLoadedData();

    Observable<Notification<Pair<Integer, T>>> getObservable();

    void loadNext(boolean force);

    boolean hasNext();

    boolean hasPrevious();

    void loadPrevious(boolean force);

    boolean hasNextError();

    boolean isNextLoading();

    boolean hasPreviousError();

    boolean isPreviousLoading();

}

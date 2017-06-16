package com.teambrella.android.data.base;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Data Pager
 */
public interface IDataPager<T> {

    T getLoadedData();

    Observable<Notification<T>> getObservable();

    void loadNext();

    boolean hasNext();

    boolean hasPrevious();

    void loadPrevious();

    boolean hasError();

    boolean isLoading();

}

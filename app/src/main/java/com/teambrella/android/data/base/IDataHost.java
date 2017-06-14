package com.teambrella.android.data.base;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Data Host
 */
public interface IDataHost {

    Observable<Notification<JsonObject>> getObservable(String tag);

    void load(String tag);

    IDataPager<JsonArray> getPager(String tag);

}

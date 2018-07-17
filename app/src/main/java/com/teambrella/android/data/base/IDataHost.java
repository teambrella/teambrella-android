package com.teambrella.android.data.base;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.reactivex.Notification;

/**
 * Data Host
 */
public interface IDataHost {

    LiveData<Notification<JsonObject>> getObservable(@NonNull String tag);

    void load(@NonNull String tag);

    IDataPager<JsonArray> getPager(@NonNull String tag);

}

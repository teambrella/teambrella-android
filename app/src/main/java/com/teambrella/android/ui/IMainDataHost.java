package com.teambrella.android.ui;

import com.google.gson.JsonObject;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Main Data Host
 */
public interface IMainDataHost {

    void requestTeamList(int teamID, int offset, int limit);

    Observable<Notification<JsonObject>> getTeamListObservable();

}

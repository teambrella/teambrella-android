package com.teambrella.android.ui;

import com.google.gson.JsonObject;

import io.reactivex.Observable;

/**
 * Main Data Host
 */
public interface IMainDataHost {

    void requestTeamList(int teamID);

    Observable<JsonObject> getTeamListObservable();

}

package com.teambrella.android.ui.teammate;

import com.google.gson.JsonObject;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Teammate Data Host
 */
public interface ITeammateDataHost {

    Observable<Notification<JsonObject>> getTeammateObservable();

    void loadTeammate();
}

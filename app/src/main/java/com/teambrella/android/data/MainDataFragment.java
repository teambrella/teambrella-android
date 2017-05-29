package com.teambrella.android.data;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.gson.JsonObject;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataLoader;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Main Data Fragment
 */
public class MainDataFragment extends Fragment {

    private TeambrellaDataLoader mTeamListDataLoader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mTeamListDataLoader = new TeambrellaDataLoader(getContext());
    }


    public Observable<Notification<JsonObject>> getTeamListObservable() {
        return mTeamListDataLoader.getObservable();
    }

    public void requestTeamList(int teamId) {
        mTeamListDataLoader.load(TeambrellaUris.getTeamUri(teamId), null);
    }
}

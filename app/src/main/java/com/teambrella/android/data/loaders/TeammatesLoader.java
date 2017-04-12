package com.teambrella.android.data.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Pair;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;

/**
 * Teammates loader
 */
public class TeammatesLoader extends AsyncTaskLoader<Pair<JsonObject, TeambrellaException>> {

    private final TeambrellaServer mServer;
    private final int mTeamId;

    public TeammatesLoader(Context context, int teamId) {
        super(context);
        mServer = new TeambrellaServer(context);
        mTeamId = teamId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Pair<JsonObject, TeambrellaException> loadInBackground() {
        try {
            return new Pair<>(mServer.execute(TeambrellaUris.getTeammatesUri(mTeamId)), null);
        } catch (TeambrellaException e) {
            return new Pair<>(null, e);
        }
    }
}

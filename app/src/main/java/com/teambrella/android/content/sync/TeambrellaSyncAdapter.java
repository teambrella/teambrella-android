package com.teambrella.android.content.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.model.ITeam;
import com.teambrella.android.api.model.ITeammate;
import com.teambrella.android.api.model.json.Factory;
import com.teambrella.android.api.model.json.JsonTeam;
import com.teambrella.android.api.model.json.JsonTeammate;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.content.TeambrellaRepository;

/**
 * Teambrella Sync Adapter
 */
class TeambrellaSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = TeambrellaSyncAdapter.class.getSimpleName();

    TeambrellaSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        final Context context = getContext();
        TeambrellaServer server = new TeambrellaServer(context);

        JsonObject result = null;
        try {
            result = server.execute(TeambrellaUris.getUpdates());
        } catch (TeambrellaException e) {
            Log.e(LOG_TAG, e.toString());
        }


        ITeam[] teams = Factory.fromArray(result.get("Teams").getAsJsonArray(), JsonTeam.class);

        for (ITeam team : teams) {
            Log.e(LOG_TAG, team.getName());
        }

        ITeammate teammates[] = Factory.fromArray(result.get("Teammates").getAsJsonArray(), JsonTeammate.class);

        for (ITeammate teammate : teammates) {
            Log.e(LOG_TAG, teammate.getName());
        }


        try {
            Cursor cursor = provider.query(TeambrellaRepository.Team.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                cursor.close();
            }
        } catch (RemoteException e) {

        }
    }
}

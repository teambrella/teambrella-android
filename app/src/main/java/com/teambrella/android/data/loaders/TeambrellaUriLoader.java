package com.teambrella.android.data.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.util.Pair;

import com.google.gson.JsonObject;
import com.teambrella.android.TeambrellaUser;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.server.TeambrellaServer;

/**
 * Teammates loader
 */
public class TeambrellaUriLoader extends AsyncTaskLoader<Pair<JsonObject, TeambrellaException>> {

    private final TeambrellaServer mServer;
    private final Uri mUri;

    public TeambrellaUriLoader(Context context, Uri uri) {
        super(context);
        String privateKey = TeambrellaUser.get(context).getPrivateKey();
        if (privateKey != null) {
            mServer = new TeambrellaServer(context, privateKey);
            mUri = uri;
        } else {
            throw new RuntimeException("Missing private key");
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Pair<JsonObject, TeambrellaException> loadInBackground() {
        //try {
            return new Pair<>(mServer.requestObservable(mUri, null).blockingFirst(), null);
        //} catch (TeambrellaException e) {
        //    return new Pair<>(null, e);
        //}
    }
}

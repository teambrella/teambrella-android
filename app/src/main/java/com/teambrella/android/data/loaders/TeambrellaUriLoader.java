package com.teambrella.android.data.loaders;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.util.Pair;

import com.google.gson.JsonObject;
import com.teambrella.android.TeambrellaApplication;
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
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByTypeForPackage(TeambrellaApplication.ACCOUNT_TYPE, context.getPackageName());
        Account account = accounts.length > 0 ? accounts[0] : null;
        String privateKey = null;
        if (account != null) {
            privateKey = accountManager.getPassword(account);
        }
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
        try {
            return new Pair<>(mServer.execute(mUri, null), null);
        } catch (TeambrellaException e) {
            return new Pair<>(null, e);
        }
    }
}

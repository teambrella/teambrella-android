package com.teambrella.android.content.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Teambrella Sync Adapter
 */
class TeambrellaSyncAdapter extends AbstractThreadedSyncAdapter {

    TeambrellaSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        //new TeambrellaAccountSyncAdapter().onPerformSync(getContext(), provider);
        //new TeambrellaBlockchainSyncAdapter().onPerformSync(getContext(), provider);
    }

}

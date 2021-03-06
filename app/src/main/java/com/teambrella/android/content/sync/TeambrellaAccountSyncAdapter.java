package com.teambrella.android.content.sync;

import android.content.ContentProviderClient;
import android.content.Context;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Account Sync Adapter
 */
class TeambrellaAccountSyncAdapter {

    private static final String LOG_TAG = TeambrellaAccountSyncAdapter.class.getSimpleName();
    private static final String PRIVATE_KEY = "cNqQ7aZWitJCk1o9dNhr1o9k3UKdeW92CDYrvDHHLuwFuEnfcBXo";
    /**
     * Key
     */
    private final ECKey mKey;


    private SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    {
        mSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        mKey = DumpedPrivateKey.fromBase58(null, PRIVATE_KEY).getKey();
    }

    void onPerformSync(Context context, ContentProviderClient provider) {
//        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
//        Account[] accounts = accountManager.getAccountsByTypeForPackage(TeambrellaApplication.ACCOUNT_TYPE, context.getPackageName());
//        Account account = accounts.length > 0 ? accounts[0] : null;
//        String privateKey = null;
//        if (account != null) {
//            //privateKey = accountManager.getPassword(account);
//        }
//        final TeambrellaServer server;
//        if (privateKey != null) {
//            server = new TeambrellaServer(context, privateKey);
//        } else {
//            throw new RuntimeException("Missing private key");
//        }
//
//
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//        final TeambrellaContentProviderClient client = new TeambrellaContentProviderClient(provider);
        try {
//
//            operations.addAll(autoApproveTxs(client));
//
//            if (!operations.isEmpty()) {
//                provider.applyBatch(operations);
//                operations.clear();
//            }
//
//
//            updateConnectionTime(provider);
//
//
//            JsonObject result = server.requestObservable(TeambrellaUris.getUpdatesUri(), getRequestBody(provider)).blockingFirst();
//
//            if (result != null) {
//                JsonObject status = result.get(TeambrellaModel.ATTR_STATUS).getAsJsonObject();
//                long timestamp = status.get(TeambrellaModel.ATTR_STATUS_TIMESTAMP).getAsLong();
//
//                JsonObject data = result.get(TeambrellaModel.ATTR_DATA).getAsJsonObject();
//
//
//                operations.addAll(clearNeedUpdateServerFlag());
//
//
//                ServerUpdates serverUpdates = new Gson().fromJson(data, ServerUpdates.class);
//
//                if (serverUpdates.teammates != null) {
//                    operations.addAll(client.insertTeammates(serverUpdates.teammates));
//                }
//
//                if (serverUpdates.teams != null) {
//                    operations.addAll(client.insertTeams(serverUpdates.teams));
//                }
//
//                if (serverUpdates.payTos != null) {
//                    operations.addAll(client.insertPayTos(serverUpdates.payTos));
//                }
//
//                if (serverUpdates.multisigs != null) {
//                    operations.addAll(client.insertMultisigs(serverUpdates.multisigs));
//                }
//
//                if (serverUpdates.cosigners != null) {
//                    operations.addAll(client.insertCosigners(serverUpdates.cosigners));
//                }
//
//                if (serverUpdates.txs != null) {
//                    operations.addAll(client.insertTx(serverUpdates.txs));
//                }
//
//                if (serverUpdates.txInputs != null) {
//                    operations.addAll(client.insertTXInputs(serverUpdates.txs, serverUpdates.txInputs));
//                }
//
//                if (serverUpdates.txOutputs != null) {
//                    operations.addAll(client.insertTXOutputs(serverUpdates.txs, serverUpdates.txOutputs));
//                }
//
//                if (serverUpdates.txSignatures != null) {
//                    operations.addAll(client.insertTXSignatures(serverUpdates.txInputs, serverUpdates.txSignatures));
//                }
//
//                if (!operations.isEmpty()) {
//                    provider.applyBatch(operations);
//                }
//
//
//                operations.clear();
//
//                if (serverUpdates.txs != null) {
//                    operations.addAll(client.checkArrivingTx(serverUpdates.txs));
//                }
//
//                if (!operations.isEmpty()) {
//                    provider.applyBatch(operations);
//                }
//
//                setLastUpdatedTimestamp(provider, timestamp);
//
//                checkAddresses(client, serverUpdates.multisigs);
//
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

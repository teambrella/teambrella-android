package com.teambrella.android.content.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.teambrella.android.TeambrellaApplication;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.content.TeambrellaContentProviderClient;
import com.teambrella.android.content.TeambrellaRepository;
import com.teambrella.android.content.model.BTCAddress;
import com.teambrella.android.content.model.Cosigner;
import com.teambrella.android.content.model.ServerUpdates;
import com.teambrella.android.content.model.Teammate;
import com.teambrella.android.content.model.Tx;
import com.teambrella.android.content.model.TxOutput;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.TestNet3Params;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByTypeForPackage(TeambrellaApplication.ACCOUNT_TYPE, context.getPackageName());
        Account account = accounts.length > 0 ? accounts[0] : null;
        String privateKey = null;
        if (account != null) {
            privateKey = accountManager.getPassword(account);
        }
        final TeambrellaServer server;
        if (privateKey != null) {
            server = new TeambrellaServer(context, privateKey);
        } else {
            throw new RuntimeException("Missing private key");
        }


        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        final TeambrellaContentProviderClient client = new TeambrellaContentProviderClient(provider);
        try {

            operations.addAll(autoApproveTxs(client));

            if (!operations.isEmpty()) {
                provider.applyBatch(operations);
                operations.clear();
            }


            updateConnectionTime(provider);


            JsonObject result = server.requestObservable(TeambrellaUris.getUpdates(), getRequestBody(provider)).blockingFirst();

            if (result != null) {
                JsonObject status = result.get(TeambrellaModel.ATTR_STATUS).getAsJsonObject();
                long timestamp = status.get(TeambrellaModel.ATTR_STATUS_TIMESTAMP).getAsLong();

                JsonObject data = result.get(TeambrellaModel.ATTR_DATA).getAsJsonObject();


                operations.addAll(clearNeedUpdateServerFlag());


                ServerUpdates serverUpdates = new Gson().fromJson(data, ServerUpdates.class);

                if (serverUpdates.teammates != null) {
                    operations.addAll(client.insertTeammates(serverUpdates.teammates));
                }

                if (serverUpdates.teams != null) {
                    operations.addAll(client.insertTeams(serverUpdates.teams));
                }

                if (serverUpdates.payTos != null) {
                    operations.addAll(client.insertPayTos(serverUpdates.payTos));
                }

                if (serverUpdates.btcAddresses != null) {
                    operations.addAll(client.insertBTCAddresses(serverUpdates.btcAddresses));
                }

                if (serverUpdates.cosigners != null) {
                    operations.addAll(client.insertCosigners(serverUpdates.cosigners));
                }

                if (serverUpdates.txs != null) {
                    operations.addAll(client.insertTx(serverUpdates.txs));
                }

                if (serverUpdates.txInputs != null) {
                    operations.addAll(client.insertTXInputs(serverUpdates.txs, serverUpdates.txInputs));
                }

                if (serverUpdates.txOutputs != null) {
                    operations.addAll(client.insertTXOutputs(serverUpdates.txs, serverUpdates.txOutputs));
                }

                if (serverUpdates.txSignatures != null) {
                    operations.addAll(client.insertTXSignatures(serverUpdates.txInputs, serverUpdates.txSignatures));
                }

                if (!operations.isEmpty()) {
                    provider.applyBatch(operations);
                }


                operations.clear();

                if (serverUpdates.txs != null) {
                    operations.addAll(client.checkArrivingTx(serverUpdates.txs));
                }

                if (!operations.isEmpty()) {
                    provider.applyBatch(operations);
                }

                setLastUpdatedTime(provider, timestamp);

                checkAddresses(client, serverUpdates.btcAddresses);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private List<ContentProviderOperation> autoApproveTxs(TeambrellaContentProviderClient client) throws RemoteException {
        List<ContentProviderOperation> operations = new LinkedList<>();
        List<Tx> txs = getTxToApprove(client);
        for (Tx tx : txs) {
            //if (getDaysToApproval(tx, mKey.getPublicKeyAsHex().equals(tx.teammate.publicKey)) <= 0) {
            operations.add(ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI)
                    .withValue(TeambrellaRepository.Tx.RESOLUTION, TeambrellaModel.TX_CLIENT_RESOLUTION_APPROVED)
                    .withValue(TeambrellaRepository.Tx.CLIENT_RESOLUTION_TIME, mSDF.format(new Date()))
                    .withValue(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, true)
                    .withSelection(TeambrellaRepository.Tx.ID + "=?", new String[]{tx.id.toString()})
                    .build());
            //}
        }
        return operations;
    }


    private int getDaysToApproval(Tx tx, boolean isClientTx) {
        boolean isPayToAddressNew = false;
        for (TxOutput txOutput : tx.txOutputs) {
            isPayToAddressNew |= isPayToAddressNew(txOutput, tx.teammate.payToAddressOkAge);
        }
        int daysPassed = getDaysPassed(tx.receivedTime);
        int daysToApproval = isClientTx ? isPayToAddressNew ? tx.teammate.autoApprovalMyNewAddress : tx.teammate.autoApprovalMyGoodAddress
                : isPayToAddressNew ? tx.teammate.autoApprovalCosignNewAddress : tx.teammate.getAutoApprovalCosignGoodAddress;
        return (daysToApproval - daysPassed);
    }


    private boolean isPayToAddressNew(TxOutput txOutput, int payToAddressOkAge) {
        return getDaysPassed(txOutput.knownSince) > payToAddressOkAge;
    }

    private int getDaysPassed(String dateString) {
        try {
            return getDays(new Date().getTime() - mSDF.parse(dateString).getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private int getDays(long time) {
        return (int) (time / (1000 * 60 * 60 * 24));
    }


    private void updateConnectionTime(ContentProviderClient provider) throws RemoteException {
        Cursor cursor = provider.query(TeambrellaRepository.Connection.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.Connection.LAST_CONNECTED, mSDF.format(new Date()));
            if (cursor.moveToFirst()) {
                provider.update(TeambrellaRepository.Connection.CONTENT_URI, cv, TeambrellaRepository.Connection.ID + "=?",
                        new String[]{cursor.getString(cursor.getColumnIndex(TeambrellaRepository.Connection.ID))});
            } else {
                provider.insert(TeambrellaRepository.Connection.CONTENT_URI, cv);
            }
            cursor.close();
        }
    }

    private void setLastUpdatedTime(ContentProviderClient provider, long time) throws RemoteException {
        Cursor cursor = provider.query(TeambrellaRepository.Connection.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.Connection.LAST_UPDATED, Long.toString(time));
            if (cursor.moveToFirst()) {
                provider.update(TeambrellaRepository.Connection.CONTENT_URI, cv, TeambrellaRepository.Connection.ID + "=?",
                        new String[]{cursor.getString(cursor.getColumnIndex(TeambrellaRepository.Connection.ID))});
            }
            cursor.close();
        }
    }


    private List<Tx> getTxToApprove(TeambrellaContentProviderClient client) throws RemoteException {
        List<Tx> list = client.queryList(TeambrellaRepository.Tx.CONTENT_URI, TeambrellaRepository.Tx.RESOLUTION + "=?",
                new String[]{Integer.toString(TeambrellaModel.TX_CLIENT_RESOLUTION_RECEIVED)}, Tx.class);
        Iterator<Tx> iterator = list != null ? list.iterator() : null;
        if (iterator != null) {
            while (iterator.hasNext()) {
                Tx tx = iterator.next();
                tx.txOutputs = client.queryList(TeambrellaRepository.TXOutput.CONTENT_URI,
                        TeambrellaRepository.TXOutput.TX_ID + "=?", new String[]{tx.id.toString()}, TxOutput.class);
                tx.teammate = client.queryOne(TeambrellaRepository.Teammate.CONTENT_URI,
                        TeambrellaRepository.TEAMMATE_TABLE + "." + TeambrellaRepository.Teammate.ID + "=?", new String[]{Long.toString(tx.teammateId)}, Teammate.class);
            }
        }
        return list;
    }


    private void checkAddresses(TeambrellaContentProviderClient client, BTCAddress[] btcAddresses) throws RemoteException {
        for (BTCAddress btcAddress : btcAddresses) {
            BTCAddress savedBtcAddress = client.queryOne(TeambrellaRepository.BTCAddress.CONTENT_URI,
                    TeambrellaRepository.BTCAddress.ADDRESS + "=?", new String[]{btcAddress.address}, BTCAddress.class);
            List<Cosigner> cosigners = getCosigners(client, savedBtcAddress);
            String address = generateStringAddress(savedBtcAddress, cosigners);
            if (!btcAddress.address.equals(generateStringAddress(savedBtcAddress, cosigners))) {
                Log.e("TEST", btcAddress.address + " not " + address);
            }
        }
    }

    private String generateStringAddress(BTCAddress btcAddress, List<Cosigner> cosigners) throws RemoteException {
        byte art[] = TeambrellaBlockchainSyncAdapter.getRedeemScript(btcAddress, cosigners).getProgram();
        return Address.fromP2SHHash(new TestNet3Params(), Utils.sha256hash160(art)).toString();
    }

    private static List<Cosigner> getCosigners(TeambrellaContentProviderClient client, BTCAddress btcAddress) throws RemoteException {
        List<Cosigner> list = client.queryList(TeambrellaRepository.Cosigner.CONTENT_URI, TeambrellaRepository.Cosigner.ADDRESS_ID + "=?",
                new String[]{btcAddress.address}, Cosigner.class);

        Collections.sort(list, new Comparator<Cosigner>() {
            @Override
            public int compare(Cosigner o1, Cosigner o2) {
                return o1.keyOrder > o2.keyOrder ? 1 :
                        o1.keyOrder < o2.keyOrder ? -1
                                : 0;
            }
        });
        return list;
    }


    private static JsonObject getRequestBody(ContentProviderClient client) throws RemoteException {
        JsonObject body = new JsonObject();
        Cursor cursor = client.query(TeambrellaRepository.Connection.CONTENT_URI, new String[]{TeambrellaRepository.Connection.LAST_UPDATED}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String value = cursor.getString(cursor.getColumnIndex(TeambrellaRepository.Connection.LAST_UPDATED));
            long lastUpdated = 0;
            if (value != null) {
                lastUpdated = Long.parseLong(value);
            }
//            if (lastUpdated > 0) {
//                body.add(TeambrellaModel.ATTR_DATA_LAST_UPDATED, new JsonPrimitive(lastUpdated));
//            }
        }
        if (cursor != null) {
            cursor.close();
        }

        cursor = client.query(TeambrellaRepository.Tx.CONTENT_URI, new String[]{TeambrellaRepository.Tx.ID, TeambrellaRepository.Tx.CLIENT_RESOLUTION_TIME,
                TeambrellaRepository.Tx.RESOLUTION}, TeambrellaRepository.Tx.NEED_UPDATE_SERVER, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            JsonArray txArray = new JsonArray();
            do {
                JsonObject info = new JsonObject();
                info.add(TeambrellaModel.ATTR_DATA_ID, new JsonPrimitive(cursor.getString(cursor.getColumnIndex(TeambrellaRepository.Tx.ID))));
                info.add(TeambrellaModel.ATTR_DATA_RESOLUTION, new JsonPrimitive(cursor.getInt(cursor.getColumnIndex(TeambrellaRepository.Tx.RESOLUTION))));
                info.add(TeambrellaModel.ATTR_DATA_RESOLUTION_TIME, new JsonPrimitive(cursor.getString(cursor.getColumnIndex(TeambrellaRepository.Tx.CLIENT_RESOLUTION_TIME))));
                txArray.add(info);
            } while (cursor.moveToNext());

            //body.add(TeambrellaModel.ATTR_DATA_TX_INFOS, txArray);
        }

        if (cursor != null) {
            cursor.close();
        }

        cursor = client.query(TeambrellaRepository.TXSignature.CONTENT_URI, new String[]{TeambrellaRepository.TXSignature.TX_INPUT_ID, TeambrellaRepository.TXSignature.TEAMMATE_ID, TeambrellaRepository.TXSignature.SIGNATURE},
                TeambrellaRepository.TXSignature.NEED_UPDATE_SERVER, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            JsonArray signaturesArray = new JsonArray();
            do {
                JsonObject signature = new JsonObject();
                signature.add(TeambrellaModel.ATTR_DATA_SIGNATURE, new JsonPrimitive(Base64.encodeToString(cursor.getBlob(cursor.getColumnIndex(TeambrellaRepository.TXSignature.SIGNATURE)), Base64.DEFAULT)));
                signature.add(TeambrellaModel.ATTR_DATA_TEAMMATE_ID, new JsonPrimitive(cursor.getInt(cursor.getColumnIndex(TeambrellaRepository.TXSignature.TEAMMATE_ID))));
                signature.add(TeambrellaModel.ATTR_DATA_TX_INPUT_ID, new JsonPrimitive(cursor.getString(cursor.getColumnIndex(TeambrellaRepository.TXSignature.TX_INPUT_ID))));
                signaturesArray.add(signature);
            } while (cursor.moveToNext());

            //body.add(TeambrellaModel.ATTR_DATA_TX_SIGNATURES, signaturesArray);
        }

        if (cursor != null) {
            cursor.close();
        }

        return body;
    }


    private List<ContentProviderOperation> clearNeedUpdateServerFlag() {
        List<ContentProviderOperation> operations = new LinkedList<>();
        operations.add(ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI)
                .withValue(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, false)
                .withSelection(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, null).build());

        operations.add(ContentProviderOperation.newUpdate(TeambrellaRepository.TXSignature.CONTENT_URI)
                .withValue(TeambrellaRepository.TXSignature.NEED_UPDATE_SERVER, false)
                .withSelection(TeambrellaRepository.TXSignature.NEED_UPDATE_SERVER, null)
                .build());

        return operations;
    }


}

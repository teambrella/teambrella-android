package com.teambrella.android.content.sync;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.IBTCAddress;
import com.teambrella.android.api.model.ICosigner;
import com.teambrella.android.api.model.IPayTo;
import com.teambrella.android.api.model.ITeam;
import com.teambrella.android.api.model.ITeammate;
import com.teambrella.android.api.model.ITx;
import com.teambrella.android.api.model.ITxInput;
import com.teambrella.android.api.model.ITxOutput;
import com.teambrella.android.api.model.ITxSignature;
import com.teambrella.android.api.model.json.Factory;
import com.teambrella.android.api.model.json.JsonBTCAddress;
import com.teambrella.android.api.model.json.JsonCosigner;
import com.teambrella.android.api.model.json.JsonPayTo;
import com.teambrella.android.api.model.json.JsonTX;
import com.teambrella.android.api.model.json.JsonTeam;
import com.teambrella.android.api.model.json.JsonTeammate;
import com.teambrella.android.api.model.json.JsonTxInput;
import com.teambrella.android.api.model.json.JsonTxOutput;
import com.teambrella.android.api.model.json.JsonTxSignature;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.content.TeambrellaContentProviderClient;
import com.teambrella.android.content.TeambrellaRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Account Sync Adapter
 */
class TeambrellaAccountSyncAdapter {

    private static final String LOG_TAG = TeambrellaAccountSyncAdapter.class.getSimpleName();

    private SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    void onPerformSync(Context context, ContentProviderClient provider) {

        final TeambrellaServer server = new TeambrellaServer(context);
        final TeambrellaContentProviderClient client = new TeambrellaContentProviderClient(provider);
        try {

            updateConnectionTime(provider);


            JsonObject result = server.execute(TeambrellaUris.getUpdates(), getRequestBody(provider));

            if (result != null) {
                JsonObject status = result.get(TeambrellaModel.ATTR_STATUS).getAsJsonObject();
                long timestamp = status.get(TeambrellaModel.ATTR_STATUS_TIMESTAMP).getAsLong();

                JsonObject data = result.get(TeambrellaModel.ATTR_DATA).getAsJsonObject();


                ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                JsonElement teammatesElement = data.get(TeambrellaModel.ATTR_DATA_TEAMMATES);
                ITeammate[] teammates = teammatesElement != null && !teammatesElement.isJsonNull() ?
                        Factory.fromArray(teammatesElement.getAsJsonArray(), JsonTeammate.class) : null;

                if (teammates != null) {
                    operations.addAll(client.insertTeammates(teammates));
                }

                JsonElement teamsElement = data.get(TeambrellaModel.ATTR_DATA_TEAMS);
                ITeam[] teams = teamsElement != null && !teamsElement.isJsonNull() ?
                        Factory.fromArray(teamsElement.getAsJsonArray(), JsonTeam.class) : null;

                if (teams != null) {
                    operations.addAll(client.insertTeams(teams));
                }

                JsonElement payTosElement = data.get(TeambrellaModel.ATTR_DATA_PAY_TOS);
                IPayTo[] payTos = payTosElement != null && !payTosElement.isJsonNull() ?
                        Factory.fromArray(payTosElement.getAsJsonArray(), JsonPayTo.class) : null;

                if (payTosElement != null) {
                    operations.addAll(client.insertPayTos(payTos));
                }

                JsonElement btcAddressesElement = data.get(TeambrellaModel.ATTR_DATA_BTC_ADDRESSES);
                IBTCAddress[] btcAddresses = btcAddressesElement != null && !btcAddressesElement.isJsonNull() ?
                        Factory.fromArray(btcAddressesElement.getAsJsonArray(), JsonBTCAddress.class) : null;

                if (btcAddresses != null) {
                    operations.addAll(client.insertBTCAddresses(btcAddresses));
                }

                JsonElement cosignersElement = data.get(TeambrellaModel.ATTR_DATA_COSIGNERS);
                ICosigner[] cosigners = cosignersElement != null && !cosignersElement.isJsonNull() ?
                        Factory.fromArray(cosignersElement.getAsJsonArray(), JsonCosigner.class) : null;

                if (cosigners != null) {
                    operations.addAll(client.insertCosigners(cosigners));
                }

                JsonElement txsElement = data.get(TeambrellaModel.ATTR_DATA_TXS);
                ITx[] txs = txsElement != null && !txsElement.isJsonNull() ?
                        Factory.fromArray(txsElement.getAsJsonArray(), JsonTX.class) : null;
                if (txs != null) {
                    operations.addAll(client.insertTx(txs));
                }

                JsonElement txInputsElement = data.get(TeambrellaModel.ATTR_DATA_TX_INPUTS);
                ITxInput[] txInputs = txInputsElement != null && !txInputsElement.isJsonNull() ?
                        Factory.fromArray(txInputsElement.getAsJsonArray(), JsonTxInput.class) : null;

                if (txInputs != null) {
                    operations.addAll(client.insertTXInputs(txs, txInputs));
                }


                JsonElement txOutputsElement = data.get(TeambrellaModel.ATTR_DATA_TX_OUTPUTS);
                ITxOutput[] txOutputs = txOutputsElement != null && !txOutputsElement.isJsonNull() ?
                        Factory.fromArray(txOutputsElement.getAsJsonArray(), JsonTxOutput.class) : null;

                if (txOutputs != null) {
                    operations.addAll(client.insertTXOutputs(txs, txOutputs));
                }

                JsonElement txSignaturesElement = data.get(TeambrellaModel.ATTR_DATA_TX_SIGNATURES);
                ITxSignature[] txSignatures = txSignaturesElement != null && !txSignaturesElement.isJsonNull() ?
                        Factory.fromArray(txSignaturesElement.getAsJsonArray(), JsonTxSignature.class) : null;

                if (txSignatures != null) {
                    operations.addAll(client.insertTXSignatures(txInputs, txSignatures));
                }

                provider.applyBatch(operations);

                setLastUpdatedTime(provider, timestamp);
            }

        } catch (TeambrellaException | RemoteException | OperationApplicationException e) {
            Log.e(LOG_TAG, e.toString());
        }
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


    private static JsonObject getRequestBody(ContentProviderClient client) throws RemoteException {
        JsonObject body = new JsonObject();
        Cursor cursor = client.query(TeambrellaRepository.Connection.CONTENT_URI, new String[]{TeambrellaRepository.Connection.LAST_UPDATED}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String value = cursor.getString(cursor.getColumnIndex(TeambrellaRepository.Connection.LAST_UPDATED));
            long lastUpdated = 0;
            if (value != null) {
                lastUpdated = Long.parseLong(value);
            }
            if (lastUpdated > 0) {
                body.add(TeambrellaModel.ATTR_DATA_LAST_UPDATED, new JsonPrimitive(lastUpdated));
            }
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

            body.add(TeambrellaModel.ATTR_DATA_TX_INFOS, txArray);
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
                signature.add(TeambrellaModel.ATTR_DATA_TX_INPUT_ID, new JsonPrimitive(cursor.getInt(cursor.getColumnIndex(TeambrellaRepository.TXSignature.TX_INPUT_ID))));
                signaturesArray.add(signature);
            } while (cursor.moveToNext());

            body.add(TeambrellaModel.ATTR_DATA_TX_SIGNATURES, signaturesArray);
        }

        if (cursor != null) {
            cursor.close();
        }


        return body;
    }
}

package com.teambrella.android.content.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.content.TeambrellaRepository;
import com.teambrella.android.content.TeambrellaRepository.Connection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * Teambrella Sync Adapter
 */
class TeambrellaSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = TeambrellaSyncAdapter.class.getSimpleName();

    private SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    TeambrellaSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mSDF.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        final Context context = getContext();
        final TeambrellaServer server = new TeambrellaServer(context);
        try {
            updateConnectionTime(provider);
            JsonObject result = server.execute(TeambrellaUris.getUpdates());

            if (result != null) {


                ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                JsonElement teammatesElement = result.get(TeambrellaModel.ATTR_DATA_TEAMMATES);
                ITeammate[] teammates = teammatesElement != null && !teammatesElement.isJsonNull() ?
                        Factory.fromArray(teammatesElement.getAsJsonArray(), JsonTeammate.class) : null;

                if (teammates != null) {
                    operations.addAll(insertTeammates(provider, teammates));
                }

                JsonElement teamsElement = result.get(TeambrellaModel.ATTR_DATA_TEAMS);
                ITeam[] teams = teamsElement != null && !teamsElement.isJsonNull() ?
                        Factory.fromArray(teamsElement.getAsJsonArray(), JsonTeam.class) : null;

                if (teams != null) {
                    operations.addAll(insertTeams(provider, teams));
                }

                JsonElement payTosElement = result.get(TeambrellaModel.ATTR_DATA_PAY_TOS);
                IPayTo[] payTos = payTosElement != null && !payTosElement.isJsonNull() ?
                        Factory.fromArray(payTosElement.getAsJsonArray(), JsonPayTo.class) : null;

                if (payTosElement != null) {
                    operations.addAll(insertPayTos(provider, payTos));
                }

                JsonElement btcAddressesElement = result.get(TeambrellaModel.ATTR_DATA_BTC_ADDRESSES);
                IBTCAddress[] btcAddresses = btcAddressesElement != null && !btcAddressesElement.isJsonNull() ?
                        Factory.fromArray(btcAddressesElement.getAsJsonArray(), JsonBTCAddress.class) : null;

                if (btcAddresses != null) {
                    operations.addAll(insertBTCAddresses(btcAddresses));
                }

                JsonElement cosignersElement = result.get(TeambrellaModel.ATTR_DATA_COSIGNERS);
                ICosigner[] cosigners = cosignersElement != null && !cosignersElement.isJsonNull() ?
                        Factory.fromArray(cosignersElement.getAsJsonArray(), JsonCosigner.class) : null;

                if (cosigners != null) {
                    operations.addAll(insertCosigners(provider, cosigners));
                }

                JsonElement txsElement = result.get(TeambrellaModel.ATTR_DATA_TXS);
                ITx[] txs = txsElement != null && !txsElement.isJsonNull() ?
                        Factory.fromArray(txsElement.getAsJsonArray(), JsonTX.class) : null;
                if (txs != null) {
                    insertTx(provider, txs);
                }

                provider.applyBatch(operations);
            }

            setLastUpdatedTime(provider, new Date());
        } catch (TeambrellaException | RemoteException | OperationApplicationException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    private List<ContentProviderOperation> insertTeammates(ContentProviderClient provider, ITeammate[] teammates) {
        List<ContentProviderOperation> list = new LinkedList<>();
        for (ITeammate teammate : teammates) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.Teammate.ID, teammate.getId());
            cv.put(TeambrellaRepository.Teammate.TEAM_ID, teammate.getId());
            cv.put(TeambrellaRepository.Teammate.NAME, teammate.getName());
            cv.put(TeambrellaRepository.Teammate.FB_NAME, teammate.getFacebookName());
            cv.put(TeambrellaRepository.Teammate.PUBLIC_KEY, teammate.getPublicKey());
            list.add(ContentProviderOperation.newInsert(TeambrellaRepository.Teammate.CONTENT_URI).withValues(cv).build());
        }
        return list;
    }

    private List<ContentProviderOperation> insertTeams(ContentProviderClient provider, ITeam[] teams) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();
        for (ITeam team : teams) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.Team.ID, team.getId());
            cv.put(TeambrellaRepository.Team.NAME, team.getName());
            cv.put(TeambrellaRepository.Team.TESTNET, team.isTestNet());
            list.add(ContentProviderOperation.newInsert(TeambrellaRepository.Team.CONTENT_URI)
                    .withValues(cv).build());
        }
        return list;
    }


    private List<ContentProviderOperation> insertPayTos(ContentProviderClient client, IPayTo[] payTos) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();
        for (IPayTo payTo : payTos) {
            Cursor cursor = client.query(TeambrellaRepository.PayTo.CONTENT_URI, null, TeambrellaRepository.PayTo.ID + "=? AND " +
                    TeambrellaRepository.PayTo.TEAMMATE_ID + "= ?", new String[]{payTo.getId(), Long.toString(payTo.getTeamId())}, null);
            boolean isDefault = payTo.isDefault();
            String address = payTo.getAddress();
            if (cursor != null && cursor.moveToFirst()) {
                isDefault = cursor.getInt(cursor.getColumnIndex(TeambrellaRepository.PayTo.IS_DEFAULT)) > 0;
                address = cursor.getString(cursor.getColumnIndex(TeambrellaRepository.PayTo.ADDRESS));
            } else {
                ContentValues cv = new ContentValues();
                cv.put(TeambrellaRepository.PayTo.ID, payTo.getId());
                cv.put(TeambrellaRepository.PayTo.TEAMMATE_ID, payTo.getTeamId());
                cv.put(TeambrellaRepository.PayTo.ADDRESS, payTo.getAddress());
                cv.put(TeambrellaRepository.PayTo.KNOWN_SINCE, mSDF.format(new Date()));
                cv.put(TeambrellaRepository.PayTo.IS_DEFAULT, payTo.isDefault());
                list.add(ContentProviderOperation.newInsert(TeambrellaRepository.PayTo.CONTENT_URI).withValues(cv).build());
            }

            if (cursor != null) {
                cursor.close();
            }

            if (isDefault) {
                ContentValues cv = new ContentValues();
                cv.put(TeambrellaRepository.PayTo.IS_DEFAULT, false);
                list.add(ContentProviderOperation.newUpdate(TeambrellaRepository.PayTo.CONTENT_URI).withValues(cv)
                        .withSelection(TeambrellaRepository.PayTo.TEAMMATE_ID + "=? AND "
                                + TeambrellaRepository.PayTo.ADDRESS + "!=?", new String[]{Long.toString(payTo.getTeamId()), address}).build());
            }
        }
        return list;
    }

    private List<ContentProviderOperation> insertBTCAddresses(IBTCAddress[] btcAddresses) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();

        for (IBTCAddress btcAddress : btcAddresses) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.BTCAddress.ADDRESS, btcAddress.getAddress());
            cv.put(TeambrellaRepository.BTCAddress.TEAMMATE_ID, btcAddress.getTeammateId());
            cv.put(TeambrellaRepository.BTCAddress.DATE_CREATED, btcAddress.getCreatedDate());
            cv.put(TeambrellaRepository.BTCAddress.STATUS, btcAddress.getStatus());
            list.add(ContentProviderOperation.newInsert(TeambrellaRepository.BTCAddress.CONTENT_URI).withValues(cv).build());
        }
        return list;
    }


    private List<ContentProviderOperation> insertCosigners(ContentProviderClient client, ICosigner[] cosigners) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();
        HashMap<String, Set<ICosigner>> cosignersMap = new HashMap<>();
        for (ICosigner cosigner : cosigners) {
            Set<ICosigner> set = cosignersMap.get(cosigner.getAddressId());
            if (set == null) {
                set = new HashSet<>();
                cosignersMap.put(cosigner.getAddressId(), set);
            }
            set.add(cosigner);
        }

        for (String addressId : cosignersMap.keySet()) {
            Cursor cursor = client.query(TeambrellaRepository.Cosigner.CONTENT_URI, null, TeambrellaRepository.Cosigner.ADDRESS_ID + "=?",
                    new String[]{addressId}, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                for (ICosigner cosigner : cosignersMap.get(addressId)) {
                    ContentValues cv = new ContentValues();
                    cv.put(TeambrellaRepository.Cosigner.ADDRESS_ID, cosigner.getAddressId());
                    cv.put(TeambrellaRepository.Cosigner.TEAMMATE_ID, cosigner.getTeammateId());
                    cv.put(TeambrellaRepository.Cosigner.KEY_ORDER, cosigner.getKeyOrder());
                    list.add(ContentProviderOperation.newInsert(TeambrellaRepository.Cosigner.CONTENT_URI)
                            .withValues(cv).build());
                }
            }

            if (cursor != null) {
                cursor.close();
            }
        }

        return list;
    }


    private void insertTXInputs(ContentProviderClient client, ITx[] txs, ITxInput[] txInputs) {

    }

    private void insertTXOutputs(ContentProviderClient client, ITx[] txs, ITxOutput[] txOutputs) {

    }

    private void insertTXSignatures(ContentProviderClient client, ITxInput[] txInputs, ITxSignature[] txSignatures) {

    }

    private List<ContentProviderOperation> insertTx(ContentProviderClient client, ITx[] txs) throws RemoteException {

        List<ContentProviderOperation> list = new LinkedList<>();

        for (ITx tx : txs) {
            Cursor cursor = client.query(TeambrellaRepository.Tx.CONTENT_URI, null, TeambrellaRepository.Tx.ID + "=?",
                    new String[]{tx.getId()}, null, null);

            if (cursor != null && cursor.moveToNext()) {
                ContentValues cv = new ContentValues();
                cv.put(TeambrellaRepository.Tx.STATE, tx.getState());
                client.update(TeambrellaRepository.Tx.CONTENT_URI, cv, TeambrellaRepository.Tx.ID + "=?",
                        new String[]{tx.getId()});
            } else {
                ContentValues cv = new ContentValues();
                cv.put(TeambrellaRepository.Tx.ID, tx.getId());
                cv.put(TeambrellaRepository.Tx.TEAMMATE_ID, tx.getTeammateId());
                cv.put(TeambrellaRepository.Tx.AMOUNT_BTC, tx.getBTCAmount());
                cv.put(TeambrellaRepository.Tx.CLAIM_ID, tx.getClaimId());
                cv.put(TeambrellaRepository.Tx.CLAIM_TEAMMATE_ID, tx.getClaimTeammateId());
                cv.put(TeambrellaRepository.Tx.WITHDRAW_REQ_ID, tx.getClaimTeammateId());
                cv.put(TeambrellaRepository.Tx.STATE, tx.getState());
                cv.put(TeambrellaRepository.Tx.KIND, tx.getKind());
                cv.put(TeambrellaRepository.Tx.INITIATED_TIME, tx.getInitiatedTime());
                cv.put(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, false);
                cv.putNull(TeambrellaRepository.Tx.CLIENT_RESOLUTION_TIME);
                list.add(ContentProviderOperation.newInsert(TeambrellaRepository.Tx.CONTENT_URI)
                        .withValues(cv).build());
            }

            if (cursor != null) {
                cursor.close();
            }
        }

        return list;
    }

    private void updateConnectionTime(ContentProviderClient provider) throws RemoteException {
        Cursor cursor = provider.query(Connection.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            ContentValues cv = new ContentValues();
            cv.put(Connection.LAST_CONNECTED, mSDF.format(new Date()));
            if (cursor.moveToFirst()) {
                provider.update(Connection.CONTENT_URI, cv, Connection.ID + "=?",
                        new String[]{cursor.getString(cursor.getColumnIndex(Connection.ID))});
            } else {
                provider.insert(Connection.CONTENT_URI, cv);
            }
            cursor.close();
        }
    }

    private void setLastUpdatedTime(ContentProviderClient provider, Date date) throws RemoteException {
        Cursor cursor = provider.query(Connection.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            ContentValues cv = new ContentValues();
            cv.put(Connection.LAST_UPDATED, mSDF.format(date));
            if (cursor.moveToFirst()) {
                provider.update(Connection.CONTENT_URI, cv, Connection.ID + "=?",
                        new String[]{cursor.getString(cursor.getColumnIndex(Connection.ID))});
            }
            cursor.close();
        }
    }
}

package com.teambrella.android.content.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.IBTCAddress;
import com.teambrella.android.api.model.ICosigner;
import com.teambrella.android.api.model.IPayTo;
import com.teambrella.android.api.model.ITeam;
import com.teambrella.android.api.model.ITeammate;
import com.teambrella.android.api.model.ITx;
import com.teambrella.android.api.model.json.Factory;
import com.teambrella.android.api.model.json.JsonBTCAddress;
import com.teambrella.android.api.model.json.JsonCosigner;
import com.teambrella.android.api.model.json.JsonPayTo;
import com.teambrella.android.api.model.json.JsonTeam;
import com.teambrella.android.api.model.json.JsonTeammate;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.content.TeambrellaRepository;
import com.teambrella.android.content.TeambrellaRepository.Connection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
                insertTeammates(provider, Factory.fromArray(result.get(TeambrellaModel.ATTR_DATA_TEAMMATES).getAsJsonArray(), JsonTeammate.class));
                insertTeams(provider, Factory.fromArray(result.get(TeambrellaModel.ATTR_DATA_TEAMS).getAsJsonArray(), JsonTeam.class));
                insertPayTos(provider, Factory.fromArray(result.get(TeambrellaModel.ATTR_DATA_PAY_TOS).getAsJsonArray(), JsonPayTo.class));
                insertBTCAddresses(provider, Factory.fromArray(result.get(TeambrellaModel.ATTR_DATA_BTC_ADDRESSES).getAsJsonArray(), JsonBTCAddress.class));
                insertCosigners(provider, Factory.fromArray(result.get(TeambrellaModel.ATTR_DATA_COSIGNERS).getAsJsonArray(), JsonCosigner.class));
            }

            setLastUpdatedTime(provider, new Date());
        } catch (TeambrellaException | RemoteException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    private void insertTeammates(ContentProviderClient provider, ITeammate[] teammates) throws RemoteException {
        for (ITeammate teammate : teammates) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.Teammate.ID, teammate.getId());
            cv.put(TeambrellaRepository.Teammate.TEAM_ID, teammate.getId());
            cv.put(TeambrellaRepository.Teammate.NAME, teammate.getName());
            cv.put(TeambrellaRepository.Teammate.FB_NAME, teammate.getFacebookName());
            cv.put(TeambrellaRepository.Teammate.PUBLIC_KEY, teammate.getPublicKey());
            provider.insert(TeambrellaRepository.Teammate.CONTENT_URI, cv);
        }
    }

    private void insertTeams(ContentProviderClient provider, ITeam[] teams) throws RemoteException {
        for (ITeam team : teams) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.Team.ID, team.getId());
            cv.put(TeambrellaRepository.Team.NAME, team.getName());
            cv.put(TeambrellaRepository.Team.TESTNET, team.isTestNet());
            provider.insert(TeambrellaRepository.Team.CONTENT_URI, cv);
        }
    }


    private void insertPayTos(ContentProviderClient client, IPayTo[] payTos) throws RemoteException {
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
                client.insert(TeambrellaRepository.PayTo.CONTENT_URI, cv);
            }

            if (cursor != null) {
                cursor.close();
            }

            if (isDefault) {
                ContentValues cv = new ContentValues();
                cv.put(TeambrellaRepository.PayTo.IS_DEFAULT, false);
                client.update(TeambrellaRepository.PayTo.CONTENT_URI, cv, TeambrellaRepository.PayTo.TEAMMATE_ID + "=? AND "
                        + TeambrellaRepository.PayTo.ADDRESS + "!=?", new String[]{Long.toString(payTo.getTeamId()), address});
            }
        }
    }

    private void insertBTCAddresses(ContentProviderClient client, IBTCAddress[] btcAddresses) throws RemoteException {
        for (IBTCAddress btcAddress : btcAddresses) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.BTCAddress.ADDRESS, btcAddress.getAddress());
            cv.put(TeambrellaRepository.BTCAddress.TEAMMATE_ID, btcAddress.getTeammateId());
            cv.put(TeambrellaRepository.BTCAddress.DATE_CREATED, btcAddress.getCreatedDate());
            cv.put(TeambrellaRepository.BTCAddress.STATUS, btcAddress.getStatus());
            client.insert(TeambrellaRepository.BTCAddress.CONTENT_URI, cv);
        }
    }


    private void insertCosigners(ContentProviderClient client, ICosigner[] cosigners) throws RemoteException {

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
                    client.insert(TeambrellaRepository.Cosigner.CONTENT_URI, cv);
                }
            }

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void insertTx(ContentProviderClient client, ITx[] txs) throws RemoteException {
        for (ITx tx : txs) {
            Cursor cursor = client.query(TeambrellaRepository.Tx.CONTENT_URI, null, TeambrellaRepository.Tx.ID + "=?",
                    new String[]{tx.getId()}, null, null);

            if (cursor != null && cursor.moveToNext()) {
                ContentValues cv = new ContentValues();
                cv.put(TeambrellaRepository.Tx.STATE, tx.getState());
                client.update(TeambrellaRepository.Tx.CONTENT_URI, cv, TeambrellaRepository.Tx.ID + "=?",
                        new String[]{tx.getId()});
            } else {

            }


            if (cursor != null) {
                cursor.close();
            }
        }
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

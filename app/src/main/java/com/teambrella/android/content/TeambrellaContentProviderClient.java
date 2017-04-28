package com.teambrella.android.content;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Base64;

import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.model.BTCAddress;
import com.teambrella.android.content.model.Cosigner;
import com.teambrella.android.content.model.PayTo;
import com.teambrella.android.content.model.TXSignature;
import com.teambrella.android.content.model.Team;
import com.teambrella.android.content.model.Teammate;
import com.teambrella.android.content.model.Tx;
import com.teambrella.android.content.model.TxInput;
import com.teambrella.android.content.model.TxOutput;

import org.chalup.microorm.MicroOrm;
import org.chalup.microorm.TypeAdapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.functions.Predicate;

/**
 * Teambrella Content Provider Client
 */
public class TeambrellaContentProviderClient {


    private static MicroOrm sMicroOrm = new MicroOrm.Builder().registerTypeAdapter(UUID.class, new TypeAdapter<UUID>() {
        @Override
        public UUID fromCursor(Cursor c, String columnName) {
            return UUID.fromString(c.getString(c.getColumnIndex(columnName)));
        }

        @Override
        public void toContentValues(ContentValues values, String columnName, UUID object) {
            values.put(columnName, object.toString());
        }
    }).build();


    private final ContentProviderClient mClient;


    public TeambrellaContentProviderClient(ContentProviderClient client) {
        mClient = client;
    }

    /**
     * Insert teambrella's teams
     *
     * @param teams list of teams
     * @return a list of operations to apply
     */
    public List<ContentProviderOperation> insertTeams(Team[] teams) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();
        for (Team team : teams) {
            Cursor cursor = mClient.query(TeambrellaRepository.Team.CONTENT_URI, new String[]{TeambrellaRepository.Team.NAME},
                    TeambrellaRepository.Team.ID + "=?", new String[]{Long.toString(team.id)}, null);

            if (cursor != null && cursor.moveToFirst()) {
                list.add(ContentProviderOperation.newUpdate(TeambrellaRepository.Team.CONTENT_URI)
                        .withValue(TeambrellaRepository.Team.NAME, team.name).build());
                cursor.close();
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                ContentValues cv = new ContentValues();
                cv.put(TeambrellaRepository.Team.ID, team.id);
                cv.put(TeambrellaRepository.Team.NAME, team.name);
                cv.put(TeambrellaRepository.Team.TESTNET, team.testNet);
                list.add(ContentProviderOperation.newInsert(TeambrellaRepository.Team.CONTENT_URI)
                        .withValues(cv).build());
            }
        }
        return list;
    }

    /**
     * Insert teambrella's teammates
     *
     * @param teammates list of teammates
     * @return a list of operations to apply
     */
    public List<ContentProviderOperation> insertTeammates(Teammate[] teammates) {
        List<ContentProviderOperation> list = new LinkedList<>();
        for (Teammate teammate : teammates) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.Teammate.ID, teammate.id);
            cv.put(TeambrellaRepository.Teammate.TEAM_ID, teammate.teamId);
            cv.put(TeambrellaRepository.Teammate.NAME, teammate.name);
            cv.put(TeambrellaRepository.Teammate.FB_NAME, teammate.facebookName);
            cv.put(TeambrellaRepository.Teammate.PUBLIC_KEY, teammate.publicKey);
            list.add(ContentProviderOperation.newInsert(TeambrellaRepository.Teammate.CONTENT_URI).withValues(cv).build());
        }
        return list;
    }


    public List<ContentProviderOperation> insertPayTos(PayTo[] payTos) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();
        for (PayTo payTo : payTos) {
            Cursor cursor = mClient.query(TeambrellaRepository.PayTo.CONTENT_URI, null, TeambrellaRepository.PayTo.ID + "=? AND " +
                    TeambrellaRepository.PayTo.TEAMMATE_ID + "= ?", new String[]{payTo.id, Long.toString(payTo.teammateId)}, null);
            boolean isDefault = payTo.isDefault;
            String address = payTo.address;
            if (cursor != null && cursor.moveToFirst()) {
                isDefault = cursor.getInt(cursor.getColumnIndex(TeambrellaRepository.PayTo.IS_DEFAULT)) > 0;
                address = cursor.getString(cursor.getColumnIndex(TeambrellaRepository.PayTo.ADDRESS));
            } else {
                ContentValues cv = new ContentValues();
                cv.put(TeambrellaRepository.PayTo.ID, payTo.id);
                cv.put(TeambrellaRepository.PayTo.TEAMMATE_ID, payTo.teammateId);
                cv.put(TeambrellaRepository.PayTo.ADDRESS, payTo.address);
                //cv.put(TeambrellaRepository.PayTo.KNOWN_SINCE, mSDF.format(new Date()));
                cv.put(TeambrellaRepository.PayTo.IS_DEFAULT, payTo.isDefault);
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
                                + TeambrellaRepository.PayTo.ADDRESS + "!=?", new String[]{Long.toString(payTo.teammateId), address}).build());
            }
        }
        return list;
    }


    /**
     * Insert cosigners
     *
     * @param cosigners list of cosigners
     * @return a list of operations to apply
     */
    public List<ContentProviderOperation> insertCosigners(Cosigner[] cosigners) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();
        HashMap<String, Set<Cosigner>> cosignersMap = new HashMap<>();
        for (Cosigner cosigner : cosigners) {
            Set<Cosigner> set = cosignersMap.get(cosigner.addressId);
            if (set == null) {
                set = new HashSet<>();
                cosignersMap.put(cosigner.addressId, set);
            }
            set.add(cosigner);
        }

        for (String addressId : cosignersMap.keySet()) {
            Cursor cursor = mClient.query(TeambrellaRepository.Cosigner.CONTENT_URI, null, TeambrellaRepository.Cosigner.ADDRESS_ID + "=?",
                    new String[]{addressId}, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                for (Cosigner cosigner : cosignersMap.get(addressId)) {
                    ContentValues cv = new ContentValues();
                    cv.put(TeambrellaRepository.Cosigner.ADDRESS_ID, cosigner.addressId);
                    cv.put(TeambrellaRepository.Cosigner.TEAMMATE_ID, cosigner.teammateId);
                    cv.put(TeambrellaRepository.Cosigner.KEY_ORDER, cosigner.keyOrder);
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


    /**
     * Insert inputs
     *
     * @param txs      transactions
     * @param txInputs inputs
     * @return a list of operations to apply
     */
    public List<ContentProviderOperation> insertTXInputs(Tx[] txs, TxInput[] txInputs) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();

        for (final TxInput txInput : txInputs) {
            Cursor cursor = mClient.query(TeambrellaRepository.TXInput.CONTENT_URI, new String[]{TeambrellaRepository.TXInput.ID}, TeambrellaRepository.TXInput.ID + "=?",
                    new String[]{txInput.id.toString()}, null);

            if (cursor != null && cursor.moveToFirst()) {
                cursor.close();
                continue;
            }

            if (cursor != null) {
                cursor.close();
            }

            long count = Observable.fromArray(txs).filter(new Predicate<Tx>() {
                @Override
                public boolean test(Tx iTx) throws Exception {
                    return iTx.id.equals(txInput.id);
                }
            }).count().blockingGet();


            if (count > 0) {
                list.add(ContentProviderOperation.newInsert(TeambrellaRepository.TXInput.CONTENT_URI)
                        .withValue(TeambrellaRepository.TXInput.ID, txInput.id)
                        .withValue(TeambrellaRepository.TXInput.TX_ID, txInput.txId)
                        .withValue(TeambrellaRepository.TXInput.AMMOUNT_BTC, txInput.btcAmount)
                        .withValue(TeambrellaRepository.TXInput.PREV_TX_ID, txInput.previousTxId)
                        .withValue(TeambrellaRepository.TXInput.PREV_TX_INDEX, txInput.previousTxId)
                        .build());
            }

        }
        return list;
    }

    /**
     * Insert outputs
     *
     * @param txs       transactions
     * @param txOutputs outputs
     * @return list of operations to apply
     */
    public List<ContentProviderOperation> insertTXOutputs(Tx[] txs, TxOutput[] txOutputs) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();
        for (final TxOutput txOutput : txOutputs) {
            Cursor cursor = mClient.query(TeambrellaRepository.TXOutput.CONTENT_URI, new String[]{TeambrellaRepository.TXOutput.ID}, TeambrellaRepository.TXOutput.ID + "=?",
                    new String[]{txOutput.id.toString()}, null);

            if (cursor != null && cursor.moveToFirst()) {
                cursor.close();
                continue;
            }

            if (cursor != null) {
                cursor.close();

            }

            long count = Observable.fromArray(txs).filter(new Predicate<Tx>() {
                @Override
                public boolean test(Tx iTx) throws Exception {
                    return iTx.id.equals(txOutput.txId);
                }
            }).count().blockingGet();

            if (count > 0) {
                list.add(ContentProviderOperation.newInsert(TeambrellaRepository.TXOutput.CONTENT_URI)
                        .withValue(TeambrellaRepository.TXOutput.ID, txOutput.id)
                        .withValue(TeambrellaRepository.TXOutput.TX_ID, txOutput.txId)
                        .withValue(TeambrellaRepository.TXOutput.AMOUNT_BTC, txOutput.btcAmount)
                        .withValue(TeambrellaRepository.TXOutput.PAY_TO_ID, txOutput.payToId)
                        .build());
            }

        }

        return list;
    }

    /**
     * Insert BTC Addresses
     *
     * @param btcAddresses addresses
     * @return a list of operations
     */
    public List<ContentProviderOperation> insertBTCAddresses(BTCAddress[] btcAddresses) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();

        for (BTCAddress btcAddress : btcAddresses) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.BTCAddress.ADDRESS, btcAddress.address);
            cv.put(TeambrellaRepository.BTCAddress.TEAMMATE_ID, btcAddress.teammateId);
            cv.put(TeambrellaRepository.BTCAddress.DATE_CREATED, btcAddress.dateCreated);
            cv.put(TeambrellaRepository.BTCAddress.STATUS, btcAddress.status);
            list.add(ContentProviderOperation.newInsert(TeambrellaRepository.BTCAddress.CONTENT_URI).withValues(cv).build());
        }
        return list;
    }


    public List<ContentProviderOperation> insertTXSignatures(TxInput[] txInputs, TXSignature[] txSignatures) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();

        for (final TXSignature txSignature : txSignatures) {
            if (hasRecord(mClient, TeambrellaRepository.TXSignature.CONTENT_URI, new String[]{TeambrellaRepository.TXSignature.TX_INPUT_ID, TeambrellaRepository.TXSignature.TEAMMATE_ID},
                    new String[]{txSignature.txInputId, Long.toString(txSignature.teammateId)})) {
                continue;
            }

            if (!hasRecord(mClient, TeambrellaRepository.TXInput.CONTENT_URI, new String[]{TeambrellaRepository.TXInput.ID},
                    new String[]{txSignature.txInputId})) {
                long count = Observable.fromArray(txInputs).filter(new Predicate<TxInput>() {
                    @Override
                    public boolean test(TxInput iTxInput) throws Exception {
                        return iTxInput.id.equals(txSignature.txInputId);
                    }
                }).count().blockingGet();

                if (count == 0) {
                    continue;
                }
            }

            list.add(ContentProviderOperation.newInsert(TeambrellaRepository.TXSignature.CONTENT_URI)
                    .withValue(TeambrellaRepository.TXSignature.ID, txSignature.id)
                    .withValue(TeambrellaRepository.TXSignature.TEAMMATE_ID, txSignature.teammateId)
                    .withValue(TeambrellaRepository.TXSignature.TX_INPUT_ID, txSignature.txInputId)
                    .withValue(TeambrellaRepository.TXSignature.SIGNATURE, Base64.decode(txSignature.signature, Base64.DEFAULT))
                    .withValue(TeambrellaRepository.TXSignature.NEED_UPDATE_SERVER, false)
                    .build());


        }


        return list;
    }

    public List<ContentProviderOperation> insertTx(Tx[] txs) throws
            RemoteException {

        List<ContentProviderOperation> list = new LinkedList<>();

        for (Tx tx : txs) {
            Cursor cursor = mClient.query(TeambrellaRepository.Tx.CONTENT_URI, null, TeambrellaRepository.Tx.ID + "=?",
                    new String[]{tx.id.toString()}, null, null);

            if (cursor != null && cursor.moveToNext()) {
                ContentValues cv = new ContentValues();
                cv.put(TeambrellaRepository.Tx.STATE, tx.state);
                list.add(ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI).withValues(cv).withSelection(TeambrellaRepository.Tx.ID + "=?",
                        new String[]{tx.id.toString()}).build());
            } else {
                ContentValues cv = new ContentValues();
                cv.put(TeambrellaRepository.Tx.ID, tx.id.toString());
                cv.put(TeambrellaRepository.Tx.TEAMMATE_ID, tx.teammateId);
                cv.put(TeambrellaRepository.Tx.AMOUNT_BTC, tx.btcAmount);
                cv.put(TeambrellaRepository.Tx.CLAIM_ID, tx.claimId);
                cv.put(TeambrellaRepository.Tx.CLAIM_TEAMMATE_ID, tx.claimTeammateId);
                cv.put(TeambrellaRepository.Tx.WITHDRAW_REQ_ID, tx.withdrawReqId);
                cv.put(TeambrellaRepository.Tx.STATE, tx.state);
                cv.put(TeambrellaRepository.Tx.KIND, tx.kind);
                cv.put(TeambrellaRepository.Tx.INITIATED_TIME, tx.initiatedTime);
                cv.put(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, false);
//                cv.put(TeambrellaRepository.Tx.RECEIVED_TIME, mSDF.format(new Date()));
//                cv.put(TeambrellaRepository.Tx.UPDATE_TIME, mSDF.format(new Date()));
                cv.put(TeambrellaRepository.Tx.RESOLUTION, TeambrellaModel.TX_CLIENT_RESOLUTION_NONE);
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

    public <T> List<T> queryList(Uri uri, String selection, String[] selectionArgs, Class<T> tClass) throws RemoteException {
        Cursor cursor = mClient.query(uri, null, selection, selectionArgs, null, null);
        List<T> result = new LinkedList<>();
        if (cursor != null) {
            result = sMicroOrm.listFromCursor(cursor, tClass);
            cursor.close();
        }
        return result;
    }

    public <T> T queryOne(Uri uri, String selection, String[] selectionArgs, Class<T> tClass) throws RemoteException {
        Cursor cursor = mClient.query(uri, null, selection, selectionArgs, null, null);
        T result = null;
        if (cursor != null && cursor.moveToFirst()) {
            result = sMicroOrm.fromCursor(cursor, tClass);
            cursor.close();
        }
        return result;
    }


    private static boolean hasRecord(ContentProviderClient client, Uri uri, String[] fields, String[] values) throws RemoteException {
        boolean result = false;
        String selection = "";
        for (int i = 0; i < fields.length; i++) {
            selection += (fields[i] + "=?");
            if (i != fields.length - 1) {
                selection += " AND ";
            }
        }
        Cursor cursor = client.query(uri, null, selection, values, null);

        if (cursor != null) {
            result = cursor.moveToFirst();
            cursor.close();
        }

        return result;
    }

}

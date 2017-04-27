package com.teambrella.android.content;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Base64;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.functions.Predicate;

/**
 * Teambrella Content Provider Client
 */
public class TeambrellaContentProviderClient {

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
    public List<ContentProviderOperation> insertTeams(ITeam[] teams) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();
        for (ITeam team : teams) {
            Cursor cursor = mClient.query(TeambrellaRepository.Team.CONTENT_URI, new String[]{TeambrellaRepository.Team.NAME},
                    TeambrellaRepository.Team.ID + "=?", new String[]{Long.toString(team.getId())}, null);

            if (cursor != null && cursor.moveToFirst()) {
                list.add(ContentProviderOperation.newUpdate(TeambrellaRepository.Team.CONTENT_URI)
                        .withValue(TeambrellaRepository.Team.NAME, team.getName()).build());
                cursor.close();
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                ContentValues cv = new ContentValues();
                cv.put(TeambrellaRepository.Team.ID, team.getId());
                cv.put(TeambrellaRepository.Team.NAME, team.getName());
                cv.put(TeambrellaRepository.Team.TESTNET, team.isTestNet());
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
    public List<ContentProviderOperation> insertTeammates(ITeammate[] teammates) {
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


    public List<ContentProviderOperation> insertPayTos(IPayTo[] payTos) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();
        for (IPayTo payTo : payTos) {
            Cursor cursor = mClient.query(TeambrellaRepository.PayTo.CONTENT_URI, null, TeambrellaRepository.PayTo.ID + "=? AND " +
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
                //cv.put(TeambrellaRepository.PayTo.KNOWN_SINCE, mSDF.format(new Date()));
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


    /**
     * Insert cosigners
     *
     * @param cosigners list of cosigners
     * @return a list of operations to apply
     */
    public List<ContentProviderOperation> insertCosigners(ICosigner[] cosigners) throws RemoteException {
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
            Cursor cursor = mClient.query(TeambrellaRepository.Cosigner.CONTENT_URI, null, TeambrellaRepository.Cosigner.ADDRESS_ID + "=?",
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


    /**
     * Insert inputs
     *
     * @param txs      transactions
     * @param txInputs inputs
     * @return a list of operations to apply
     */
    public List<ContentProviderOperation> insertTXInputs(ITx[] txs, ITxInput[] txInputs) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();

        for (final ITxInput txInput : txInputs) {
            Cursor cursor = mClient.query(TeambrellaRepository.TXInput.CONTENT_URI, new String[]{TeambrellaRepository.TXInput.ID}, TeambrellaRepository.TXInput.ID + "=?",
                    new String[]{txInput.getId()}, null);

            if (cursor != null && cursor.moveToFirst()) {
                cursor.close();
                continue;
            }

            if (cursor != null) {
                cursor.close();
            }

            long count = Observable.fromArray(txs).filter(new Predicate<ITx>() {
                @Override
                public boolean test(ITx iTx) throws Exception {
                    return iTx.getId().equals(txInput.getId());
                }
            }).count().blockingGet();


            if (count > 0) {
                list.add(ContentProviderOperation.newInsert(TeambrellaRepository.TXInput.CONTENT_URI)
                        .withValue(TeambrellaRepository.TXInput.ID, txInput.getId())
                        .withValue(TeambrellaRepository.TXInput.TX_ID, txInput.getTxId())
                        .withValue(TeambrellaRepository.TXInput.AMMOUNT_BTC, txInput.getBTCAmount())
                        .withValue(TeambrellaRepository.TXInput.PREV_TX_ID, txInput.getPreviousTxId())
                        .withValue(TeambrellaRepository.TXInput.PREV_TX_INDEX, txInput.getPreviousTxIndex())
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
    public List<ContentProviderOperation> insertTXOutputs(ITx[] txs, ITxOutput[] txOutputs) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();
        for (final ITxOutput txOutput : txOutputs) {
            Cursor cursor = mClient.query(TeambrellaRepository.TXOutput.CONTENT_URI, new String[]{TeambrellaRepository.TXOutput.ID}, TeambrellaRepository.TXOutput.ID + "=?",
                    new String[]{txOutput.getId()}, null);

            if (cursor != null && cursor.moveToFirst()) {
                cursor.close();
                continue;
            }

            if (cursor != null) {
                cursor.close();

            }

            long count = Observable.fromArray(txs).filter(new Predicate<ITx>() {
                @Override
                public boolean test(ITx iTx) throws Exception {
                    return iTx.getId().equals(txOutput.getId());
                }
            }).count().blockingGet();

            if (count > 0) {
                list.add(ContentProviderOperation.newInsert(TeambrellaRepository.TXOutput.CONTENT_URI)
                        .withValue(TeambrellaRepository.TXOutput.ID, txOutput.getId())
                        .withValue(TeambrellaRepository.TXOutput.TX_ID, txOutput.getTxId())
                        .withValue(TeambrellaRepository.TXOutput.AMOUNT_BTC, txOutput.getBTCAmount())
                        .withValue(TeambrellaRepository.TXOutput.PAY_TO_ID, txOutput.getPayToId())
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
    public List<ContentProviderOperation> insertBTCAddresses(IBTCAddress[] btcAddresses) throws RemoteException {
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


    public List<ContentProviderOperation> insertTXSignatures(ITxInput[] txInputs, ITxSignature[] txSignatures) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();

        for (final ITxSignature txSignature : txSignatures) {
            if (hasRecord(mClient, TeambrellaRepository.TXSignature.CONTENT_URI, new String[]{TeambrellaRepository.TXSignature.TX_INPUT_ID, TeambrellaRepository.TXSignature.TEAMMATE_ID},
                    new String[]{txSignature.getTxInputId(), Long.toString(txSignature.getTeammateId())})) {
                continue;
            }

            if (!hasRecord(mClient, TeambrellaRepository.TXInput.CONTENT_URI, new String[]{TeambrellaRepository.TXInput.ID},
                    new String[]{txSignature.getTxInputId()})) {
                long count = Observable.fromArray(txInputs).filter(new Predicate<ITxInput>() {
                    @Override
                    public boolean test(ITxInput iTxInput) throws Exception {
                        return iTxInput.getId().equals(txSignature.getTxInputId());
                    }
                }).count().blockingGet();

                if (count == 0) {
                    continue;
                }
            }

            list.add(ContentProviderOperation.newInsert(TeambrellaRepository.TXSignature.CONTENT_URI)
                    .withValue(TeambrellaRepository.TXSignature.ID, txSignature.getId())
                    .withValue(TeambrellaRepository.TXSignature.TEAMMATE_ID, txSignature.getTeammateId())
                    .withValue(TeambrellaRepository.TXSignature.TX_INPUT_ID, txSignature.getTxInputId())
                    .withValue(TeambrellaRepository.TXSignature.SIGNATURE, Base64.decode(txSignature.getSignature(), Base64.DEFAULT))
                    .withValue(TeambrellaRepository.TXSignature.NEED_UPDATE_SERVER, false)
                    .build());


        }


        return list;
    }

    public List<ContentProviderOperation> insertTx(ITx[] txs) throws
            RemoteException {

        List<ContentProviderOperation> list = new LinkedList<>();

        for (ITx tx : txs) {
            Cursor cursor = mClient.query(TeambrellaRepository.Tx.CONTENT_URI, null, TeambrellaRepository.Tx.ID + "=?",
                    new String[]{tx.getId()}, null, null);

            if (cursor != null && cursor.moveToNext()) {
                ContentValues cv = new ContentValues();
                cv.put(TeambrellaRepository.Tx.STATE, tx.getState());
                list.add(ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI).withValues(cv).withSelection(TeambrellaRepository.Tx.ID + "=?",
                        new String[]{tx.getId()}).build());
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

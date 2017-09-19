package com.teambrella.android.content;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.model.Cosigner;
import com.teambrella.android.content.model.Multisig;
import com.teambrella.android.content.model.PayTo;
import com.teambrella.android.content.model.ServerUpdates;
import com.teambrella.android.content.model.TXSignature;
import com.teambrella.android.content.model.Team;
import com.teambrella.android.content.model.Teammate;
import com.teambrella.android.content.model.Tx;
import com.teambrella.android.content.model.TxInput;
import com.teambrella.android.content.model.TxOutput;

import org.chalup.microorm.MicroOrm;
import org.chalup.microorm.TypeAdapter;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.functions.Predicate;

/**
 * Teambrella Content Provider Client
 */
public class TeambrellaContentProviderClient {

    private static SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);


    private static MicroOrm sMicroOrm = new MicroOrm.Builder().registerTypeAdapter(UUID.class, new TypeAdapter<UUID>() {
        @Override
        public UUID fromCursor(Cursor c, String columnName) {
            return UUID.fromString(c.getString(c.getColumnIndex(columnName)));
        }

        @Override
        public void toContentValues(ContentValues values, String columnName, UUID object) {
            values.put(columnName, object.toString());
        }
    }).registerTypeAdapter(byte[].class, new TypeAdapter<byte[]>() {
        @Override
        public byte[] fromCursor(Cursor c, String columnName) {
            return c.getBlob(c.getColumnIndex(columnName));
        }

        @Override
        public void toContentValues(ContentValues values, String columnName, byte[] object) {
            values.put(columnName, object);
        }
    }).build();


    private final ContentProviderClient mClient;

    static {
        mSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

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
    public List<ContentProviderOperation> insertTeammates(Teammate[] teammates) throws RemoteException{
        List<ContentProviderOperation> list = new LinkedList<>();
        for (Teammate teammate : teammates) {
            ContentProviderOperation op = insertOrUpdateOneTeammate(teammate);
            if (op != null){
                list.add(op);
            }
        }
        return list;
    }

    private ContentProviderOperation insertOrUpdateOneTeammate(Teammate teammate) throws RemoteException{
        Teammate existingTeammate = getTeammateById(teammate.id);
        if (existingTeammate == null) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.Teammate.ID, teammate.id);
            cv.put(TeambrellaRepository.Teammate.TEAM_ID, teammate.teamId);
            cv.put(TeambrellaRepository.Teammate.NAME, teammate.name);
            cv.put(TeambrellaRepository.Teammate.FB_NAME, teammate.facebookName);
            cv.put(TeambrellaRepository.Teammate.PUBLIC_KEY, teammate.publicKey);
            cv.put(TeambrellaRepository.Teammate.PUBLIC_KEY_ADDRESS, teammate.publicKeyAddress);
            return ContentProviderOperation.newInsert(TeambrellaRepository.Teammate.CONTENT_URI).withValues(cv).build();
        }else{
            if (existingTeammate.publicKey != null && !Objects.equals(teammate.publicKey, existingTeammate.publicKey) ||
                existingTeammate.publicKeyAddress != null && !Objects.equals(teammate.publicKeyAddress, existingTeammate.publicKeyAddress) ||
                existingTeammate.facebookName != null && !Objects.equals(teammate.facebookName, existingTeammate.facebookName)){
                return null; // we don't allow to change basic info
            }

            ContentProviderOperation.Builder op;
            op = ContentProviderOperation.newUpdate(TeambrellaRepository.Teammate.CONTENT_URI);
            op = op.withValue(TeambrellaRepository.Teammate.NAME, teammate.name);
            if (existingTeammate.publicKey == null){
                op = op.withValue(TeambrellaRepository.Teammate.PUBLIC_KEY, teammate.publicKey);
            }
            if (existingTeammate.publicKeyAddress == null){
                op = op.withValue(TeambrellaRepository.Teammate.PUBLIC_KEY_ADDRESS, teammate.publicKeyAddress);
            }
            op = op.withSelection(TeambrellaRepository.Teammate.ID + "=?", new String[]{Long.toString(teammate.id)});
            return op.build();
        }
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
                cv.put(TeambrellaRepository.PayTo.KNOWN_SINCE, mSDF.format(new Date()));
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
        HashMap<Long, Set<Cosigner>> cosignersMap = new HashMap<>();
        for (Cosigner cosigner : cosigners) {
            Set<Cosigner> set = cosignersMap.get(cosigner.multisigId);
            if (set == null) {
                set = new HashSet<>();
                cosignersMap.put(cosigner.multisigId, set);
            }
            set.add(cosigner);
        }

        for (Long multisigId : cosignersMap.keySet()) {
            Cursor cursor = mClient.query(TeambrellaRepository.Cosigner.CONTENT_URI, null, TeambrellaRepository.Cosigner.MULTISIG_ID + "=?",
                    new String[]{multisigId.toString()}, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                for (Cosigner cosigner : cosignersMap.get(multisigId)) {
                    ContentValues cv = new ContentValues();
                    cv.put(TeambrellaRepository.Cosigner.MULTISIG_ID, cosigner.multisigId);
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
                    return iTx.id.equals(txInput.txId);
                }
            }).count().blockingGet();


            if (count > 0) {
                list.add(ContentProviderOperation.newInsert(TeambrellaRepository.TXInput.CONTENT_URI)
                        .withValue(TeambrellaRepository.TXInput.ID, txInput.id.toString())
                        .withValue(TeambrellaRepository.TXInput.TX_ID, txInput.txId.toString())
                        .withValue(TeambrellaRepository.TXInput.AMOUNT_CRYPTO, txInput.cryptoAmount)
                        .withValue(TeambrellaRepository.TXInput.PREV_TX_ID, txInput.previousTxId)
                        .withValue(TeambrellaRepository.TXInput.PREV_TX_INDEX, txInput.previousTxIndex)
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
            Cursor cursor = mClient.query(TeambrellaRepository.TXOutput.CONTENT_URI, new String[]{TeambrellaRepository.TXOutput.ID}, TeambrellaRepository.TX_OUTPUT_TABLE + "." + TeambrellaRepository.TXOutput.ID + "=?",
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
                        .withValue(TeambrellaRepository.TXOutput.ID, txOutput.id.toString())
                        .withValue(TeambrellaRepository.TXOutput.TX_ID, txOutput.txId.toString())
                        .withValue(TeambrellaRepository.TXOutput.AMOUNT_CRYPTO, txOutput.cryptoAmount)
                        .withValue(TeambrellaRepository.TXOutput.PAY_TO_ID, txOutput.payToId)
                        .build());
            }

        }

        return list;
    }


    public List<ContentProviderOperation> checkArrivingTx(Tx[] arrivingTxs) throws RemoteException {
        List<ContentProviderOperation> operations = new LinkedList<>();
        for (Tx arrivingTx : arrivingTxs) {
            Tx tx = queryOne(TeambrellaRepository.Tx.CONTENT_URI, TeambrellaRepository.Tx.ID + "=?", new String[]{arrivingTx.id.toString()}, Tx.class);
//            boolean isWalletMove = tx.kind == TeambrellaModel.TX_KIND_MOVE_TO_NEXT_WALLET || tx.kind == TeambrellaModel.TX_KIND_SAVE_FROM_PREV_WALLLET;
//            List<TxOutput> outputs = queryList(TeambrellaRepository.TXOutput.CONTENT_URI, TeambrellaRepository.TXOutput.TX_ID + "=?", new String[]{tx.id.toString()}, TxOutput.class);
//            if (!isWalletMove && outputs.isEmpty()) {
//                operations.add(ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI)
//                        .withValue(TeambrellaRepository.Tx.RESOLUTION, TeambrellaModel.TX_CLIENT_RESOLUTION_ERROR_BAD_REQUEST)
//                        .build());
//                continue;
//            }
////
//            float totalAmount = 0f;
//
//            for (TxOutput txOutput : outputs) {
//                totalAmount += txOutput.cryptoAmount;
//            }
//
//            if (!isWalletMove && Math.abs(totalAmount - tx.cryptoAmount) > 0.000001f) {
//                operations.add(ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI)
//                        .withValue(TeambrellaRepository.Tx.RESOLUTION, TeambrellaModel.TX_CLIENT_RESOLUTION_ERROR_BAD_REQUEST)
//                        .build());
//                continue;
//            }

            if (tx.resolution == TeambrellaModel.TX_CLIENT_RESOLUTION_NONE) {
                operations.add(ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI)
                        .withValue(TeambrellaRepository.Tx.RESOLUTION, TeambrellaModel.TX_CLIENT_RESOLUTION_RECEIVED)
                        .build());
            }
        }

        return operations;
    }


    /**
     * Insert Crypto Addresses
     *
     * @param multisigs multisigs
     * @return a list of operations
     */
    public List<ContentProviderOperation> insertMultisigs(com.teambrella.android.content.model.Multisig[] multisigs) throws RemoteException {
        List<ContentProviderOperation> list = new LinkedList<>();

        for (com.teambrella.android.content.model.Multisig multisig : multisigs) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.Multisig.ID, multisig.id);
            cv.put(TeambrellaRepository.Multisig.ADDRESS, multisig.address);
            cv.put(TeambrellaRepository.Multisig.TEAMMATE_ID, multisig.teammateId);
            cv.put(TeambrellaRepository.Multisig.DATE_CREATED, multisig.dateCreated);
            cv.put(TeambrellaRepository.Multisig.STATUS, multisig.status);
            cv.put(TeambrellaRepository.Multisig.NEED_UPDATE_SERVER, false);
            list.add(ContentProviderOperation.newInsert(TeambrellaRepository.Multisig.CONTENT_URI).withValues(cv).build());
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
                        return iTxInput.id.toString().equals(txSignature.txInputId);
                    }
                }).count().blockingGet();

                if (count == 0) {
                    continue;
                }
            }

            list.add(ContentProviderOperation.newInsert(TeambrellaRepository.TXSignature.CONTENT_URI)
                    .withValue(TeambrellaRepository.TXSignature.ID, UUID.randomUUID().toString())
                    .withValue(TeambrellaRepository.TXSignature.TEAMMATE_ID, txSignature.teammateId)
                    .withValue(TeambrellaRepository.TXSignature.TX_INPUT_ID, txSignature.txInputId)
                    .withValue(TeambrellaRepository.TXSignature.SIGNATURE, Base64.decode(txSignature.signature, Base64.NO_WRAP))
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
                cv.put(TeambrellaRepository.Tx.AMOUNT_CRYPTO, tx.cryptoAmount);
                cv.put(TeambrellaRepository.Tx.CLAIM_ID, tx.claimId);
                cv.put(TeambrellaRepository.Tx.CLAIM_TEAMMATE_ID, tx.claimTeammateId);
                cv.put(TeambrellaRepository.Tx.WITHDRAW_REQ_ID, tx.withdrawReqId);
                cv.put(TeambrellaRepository.Tx.STATE, tx.state);
                cv.put(TeambrellaRepository.Tx.KIND, tx.kind);
                cv.put(TeambrellaRepository.Tx.INITIATED_TIME, tx.initiatedTime);
                cv.put(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, false);
                cv.put(TeambrellaRepository.Tx.RECEIVED_TIME, mSDF.format(new Date()));
                cv.put(TeambrellaRepository.Tx.UPDATE_TIME, mSDF.format(new Date()));
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


    public List<Tx> getCosinableTx() throws RemoteException {
        List<Tx> list = queryList(TeambrellaRepository.Tx.CONTENT_URI, TeambrellaRepository.Tx.RESOLUTION + "=? AND "
                + TeambrellaRepository.Tx.STATE + "=?", new String[]{Integer.toString(TeambrellaModel.TX_CLIENT_RESOLUTION_APPROVED),
                Integer.toString(TeambrellaModel.TX_STATE_SELECTED_FOR_COSIGNING)}, Tx.class);
        Iterator<Tx> iterator = list != null ? list.iterator() : null;
        if (iterator != null) {
            while (iterator.hasNext()) {
                Tx tx = iterator.next();
                tx.txInputs = queryList(TeambrellaRepository.TXInput.CONTENT_URI, TeambrellaRepository.TXInput.TX_ID + "=?", new String[]{tx.id.toString()}, TxInput.class);
                if (tx.txInputs == null || tx.txInputs.isEmpty()) {
                    iterator.remove();
                } else {

                    Collections.sort(tx.txInputs);

                    tx.teammate = queryOne(TeambrellaRepository.Teammate.CONTENT_URI,
                            TeambrellaRepository.TEAMMATE_TABLE + "." + TeambrellaRepository.Teammate.ID + "=?", new String[]{Long.toString(tx.teammateId)}, Teammate.class);
                    if (tx.teammate != null) {
                        tx.teammate.multisigs = queryList(TeambrellaRepository.Multisig.CONTENT_URI, TeambrellaRepository.Multisig.TEAMMATE_ID + "=?"
                                , new String[]{Long.toString(tx.teammate.id)}, com.teambrella.android.content.model.Multisig.class);
                    }


                    tx.cosigners = getCosigners(tx.getFromAddress());

                    Collections.sort(tx.cosigners);

                    tx.txOutputs = queryList(TeambrellaRepository.TXOutput.CONTENT_URI,
                            TeambrellaRepository.TXOutput.TX_ID + "=?", new String[]{tx.id.toString()}, TxOutput.class);
                }
            }
        }
        return list;
    }


    public static List<ContentProviderOperation> clearNeedUpdateServerFlag() {
        List<ContentProviderOperation> operations = new LinkedList<>();

        operations.add(ContentProviderOperation.newUpdate(TeambrellaRepository.Multisig.CONTENT_URI)
                .withValue(TeambrellaRepository.Multisig.NEED_UPDATE_SERVER, false)
                .withSelection(TeambrellaRepository.Multisig.NEED_UPDATE_SERVER, null).build());

        operations.add(ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI)
                .withValue(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, false)
                .withSelection(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, null).build());

        operations.add(ContentProviderOperation.newUpdate(TeambrellaRepository.TXSignature.CONTENT_URI)
                .withValue(TeambrellaRepository.TXSignature.NEED_UPDATE_SERVER, false)
                .withSelection(TeambrellaRepository.TXSignature.NEED_UPDATE_SERVER, null)
                .build());

        return operations;
    }

    public static ContentProviderOperation addSignature(String txInputId, long teammateId, byte[] signature) {
        return ContentProviderOperation.newInsert(TeambrellaRepository.TXSignature.CONTENT_URI)
                .withValue(TeambrellaRepository.TXSignature.ID, UUID.randomUUID().toString())
                .withValue(TeambrellaRepository.TXSignature.TX_INPUT_ID, txInputId)
                .withValue(TeambrellaRepository.TXSignature.TEAMMATE_ID, teammateId)
                .withValue(TeambrellaRepository.TXSignature.SIGNATURE, signature)
                .withValue(TeambrellaRepository.TXSignature.NEED_UPDATE_SERVER, true)
                .build();
    }

    public static ContentProviderOperation setTxSigned(Tx tx) {
        return ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI)
                .withValue(TeambrellaRepository.Tx.RESOLUTION, TeambrellaModel.TX_CLIENT_RESOLUTION_SIGNED)
                .withValue(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, true)
                .withValue(TeambrellaRepository.Tx.CLIENT_RESOLUTION_TIME, mSDF.format(new Date()))
                .withSelection(TeambrellaRepository.Tx.ID + "=?", new String[]{tx.id.toString()})
                .build();
    }

    public static ContentProviderOperation setTxPublished(Tx tx) {
        return ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI)
                .withValue(TeambrellaRepository.Tx.RESOLUTION, TeambrellaModel.TX_CLIENT_RESOLUTION_PUBLISHED)
                .withValue(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, true)
                .withValue(TeambrellaRepository.Tx.CLIENT_RESOLUTION_TIME, mSDF.format(new Date()))
                .withSelection(TeambrellaRepository.Tx.ID + "=?", new String[]{tx.id.toString()})
                .build();
    }

    public List<com.teambrella.android.content.model.Multisig> getMultisigsToCreate(String publicKey) throws RemoteException {
        List<com.teambrella.android.content.model.Multisig> list = queryList(TeambrellaRepository.Multisig.CONTENT_URI,
                TeambrellaRepository.Multisig.ADDRESS + " IS NULL AND " +
                        TeambrellaRepository.Multisig.STATUS + " = " + TeambrellaModel.USER_MULTISIG_STATUS_CURRENT + " AND " +
                TeambrellaRepository.Teammate.PUBLIC_KEY + "=?",
                new String[]{publicKey}, com.teambrella.android.content.model.Multisig.class);
        Iterator<com.teambrella.android.content.model.Multisig> iterator = list != null ? list.iterator() : null;
        if (iterator != null) {
            while (iterator.hasNext()) {
                com.teambrella.android.content.model.Multisig m = iterator.next();
                m.cosigners = getCosigners(m);
            }
        }
        return list;
    }

    public List<com.teambrella.android.content.model.Multisig> getMultisigsWithAddressByTeammate(String publicKey, long teammateId) throws RemoteException {
        List<com.teambrella.android.content.model.Multisig> list = queryList(TeambrellaRepository.Multisig.CONTENT_URI,
                TeambrellaRepository.Teammate.PUBLIC_KEY + "=? AND " +
                        TeambrellaRepository.Multisig.TEAMMATE_ID + "=? AND " +
                        "(" + TeambrellaRepository.Multisig.ADDRESS + " IS NOT NULL"
                        + " OR " + TeambrellaRepository.Multisig.CREATION_TX + " IS NOT NULL"
                        + ")",
                new String[]{publicKey, Long.toString(teammateId)}, com.teambrella.android.content.model.Multisig.class);
        return list;
    }

    public List<com.teambrella.android.content.model.Multisig> getMultisigsInCreation(String publicKey) throws RemoteException {
        List<com.teambrella.android.content.model.Multisig> list = queryList(TeambrellaRepository.Multisig.CONTENT_URI,
                        TeambrellaRepository.Teammate.PUBLIC_KEY + "=? AND " +
                        TeambrellaRepository.Multisig.ADDRESS + " IS NULL AND " +
                        TeambrellaRepository.Multisig.CREATION_TX + " IS NOT NULL AND " +
                        TeambrellaRepository.Multisig.STATUS + " <> " + TeambrellaModel.USER_MULTISIG_STATUS_CREATION_FAILED,
                new String[]{publicKey}, com.teambrella.android.content.model.Multisig.class);
        return list;
    }


    public List<com.teambrella.android.content.model.Multisig> getCurrentMultisigsWithAddress(String publicKey) throws RemoteException {
        List<com.teambrella.android.content.model.Multisig> list = queryList(TeambrellaRepository.Multisig.CONTENT_URI,
                TeambrellaRepository.Teammate.PUBLIC_KEY + "=? AND " +
                        TeambrellaRepository.Multisig.ADDRESS + " IS NOT NULL AND " +
                        TeambrellaRepository.Multisig.STATUS + "=" + TeambrellaModel.USER_MULTISIG_STATUS_CURRENT,
                new String[]{publicKey}, com.teambrella.android.content.model.Multisig.class);
        return list;
    }

    public static ContentProviderOperation setMutisigStatus(com.teambrella.android.content.model.Multisig m, int newStatus) {
        ContentProviderOperation.Builder op;
        op = ContentProviderOperation.newUpdate(TeambrellaRepository.Multisig.CONTENT_URI);
        op = op.withValue(TeambrellaRepository.Multisig.STATUS, newStatus);
        op = op.withSelection(TeambrellaRepository.Multisig.ID + "=?", new String[]{Long.toString(m.id)});
        return op.build();
    }

    public static ContentProviderOperation setMutisigAddressAndNeedsServerUpdate(com.teambrella.android.content.model.Multisig m, String newAddress) {
        ContentProviderOperation.Builder op;
        op = ContentProviderOperation.newUpdate(TeambrellaRepository.Multisig.CONTENT_URI);
        op = op.withValue(TeambrellaRepository.Multisig.ADDRESS, newAddress);
        op = op.withValue(TeambrellaRepository.Multisig.NEED_UPDATE_SERVER, true);
        op = op.withSelection(TeambrellaRepository.Multisig.ID + "=?", new String[]{Long.toString(m.id)});
        return op.build();
    }

    public static ContentProviderOperation setMutisigAddressTxAndNeedsServerUpdate(com.teambrella.android.content.model.Multisig m, String newAddress, String newCreationTx, boolean needServerUpdate) {
        ContentProviderOperation.Builder op;
        op = ContentProviderOperation.newUpdate(TeambrellaRepository.Multisig.CONTENT_URI);
        op = op.withValue(TeambrellaRepository.Multisig.ADDRESS, newAddress);
        op = op.withValue(TeambrellaRepository.Multisig.CREATION_TX, newCreationTx);
        op = op.withValue(TeambrellaRepository.Multisig.NEED_UPDATE_SERVER, needServerUpdate);
        op = op.withSelection(TeambrellaRepository.Multisig.ID + "=?", new String[]{Long.toString(m.id)});
        return op.build();
    }

    private List<Cosigner> getCosigners(com.teambrella.android.content.model.Multisig multisig) throws RemoteException {
        List<Cosigner> list = queryList(TeambrellaRepository.Cosigner.CONTENT_URI, TeambrellaRepository.Cosigner.MULTISIG_ID + "=?",
                new String[]{Long.toString(multisig.id)}, Cosigner.class);
        Collections.sort(list);
        return list;
    }

    public List<Tx> getApprovedAndCosignedTxs() throws RemoteException {
        List<Tx> list = queryList(TeambrellaRepository.Tx.CONTENT_URI, TeambrellaRepository.Tx.RESOLUTION + "=? AND "
                + TeambrellaRepository.Tx.STATE + "= ?", new String[]{Integer.toString(TeambrellaModel.TX_CLIENT_RESOLUTION_APPROVED),
                Integer.toString(TeambrellaModel.TX_STATE_COSIGNED)}, Tx.class);
        Iterator<Tx> iterator = list != null ? list.iterator() : null;
        if (iterator != null) {
            while (iterator.hasNext()) {
                Tx tx = iterator.next();
                tx.txInputs = queryList(TeambrellaRepository.TXInput.CONTENT_URI, TeambrellaRepository.TXInput.TX_ID + "=?", new String[]{tx.id.toString()}, TxInput.class);
                if (tx.txInputs == null || tx.txInputs.isEmpty()) {
                    iterator.remove();
                } else {
                    Collections.sort(tx.txInputs);

                    tx.teammate = queryOne(TeambrellaRepository.Teammate.CONTENT_URI,
                            TeambrellaRepository.TEAMMATE_TABLE + "." + TeambrellaRepository.Teammate.ID + "=?", new String[]{Long.toString(tx.teammateId)}, Teammate.class);
                    if (tx.teammate != null) {
                        tx.teammate.multisigs = queryList(TeambrellaRepository.Multisig.CONTENT_URI, TeambrellaRepository.Multisig.TEAMMATE_ID + "=?"
                                , new String[]{Long.toString(tx.teammate.id)}, com.teambrella.android.content.model.Multisig.class);
                    }

                    tx.cosigners = getCosigners(tx.getFromAddress());
                    for (TxInput txInput : tx.txInputs) {
                        List<TXSignature> signatures = queryList(TeambrellaRepository.TXSignature.CONTENT_URI, TeambrellaRepository.TXSignature.TX_INPUT_ID + "=?",
                                new String[]{txInput.id.toString()}, TXSignature.class);
                        for (TXSignature signature : signatures) {
                            txInput.signatures.put(signature.teammateId, signature);
                        }
                    }


                    tx.txOutputs = queryList(TeambrellaRepository.TXOutput.CONTENT_URI,
                            TeambrellaRepository.TXOutput.TX_ID + "=?", new String[]{tx.id.toString()}, TxOutput.class);
                }
            }
        }
        return list;
    }


    public List<ContentProviderOperation> autoApproveTxs() throws RemoteException {
        List<ContentProviderOperation> operations = new LinkedList<>();
        List<Tx> txs = getTxToApprove();
        for (Tx tx : txs) {
            operations.add(ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI)
                    .withValue(TeambrellaRepository.Tx.RESOLUTION, TeambrellaModel.TX_CLIENT_RESOLUTION_APPROVED)
                    .withValue(TeambrellaRepository.Tx.CLIENT_RESOLUTION_TIME, mSDF.format(new Date()))
                    .withValue(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, true)
                    .withSelection(TeambrellaRepository.Tx.ID + "=?", new String[]{tx.id.toString()})
                    .build());
        }
        return operations;
    }


    private List<Tx> getTxToApprove() throws RemoteException {
        List<Tx> list = queryList(TeambrellaRepository.Tx.CONTENT_URI, TeambrellaRepository.Tx.RESOLUTION + "=?",
                new String[]{Integer.toString(TeambrellaModel.TX_CLIENT_RESOLUTION_RECEIVED)}, Tx.class);
        Iterator<Tx> iterator = list != null ? list.iterator() : null;
        if (iterator != null) {
            while (iterator.hasNext()) {
                Tx tx = iterator.next();
                tx.txOutputs = queryList(TeambrellaRepository.TXOutput.CONTENT_URI,
                        TeambrellaRepository.TXOutput.TX_ID + "=?", new String[]{tx.id.toString()}, TxOutput.class);
                tx.teammate = queryOne(TeambrellaRepository.Teammate.CONTENT_URI,
                        TeambrellaRepository.TEAMMATE_TABLE + "." + TeambrellaRepository.Teammate.ID + "=?", new String[]{Long.toString(tx.teammateId)}, Teammate.class);
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

    public List<ContentProviderOperation> applyUpdates(ServerUpdates serverUpdates) throws RemoteException {
        List<ContentProviderOperation> operations = new LinkedList<>();
        if (serverUpdates.teammates != null) {
            operations.addAll(insertTeammates(serverUpdates.teammates));
        }

        if (serverUpdates.teams != null) {
            operations.addAll(insertTeams(serverUpdates.teams));
        }

        if (serverUpdates.payTos != null) {
            operations.addAll(insertPayTos(serverUpdates.payTos));
        }

        if (serverUpdates.multisigs != null) {
            operations.addAll(insertMultisigs(serverUpdates.multisigs));
        }

        if (serverUpdates.cosigners != null) {
            operations.addAll(insertCosigners(serverUpdates.cosigners));
        }

        if (serverUpdates.txs != null) {
            operations.addAll(insertTx(serverUpdates.txs));
        }

        if (serverUpdates.txInputs != null) {
            operations.addAll(insertTXInputs(serverUpdates.txs, serverUpdates.txInputs));
        }

        if (serverUpdates.txOutputs != null) {
            operations.addAll(insertTXOutputs(serverUpdates.txs, serverUpdates.txOutputs));
        }

        if (serverUpdates.txSignatures != null) {
            operations.addAll(insertTXSignatures(serverUpdates.txInputs, serverUpdates.txSignatures));
        }
        return operations;
    }


    public void updateConnectionTime(long time) throws RemoteException {
        Cursor cursor = mClient.query(TeambrellaRepository.Connection.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.Connection.LAST_CONNECTED, time);
            if (cursor.moveToFirst()) {
                mClient.update(TeambrellaRepository.Connection.CONTENT_URI, cv, TeambrellaRepository.Connection.ID + "=?",
                        new String[]{cursor.getString(cursor.getColumnIndex(TeambrellaRepository.Connection.ID))});
            } else {
                mClient.insert(TeambrellaRepository.Connection.CONTENT_URI, cv);
            }
            cursor.close();
        }
    }

    public void setLastUpdatedTime(long time) throws RemoteException {
        Cursor cursor = mClient.query(TeambrellaRepository.Connection.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.Connection.LAST_UPDATED, Long.toString(time));
            if (cursor.moveToFirst()) {
                mClient.update(TeambrellaRepository.Connection.CONTENT_URI, cv, TeambrellaRepository.Connection.ID + "=?",
                        new String[]{cursor.getString(cursor.getColumnIndex(TeambrellaRepository.Connection.ID))});
            }
            cursor.close();
        }
    }


    public Teammate getTeammate(String publicKey) throws RemoteException {
        return queryOne(TeambrellaRepository.Teammate.CONTENT_URI, TeambrellaRepository.Teammate.PUBLIC_KEY + "=?",
                new String[]{publicKey}, Teammate.class);
    }

    private Teammate getTeammateById(long id) throws RemoteException {
        // Use [Teammate].Id rather than just Id in the query
        // in order to avoid: "android.database.sqlite.SQLiteException: ambiguous column name: Id"
        return queryOne(TeambrellaRepository.Teammate.CONTENT_URI, TeambrellaRepository.TEAMMATE_TABLE + "." + TeambrellaRepository.Teammate.ID + "=?",
                new String[]{Long.toString(id)}, Teammate.class);
    }

    public JsonObject getClientUpdates() throws RemoteException {
        JsonObject body = new JsonObject();
        Cursor cursor = mClient.query(TeambrellaRepository.Connection.CONTENT_URI, new String[]{TeambrellaRepository.Connection.LAST_UPDATED}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String value = cursor.getString(cursor.getColumnIndex(TeambrellaRepository.Connection.LAST_UPDATED));
            long lastUpdated = 0;
            if (value != null) {
                lastUpdated = Long.parseLong(value);
            }
            body.add(TeambrellaModel.ATTR_DATA_LAST_UPDATED, new JsonPrimitive(lastUpdated));
        }
        if (cursor != null) {
            cursor.close();
        }

        cursor = mClient.query(TeambrellaRepository.Multisig.CONTENT_URI, new String[]{TeambrellaRepository.Multisig.ID, TeambrellaRepository.Multisig.TEAMMATE_ID, TeambrellaRepository.Multisig.ADDRESS},
                TeambrellaRepository.Multisig.NEED_UPDATE_SERVER, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            JsonArray txArray = new JsonArray();
            do {
                JsonObject contract = new JsonObject();
                contract.add(TeambrellaModel.ATTR_DATA_ID, new JsonPrimitive(cursor.getString(cursor.getColumnIndex(TeambrellaRepository.Multisig.ID))));
                contract.add(TeambrellaModel.ATTR_DATA_TEAMMATE_ID, new JsonPrimitive(cursor.getInt(cursor.getColumnIndex(TeambrellaRepository.Multisig.TEAMMATE_ID))));
                contract.add(TeambrellaModel.ATTR_DATA_BLOCKCHAIN_TX_ID, new JsonPrimitive(cursor.getString(cursor.getColumnIndex(TeambrellaRepository.Multisig.CREATION_TX))));
                txArray.add(contract);
            } while (cursor.moveToNext());

            body.add(TeambrellaModel.ATTR_DATA_CRYPTO_CONTRACT, txArray);
        }

        if (cursor != null) {
            cursor.close();
        }

        cursor = mClient.query(TeambrellaRepository.Tx.CONTENT_URI, new String[]{TeambrellaRepository.Tx.ID, TeambrellaRepository.Tx.CLIENT_RESOLUTION_TIME,
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

        cursor = mClient.query(TeambrellaRepository.TXSignature.CONTENT_URI, new String[]{TeambrellaRepository.TXSignature.TX_INPUT_ID, TeambrellaRepository.TXSignature.TEAMMATE_ID, TeambrellaRepository.TXSignature.SIGNATURE},
                TeambrellaRepository.TXSignature.NEED_UPDATE_SERVER, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            JsonArray signaturesArray = new JsonArray();
            do {
                JsonObject signature = new JsonObject();
                signature.add(TeambrellaModel.ATTR_DATA_SIGNATURE, new JsonPrimitive(Base64.encodeToString(cursor.getBlob(cursor.getColumnIndex(TeambrellaRepository.TXSignature.SIGNATURE)), Base64.NO_WRAP)));
                signature.add(TeambrellaModel.ATTR_DATA_TEAMMATE_ID, new JsonPrimitive(cursor.getInt(cursor.getColumnIndex(TeambrellaRepository.TXSignature.TEAMMATE_ID))));
                signature.add(TeambrellaModel.ATTR_DATA_TX_INPUT_ID, new JsonPrimitive(cursor.getString(cursor.getColumnIndex(TeambrellaRepository.TXSignature.TX_INPUT_ID))));
                signaturesArray.add(signature);
            } while (cursor.moveToNext());

            body.add(TeambrellaModel.ATTR_DATA_TX_SIGNATURES, signaturesArray);
        }

        if (cursor != null) {
            cursor.close();
        }

        return body;
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

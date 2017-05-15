package com.teambrella.android.util;

import android.app.IntentService;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.subgraph.orchid.encoders.Hex;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.blockchain.BlockchainServer;
import com.teambrella.android.content.TeambrellaContentProviderClient;
import com.teambrella.android.content.TeambrellaRepository;
import com.teambrella.android.content.model.BTCAddress;
import com.teambrella.android.content.model.Cosigner;
import com.teambrella.android.content.model.TXSignature;
import com.teambrella.android.content.model.Teammate;
import com.teambrella.android.content.model.Tx;
import com.teambrella.android.content.model.TxInput;
import com.teambrella.android.content.model.TxOutput;
import com.teambrella.android.content.model.Updates;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;

import java.math.BigDecimal;
import java.math.MathContext;
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
import java.util.UUID;


/**
 * Teambrella util service
 */
public class TeambrellaUtilService extends IntentService {

    private static final String LOG_TAG = TeambrellaUtilService.class.getSimpleName();
    private static final String EXTRA_URI = "uri";


    private final static String ACTION_UPDATE = "update";
    private final static String ACTION_APPROVE = "approve";
    private final static String ACTION_COSING = "cosign";
    private final static String ACTION_PUBLISH = "publish";
    private final static String SHOW = "show";


    public final long NORMAL_FEE_BTC = 10000L;


    private static final String PRIVATE_KEY = "cNqQ7aZWitJCk1o9dNhr1o9k3UKdeW92CDYrvDHHLuwFuEnfcBXo";
    private TeambrellaServer mServer;
    private ContentProviderClient mClient;
    private TeambrellaContentProviderClient mTeambrellaClient;
    /**
     * Key
     */
    private final ECKey mKey;


    private final SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);


    public TeambrellaUtilService() {
        super("Util Service");
        mSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        mKey = DumpedPrivateKey.fromBase58(null, PRIVATE_KEY).getKey();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mServer = new TeambrellaServer(this);
        mClient = getContentResolver().acquireContentProviderClient(TeambrellaRepository.AUTHORITY);
        mTeambrellaClient = new TeambrellaContentProviderClient(mClient);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        if (action != null) {
            switch (action) {
                case ACTION_UPDATE:
                    update();
                    break;
                case ACTION_APPROVE:
                    try {
                        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                        operations.addAll(autoApproveTxs(mTeambrellaClient));
                        mClient.applyBatch(operations);
                    } catch (RemoteException | OperationApplicationException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_COSING:
                    try {
                        cosignApprovedTransactions(mClient);
                    } catch (RemoteException | OperationApplicationException e) {
                        e.printStackTrace();
                    }
                    break;
                case SHOW:
                    try {
                        show(Uri.parse(intent.getStringExtra(EXTRA_URI)));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_PUBLISH:
                    try {
                        publishApprovedAndCosignedTxs(mClient);
                    } catch (RemoteException | OperationApplicationException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    Log.e(LOG_TAG, "unknown action " + action);
            }
        } else {
            Log.e(LOG_TAG, "action is null");
        }
    }

    private void update() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        try {

            updateConnectionTime();


            JsonObject result = mServer.execute(TeambrellaUris.getUpdates(), getRequestBody());

            if (result != null) {
                JsonObject status = result.get(TeambrellaModel.ATTR_STATUS).getAsJsonObject();
                long timestamp = status.get(TeambrellaModel.ATTR_STATUS_TIMESTAMP).getAsLong();

                JsonObject data = result.get(TeambrellaModel.ATTR_DATA).getAsJsonObject();


                operations.addAll(clearNeedUpdateServerFlag());


                Updates updates = new Gson().fromJson(data, Updates.class);

                if (updates.teammates != null) {
                    operations.addAll(mTeambrellaClient.insertTeammates(updates.teammates));
                }

                if (updates.teams != null) {
                    operations.addAll(mTeambrellaClient.insertTeams(updates.teams));
                }

                if (updates.payTos != null) {
                    operations.addAll(mTeambrellaClient.insertPayTos(updates.payTos));
                }

                if (updates.btcAddresses != null) {
                    operations.addAll(mTeambrellaClient.insertBTCAddresses(updates.btcAddresses));
                }

                if (updates.cosigners != null) {
                    operations.addAll(mTeambrellaClient.insertCosigners(updates.cosigners));
                }

                if (updates.txs != null) {
                    operations.addAll(mTeambrellaClient.insertTx(updates.txs));
                }

                if (updates.txInputs != null) {
                    operations.addAll(mTeambrellaClient.insertTXInputs(updates.txs, updates.txInputs));
                }

                if (updates.txOutputs != null) {
                    operations.addAll(mTeambrellaClient.insertTXOutputs(updates.txs, updates.txOutputs));
                }

                if (updates.txSignatures != null) {
                    operations.addAll(mTeambrellaClient.insertTXSignatures(updates.txInputs, updates.txSignatures));
                }

                if (!operations.isEmpty()) {
                    mClient.applyBatch(operations);
                }


                operations.clear();

                if (updates.txs != null) {
                    operations.addAll(mTeambrellaClient.checkArrivingTx(updates.txs));
                }

                if (!operations.isEmpty()) {
                    mClient.applyBatch(operations);
                }

                setLastUpdatedTime(timestamp);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cosignApprovedTransactions(ContentProviderClient client) throws RemoteException, OperationApplicationException {
        TeambrellaContentProviderClient tbClient = new TeambrellaContentProviderClient(client);
        List<Tx> list = getCosinableTx(tbClient);
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        if (list != null) {
            for (Tx tx : list) {
                Transaction transaction = getTransaction(tx);
                if (transaction != null) {
                    Log.e(LOG_TAG, tx.id.toString());
                    Log.d(LOG_TAG, transaction.toString());
                    BTCAddress address = tx.getFromAddress();
                    if (address != null) {
                        List<Cosigner> cosigners = getCosigners(tbClient, address);
                        Script redeemScript = getRedeemScript(address, cosigners);
                        Collections.sort(tx.txInputs, new Comparator<TxInput>() {
                            @Override
                            public int compare(TxInput o1, TxInput o2) {
                                return o1.id.compareTo(o2.id);
                            }
                        });
                        for (int i = 0; i < tx.txInputs.size(); i++) {
                            byte[] signature = cosign(redeemScript, transaction, i);
                            operations.add(addSignature(tx.txInputs.get(i).id.toString(), 2275, signature));
                        }
                    }
                }
                operations.add(setTxSigned(tx));
            }
        }
        //client.applyBatch(operations);
    }

    private Transaction getTransaction(final Tx tx) {
        NetworkParameters params = new TestNet3Params();
        final float normalFeeBTC = 0.0001f;
        BigDecimal totalBTCAmount = new BigDecimal(0, MathContext.UNLIMITED);
        Transaction transaction = null;
        if (tx.txInputs != null) {
            Collections.sort(tx.txInputs, new Comparator<TxInput>() {
                @Override
                public int compare(TxInput o1, TxInput o2) {
                    return o1.id.compareTo(o2.id);
                }
            });

            transaction = new Transaction(params);

            for (TxInput txInput : tx.txInputs) {
                totalBTCAmount = totalBTCAmount.add(new BigDecimal(txInput.btcAmount, MathContext.UNLIMITED));
                TransactionOutPoint outpoint = new TransactionOutPoint(params, txInput.previousTxIndex,
                        Sha256Hash.wrap(txInput.previousTxId));
                transaction.addInput(new TransactionInput(params, transaction, new byte[0], outpoint))
                ;
            }

            totalBTCAmount = totalBTCAmount.subtract(new BigDecimal("0.0001", MathContext.UNLIMITED));

            if (totalBTCAmount.compareTo(new BigDecimal(tx.btcAmount)) == -1) {
                return null;
            }

            if (tx.kind == TeambrellaModel.TX_KIND_PAYOUT || tx.kind == TeambrellaModel.TX_KIND_WITHDRAW) {
                Collections.sort(tx.txOutputs, new Comparator<TxOutput>() {
                    @Override
                    public int compare(TxOutput o1, TxOutput o2) {
                        return o1.id.compareTo(o2.id);
                    }
                });

                BigDecimal outputSum = new BigDecimal(0f, MathContext.UNLIMITED);

                for (TxOutput txOutput : tx.txOutputs) {
                    Address address = Address.fromBase58(params, txOutput.address);
                    transaction.addOutput(new TransactionOutput(params, null, Coin.parseCoin(new BigDecimal(txOutput.btcAmount).toString()), address));
                    outputSum = outputSum.add(new BigDecimal(txOutput.btcAmount));
                }

                BigDecimal changeAmount = totalBTCAmount.subtract(outputSum);

                if (changeAmount.compareTo(new BigDecimal("0.0001", MathContext.UNLIMITED)) == 1) {
                    BTCAddress current = tx.teammate.getCurrentAddress();
                    if (current != null) {
                        transaction.addOutput(new TransactionOutput(params, null, Coin.parseCoin(changeAmount.toString()),
                                Address.fromBase58(params, current.address)));
                    }
                } else if (tx.kind == TeambrellaModel.TX_KIND_MOVE_TO_NEXT_WALLET) {
                    BTCAddress next = tx.teammate.getNextAddress();
                    if (next != null) {
                        transaction.addOutput(new TransactionOutput(params, null, Coin.parseCoin(totalBTCAmount.toString()),
                                Address.fromBase58(params, next.address)));
                    }
                } else if (tx.kind == TeambrellaModel.TX_KIND_SAVE_FROM_PREV_WALLLET) {
                    BTCAddress current = tx.teammate.getCurrentAddress();
                    if (current != null) {
                        transaction.addOutput(new TransactionOutput(params, null, Coin.parseCoin(totalBTCAmount.toString()),
                                Address.fromBase58(params, current.address)));
                    }
                }
            }
        }

        return transaction;
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


    private static ContentProviderOperation addSignature(String txInputId, long teammateId, byte[] signature) {
        return ContentProviderOperation.newInsert(TeambrellaRepository.TXSignature.CONTENT_URI)
                .withValue(TeambrellaRepository.TXSignature.ID, UUID.randomUUID().toString())
                .withValue(TeambrellaRepository.TXSignature.TX_INPUT_ID, txInputId)
                .withValue(TeambrellaRepository.TXSignature.TEAMMATE_ID, teammateId)
                .withValue(TeambrellaRepository.TXSignature.SIGNATURE, signature)
                .withValue(TeambrellaRepository.TXSignature.NEED_UPDATE_SERVER, true)
                .build();
    }

    private ContentProviderOperation setTxSigned(Tx tx) {
        return ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI)
                .withValue(TeambrellaRepository.Tx.RESOLUTION, TeambrellaModel.TX_CLIENT_RESOLUTION_SIGNED)
                .withValue(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, true)
                .withValue(TeambrellaRepository.Tx.CLIENT_RESOLUTION_TIME, mSDF.format(new Date()))
                .withSelection(TeambrellaRepository.Tx.ID + "=?", new String[]{tx.id.toString()})
                .build();
    }

    private ContentProviderOperation setTxPublished(Tx tx) {
        return ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI)
                .withValue(TeambrellaRepository.Tx.RESOLUTION, TeambrellaModel.TX_CLIENT_RESOLUTION_PUBLISHED)
                .withValue(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, true)
                .withValue(TeambrellaRepository.Tx.CLIENT_RESOLUTION_TIME, mSDF.format(new Date()))
                .withSelection(TeambrellaRepository.Tx.ID + "=?", new String[]{tx.id.toString()})
                .build();
    }


    static Script getRedeemScript(BTCAddress btcAddress, List<Cosigner> cosigners) {
        ScriptBuilder builder = new ScriptBuilder();
        builder.data(Hex.decode(btcAddress.teammatePublicKey)).op(ScriptOpCodes.OP_CHECKSIGVERIFY);
        int size = cosigners.size();
        if (size > 6) {
            builder.op(ScriptOpCodes.OP_3);
        } else if (size > 3) {
            builder.op(ScriptOpCodes.OP_2);
        } else if (size > 0) {
            builder.op(ScriptOpCodes.OP_1);
        } else {
            builder.op(ScriptOpCodes.OP_0);
        }
        for (Cosigner cosigner : cosigners) {
            builder.data(Hex.decode(cosigner.publicKey));
        }
        builder.op(ScriptOpCodes.OP_RESERVED + size);
        builder.op(ScriptOpCodes.OP_CHECKMULTISIG);
        builder.number(Long.parseLong(btcAddress.teamId));
        builder.op(ScriptOpCodes.OP_DROP);
        return builder.build();
    }

    private static byte[] cosign(Script redeemScript, Transaction transaction, int inputNum) {
        DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(null, "cNqQ7aZWitJCk1o9dNhr1o9k3UKdeW92CDYrvDHHLuwFuEnfcBXo");
        ECKey key = dpk.getKey();
        Sha256Hash hash = transaction.hashForSignature(inputNum, redeemScript, Transaction.SigHash.ALL, false);
        return key.sign(hash).encodeToDER();
    }


    private static List<Tx> getCosinableTx(TeambrellaContentProviderClient client) throws RemoteException {
        List<Tx> list = client.queryList(TeambrellaRepository.Tx.CONTENT_URI, TeambrellaRepository.Tx.RESOLUTION + "=? AND "
                + TeambrellaRepository.Tx.STATE + "=?", new String[]{Integer.toString(TeambrellaModel.TX_CLIENT_RESOLUTION_APPROVED),
                Integer.toString(TeambrellaModel.TX_STATE_SELECTED_FOR_COSIGNING)}, Tx.class);
        Iterator<Tx> iterator = list != null ? list.iterator() : null;
        if (iterator != null) {
            while (iterator.hasNext()) {
                Tx tx = iterator.next();
                tx.txInputs = client.queryList(TeambrellaRepository.TXInput.CONTENT_URI, TeambrellaRepository.TXInput.TX_ID + "=?", new String[]{tx.id.toString()}, TxInput.class);
                if (tx.txInputs == null || tx.txInputs.isEmpty()) {
                    iterator.remove();
                } else {
                    tx.txOutputs = client.queryList(TeambrellaRepository.TXOutput.CONTENT_URI,
                            TeambrellaRepository.TXOutput.TX_ID + "=?", new String[]{tx.id.toString()}, TxOutput.class);
                    tx.teammate = client.queryOne(TeambrellaRepository.Teammate.CONTENT_URI,
                            TeambrellaRepository.TEAMMATE_TABLE + "." + TeambrellaRepository.Teammate.ID + "=?", new String[]{Long.toString(tx.teammateId)}, Teammate.class);
                    if (tx.teammate != null) {
                        tx.teammate.addresses = client.queryList(TeambrellaRepository.BTCAddress.CONTENT_URI, TeambrellaRepository.BTCAddress.TEAMMATE_ID + "=?"
                                , new String[]{Long.toString(tx.teammate.id)}, BTCAddress.class);
                    }
                }
            }
        }
        return list;
    }


    private JsonObject getRequestBody() throws RemoteException {
        JsonObject body = new JsonObject();
        Cursor cursor = mClient.query(TeambrellaRepository.Connection.CONTENT_URI, new String[]{TeambrellaRepository.Connection.LAST_UPDATED}, null, null, null);
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

    private List<ContentProviderOperation> autoApproveTxs(TeambrellaContentProviderClient client) throws RemoteException {
        List<ContentProviderOperation> operations = new LinkedList<>();
        List<Tx> txs = getTxToApprove(client);
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


    private void updateConnectionTime() throws RemoteException {
        Cursor cursor = mClient.query(TeambrellaRepository.Connection.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            ContentValues cv = new ContentValues();
            cv.put(TeambrellaRepository.Connection.LAST_CONNECTED, new Date().getTime());
            if (cursor.moveToFirst()) {
                mClient.update(TeambrellaRepository.Connection.CONTENT_URI, cv, TeambrellaRepository.Connection.ID + "=?",
                        new String[]{cursor.getString(cursor.getColumnIndex(TeambrellaRepository.Connection.ID))});
            } else {
                mClient.insert(TeambrellaRepository.Connection.CONTENT_URI, cv);
            }
            cursor.close();
        }
    }

    private void setLastUpdatedTime(long time) throws RemoteException {
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


    private void show(Uri uri) throws RemoteException {
        Cursor cursor = mClient.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Log.e(LOG_TAG, "***");
                for (String name : cursor.getColumnNames()) {
                    Log.d(LOG_TAG, name + ":" + cursor.getString(cursor.getColumnIndex(name)));
                }

            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
    }


    private void publishApprovedAndCosignedTxs(ContentProviderClient client) throws RemoteException, OperationApplicationException {

        BlockchainServer server = new BlockchainServer(true);

        TeambrellaContentProviderClient tbClient = new TeambrellaContentProviderClient(client);
        List<Tx> txs = getApprovedAndCosignedTxs(tbClient);
        for (Tx tx : txs) {
            Transaction transaction = getTransaction(tx);

            if (transaction == null) {
                continue;
            }

            BTCAddress fromAddress = tx.getFromAddress();
            List<Cosigner> cosigners = getCosigners(tbClient, fromAddress);
            Collections.sort(cosigners, new Comparator<Cosigner>() {
                @Override
                public int compare(Cosigner o1, Cosigner o2) {
                    return Integer.valueOf(o1.keyOrder).compareTo(o2.keyOrder);
                }
            });
            Script script = getRedeemScript(tx.getFromAddress(), cosigners);
            ScriptBuilder[] ops = new ScriptBuilder[tx.txInputs.size()];

            for (Cosigner cosigner : cosigners) {
                for (int i = 0; i < tx.txInputs.size(); i++) {
                    TxInput txInput = tx.txInputs.get(i);
                    TXSignature signature = tbClient.queryOne(TeambrellaRepository.TXSignature.CONTENT_URI, TeambrellaRepository.TXSignature.TX_INPUT_ID + "=? AND "
                            + TeambrellaRepository.TXSignature.TEAMMATE_ID + "=?", new String[]{txInput.id.toString(), Long.toString(cosigner.teammateId)}, TXSignature.class);

                    if (signature == null) {
                        break;
                    }

                    if (ops[i] == null) {
                        ScriptBuilder builder = new ScriptBuilder();
                        builder.addChunk(new ScriptChunk(ScriptOpCodes.OP_0, null));
                        ops[i] = builder;
                    }


                    byte[] data = new byte[signature.bSignature.length + 1];
                    System.arraycopy(signature.bSignature, 0, data, 0, signature.bSignature.length);
                    data[signature.bSignature.length] = Transaction.SigHash.ALL.byteValue();
                    ops[i].data(data);
                }
            }

            for (int i = 0; i < tx.txInputs.size(); i++) {
                byte[] signature = cosign(script, transaction, i);
                addSignature(tx.txInputs.get(i).id.toString(), tx.teammateId, signature);
                byte[] vSignature = new byte[signature.length + 1];
                System.arraycopy(signature, 0, vSignature, 0, signature.length);
                vSignature[signature.length] = Transaction.SigHash.ALL.byteValue();
                ops[i].data(vSignature);
                ops[i].data(script.getProgram());
                transaction.getInput(i).setScriptSig(ops[i].build());
            }

            if (server.checkTransaction(transaction.getHashAsString()) || server.pushTransaction(org.spongycastle.util.encoders.Hex.toHexString(transaction.bitcoinSerialize()))) {
                ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                operations.add(setTxPublished(tx));
                client.applyBatch(operations);
            } else {
                throw new RuntimeException("unable to publish");
            }

        }
    }


    private static List<Tx> getApprovedAndCosignedTxs(TeambrellaContentProviderClient client) throws RemoteException {
        List<Tx> list = client.queryList(TeambrellaRepository.Tx.CONTENT_URI, TeambrellaRepository.Tx.RESOLUTION + "=? AND "
                + TeambrellaRepository.Tx.STATE + "= ?", new String[]{Integer.toString(TeambrellaModel.TX_CLIENT_RESOLUTION_APPROVED),
                Integer.toString(TeambrellaModel.TX_STATE_COSIGNED)}, Tx.class);
        Iterator<Tx> iterator = list != null ? list.iterator() : null;
        if (iterator != null) {
            while (iterator.hasNext()) {
                Tx tx = iterator.next();
                tx.txInputs = client.queryList(TeambrellaRepository.TXInput.CONTENT_URI, TeambrellaRepository.TXInput.TX_ID + "=?", new String[]{tx.id.toString()}, TxInput.class);
                if (tx.txInputs == null || tx.txInputs.isEmpty()) {
                    iterator.remove();
                } else {
                    tx.txOutputs = client.queryList(TeambrellaRepository.TXOutput.CONTENT_URI,
                            TeambrellaRepository.TXOutput.TX_ID + "=?", new String[]{tx.id.toString()}, TxOutput.class);
                    tx.teammate = client.queryOne(TeambrellaRepository.Teammate.CONTENT_URI,
                            TeambrellaRepository.TEAMMATE_TABLE + "." + TeambrellaRepository.Teammate.ID + "=?", new String[]{Long.toString(tx.teammateId)}, Teammate.class);
                    if (tx.teammate != null) {
                        tx.teammate.addresses = client.queryList(TeambrellaRepository.BTCAddress.CONTENT_URI, TeambrellaRepository.BTCAddress.TEAMMATE_ID + "=?"
                                , new String[]{Long.toString(tx.teammate.id)}, BTCAddress.class);
                    }
                }
            }
        }
        return list;
    }
}

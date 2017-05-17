package com.teambrella.android.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.teambrella.android.TeambrellaApplication;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.blockchain.BlockchainServer;
import com.teambrella.android.content.TeambrellaContentProviderClient;
import com.teambrella.android.content.TeambrellaRepository;
import com.teambrella.android.content.model.BTCAddress;
import com.teambrella.android.content.model.ServerUpdates;
import com.teambrella.android.content.model.Tx;
import com.teambrella.android.content.model.TxInput;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.script.Script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


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
    private final static String ACTION_MASTER_SIGNATURE = "master_signature";
    private final static String SHOW = "show";

    private TeambrellaServer mServer;
    private ContentProviderClient mClient;
    private TeambrellaContentProviderClient mTeambrellaClient;

    /**
     * Key
     */
    private final ECKey mKey;


    public TeambrellaUtilService() {
        super("Util Service");
        AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByTypeForPackage(TeambrellaApplication.ACCOUNT_TYPE, getPackageName());
        Account account = accounts.length > 0 ? accounts[0] : null;
        String privateKey = null;
        if (account != null) {
            privateKey = accountManager.getPassword(account);
        }

        if (privateKey != null) {
            mServer = new TeambrellaServer(this, privateKey);
            mKey = DumpedPrivateKey.fromBase58(null, privateKey).getKey();
        } else {
            throw new RuntimeException("Missing private key");
        }
        mServer = new TeambrellaServer(this, privateKey);
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
                        operations.addAll(mTeambrellaClient.autoApproveTxs());
                        mClient.applyBatch(operations);
                    } catch (RemoteException | OperationApplicationException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_COSING:
                    try {
                        cosignApprovedTransactions();
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
                        publishApprovedAndCosignedTxs();
                    } catch (RemoteException | OperationApplicationException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_MASTER_SIGNATURE:
                    try {
                        masterSign();
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

            mTeambrellaClient.updateConnectionTime(new Date().getTime());

            JsonObject result = mServer.execute(TeambrellaUris.getUpdates(), mTeambrellaClient.getClientUpdates());

            if (result != null) {
                JsonObject status = result.get(TeambrellaModel.ATTR_STATUS).getAsJsonObject();
                long timestamp = status.get(TeambrellaModel.ATTR_STATUS_TIMESTAMP).getAsLong();

                JsonObject data = result.get(TeambrellaModel.ATTR_DATA).getAsJsonObject();


                operations.addAll(TeambrellaContentProviderClient.clearNeedUpdateServerFlag());


                ServerUpdates serverUpdates = new Gson().fromJson(data, ServerUpdates.class);

                operations.addAll(mTeambrellaClient.applyUpdates(serverUpdates));

                if (!operations.isEmpty()) {
                    mClient.applyBatch(operations);
                }

                operations.clear();

                if (serverUpdates.txs != null) {
                    operations.addAll(mTeambrellaClient.checkArrivingTx(serverUpdates.txs));
                }

                if (!operations.isEmpty()) {
                    mClient.applyBatch(operations);
                }

                mTeambrellaClient.setLastUpdatedTime(timestamp);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Cosign transaction
     *
     * @param tx transaction
     * @return list of operations to apply
     */
    private List<ContentProviderOperation> cosignTransaction(Tx tx) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        Transaction transaction = SignHelper.getTransaction(tx);
        if (transaction != null) {
            BTCAddress address = tx.getFromAddress();
            if (address != null) {
                Script redeemScript = SignHelper.getRedeemScript(address, tx.cosigners);
                Collections.sort(tx.txInputs, new Comparator<TxInput>() {
                    @Override
                    public int compare(TxInput o1, TxInput o2) {
                        return o1.id.compareTo(o2.id);
                    }
                });
                for (int i = 0; i < tx.txInputs.size(); i++) {
                    byte[] signature = cosign(redeemScript, transaction, i);
                    operations.add(TeambrellaContentProviderClient.addSignature(tx.txInputs.get(i).id.toString(), 2275, signature));
                }
            }
        }
        return operations;
    }

    private void cosignApprovedTransactions() throws RemoteException, OperationApplicationException {
        List<Tx> list = mTeambrellaClient.getCosinableTx();
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        if (list != null) {
            for (Tx tx : list) {
                operations.addAll(cosignTransaction(tx));
                operations.add(TeambrellaContentProviderClient.setTxSigned(tx));
            }
        }
        mClient.applyBatch(operations);
    }


    private void masterSign() throws RemoteException, OperationApplicationException {
        List<Tx> list = mTeambrellaClient.getApprovedAndCosignedTxs();
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        if (list != null) {
            for (Tx tx : list) {
                operations.addAll(cosignTransaction(tx));
                operations.add(TeambrellaContentProviderClient.setTxSigned(tx));
            }
        }
        mClient.applyBatch(operations);
    }


    private byte[] cosign(Script redeemScript, Transaction transaction, int inputNum) {
        Sha256Hash hash = transaction.hashForSignature(inputNum, redeemScript, Transaction.SigHash.ALL, false);
        return mKey.sign(hash).encodeToDER();
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


    private void publishApprovedAndCosignedTxs() throws RemoteException, OperationApplicationException {
        BlockchainServer server = new BlockchainServer(true);
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        List<Tx> txs = mTeambrellaClient.getApprovedAndCosignedTxs();
        for (Tx tx : txs) {
            Transaction transaction = SignHelper.getTransactionToPublish(tx);
            if (transaction != null && server.checkTransaction(transaction.getHashAsString()) || server.pushTransaction(org.spongycastle.util.encoders.Hex.toHexString(transaction.bitcoinSerialize()))) {
                operations.add(TeambrellaContentProviderClient.setTxPublished(tx));
                mClient.applyBatch(operations);
            } else {
                throw new RuntimeException("unable to publish");
            }

        }
    }
}

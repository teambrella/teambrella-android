package com.teambrella.android.util;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.teambrella.android.TeambrellaUser;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.blockchain.BlockchainServer;
import com.teambrella.android.content.TeambrellaContentProviderClient;
import com.teambrella.android.content.TeambrellaRepository;
import com.teambrella.android.content.model.BTCAddress;
import com.teambrella.android.content.model.ServerUpdates;
import com.teambrella.android.content.model.Teammate;
import com.teambrella.android.content.model.Tx;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.script.Script;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Teambrella util service
 */
public class TeambrellaUtilService extends GcmTaskService {

    private static final String LOG_TAG = TeambrellaUtilService.class.getSimpleName();
    private static final String EXTRA_URI = "uri";


    private final static String ACTION_UPDATE = "update";
    private final static String ACTION_APPROVE = "approve";
    private final static String ACTION_COSING = "cosign";
    private final static String ACTION_PUBLISH = "publish";
    private final static String ACTION_MASTER_SIGNATURE = "master_signature";
    private final static String SHOW = "show";
    private final static String ACTION_SYNC = "sync";

    private TeambrellaServer mServer;
    private ContentProviderClient mClient;
    private TeambrellaContentProviderClient mTeambrellaClient;

    /**
     * Key
     */
    private ECKey mKey;


//    public TeambrellaUtilService() {
//        super("Util Service");
//    }


    @Override
    public void onCreate() {
        super.onCreate();
        String privateKey = TeambrellaUser.get(this).getPrivateKey();
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

//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//        try {
//            processIntent(intent);
//        } catch (RemoteException | OperationApplicationException | TeambrellaException e) {
//            e.printStackTrace();
//        }
//    }


    @Override
    public int onRunTask(TaskParams taskParams) {
        try {
            sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    private void processIntent(Intent intent) throws RemoteException, OperationApplicationException, TeambrellaException {
        String action = intent != null ? intent.getAction() : null;
        if (action != null) {
            switch (action) {
                case ACTION_UPDATE:
                    update();
                    break;
                case ACTION_APPROVE:
                    autoApproveTxs();
                    break;
                case ACTION_COSING:
                    cosignApprovedTransactions();
                    break;
                case SHOW:
                    show(Uri.parse(intent.getStringExtra(EXTRA_URI)));
                    break;
                case ACTION_PUBLISH:
                    publishApprovedAndCosignedTxs();
                    break;
                case ACTION_MASTER_SIGNATURE:
                    masterSign();
                    break;
                case ACTION_SYNC:
                    sync();
                    break;
                default:
                    Log.e(LOG_TAG, "unknown action " + action);
            }
        } else {
            Log.e(LOG_TAG, "action is null");
        }
    }

    private boolean update() throws RemoteException, OperationApplicationException, TeambrellaException {
        boolean result = false;
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        mTeambrellaClient.updateConnectionTime(new Date().getTime());

        JsonObject response = mServer.execute(TeambrellaUris.getUpdates(), mTeambrellaClient.getClientUpdates());

        if (response != null) {
            JsonObject status = response.get(TeambrellaModel.ATTR_STATUS).getAsJsonObject();
            long timestamp = status.get(TeambrellaModel.ATTR_STATUS_TIMESTAMP).getAsLong();

            JsonObject data = response.get(TeambrellaModel.ATTR_DATA).getAsJsonObject();

            ServerUpdates serverUpdates = new Gson().fromJson(data, ServerUpdates.class);

            operations.addAll(mTeambrellaClient.applyUpdates(serverUpdates));


            result = !operations.isEmpty();

            operations.addAll(TeambrellaContentProviderClient.clearNeedUpdateServerFlag());
            mClient.applyBatch(operations);
            operations.clear();

            if (serverUpdates.txs != null) {
                operations.addAll(mTeambrellaClient.checkArrivingTx(serverUpdates.txs));
            }

            if (!operations.isEmpty()) {
                mClient.applyBatch(operations);
            }

            mTeambrellaClient.setLastUpdatedTime(timestamp);

        }
        return result;
    }


    private boolean autoApproveTxs() throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.addAll(mTeambrellaClient.autoApproveTxs());
        if (!operations.isEmpty()) {
            mClient.applyBatch(operations);
        }
        return !operations.isEmpty();
    }


    /**
     * Cosign transaction
     *
     * @param tx transaction
     * @return list of operations to apply
     */
    private List<ContentProviderOperation> cosignTransaction(Tx tx, long userId) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        Transaction transaction = SignHelper.getTransaction(tx);
        if (transaction != null) {
            BTCAddress address = tx.getFromAddress();
            if (address != null) {
                Script redeemScript = SignHelper.getRedeemScript(address, tx.cosigners);
                for (int i = 0; i < tx.txInputs.size(); i++) {
                    byte[] signature = cosign(redeemScript, transaction, i);
                    operations.add(TeambrellaContentProviderClient.addSignature(tx.txInputs.get(i).id.toString(), userId, signature));
                }
            }
        }
        return operations;
    }

    private boolean cosignApprovedTransactions() throws RemoteException, OperationApplicationException {
        List<Tx> list = mTeambrellaClient.getCosinableTx();
        Teammate user = mTeambrellaClient.getTeammate(mKey.getPublicKeyAsHex());
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        if (list != null) {
            for (Tx tx : list) {
                operations.addAll(cosignTransaction(tx, user.id));
                operations.add(TeambrellaContentProviderClient.setTxSigned(tx));
            }
        }
        mClient.applyBatch(operations);
        return !operations.isEmpty();
    }


    private boolean masterSign() throws RemoteException, OperationApplicationException {
        List<Tx> list = mTeambrellaClient.getApprovedAndCosignedTxs();
        Teammate user = mTeambrellaClient.getTeammate(mKey.getPublicKeyAsHex());
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        if (list != null) {
            for (Tx tx : list) {
                operations.addAll(cosignTransaction(tx, user.id));
                operations.add(TeambrellaContentProviderClient.setTxSigned(tx));
            }
        }
        mClient.applyBatch(operations);
        return !operations.isEmpty();
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


    private boolean publishApprovedAndCosignedTxs() throws RemoteException, OperationApplicationException {
        BlockchainServer server = new BlockchainServer(true);
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        List<Tx> txs = mTeambrellaClient.getApprovedAndCosignedTxs();
        for (Tx tx : txs) {
            Transaction transaction = SignHelper.getTransactionToPublish(tx);
            if (transaction != null && server.checkTransaction(transaction.getHashAsString()) || server.pushTransaction(org.spongycastle.util.encoders.Hex.toHexString(transaction.bitcoinSerialize()))) {
                operations.add(TeambrellaContentProviderClient.setTxPublished(tx));
                mClient.applyBatch(operations);
            }
        }

        return !operations.isEmpty();
    }

    private void sync() throws RemoteException, OperationApplicationException, TeambrellaException {
        Log.v(LOG_TAG, "start syncing...");
        do {
            autoApproveTxs();
            cosignApprovedTransactions();
            masterSign();
            publishApprovedAndCosignedTxs();
        } while (update());
    }
}

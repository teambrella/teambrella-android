package com.teambrella.android.util;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.TaskParams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.backup.TeambrellaBackupData;
import com.teambrella.android.blockchain.CryptoException;
import com.teambrella.android.content.TeambrellaContentProviderClient;
import com.teambrella.android.content.TeambrellaRepository;
import com.teambrella.android.content.model.Multisig;
import com.teambrella.android.content.model.ServerUpdates;
import com.teambrella.android.content.model.Teammate;
import com.teambrella.android.content.model.Tx;
import com.teambrella.android.content.model.Unconfirmed;
import com.teambrella.android.services.TeambrellaNotificationService;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.util.log.Log;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.android.gms.gcm.Task.NETWORK_STATE_CONNECTED;


/**
 * Teambrella util service
 */
public class TeambrellaUtilService extends GcmTaskService {

    public static final String SYNC_WALLET_TASK_TAG = "TEAMBRELLA-SYNC-WALLET";
    public static final String SYNC_WALLET_ONCE_TAG = "TEAMBRELLA-SYNC-WALLET-ONCE";
    public static final String CHECK_SOCKET = "TEAMBRELLA_CHECK_SOCKET";

    private static final String LOG_TAG = TeambrellaUtilService.class.getSimpleName();
    private static final String EXTRA_URI = "uri";


    private final static String ACTION_UPDATE = "update";
    private final static String ACTION_CREATE = "create";
    private final static String ACTION_VERIFY = "verify";
    private final static String ACTION_DEPOSIT = "deposit";
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


    public static void scheduleWalletSync(Context context) {
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(TeambrellaUtilService.class)
                .setTag(TeambrellaUtilService.SYNC_WALLET_TASK_TAG)
                .setUpdateCurrent(true) // kill tasks with the same tag if any
                .setPersisted(true)
                .setPeriod(30 * 60)     // 30 minutes period
                .setRequiredNetwork(NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .build();
        GcmNetworkManager.getInstance(context).schedule(task);
    }

    public static void oneoffWalletSync(Context context) {
        OneoffTask task = new OneoffTask.Builder()
                .setService(TeambrellaUtilService.class)
                .setTag(SYNC_WALLET_ONCE_TAG)
                .setExecutionWindow(0L, 1L)
                .setRequiresCharging(false)
                .build();
        GcmNetworkManager.getInstance(context).schedule(task);
    }

    public static void scheduleCheckingSocket(Context context) {
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(TeambrellaUtilService.class)
                .setTag(TeambrellaUtilService.CHECK_SOCKET)
                .setUpdateCurrent(true) // kill tasks with the same tag if any
                .setPersisted(true)
                .setPeriod(60)     // 30 minutes period
                .setFlex(30)       // +/- 10 minutes
                .setRequiredNetwork(NETWORK_STATE_CONNECTED)
                .build();
        GcmNetworkManager.getInstance(context).schedule(task);
    }


    @Override
    public void onCreate() {
        //Log.v(LOG_TAG, "Periodic task created");
        super.onCreate();

        tryInit();
    }

    private boolean tryInit() {
        if (mKey != null) return true;

        TeambrellaUser user = TeambrellaUser.get(this);
        String privateKey = !user.isDemoUser() ? TeambrellaUser.get(this).getPrivateKey() : null;
        if (privateKey != null) {
            mKey = DumpedPrivateKey.fromBase58(null, privateKey).getKey();
            mServer = new TeambrellaServer(this, privateKey);
            mClient = getContentResolver().acquireContentProviderClient(TeambrellaRepository.AUTHORITY);
            mTeambrellaClient = new TeambrellaContentProviderClient(mClient);
            return true;
        } else {
            Log.w(LOG_TAG, "No crypto key has been generated for this user yet. Skipping the sync task till her Facebook login.");
            return false;
        }
    }


    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        scheduleWalletSync(this);
        scheduleCheckingSocket(this);
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i1) {
        //Log.v(LOG_TAG, "Periodic task started a command" + intent.toString());

//        if(BuildConfig.DEBUG){
//            new android.os.AsyncTask<Void, Void, Void>() {
//                @Override
//                protected Void doInBackground(Void... voids) {
//                    try {
//                          processIntent(intent);
//                    } catch (Exception e) {
//                        Log.e(LOG_TAG, "" + e.getMessage(), e);
//                    }
//                    return null;
//                }
//            }.execute();
//
//            Log.e(LOG_TAG, "INTENT STARTED" + intent.toString());
//            return START_NOT_STICKY;
//        }else

        return super.onStartCommand(intent, i, i1);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        String tag = taskParams.getTag();
        if (tag != null) {
            switch (tag) {
                case SYNC_WALLET_TASK_TAG:
                case SYNC_WALLET_ONCE_TAG:
                    Log.v(LOG_TAG, "Sync wallet task ran");
                    try {
                        if (tryInit()) {
                            sync();
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "sync attempt failed:");
                        Log.e(LOG_TAG, "sync error message was: " + e.getMessage());
                        Log.e(LOG_TAG, "sync error call stack was: ", e);
                        if (!BuildConfig.DEBUG) {
                            Crashlytics.logException(e);
                        }
                    }
                    break;
                case CHECK_SOCKET:
                    startService(new Intent(this, TeambrellaNotificationService.class)
                            .setAction(TeambrellaNotificationService.CONNECT_ACTION));
                    break;
            }
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    private void processIntent(Intent intent) throws CryptoException, RemoteException, OperationApplicationException, TeambrellaException {
        String action = intent != null ? intent.getAction() : null;
        if (action != null) {
            switch (action) {
                case ACTION_UPDATE:
                    update();
                    break;
                case ACTION_CREATE:
                    createWallets(1_500_000);
                    break;
                case ACTION_VERIFY:
                    verifyIfWalletIsCreated(1_500_000);
                    break;
                case ACTION_DEPOSIT:
                    depositWallet();
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
                case "test":
                    if (BuildConfig.DEBUG) {
                        Log.e(LOG_TAG, "Test message is OK!");
                    }
                    break;
                default:
                    if (BuildConfig.DEBUG) {
                        Log.e(LOG_TAG, "unknown action " + action);
                    }
            }
        } else {
            Log.e(LOG_TAG, "action is null");
        }
    }

    private boolean update() throws RemoteException, OperationApplicationException, TeambrellaException {
        boolean result = false;
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        mTeambrellaClient.updateConnectionTime(new Date());

        JsonObject response = mServer.requestObservable(TeambrellaUris.getUpdates(), mTeambrellaClient.getClientUpdates())
                .blockingFirst();

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

            mTeambrellaClient.setLastUpdatedTimestamp(timestamp);

        }
        return result;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean createWallets(long gasLimit) throws RemoteException, TeambrellaException, OperationApplicationException, CryptoException {
        String myPublicKey = mKey.getPublicKeyAsHex();
        List<Multisig> myUncreatedMultisigs;
        myUncreatedMultisigs = mTeambrellaClient.getMultisigsToCreate(myPublicKey);
        if (myUncreatedMultisigs.size() == 0) {
            return false;
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        EthWallet myWallet = getWallet();
        myWallet.refreshGasPrice();
        long myNonce = myWallet.checkMyNonce();
        for (Multisig m : myUncreatedMultisigs) {
            Multisig sameTeammateMultisig = getMyTeamMultisigIfAny(myPublicKey, m.teammateId, myUncreatedMultisigs);
            if (sameTeammateMultisig != null) {

                // todo: move "cosigner list", and send to the server the move tx (not creation tx).
                ////boolean needServerUpdate = (sameTeammateMultisig.address != null);
                ////operations.add(mTeambrellaClient.setMutisigAddressTxAndNeedsServerUpdate(m, sameTeammateMultisig.address, sameTeammateMultisig.creationTx, needServerUpdate));

            } else {
                long gasPrice = myWallet.getGasPriceForContractCreation();
                String txHex = myWallet.createOneWallet(myNonce, m, gasLimit, gasPrice);
                if (txHex != null) {
                    // There could be 2 my pending mutisigs (Current and Next) for the same team. So we remember the first creation tx and don't create 2 contracts for the same team.
                    m.creationTx = txHex;
                    operations.add(TeambrellaContentProviderClient.setMutisigAddressTxAndNeedsServerUpdate(m, null, txHex, false));
                    operations.add(mTeambrellaClient.insertUnconfirmed(m.id, txHex, gasPrice, myNonce, new Date()));
                    myNonce++;
                }
            }
        }

        if (!operations.isEmpty()) {
            // Since now the local db will remember all the created contracts.
            mClient.applyBatch(operations);
            return true;
        }
        return false;
    }

    private Multisig getMyTeamMultisigIfAny(String myPublicKey, long myTeammateId, List<Multisig> myUncreatedMultisigs) throws RemoteException {
        // myUncreatedMultisigs remembers (not commited to local db) created addresses. So we don't create 2 contracts for the same team:
        for (Multisig m : myUncreatedMultisigs) {
            if (m.teammateId == myTeammateId && m.creationTx != null) {
                return m;
            }
        }

        List<Multisig> sameTeamMultisigs = mTeambrellaClient.getMultisigsWithAddressByTeammate(myPublicKey, myTeammateId);
        if (sameTeamMultisigs.size() > 0) {
            return sameTeamMultisigs.get(0);
        }

        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean verifyIfWalletIsCreated(long gasLimit) throws CryptoException, RemoteException, OperationApplicationException {

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        String myPublicKey = mKey.getPublicKeyAsHex();
        List<Multisig> creationTxes = mTeambrellaClient.getMultisigsInCreation(myPublicKey);
        for (Multisig m : creationTxes) {

            Unconfirmed oldUnconfirmed = mTeambrellaClient.getUnconfirmed(m.id, m.creationTx);
            m.unconfirmed = oldUnconfirmed;

            getWallet().validateCreationTx(m, gasLimit);
            if (m.address != null) {

                operations.add(TeambrellaContentProviderClient.setMultisigAddressAndNeedsServerUpdate(m, m.address));

            } else if (m.unconfirmed != oldUnconfirmed) {

                operations.add(TeambrellaContentProviderClient.setMutisigAddressTxAndNeedsServerUpdate(m, null, m.creationTx, false));
                operations.add(mTeambrellaClient.insertUnconfirmed(m.unconfirmed));

            } else if (m.status == TeambrellaModel.USER_MULTISIG_STATUS_CREATION_FAILED) {

                operations.add(TeambrellaContentProviderClient.setMultisigStatus(m, TeambrellaModel.USER_MULTISIG_STATUS_CREATION_FAILED));

            } else if (m.unconfirmed == null) {

                operations.add(TeambrellaContentProviderClient.setMutisigAddressTxAndNeedsServerUpdate(m, null, null, false));

            }

        }
        if (!operations.isEmpty()) {
            mClient.applyBatch(operations);
            return true;
        }
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean depositWallet() throws CryptoException, RemoteException {
        String myPublicKey = mKey.getPublicKeyAsHex();
        List<Multisig> myCurrentMultisigs = mTeambrellaClient.getCurrentMultisigsWithAddress(myPublicKey);
        if (myCurrentMultisigs.size() == 1) {
            return getWallet().deposit(myCurrentMultisigs.get(0));
        }

        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
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
    private List<ContentProviderOperation> cosignTransaction(Tx tx, long userId) throws CryptoException {

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        ////btc:Transaction transaction = SignHelper.getTransaction(tx);
        ////if (transaction != null) {
        switch (tx.kind) {
            case TeambrellaModel.TX_KIND_PAYOUT:
            case TeambrellaModel.TX_KIND_WITHDRAW:
                Multisig multisig = tx.getFromMultisig();
                if (multisig != null) {
                    EthWallet wallet = getWallet();

                    String from = multisig.address;
                    for (int i = 0; i < tx.txInputs.size(); i++) {
                        byte[] signature = wallet.cosign(tx, tx.txInputs.get(i));
                        operations.add(TeambrellaContentProviderClient.addSignature(tx.txInputs.get(i).id.toString(), userId, signature));
                    }
                }
                break;
            default:
                // TODO: support move & incoming TXs
                break;
        }
        ////}
        return operations;
    }

    private EthWallet getWallet() throws CryptoException {
        String myPublicKey = mKey.getPublicKeyAsHex();
        String keyStorePath = getApplicationContext().getFilesDir().getPath() + "/keystore/" + myPublicKey;
        String keyStoreSecret = mKey.getPrivateKeyAsWiF(new MainNetParams());
        byte[] privateKey = mKey.getPrivKeyBytes();

        return new EthWallet(privateKey, keyStorePath, keyStoreSecret, BuildConfig.isTestNet);
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean cosignApprovedTransactions() throws RemoteException, OperationApplicationException, CryptoException {
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


    @SuppressWarnings("UnusedReturnValue")
    private boolean masterSign() throws RemoteException, OperationApplicationException, CryptoException {
        return false;
    }


    private void show(Uri uri) throws RemoteException {
        Cursor cursor = mClient.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                if (BuildConfig.DEBUG) {
                    Log.e(LOG_TAG, "***");
                    for (String name : cursor.getColumnNames()) {
                        Log.d(LOG_TAG, name + ":" + cursor.getString(cursor.getColumnIndex(name)));
                    }
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
    }


    @SuppressWarnings("UnusedReturnValue")
    private boolean publishApprovedAndCosignedTxs() throws RemoteException, OperationApplicationException, CryptoException {

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        List<Tx> txs = mTeambrellaClient.getApprovedAndCosignedTxs();
        for (Tx tx : txs) {

            EthWallet wallet = getWallet();
            wallet.refreshGasPrice();
            switch (tx.kind) {
                case TeambrellaModel.TX_KIND_PAYOUT:
                case TeambrellaModel.TX_KIND_WITHDRAW:
                    String cryptoTxHash = wallet.publish(tx);
                    if (cryptoTxHash != null) {
                        operations.add(TeambrellaContentProviderClient.setTxPublished(tx, cryptoTxHash));
                        mClient.applyBatch(operations);
                    }
                    break;
                default:
                    // TODO: support move & incoming TXs
                    break;
            }
        }

        return !operations.isEmpty();
    }

    private void sync() throws CryptoException, RemoteException, OperationApplicationException, TeambrellaException {

        Log.v(LOG_TAG, "start syncing...");


        boolean hasNews = true;
        for (int attempt = 0; attempt < 3 && hasNews; attempt++) {

            boolean fixed = hotFix4CorruptedContract();
            if (!fixed) continue;

            createWallets(1_300_000);
            verifyIfWalletIsCreated(1_300_000);
            depositWallet();
            autoApproveTxs();
            cosignApprovedTransactions();
            masterSign();
            publishApprovedAndCosignedTxs();


            hasNews = update();
        }

        backUpPrivateKey();
    }

    private boolean hotFix4CorruptedContract() throws CryptoException, RemoteException, OperationApplicationException {

        String myPublicKey = mKey.getPublicKeyAsHex();
        List<Multisig> myCurrentMultisigs = mTeambrellaClient.getCurrentMultisigsWithAddress(myPublicKey);
        mTeambrellaClient.joinCosigners(myCurrentMultisigs);

        if (myCurrentMultisigs.size() == 1) {

            Multisig m = myCurrentMultisigs.get(0);
            Unconfirmed oldUnconfirmed = m.unconfirmed;
            String oldCreationTx = m.creationTx;

            boolean fixed = getWallet().hotFix4CorruptedContract(myCurrentMultisigs.get(0));
            if (fixed && m.unconfirmed != oldUnconfirmed && m.creationTx != oldCreationTx) {

                ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                operations.add(TeambrellaContentProviderClient.setMutisigAddressTxAndNeedsServerUpdate(m, null, m.creationTx, false));
                operations.add(mTeambrellaClient.insertUnconfirmed(m.unconfirmed));
                mClient.applyBatch(operations);

            }
            return fixed;
        }

        return true;
    }


    private void backUpPrivateKey() throws CryptoException, RemoteException, OperationApplicationException {
        TeambrellaBackupData backupData = new TeambrellaBackupData(this);
        Teammate teammate = mTeambrellaClient.getTeammate(mKey.getPublicKeyAsHex());
        if (teammate != null && teammate.facebookName != null) {
            String key = Integer.toString(teammate.facebookName.hashCode());
            if (backupData.getValue(key) == null) {
                backupData.setValue(key, TeambrellaUser.get(this).getPrivateKey());
            }
        }
    }
}

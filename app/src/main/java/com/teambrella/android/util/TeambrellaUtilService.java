package com.teambrella.android.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.TaskParams;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.R;
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

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.android.gms.gcm.Task.NETWORK_STATE_CONNECTED;


/**
 * Teambrella util service
 */
public class TeambrellaUtilService extends GcmTaskService {

    private static final long MIN_SYNC_DELAY = 1000 * 60 * 30;
    private static final String ACTION_LOCAL_SYNC = "com.teambrella.android.util.ACTION_SYNC";
    private static final String EXTRA_TAG = "extra_tag";
    private static final String TASK_EXTRAS = "tag_extras";

    public static final String SYNC_WALLET_TASK_TAG = "TEAMBRELLA-SYNC-WALLET";
    public static final String SYNC_WALLET_ONCE_TAG = "TEAMBRELLA-SYNC-WALLET-ONCE";
    public static final String CHECK_SOCKET = "TEAMBRELLA_CHECK_SOCKET";
    public static final String DEBUG_DB_TASK_TAG = "TEAMBRELLA_DEBUG_DB";
    public static final String DEBUG_UPDATE_TAG = "TEAMBRELLA_DEBUG_UPDATE";

    private static final String LOG_TAG = TeambrellaUtilService.class.getSimpleName();
    private static final String EXTRA_DEBUG_LOGGING = "debug_logging";

    private TeambrellaServer mServer;
    private ContentProviderClient mClient;
    private TeambrellaContentProviderClient mTeambrellaClient;

    private ECKey mKey;
    private EthWallet mWallet;


    public static void scheduleWalletSync(Context context) {
        if (checkGooglePlayServices(context)) {
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
    }

    public static void oneoffWalletSync(Context context) {
        oneoffWalletSync(context, false);
    }


    public static void oneoffWalletSync(Context context, boolean debug) {
        try {
            Bundle args = new Bundle();
            args.putBoolean(EXTRA_DEBUG_LOGGING, debug);
            context.startService(new Intent(context, TeambrellaUtilService.class)
                    .setAction(ACTION_LOCAL_SYNC)
                    .putExtra(EXTRA_TAG, SYNC_WALLET_ONCE_TAG)
                    .putExtra(TASK_EXTRAS, args));
        } catch (Throwable throwable) {
            Log.e(LOG_TAG, throwable.toString());
        }
    }

    public static void oneOffUpdate(Context context, boolean debug) {
        try {
            Bundle args = new Bundle();
            args.putBoolean(EXTRA_DEBUG_LOGGING, debug);
            context.startService(new Intent(context, TeambrellaUtilService.class)
                    .setAction(ACTION_LOCAL_SYNC)
                    .putExtra(EXTRA_TAG, DEBUG_UPDATE_TAG)
                    .putExtra(TASK_EXTRAS, args));
        } catch (Throwable throwable) {
            Log.e(LOG_TAG, throwable.toString());
        }
    }

    public static void scheduleCheckingSocket(Context context) {
        if (checkGooglePlayServices(context)) {
            PeriodicTask task = new PeriodicTask.Builder()
                    .setService(TeambrellaUtilService.class)
                    .setTag(TeambrellaUtilService.CHECK_SOCKET)
                    .setUpdateCurrent(true) // kill tasks with the same tag if any
                    .setPersisted(true)
                    .setPeriod(60)     // 30 minutes period
                    .setFlex(30)       // +/- 10 minutes
                    .setUpdateCurrent(true) // kill tasks with the same tag if any
                    .setRequiredNetwork(NETWORK_STATE_CONNECTED)
                    .build();
            GcmNetworkManager.getInstance(context).schedule(task);
        }
    }

    public static void scheduleDebugDB(Context context) {
        try {
            context.startService(new Intent(context, TeambrellaUtilService.class)
                    .setAction(ACTION_LOCAL_SYNC)
                    .putExtra(EXTRA_TAG, DEBUG_DB_TASK_TAG));
        } catch (Throwable throwable) {
            Log.e(LOG_TAG, throwable.toString());
        }
    }

    public static boolean checkGooglePlayServices(Context context) {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        boolean result;
        switch (code) {
            case ConnectionResult.SUCCESS:
                result = true;
                break;
            case ConnectionResult.SERVICE_MISSING:
                Log.e(LOG_TAG, "google play service missing");
                result = false;
                break;
            case ConnectionResult.SERVICE_UPDATING:
                Log.e(LOG_TAG, "google play service updating");
                result = false;
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Log.e(LOG_TAG, "google play service update required");
                result = false;
                break;
            case ConnectionResult.SERVICE_DISABLED:
                Log.e(LOG_TAG, "google play service disabled");
                result = false;
                break;
            case ConnectionResult.SERVICE_INVALID:
                Log.e(LOG_TAG, "google play service invalid");
                result = false;
                break;
            default:
                result = false;
        }

        if (!result) {
            GoogleApiAvailability.getInstance().showErrorNotification(context, code);
            if (!BuildConfig.DEBUG) {
                Crashlytics.logException(new Exception("google play service error"));
            }
        }

        return result;
    }


    private boolean tryInit() throws CryptoException {
        Log.d(LOG_TAG, "---> SYNC -> tryInit() started...");
        if (mKey != null) return true;
        TeambrellaUser user = TeambrellaUser.get(this);
        String privateKey = !user.isDemoUser() ? user.getPrivateKey() : null;
        String deviceToken = !user.isDemoUser() ? FirebaseInstanceId.getInstance().getToken() : null;
        if (privateKey != null) {
            mKey = DumpedPrivateKey.fromBase58(null, privateKey).getKey();
            mServer = new TeambrellaServer(this, privateKey, user.getDeviceCode(), deviceToken);
            mClient = getContentResolver().acquireContentProviderClient(TeambrellaRepository.AUTHORITY);
            mTeambrellaClient = new TeambrellaContentProviderClient(mClient);
            mWallet = getWallet();
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
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_LOCAL_SYNC.equals(action)) {
            TaskParams taskParams = new TaskParams(intent.getStringExtra(EXTRA_TAG)
                    , intent.getBundleExtra(TASK_EXTRAS));
            new Thread(() -> onRunTask(taskParams)).start();
            return START_STICKY;
        } else {
            return super.onStartCommand(intent, i, i1);
        }
    }

    @Override
    public synchronized int onRunTask(TaskParams taskParams) {
        String tag = taskParams.getTag();
        if (tag != null) {
            switch (tag) {
                case SYNC_WALLET_TASK_TAG:
                case SYNC_WALLET_ONCE_TAG: {
                    if (isDebugLogging(taskParams) || canSyncByTime(System.currentTimeMillis())) {
                        StatisticHelper.onWalletSync(this, tag);
                        if (isDebugLogging(taskParams)) {
                            Log.startDebugging(this);
                        }
                        Log.d(LOG_TAG, "---> SYNC -> onRunTask() started... tag:" + tag);
                        try {
                            if (tryInit()) {
                                sync();
                            }
                            TeambrellaUser.get(this).setLastSyncTime(System.currentTimeMillis());
                        } catch (Exception e) {
                            onSyncException(e);
                        } finally {
                            if (isDebugLogging(taskParams)) {
                                String path = Log.stopDebugging();
                                if (path != null) {
                                    debugLog(this, path);
                                }
                            }
                        }
                    }
                }
                break;

                case DEBUG_UPDATE_TAG: {
                    if (isDebugLogging(taskParams)) {
                        Log.startDebugging(this);
                    }
                    Log.d(LOG_TAG, "---> UPDATE -> onRunTask() started... tag:" + tag);
                    try {
                        if (tryInit()) {
                            update();
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "update attempt failed:");
                        Log.e(LOG_TAG, "update error message was: " + e.getMessage());
                        Log.e(LOG_TAG, "update error call stack was: ", e);
                        Log.reportNonFatal(LOG_TAG, e);
                    } finally {
                        if (isDebugLogging(taskParams)) {
                            String path = Log.stopDebugging();
                            if (path != null) {
                                debugLog(this, path);
                            }
                        }
                    }
                }
                break;

                case CHECK_SOCKET:
                    try {
                        startService(new Intent(this, TeambrellaNotificationService.class)
                                .setAction(TeambrellaNotificationService.CONNECT_ACTION));
                    } catch (Exception e) {
                        Log.reportNonFatal(LOG_TAG, e);
                    }
                    break;
                case DEBUG_DB_TASK_TAG:
                    debugDB(this);
                    break;
            }
        } else {
            Log.reportNonFatal(LOG_TAG, new Exception("onRunTask got null tag."));
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }

    private void onSyncException(Exception e) {
        Log.e(LOG_TAG, "sync attempt failed:");
        Log.e(LOG_TAG, "sync error message was: " + e.getMessage());
        Log.e(LOG_TAG, "sync error call stack was: ", e);
        Log.reportNonFatal(LOG_TAG, e);

        Log.e(LOG_TAG, " --- SYNC -> onRunTask() Resetting server data...");
        try {
            mTeambrellaClient.setLastUpdatedTimestamp(0L);
            mTeambrellaClient.removeLostRecords();
        } catch (Exception e2) {
            Log.e(LOG_TAG, " --- SYNC -> onRunTask() failed to reset server data.");
            Log.reportNonFatal(LOG_TAG, e2);
        }
    }

    private boolean update() throws RemoteException, OperationApplicationException, TeambrellaException {
        Log.d(LOG_TAG, "---> SYNC -> update() started...");
        boolean result = false;
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        mTeambrellaClient.updateConnectionTime(new Date());

        JsonObject response = mServer.requestObservable(TeambrellaUris.getUpdates(), mTeambrellaClient.getClientUpdates())
                .blockingFirst();

        if (response != null) {
            JsonObject status = response.get(TeambrellaModel.ATTR_STATUS).getAsJsonObject();

            // checking status
            checkStatus(status);

            long timestamp = status.get(TeambrellaModel.ATTR_STATUS_TIMESTAMP).getAsLong();

            JsonObject data = response.get(TeambrellaModel.ATTR_DATA).getAsJsonObject();

            ServerUpdates serverUpdates = new Gson().fromJson(data, ServerUpdates.class);

            operations.addAll(mTeambrellaClient.applyUpdates(serverUpdates));


            Log.d(LOG_TAG, " ---- SYNC -- update() operation count:" + operations.size());
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
        Log.d(LOG_TAG, " ^--- SYNC ^- update() finished! result:" + result);
        return result;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean createWallets(long gasLimit) throws RemoteException, TeambrellaException, OperationApplicationException, CryptoException {
        Log.d(LOG_TAG, "---> SYNC -> createWallets() started...");

        String myPublicKey = mKey.getPublicKeyAsHex();
        List<Multisig> myUncreatedMultisigs;
        myUncreatedMultisigs = mTeambrellaClient.getMultisigsToCreate(myPublicKey);
        if (myUncreatedMultisigs.size() == 0) {
            Log.d(LOG_TAG, " ^--- SYNC ^- createWallets() finished! No multisigs to create.");
            return false;
        }
        if (isZeroBalance()) {
            Log.d(LOG_TAG, " ^--- SYNC ^- createWallets() finished! No funds.");
            return false;
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (Multisig m : myUncreatedMultisigs) {
            Log.d(LOG_TAG, " ---- SYNC -- createWallets() detected 1 multisig to created. id:" + m.id);

            Multisig sameTeammateMultisig = getMyTeamMultisigIfAny(myPublicKey, m.teammateId, myUncreatedMultisigs);
            if (sameTeammateMultisig != null) {

                Log.d(LOG_TAG, " ^--- SYNC ^- createWallets() detected another multisig for the same teammate. Not supported.to created.");
                // todo: move "cosigner list", and send to the server the move tx (not creation tx).
                ////boolean needServerUpdate = (sameTeammateMultisig.address != null);
                ////operations.add(mTeambrellaClient.setMutisigAddressTxAndNeedsServerUpdate(m, sameTeammateMultisig.address, sameTeammateMultisig.creationTx, needServerUpdate));

            } else {
                long gasPrice = mWallet.getGasPriceForContractCreation();
                long myNonceBeforeCreation = mWallet.getMyNonce();
                String txHex = mWallet.createOneWallet(m, gasLimit, gasPrice);
                Log.d(LOG_TAG, " ---- SYNC -- createWallets() creation tx published. txHex:" + txHex);
                if (txHex != null) {
                    // There could be 2 my pending mutisigs (Current and Next) for the same team. So we remember the first creation tx and don't create 2 contracts for the same team.
                    m.creationTx = txHex;
                    operations.add(TeambrellaContentProviderClient.setMutisigAddressTxAndNeedsServerUpdate(m, null, txHex, false));
                    operations.add(mTeambrellaClient.insertUnconfirmed(m.id, txHex, gasPrice, myNonceBeforeCreation, new Date()));
                }
            }
        }

        if (!operations.isEmpty()) {
            // Since now the local db will remember all the created contracts.
            mClient.applyBatch(operations);
            Log.d(LOG_TAG, " ^--- SYNC ^- createWallets() finished! result: true");
            return true;
        }

        Log.d(LOG_TAG, " ^--- SYNC ^- createWallets() finished! result: false");
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
        Log.d(LOG_TAG, "---> SYNC -> verifyIfWalletIsCreated() started...");

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        String myPublicKey = mKey.getPublicKeyAsHex();
        List<Multisig> creationTxes = mTeambrellaClient.getMultisigsInCreation(myPublicKey);
        for (Multisig m : creationTxes) {
            Log.d(LOG_TAG, " ---- SYNC -- verifyIfWalletIsCreated() detected 1 multisig in creation. id:" + m.id);

            Unconfirmed oldUnconfirmed = mTeambrellaClient.getUnconfirmed(m.id, m.creationTx);
            m.unconfirmed = oldUnconfirmed;

            mWallet.validateCreationTx(m, gasLimit);
            if (m.address != null) {

                Log.d(LOG_TAG, " ---- SYNC -- verifyIfWalletIsCreated() address validated:" + m.address);
                operations.add(TeambrellaContentProviderClient.setMultisigAddressAndNeedsServerUpdate(m, m.address));

            } else if (m.unconfirmed != oldUnconfirmed) {

                Log.d(LOG_TAG, " ---- SYNC -- verifyIfWalletIsCreated() creation tx outdated. New creaton tx:" + m.creationTx);
                operations.add(TeambrellaContentProviderClient.setMutisigAddressTxAndNeedsServerUpdate(m, null, m.creationTx, false));
                operations.add(mTeambrellaClient.insertUnconfirmed(m.unconfirmed));

            } else if (m.status == TeambrellaModel.USER_MULTISIG_STATUS_CREATION_FAILED) {

                Log.d(LOG_TAG, " ---- SYNC -- verifyIfWalletIsCreated() creation tx failed");
                operations.add(TeambrellaContentProviderClient.setMultisigStatus(m, TeambrellaModel.USER_MULTISIG_STATUS_CREATION_FAILED));

            } else if (m.unconfirmed == null) {

                Log.d(LOG_TAG, " ---- SYNC -- verifyIfWalletIsCreated() creation tx not exist any more. Resetting creation tx.");
                operations.add(TeambrellaContentProviderClient.setMutisigAddressTxAndNeedsServerUpdate(m, null, null, false));

            }

        }
        if (!operations.isEmpty()) {
            mClient.applyBatch(operations);
            Log.d(LOG_TAG, " ^--- SYNC ^- verifyIfWalletIsCreated() finished! result:true");
            return true;
        }
        Log.d(LOG_TAG, " ^--- SYNC ^- verifyIfWalletIsCreated() finished! result:false");
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean depositWallet() throws CryptoException, RemoteException {
        Log.d(LOG_TAG, "---> SYNC -> depositWallet() started...");

        String myPublicKey = mKey.getPublicKeyAsHex();
        List<Multisig> myCurrentMultisigs = mTeambrellaClient.getCurrentMultisigsWithAddress(myPublicKey);
        if (myCurrentMultisigs.size() == 1) {
            Log.d(LOG_TAG, " ---- SYNC -- depositWallet() detected exactly 1 current multisig with address:" + myCurrentMultisigs.get(0).address);
            return mWallet.deposit(myCurrentMultisigs.get(0));
        }

        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean autoApproveTxs() throws RemoteException, OperationApplicationException {
        Log.d(LOG_TAG, "---> SYNC -> autoApproveTxs() started...");

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.addAll(mTeambrellaClient.autoApproveTxs());
        if (!operations.isEmpty()) {
            Log.d(LOG_TAG, " ---- SYNC -- autoApproveTxs() detected approve count:" + operations.size());
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
            case TeambrellaModel.TX_KIND_MOVE_TO_NEXT_WALLET:
                Multisig multisig = tx.getFromMultisig();
                if (multisig != null) {

                    for (int i = 0; i < tx.txInputs.size(); i++) {
                        byte[] signature = mWallet.cosign(tx, tx.txInputs.get(i));
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

    private boolean isZeroBalance() throws CryptoException {
        return mWallet.getBalance().compareTo(BigDecimal.ZERO) <= 0;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean cosignApprovedTransactions() throws RemoteException, OperationApplicationException, CryptoException {
        Log.d(LOG_TAG, "---> SYNC -> cosignApprovedTransactions() started...");

        String publicKey = mKey.getPublicKeyAsHex();
        List<Tx> list = mTeambrellaClient.getCosinableTx(publicKey);
        Teammate user = mTeambrellaClient.getTeammate(publicKey);
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        if (list != null) {
            for (Tx tx : list) {
                try {
                    Log.d(LOG_TAG, " ---- SYNC -- cosignApprovedTransactions() detected tx to cosign. id:" + tx.id);
                    operations.addAll(cosignTransaction(tx, user.id));
                    operations.add(TeambrellaContentProviderClient.setTxSigned(tx));
                } catch (Exception e) {
                    Log.e(LOG_TAG, " ---- SYNC -- cosignApprovedTransactions() failed to cosign tx! id:" + tx.id + ". Continue with others...");
                    Log.reportNonFatal(LOG_TAG, e);
                }
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
        Log.d(LOG_TAG, "---> SYNC -> publishApprovedAndCosignedTxs() started...");

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        List<Tx> txs = mTeambrellaClient.getApprovedAndCosignedTxs(mKey.getPublicKeyAsHex());
        for (Tx tx : txs) {
            Log.d(LOG_TAG, " ---- SYNC -- publishApprovedAndCosignedTxs() detected tx to publish. id:" + tx.id);

            switch (tx.kind) {
                case TeambrellaModel.TX_KIND_PAYOUT:
                case TeambrellaModel.TX_KIND_WITHDRAW:
                case TeambrellaModel.TX_KIND_MOVE_TO_NEXT_WALLET:
                    String cryptoTxHash = mWallet.publish(tx);
                    Log.d(LOG_TAG, " ---- SYNC -- publishApprovedAndCosignedTxs() published. tx hash:" + cryptoTxHash);
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
        Log.d(LOG_TAG, "---> SYNC -> sync() started...");


        boolean hasNews = true;
        for (int attempt = 0; attempt < 3 && hasNews; attempt++) {
            Log.d(LOG_TAG, " ---- SYNC -- sync() attempt:" + attempt);

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


    private static boolean isDebugLogging(TaskParams params) {
        Bundle extras = params.getExtras();
        return extras != null && extras.getBoolean(EXTRA_DEBUG_LOGGING, false);
    }


    private static void debugDB(Context context) {
        try {
            TeambrellaUser user = TeambrellaUser.get(context);
            TeambrellaServer server = new TeambrellaServer(context, user.getPrivateKey(), user.getDeviceCode(), !user.isDemoUser() ? FirebaseInstanceId.getInstance().getToken() : null);
            server.requestObservable(TeambrellaUris.getDebugDbUri(context.getDatabasePath("teambrella").getAbsolutePath()), null)
                    .blockingFirst();
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
        }
    }

    private static void debugLog(Context context, String logPath) {
        try {
            TeambrellaUser user = TeambrellaUser.get(context);
            TeambrellaServer server = new TeambrellaServer(context, user.getPrivateKey(), user.getDeviceCode(), !user.isDemoUser() ? FirebaseInstanceId.getInstance().getToken() : null);
            server.requestObservable(TeambrellaUris.getDebugLogUri(logPath), null)
                    .blockingFirst();

            File file = new File(logPath);
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
        }
    }


    private boolean canSyncByTime(long time) {
        long delay = time - TeambrellaUser.get(this).getLastSyncTime();
        Log.d(LOG_TAG, "" + (delay / (1000 * 60)) + " minutes since last sync");
        return Math.abs(delay) > MIN_SYNC_DELAY;
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

    private void checkStatus(JsonObject status) {
        int recommendedVersion = status.get(TeambrellaModel.ATTR_STATUS_RECOMMENDING_VERSION).getAsInt();
        if (recommendedVersion > BuildConfig.VERSION_CODE) {
            long current = System.currentTimeMillis();
            TeambrellaUser user = TeambrellaUser.get(this);
            final long minDelay = 1000 * 60 * 60 * 24 * 3;
            if (Math.abs(current - Math.max(user.getNewVersionLastScreenTime(), user.getNewVersionLastNotificationTime())) >= minDelay) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification notification = new NotificationCompat.Builder(this, null)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.app_is_outdated_description_notification)))
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_teambrella_status)
                        .setColor(getResources().getColor(R.color.lightBlue))
                        .setContentTitle(getString(R.string.app_is_outdated_title))
                        .setContentText(getString(R.string.app_is_outdated_description_notification))
                        .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(android.content.Intent.ACTION_VIEW)
                                .setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)), PendingIntent.FLAG_UPDATE_CURRENT))
                        .build();
                if (notificationManager != null) {
                    final int id = 333;
                    notificationManager.notify(id, notification);
                }
                user.setNewVersionLastNotificationTime(current);
            }
        }
    }
}

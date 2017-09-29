package com.teambrella.android.util;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.blockchain.AbiArguments;
import com.teambrella.android.blockchain.CryptoException;
import com.teambrella.android.blockchain.EtherAccount;
import com.teambrella.android.blockchain.EtherNode;
import com.teambrella.android.blockchain.Hex;
import com.teambrella.android.blockchain.Scan;
import com.teambrella.android.blockchain.ScanResultTxReceipt;
import com.teambrella.android.blockchain.Sha3;
import com.teambrella.android.content.TeambrellaContentProviderClient;
import com.teambrella.android.content.TeambrellaRepository;
import com.teambrella.android.content.model.Cosigner;
import com.teambrella.android.content.model.Multisig;
import com.teambrella.android.content.model.ServerUpdates;
import com.teambrella.android.content.model.Teammate;
import com.teambrella.android.content.model.Tx;
import com.teambrella.android.services.TeambrellaNotificationService;
import com.teambrella.android.ui.TeambrellaUser;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.ethereum.geth.Account;
import org.ethereum.geth.Accounts;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Geth;
import org.ethereum.geth.KeyStore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Teambrella util service
 */
public class TeambrellaUtilService extends GcmTaskService {

    public static final String SYNC_WALLET_TASK_TAG = "TEAMBRELLA-SYNC-WALLET";
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


    @Override
    public void onCreate() {
        Log.v(LOG_TAG, "Periodic task created");
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
    public int onStartCommand(Intent intent, int i, int i1) {
        //Log.v(LOG_TAG, "Periodic task started a command" + intent.toString());

//        if(BuildConfig.DEBUG){
//            new AsyncTask<Void, Void, Void>() {
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
    private static final String METHOD_ID_TRANSFER = "91f34dbd";
    private static final String TX_PREFIX = "5452";
    private static final String NS_PREFIX = "4E53";
    private byte[] getTransferDataHash(int teamId, int opNum, String[] addresses, long[] values){

        String a0 = TX_PREFIX; // Arraay (offset where the array data starts.
        String a1 = String.format("%064x", teamId);
        String a2 = String.format("%064x", opNum);
        int n = addresses.length;
        String[] a3 = new String[n];
        for (int i = 0; i < n; i++) {
            a3[i] = addresses[i].startsWith("0x") ? addresses[i].substring(2) : addresses[i];
        }
        String[] a4 = new String[n];
        for (int i = 0; i < n; i++) {
            a4[i] = String.format("%064x", values[i]);
        }

        byte[] data = Hex.toBytes(a0, a1, a2, a3, a4);
        return Sha3.getKeccak256Hash(data);
    }
    @Override
    public int onRunTask(TaskParams taskParams) {
        String tag = taskParams.getTag();
        if (tag != null) {
            switch (tag) {
                case SYNC_WALLET_TASK_TAG:
                    Log.v(LOG_TAG, "Sync wallet task ran");
                    try {
                        if (tryInit()) {
                            sync();
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "sync attempt failed:");
                        Log.e(LOG_TAG, "sync error message was: " + e.getMessage());
                        Log.e(LOG_TAG, "sync error call stack was: ", e);
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
                    createWallets(3_000_000);
                    break;
                case ACTION_VERIFY:
                    verifyIfWalletIsCreated(3_000_000);
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

    private boolean createWallets(long gasLimit) throws RemoteException, TeambrellaException, OperationApplicationException {
        String myPublicKey = mKey.getPublicKeyAsHex();
        List<Multisig> myUncreatedMultisigs;
        myUncreatedMultisigs = mTeambrellaClient.getMultisigsToCreate(myPublicKey);
        if (myUncreatedMultisigs.size() == 0) {
            return false;
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        long myNonce = getMyNonce();
        for (Multisig m : myUncreatedMultisigs) {
            Multisig sameTeammateMultisig = getMyTeamMultisigIfAny(myPublicKey, m.teammateId, myUncreatedMultisigs);
            if (sameTeammateMultisig != null) {

                // todo: move "cosigner list", and send to the server the move tx (not creation tx).
                ////boolean needServerUpdate = (sameTeammateMultisig.address != null);
                ////operations.add(mTeambrellaClient.setMutisigAddressTxAndNeedsServerUpdate(m, sameTeammateMultisig.address, sameTeammateMultisig.creationTx, needServerUpdate));

            } else {
                String txHex = createOneWallet(myNonce, m, gasLimit);
                if (txHex != null) {
                    // There could be 2 my pending mutisigs (Current and Next) for the same team. So we remember the first creation tx and don't create 2 contracts for the same team.
                    m.creationTx = txHex;
                    operations.add(mTeambrellaClient.setMutisigAddressTxAndNeedsServerUpdate(m, null, txHex, false));
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

    private String createOneWallet(long myNonce, Multisig m, long gasLimit) throws RemoteException {

        List<Cosigner> cosigners = m.cosigners;
        int n = cosigners.size();
        if (n <= 0) {
            Log.e(LOG_TAG, String.format("Multisig address id:%s has no cosigners", m.id));
            return null;
        }

        String[] cosignerAddresses = new String[n];
        for (int i = 0; i < n; i++) {
            String addr = cosigners.get(i).publicKeyAddress;
            cosignerAddresses[i] = addr;
            if (null == addr) {
                Log.e(LOG_TAG, String.format("Cosigner (teammate id: %s) for multisig id:%s has no publickKeyAddress", cosigners.get(i).teammateId, m.id));
                return null;
            }
        }

        org.ethereum.geth.Transaction cryptoTx;
        KeyStore ks = getEthKeyStore();
        Account myAccount = getEthAccount(ks);
        cryptoTx = createNewWalletTx(myNonce, gasLimit, m.teamId, cosignerAddresses);
        cryptoTx = sign(cryptoTx, ks, myAccount, BuildConfig.isTestNet);
        Log.v(LOG_TAG, toCreationInfoString(m) + " signed.");

        String txHex = publishCryptoTx(cryptoTx);
        return txHex;
    }

    private org.ethereum.geth.Transaction sign(org.ethereum.geth.Transaction cryptoTx, KeyStore ks, Account ethAcc, boolean isTestnet) throws RemoteException {

        try {
            BigInt chainId = new BigInt(isTestnet ? 3 : 1);                 // 3 is for Ropsten TestNet; 1 is for MainNet
            String secret = mKey.getPrivateKeyAsWiF(new MainNetParams());
            return ks.signTxPassphrase(ethAcc, secret, cryptoTx, chainId);
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
            throw new RemoteException(e.getMessage());
        }
    }


    private boolean verifyIfWalletIsCreated(long gasLimit) throws RemoteException, OperationApplicationException {

        EtherNode blockchain = new EtherNode(BuildConfig.isTestNet);

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        String myPublicKey = mKey.getPublicKeyAsHex();
        List<Multisig> creationTxes = mTeambrellaClient.getMultisigsInCreation(myPublicKey);
        for (Multisig m : creationTxes) {

            Scan<ScanResultTxReceipt> receipt = blockchain.checkTx(m.creationTx);
            if (receipt != null) {
                ScanResultTxReceipt res = receipt.result;
                boolean allGasIsUsed = false;
                if (res != null && res.blockNumber != null) {
                    long gasUsed = Long.parseLong(res.gasUsed.substring(2), 16);
                    allGasIsUsed = gasUsed == gasLimit;
                    if (!allGasIsUsed) {

                        operations.add(mTeambrellaClient.setMutisigAddressAndNeedsServerUpdate(m, res.contractAddress));

                    }
                }
                addErrorOperationIfAny(operations, m, receipt, allGasIsUsed);
            }

        }
        if (!operations.isEmpty()) {
            mClient.applyBatch(operations);
            return true;
        }
        return false;
    }

    private void addErrorOperationIfAny(List<ContentProviderOperation> operations, Multisig m, Scan<ScanResultTxReceipt> receipt, boolean allGasIsUsed) {
        if (allGasIsUsed) {
            Log.e(LOG_TAG, toCreationInfoString(m) + " did not create the contract and consumed all the gas. Holding the tx status for later investigation.");
            operations.add(mTeambrellaClient.setMutisigStatus(m, TeambrellaModel.USER_MULTISIG_STATUS_CREATION_FAILED));
        } else if (receipt.error != null) {
            Log.e(LOG_TAG, toCreationInfoString(m) + " denied. Resetting tx and mark for retry. Error was: " + receipt.error.toString());
            operations.add(mTeambrellaClient.setMutisigAddressTxAndNeedsServerUpdate(m, null, null, false));
        }
    }

    private boolean depositWallet() throws CryptoException, RemoteException {
        String myPublicKey = mKey.getPublicKeyAsHex();
        List<Multisig> myCurrentMultisigs = mTeambrellaClient.getCurrentMultisigsWithAddress(myPublicKey);
        if (myCurrentMultisigs.size() == 1) {
            EtherAccount myAcc = new EtherAccount(mKey, getApplicationContext());

            EtherNode blockchain = new EtherNode(BuildConfig.isTestNet);
            long gasWalletAmount = blockchain.checkBalance(myAcc.getDepositAddress());

            if (gasWalletAmount > 20_000_000_000_000_000L) {
                long minRestForGas = 10_000_000_000_000_000L;
                long myNonce = getMyNonce();
                org.ethereum.geth.Transaction depositTx;
                depositTx = myAcc.newDepositTx(myNonce, 50_000L, myCurrentMultisigs.get(0).address, BuildConfig.isTestNet, gasWalletAmount - minRestForGas);
                depositTx = myAcc.signTx(depositTx, BuildConfig.isTestNet);
                publishCryptoTx(depositTx);
            }
        }

        return true;
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
    private List<ContentProviderOperation> cosignTransaction(Tx tx, long userId) throws CryptoException {

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        ////btc:Transaction transaction = SignHelper.getTransaction(tx);
        ////if (transaction != null) {
        switch (tx.kind){
            case TeambrellaModel.TX_KIND_PAYOUT:
            case TeambrellaModel.TX_KIND_WITHDRAW:
                Multisig multisig = tx.getFromMultisig();
                if (multisig != null) {
                    ////btc: Script redeemScript = SignHelper.getRedeemScript(address, tx.cosigners);
                    EthWallet wallet = getWallet();

                    String from = multisig.address;
                    for (int i = 0; i < tx.txInputs.size(); i++) {
                        ////btc:byte[] signature = cosign(redeemScript, transaction, i);
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

    private EthWallet getWallet() throws CryptoException{
        String myPublicKey = mKey.getPublicKeyAsHex();
        String keyStorePath = getApplicationContext().getFilesDir().getPath() + "/keystore/" + myPublicKey;
        String keyStoreSecret = mKey.getPrivateKeyAsWiF(new MainNetParams());
        byte[] privateKey = mKey.getPrivKeyBytes();

        return new EthWallet(privateKey, keyStorePath, keyStoreSecret, BuildConfig.isTestNet);
    }

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


    private boolean masterSign() throws RemoteException, OperationApplicationException, CryptoException {
////btc:
//        List<Tx> list = mTeambrellaClient.getApprovedAndCosignedTxs();
//        Teammate user = mTeambrellaClient.getTeammate(mKey.getPublicKeyAsHex());
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//        if (list != null) {
//            for (Tx tx : list) {
//                operations.addAll(cosignTransaction(tx, user.id));
//                operations.add(TeambrellaContentProviderClient.setTxSigned(tx));
//            }
//        }
//        mClient.applyBatch(operations);
//        return !operations.isEmpty();
        return false;
    }


    //// btc:
//    private byte[] cosign(Script redeemScript, Transaction transaction, int inputNum) {
//        Sha256Hash hash = transaction.hashForSignature(inputNum, redeemScript, Transaction.SigHash.ALL, false);
//        return mKey.sign(hash).encodeToDER();
//    }

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


    private boolean publishApprovedAndCosignedTxs() throws RemoteException, OperationApplicationException, CryptoException {
        ////btc:
//        BlockchainNode blockchain = new BlockchainNode(true);
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//        List<Tx> txs = mTeambrellaClient.getApprovedAndCosignedTxs();
//        for (Tx tx : txs) {
//            Transaction transaction = SignHelper.getTransactionToPublish(tx);
//            if (transaction != null && blockchain.checkTransaction(transaction.getHashAsString()) || blockchain.pushTransaction(org.spongycastle.util.encoders.Hex.toHexString(transaction.bitcoinSerialize()))) {
//                operations.add(TeambrellaContentProviderClient.setTxPublished(tx));
//                mClient.applyBatch(operations);
//            }
//        }
//
//        return !operations.isEmpty();
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        List<Tx> txs = mTeambrellaClient.getApprovedAndCosignedTxs();
        for (Tx tx : txs) {

            EtherNode blockchain = new EtherNode(BuildConfig.isTestNet);
            EthWallet wallet = getWallet();
            switch (tx.kind){
                case TeambrellaModel.TX_KIND_PAYOUT:
                case TeambrellaModel.TX_KIND_WITHDRAW:
                    String cryptoTxHash = wallet.publish(tx);
                    if (cryptoTxHash != null){
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
            createWallets(3_000_000);
            verifyIfWalletIsCreated(3_000_000);
            depositWallet();
            autoApproveTxs();
            cosignApprovedTransactions();
            masterSign();
            publishApprovedAndCosignedTxs();

            hasNews = update();
        }
    }

    private KeyStore getEthKeyStore() throws RemoteException {
        String myPublicKey = mKey.getPublicKeyAsHex();
        String documentsPath = getApplicationContext().getFilesDir().getPath();     //!!!
        KeyStore ks = new KeyStore(documentsPath + "/keystore/" + myPublicKey, Geth.LightScryptN, Geth.LightScryptP);

        return ks;
    }

    private Account getEthAccount(KeyStore ks) throws RemoteException {
        try {
            String secret = mKey.getPrivateKeyAsWiF(new MainNetParams());

            Accounts aaa = ks.getAccounts();
            Account acc;
            if (aaa.size() > 0)
                acc = aaa.get(0);
            else
                //com.teambrella.android W/System.err: go.Universe$proxyerror: invalid length, need 256 bits
                acc = ks.importECDSAKey(mKey.getPrivKeyBytes(), secret);

            return acc;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Was unnable to read account.", e);
            throw new RemoteException(e.getMessage());
        }
    }

    private org.ethereum.geth.Transaction createNewWalletTx(long nonce, long gasLimit, long teamId, String[] addresses) throws RemoteException {
        String data = createNewWalletData(teamId, addresses);
        long gasPrice = BuildConfig.isTestNet ? 150_000_000_000L : 1_000_000_000L;  // 150 Gwei for TestNet and 0.5 Gwei for MainNet (1 Gwei = 10^9 wei)

        String json = String.format("{\"nonce\":\"0x%x\",\"gasPrice\":\"0x%x\",\"gas\":\"0x%x\",\"value\":\"0x0\",\"input\":\"%s\",\"v\":\"0x29\",\"r\":\"0x29\",\"s\":\"0x29\"}",
                nonce,
                gasPrice,
                gasLimit,
                data
        );
        Log.v(LOG_TAG, "Constructing " + toCreationInfoString(teamId, null) + " " + json);

        try {
            org.ethereum.geth.Transaction tx = Geth.newTransactionFromJSON(json);
            Log.v(LOG_TAG, toCreationInfoString(teamId, null) + " constructed.");
            return tx;
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
            throw new RemoteException(e.getMessage());
        }
    }

    private static BigInt fromME(int me) {
        BigDecimal wei = BigDecimal.valueOf(me, -15);  // 1 milli-eth has 10^15 wei.
        //TODO: max is 9.223372 eth
        return new BigInt(wei.longValue());
    }

    private static BigInt fromGwei(int gwei) {
        BigDecimal wei = BigDecimal.valueOf(gwei, -9);  // 1 Gwei has 10^9 wei.
        //TODO: max is 9.223372 eth
        return new BigInt(wei.longValue());
    }

    private String createNewWalletData(long teamId, String[] addresses) {
        int n = addresses.length;
        String contractV002 = "6060604052604051610ecd380380610ecd8339810160405280805182019190602001805191506003905082805161003a929160200190610064565b50600190815560028054600160a060020a03191633600160a060020a0316179055600055506100f2565b8280548282559060005260206000209081019282156100bb579160200282015b828111156100bb5782518254600160a060020a031916600160a060020a039190911617825560209290920191600190910190610084565b506100c79291506100cb565b5090565b6100ef91905b808211156100c7578054600160a060020a03191681556001016100d1565b90565b610dcc806101016000396000f300606060405236156100965763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416630b7b3eb7811461009857806322c5ec0f146100ca5780633bf2b4cd146100e05780638d475461146100f357806391f34dbd14610118578063a0175b961461016e578063d41097e3146101b7578063deff41c1146101d6578063df98ba00146101e9575b005b34156100a357600080fd5b6100ae6004356101fc565b604051600160a060020a03909116815260200160405180910390f35b34156100d557600080fd5b6100ae600435610224565b34156100eb57600080fd5b610096610232565b34156100fe57600080fd5b610106610312565b60405190815260200160405180910390f35b341561012357600080fd5b61009660048035906024803580820192908101359160443580820192908101359160649160c43580830192908201359160e43580830192908201359161010435918201910135610318565b341561017957600080fd5b61009660048035906024803580820192908101359160449160a43580830192908201359160c43580830192908201359160e435918201910135610628565b34156101c257600080fd5b610096600160a060020a036004351661081d565b34156101e157600080fd5b6100ae6108c0565b34156101f457600080fd5b6101066108cf565b600480548290811061020a57fe5b600091825260209091200154600160a060020a0316905081565b600380548290811061020a57fe5b60005b6004548110156102805733600160a060020a031660048281548110151561025857fe5b600091825260209091200154600160a060020a031614156102785761030f565b600101610235565b5060005b60035481101561030f5733600160a060020a03166003828154811015156102a757fe5b600091825260209091200154600160a060020a031614156103075760048054600181016102d48382610cac565b506000918252602090912001805473ffffffffffffffffffffffffffffffffffffffff191633600160a060020a03161790555b600101610284565b50565b60015481565b60025460009033600160a060020a0390811691161461033657600080fd5b6000548d9081101561034757600080fd5b30600160a060020a0316316103888c8c80806020026020016040519081016040528093929190818152602001838360200280828437506108d5945050505050565b111561039357600080fd5b6001548e6103cd8f8f808060200260200160405190810160405280939291908181526020018383602002808284375061090a945050505050565b6104038e8e8080602002602001604051908101604052809392919081815260200183836020028082843750610a05945050505050565b60405180807f545200000000000000000000000000000000000000000000000000000000000081525060020185815260200184815260200183805190602001908083835b602083106104665780518252601f199092019160209182019101610447565b6001836020036101000a038019825116818451161790925250505091909101905082805190602001908083835b602083106104b25780518252601f199092019160209182019101610493565b6001836020036101000a038019825116818451161790925250505091909101955060409450505050505190819003902091506105a1828a600360606040519081016040529190828260608082843782019150505050508a8a8080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505089898080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505088888080601f016020809104026020016040519081016040528181529291906020840183838082843750610af0945050505050565b15156105ac57600080fd5b60018e016000556106188d8d8060208082020160405190810160405280939291908181526020018383602002808284378201915050505050508c8c8080602002602001604051908101604052809392919081815260200183836020028082843750610b80945050505050565b5050505050505050505050505050565b60025460009033600160a060020a0390811691161461064657600080fd5b6000548b9081101561065757600080fd5b6001548c6106918d8d808060200260200160405190810160405280939291908181526020018383602002808284375061090a945050505050565b60405180807f4e5300000000000000000000000000000000000000000000000000000000000081525060020184815260200183815260200182805190602001908083835b602083106106f45780518252601f1990920191602091820191016106d5565b6001836020036101000a0380198251168184511617909252505050919091019450604093505050505190819003902091506107e2828a600360606040519081016040529190828260608082843782019150505050508a8a8080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505089898080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505088888080601f016020809104026020016040519081016040528181529291906020840183838082843750610af0945050505050565b15156107ed57600080fd5b60018c016000908155610801600482610cac565b5061080e60038c8c610cd0565b50505050505050505050505050565b600254600090819033600160a060020a0390811691161461083d57600080fd5b5050600354600454600682111561085b576002811161085b57600080fd5b6003821115610871576001811161087157600080fd5b6000811161087e57600080fd5b82600160a060020a03166108fc30600160a060020a0316319081150290604051600060405180830381858888f1935050505015156108bb57600080fd5b505050565b600254600160a060020a031681565b60005481565b6000805b8251811015610904578281815181106108ee57fe5b90602001906020020151909101906001016108d9565b50919050565b610912610d40565b60008083516014026040518059106109275750595b90808252806020026020018201604052509250600091505b83518210156109fe575060005b60148110156109f3578060130360080260020a84838151811061096b57fe5b90602001906020020151600160a060020a031681151561098757fe5b047f01000000000000000000000000000000000000000000000000000000000000000283828460140201815181106109bb57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a90535060010161094c565b60019091019061093f565b5050919050565b610a0d610d40565b6000808351602002604051805910610a225750595b90808252806020026020018201604052509250600091505b83518210156109fe575060005b6020811015610ae55780601f0360080260020a848381518110610a6657fe5b90602001906020020151811515610a7957fe5b047f0100000000000000000000000000000000000000000000000000000000000000028382846020020181518110610aad57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a905350600101610a47565b600190910190610a3a565b600380546000918290610b2d90899088908a855b602002015181548110610b1357fe5b600091825260209091200154600160a060020a0316610bf1565b90506003821115610b5257808015610b4f5750610b4f888660038a6001610b04565b90505b6006821115610b7557808015610b725750610b72888560038a6002610b04565b90505b979650505050505050565b60005b81518110156108bb57828181518110610b9857fe5b90602001906020020151600160a060020a03166108fc838381518110610bba57fe5b906020019060200201519081150290604051600060405180830381858888f193505050501515610be957600080fd5b600101610b83565b6000806000610c008686610c32565b90925090506001821515148015610c28575083600160a060020a031681600160a060020a0316145b9695505050505050565b60008060008060006020860151925060408601519150606086015160001a9050610c5e87828585610c6c565b945094505050509250929050565b60008060008060405188815287602082015286604082015285606082015260208160808360006001610bb8f1925080519299929850919650505050505050565b8154818355818115116108bb576000838152602090206108bb918101908301610d52565b828054828255906000526020600020908101928215610d30579160200282015b82811115610d3057815473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a03843516178255602090920191600190910190610cf0565b50610d3c929150610d6f565b5090565b60206040519081016040526000815290565b610d6c91905b80821115610d3c5760008155600101610d58565b90565b610d6c91905b80821115610d3c57805473ffffffffffffffffffffffffffffffffffffffff19168155600101610d755600a165627a7a7230582022e6d8a992945b19566381b295f214afecfe2a94d99a7c72506490dba86306200029";
        String a0 = "0000000000000000000000000000000000000000000000000000000000000040"; // Arraay (offset where the array data starts.
        String a1 = String.format("%064x", teamId);
        String a2 = String.format("%064x", n);

        StringBuilder hexString = new StringBuilder("0x").append(contractV002).append(a0).append(a1).append(a2);
        for (int i = 0; i < n; i++) {
            hexString.append("000000000000000000000000").append(addresses[i].substring(2)); // "0xABC..." to "000000000000000000000000000ABC..."
        }

        return hexString.toString();
    }

    private static byte[] toByteArray(String hexString) {
        int len = hexString.length();
        byte[] res = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            res[i / 2] = (byte) (
                    Character.digit(hexString.charAt(i), 16) << 4 +
                            Character.digit(hexString.charAt(i + 1), 16)
            );
        }
        return res;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int b = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[b >>> 4];
            hexChars[j * 2 + 1] = hexArray[b & 0x0F];
        }
        return new String(hexChars);
    }

    private long getMyNonce() throws RemoteException {

        KeyStore ks = getEthKeyStore();
        Account myAccount = getEthAccount(ks);
        String myHex = myAccount.getAddress().getHex();

        long myNonce = getNonce(myHex);
        return myNonce;
    }

    private long getNonce(String addressHex) {
        EtherNode blockchain = new EtherNode(BuildConfig.isTestNet);
        return blockchain.checkNonce(addressHex);
    }

    private String publishCryptoTx(org.ethereum.geth.Transaction cryptoTx) throws RemoteException {
        try {
            byte[] rlp = cryptoTx.encodeRLP();
            Log.v(LOG_TAG, "Publishing 'Multisig creation' tx:" + cryptoTx.getHash().getHex() + " " + cryptoTx.encodeJSON());
            String hex = "0x" + toHexString(rlp);

            EtherNode blockchain = new EtherNode(BuildConfig.isTestNet);
            return blockchain.pushTx(hex);

        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
            throw new RemoteException(e.getMessage());
        }
    }

    private static String toCreationInfoString(Multisig m) {
        if (null == m) return "null";

        return toCreationInfoString(m.teamId, m.creationTx);
    }

    private static String toCreationInfoString(long teamId, String creationTx) {
        return String.format("'Multisig creation(teamId=%s)' tx:%s", teamId, creationTx);
    }
}

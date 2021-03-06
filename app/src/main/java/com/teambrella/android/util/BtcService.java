//package com.teambrella.android.util;
//
//import android.content.ContentProviderClient;
//import android.content.ContentProviderOperation;
//import android.content.Intent;
//import android.content.OperationApplicationException;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.RemoteException;
//import com.teambrella.android.util.log.Log;
//
//import com.google.android.gms.gcm.GcmNetworkManager;
//import com.google.android.gms.gcm.GcmTaskService;
//import com.google.android.gms.gcm.TaskParams;
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import com.teambrella.android.BuildConfig;
//import com.teambrella.android.api.TeambrellaException;
//import com.teambrella.android.api.TeambrellaModel;
//import com.teambrella.android.api.server.TeambrellaServer;
//import com.teambrella.android.api.server.TeambrellaUris;
//import com.teambrella.android.blockchain.BlockchainNode;
//import com.teambrella.android.blockchain.CryptoException;
//import com.teambrella.android.blockchain.EtherNode;
//import com.teambrella.android.blockchain.Scan;
//import com.teambrella.android.blockchain.ScanResultTxReceipt;
//import com.teambrella.android.content.TeambrellaContentProviderClient;
//import com.teambrella.android.content.TeambrellaRepository;
//import com.teambrella.android.content.model.Cosigner;
//import com.teambrella.android.content.model.Multisig;
//import com.teambrella.android.content.model.ServerUpdates;
//import com.teambrella.android.content.model.Teammate;
//import com.teambrella.android.content.model.Tx;
//import com.teambrella.android.ui.TeambrellaUser;
//
//import org.bitcoinj.core.DumpedPrivateKey;
//import org.bitcoinj.core.ECKey;
//import org.bitcoinj.core.Sha256Hash;
//import org.bitcoinj.core.Transaction;
//import org.bitcoinj.params.MainNetParams;
//import org.bitcoinj.script.Script;
//import org.ethereum.geth.Account;
//import org.ethereum.geth.Accounts;
//import org.ethereum.geth.BigInt;
//import org.ethereum.geth.Geth;
//import org.ethereum.geth.KeyStore;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//
///**
// * Teambrella util service
// */
//public class BtcService extends GcmTaskService {
//
//    private static final String LOG_TAG = BtcService.class.getSimpleName();
//    private static final String EXTRA_URI = "uri";
//
//
//    private final static String ACTION_UPDATE = "update";
//    private final static String ACTION_CREATE = "create";
//    private final static String ACTION_VERIFY = "verify";
//    private final static String ACTION_DEPOSIT = "deposit";
//    private final static String ACTION_APPROVE = "approve";
//    private final static String ACTION_COSING = "cosign";
//    private final static String ACTION_PUBLISH = "publish";
//    private final static String ACTION_MASTER_SIGNATURE = "master_signature";
//    private final static String SHOW = "show";
//    private final static String ACTION_SYNC = "sync";
//
//    private TeambrellaServer mServer;
//    private ContentProviderClient mClient;
//    private TeambrellaContentProviderClient mTeambrellaClient;
//
//    /**
//     * Key
//     */
//    private ECKey mKey;
//
//
//    @Override
//    public void onCreate() {
//        Log.v(LOG_TAG, "Periodic task created");
//        super.onCreate();
//
//        tryInit();
//    }
//
//    private boolean tryInit() {
//        if (mKey != null) return true;
//
//        String privateKey = TeambrellaUser.get(this).getPrivateKey();
//        if (privateKey != null) {
//            mKey = DumpedPrivateKey.fromBase58(null, privateKey).getKey();
//            mServer = new TeambrellaServer(this, privateKey);
//            mClient = getContentResolver().acquireContentProviderClient(TeambrellaRepository.AUTHORITY);
//            mTeambrellaClient = new TeambrellaContentProviderClient(mClient);
//            return true;
//        } else {
//            Log.w(LOG_TAG, "No crypto key has been generated for this user yet. Skipping the sync task till her Facebook login.");
//            return false;
//        }
//    }
//
//
//    @Override
//    public int onStartCommand(Intent intent, int i, int i1) {
//        Log.v(LOG_TAG, "Periodic task started a command" + intent.toString());
//
////        if(BuildConfig.DEBUG){
////            new AsyncTask<Void, Void, Void>() {
////                @Override
////                protected Void doInBackground(Void... voids) {
////                    try {
////                        processIntent(intent);
////
////                    } catch (RemoteException | OperationApplicationException | TeambrellaException | CryptoException e) {
////                        Log.e(LOG_TAG, e.toString());
////                    }
////                    return null;
////                }
////            }.execute();
////
////            Log.e(LOG_TAG, "INTENT STARTED" + intent.toString());
////            return START_NOT_STICKY;
////        }else
//
//        return super.onStartCommand(intent, i, i1);
//    }
//
//    @Override
//    public int onRunTask(TaskParams taskParams) {
//        Log.v(LOG_TAG, "Periodic task ran");
//        try {
//            if (tryInit()) {
//                sync();
//            }
//        } catch (Exception e) {
//            Log.e(LOG_TAG, "sync attempt failed:");
//            Log.e(LOG_TAG, "sync error message was: " + e.getMessage());
//            Log.e(LOG_TAG, "sync error call stack was: ", e);
//        }
//        return GcmNetworkManager.RESULT_SUCCESS;
//    }
//
//    private void processIntent(Intent intent) throws CryptoException, RemoteException, OperationApplicationException, TeambrellaException {
//        String action = intent != null ? intent.getAction() : null;
//        if (action != null) {
//            switch (action) {
//                case ACTION_UPDATE:
//                    update();
//                    break;
//                case ACTION_CREATE:
//                    createWallets(3_000_000);
//                    break;
//                case ACTION_VERIFY:
//                    verifyIfWalletIsCreated(3_000_000);
//                    break;
//                case ACTION_DEPOSIT:
//                    depositWallet();
//                    break;
//                case ACTION_APPROVE:
//                    autoApproveTxs();
//                    break;
//                case ACTION_COSING:
//                    cosignApprovedTransactions();
//                    break;
//                case SHOW:
//                    show(Uri.parse(intent.getStringExtra(EXTRA_URI)));
//                    break;
//                case ACTION_PUBLISH:
//                    publishApprovedAndCosignedTxs();
//                    break;
//                case ACTION_MASTER_SIGNATURE:
//                    masterSign();
//                    break;
//                case ACTION_SYNC:
//                    sync();
//                    break;
//                default:
//                    Log.e(LOG_TAG, "unknown action " + action);
//            }
//        } else {
//            Log.e(LOG_TAG, "action is null");
//        }
//    }
//
//    private boolean update() throws RemoteException, OperationApplicationException, TeambrellaException {
//        boolean result = false;
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//
//        mTeambrellaClient.updateConnectionTime(new Date());
//
//        JsonObject response = mServer.requestObservable(TeambrellaUris.getUpdatesUri(), mTeambrellaClient.getClientUpdates())
//                .blockingFirst();
//
//        if (response != null) {
//            JsonObject status = response.get(TeambrellaModel.ATTR_STATUS).getAsJsonObject();
//            long timestamp = status.get(TeambrellaModel.ATTR_STATUS_TIMESTAMP).getAsLong();
//
//            JsonObject data = response.get(TeambrellaModel.ATTR_DATA).getAsJsonObject();
//
//            ServerUpdates serverUpdates = new Gson().fromJson(data, ServerUpdates.class);
//
//            operations.addAll(mTeambrellaClient.applyUpdates(serverUpdates));
//
//
//            result = !operations.isEmpty();
//
//            operations.addAll(TeambrellaContentProviderClient.clearNeedUpdateServerFlag());
//            mClient.applyBatch(operations);
//            operations.clear();
//
//            if (serverUpdates.txs != null) {
//                operations.addAll(mTeambrellaClient.checkArrivingTx(serverUpdates.txs));
//            }
//
//            if (!operations.isEmpty()) {
//                mClient.applyBatch(operations);
//            }
//
//            mTeambrellaClient.setLastUpdatedTimestamp(timestamp);
//
//        }
//        return result;
//    }
//
//    private boolean createWallets(long gasLimit) throws RemoteException, TeambrellaException, OperationApplicationException {
//        String myPublicKey = mKey.getPublicKeyAsHex();
//        List<Multisig> myUncreatedMultisigs;
//        myUncreatedMultisigs = mTeambrellaClient.getMultisigsToCreate(myPublicKey);
//        if (myUncreatedMultisigs.size() == 0) {
//            return false;
//        }
//
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//        long myNonce = getMyNonce();
//        for (Multisig m : myUncreatedMultisigs) {
//            Multisig sameTeammateMultisig = getMyTeamMultisigIfAny(myPublicKey, m.teammateId, myUncreatedMultisigs);
//            if (sameTeammateMultisig != null) {
//
//                // todo: move "cosigner list", and send to the server the move tx (not creation tx).
//                ////boolean needServerUpdate = (sameTeammateMultisig.address != null);
//                ////operations.add(mTeambrellaClient.setMutisigAddressTxAndNeedsServerUpdate(m, sameTeammateMultisig.address, sameTeammateMultisig.creationTx, needServerUpdate));
//
//            } else {
//                String txHex = createOneWallet(myNonce, m, gasLimit);
//                if (txHex != null) {
//                    // There could be 2 my pending mutisigs (Current and Next) for the same team. So we remember the first creation tx and don't create 2 contracts for the same team.
//                    m.creationTx = txHex;
//                    operations.add(mTeambrellaClient.setMutisigAddressTxAndNeedsServerUpdate(m, null, txHex, false));
//                    myNonce++;
//                }
//            }
//        }
//
//        if (!operations.isEmpty()) {
//            // Since now the local db will remember all the created contracts.
//            mClient.applyBatch(operations);
//            return true;
//        }
//        return false;
//    }
//
//    private Multisig getMyTeamMultisigIfAny(String myPublicKey, long myTeammateId, List<Multisig> myUncreatedMultisigs) throws RemoteException {
//        // myUncreatedMultisigs remembers (not commited to local db) created addresses. So we don't create 2 contracts for the same team:
//        for (Multisig m : myUncreatedMultisigs) {
//            if (m.teammateId == myTeammateId && m.creationTx != null) {
//                return m;
//            }
//        }
//
//        List<Multisig> sameTeamMultisigs = mTeambrellaClient.getMultisigsWithAddressByTeammate(myPublicKey, myTeammateId);
//        if (sameTeamMultisigs.size() > 0) {
//            return sameTeamMultisigs.get(0);
//        }
//
//        return null;
//    }
//
//    private String createOneWallet(long myNonce, Multisig m, long gasLimit) throws RemoteException {
//
//        List<Cosigner> cosigners = m.cosigners;
//        int n = cosigners.size();
//        if (n <= 0) {
//            Log.e(LOG_TAG, String.format("Multisig address id:%s has no cosigners", m.id));
//            return null;
//        }
//
//        String[] cosignerAddresses = new String[n];
//        for (int i = 0; i < n; i++) {
//            String addr = cosigners.get(i).publicKeyAddress;
//            cosignerAddresses[i] = addr;
//            if (null == addr) {
//                Log.e(LOG_TAG, String.format("Cosigner (teammate id: %s) for multisig id:%s has no publickKeyAddress", cosigners.get(i).teammateId, m.id));
//                return null;
//            }
//        }
//
//        org.ethereum.geth.Transaction cryptoTx;
//        KeyStore ks = getEthKeyStore();
//        Account myAccount = getEthAccount(ks);
//        cryptoTx = createNewWalletTx(myNonce, gasLimit, m.teamId, cosignerAddresses);
//        cryptoTx = sign(cryptoTx, ks, myAccount, BuildConfig.isTestNet);
//        Log.v(LOG_TAG, toCreationInfoString(m) + " signed.");
//
//        String txHex = publishCryptoTx(cryptoTx);
//        return txHex;
//    }
//
//    private org.ethereum.geth.Transaction sign(org.ethereum.geth.Transaction cryptoTx, KeyStore ks, Account ethAcc, boolean isTestnet) throws RemoteException {
//
//        try {
//            BigInt chainId = new BigInt(isTestnet ? 3 : 1);                 // 3 is for Ropsten TestNet; 1 is for MainNet
//            String secret = mKey.getPrivateKeyAsWiF(new MainNetParams());
//            return ks.signTxPassphrase(ethAcc, secret, cryptoTx, chainId);
//        } catch (Exception e) {
//            Log.e(LOG_TAG, "", e);
//            throw new RemoteException(e.getMessage());
//        }
//    }
//
//
//    private boolean verifyIfWalletIsCreated(long gasLimit) throws RemoteException, OperationApplicationException {
//
//        EtherNode blockchain = new EtherNode(BuildConfig.isTestNet);
//
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//        String myPublicKey = mKey.getPublicKeyAsHex();
//        List<Multisig> creationTxes = mTeambrellaClient.getMultisigsInCreation(myPublicKey);
//        for (Multisig m : creationTxes) {
//
//            Scan<ScanResultTxReceipt> receipt = blockchain.checkTx(m.creationTx);
//            if (receipt != null) {
//                ScanResultTxReceipt res = receipt.result;
//                boolean allGasIsUsed = false;
//                if (res != null && res.blockNumber != null) {
//                    long gasUsed = Long.parseLong(res.gasUsed.substring(2), 16);
//                    allGasIsUsed = gasUsed == gasLimit;
//                    if (!allGasIsUsed) {
//
//                        operations.add(mTeambrellaClient.setMutisigAddressAndNeedsServerUpdate(m, res.contractAddress));
//
//                    }
//                }
//                addErrorOperationIfAny(operations, m, receipt, allGasIsUsed);
//            }
//
//        }
//        if (!operations.isEmpty()) {
//            mClient.applyBatch(operations);
//            return true;
//        }
//        return false;
//    }
//
//    private void addErrorOperationIfAny(List<ContentProviderOperation> operations, Multisig m, Scan<ScanResultTxReceipt> receipt, boolean allGasIsUsed) {
//        if (allGasIsUsed) {
//            Log.e(LOG_TAG, toCreationInfoString(m) + " did not create the contract and consumed all the gas. Holding the tx status for later investigation.");
//            operations.add(mTeambrellaClient.setMutisigStatus(m, TeambrellaModel.USER_MULTISIG_STATUS_CREATION_FAILED));
//        } else if (receipt.error != null) {
//            Log.e(LOG_TAG, toCreationInfoString(m) + " denied. Resetting tx and mark for retry. Error was: " + receipt.error.toString());
//            operations.add(mTeambrellaClient.setMutisigAddressTxAndNeedsServerUpdate(m, null, null, false));
//        }
//    }
//
//    private boolean depositWallet() throws CryptoException, RemoteException {
////        String myPublicKey = mKey.getPublicKeyAsHex();
////        List<Multisig> myCurrentMultisigs = mTeambrellaClient.getCurrentMultisigsWithAddress(myPublicKey);
////        if (myCurrentMultisigs.size() == 1) {
////            EtherAccount myAcc = new EtherAccount(mKey, getApplicationContext());
////
////            EtherNode blockchain = new EtherNode(BuildConfig.isTestNet);
////            long gasWalletAmount = 0//blockchain.checkBalance(myAcc.getDepositAddress());
////
////            if (gasWalletAmount > 20_000_000_000_000_000L) {
////                long minRestForGas = 10_000_000_000_000_000L;
////                long myNonce = getMyNonce();
////                org.ethereum.geth.Transaction depositTx;
////                ..depositTx = myAcc.newDepositTx(myNonce, 50_000L, myCurrentMultisigs.get(0).address, BuildConfig.isTestNet, gasWalletAmount - minRestForGas);
////                depositTx = myAcc.signTx(depositTx, BuildConfig.isTestNet);
////                publishCryptoTx(depositTx);
////            }
////        }
//
//        return true;
//    }
//
//    private boolean autoApproveTxs() throws RemoteException, OperationApplicationException {
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//        operations.addAll(mTeambrellaClient.autoApproveTxs());
//        if (!operations.isEmpty()) {
//            mClient.applyBatch(operations);
//        }
//        return !operations.isEmpty();
//    }
//
//
//// TODO: move to btc:
////    /**
////     * Cosign transaction
////     *
////     * @param tx transaction
////     * @return list of operations to apply
////     */
////    private List<ContentProviderOperation> cosignTransaction(Tx tx, long userId) {
////        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
////        Transaction transaction = SignHelper.getTransaction(tx);
////        if (transaction != null) {
////            Multisig address = tx.getFromMultisig();
////            if (address != null) {
////                Script redeemScript = SignHelper.getRedeemScript(address, tx.cosigners);
////                for (int i = 0; i < tx.txInputs.size(); i++) {
////                    byte[] signature = cosign(redeemScript, transaction, i);
////                    operations.add(TeambrellaContentProviderClient.addSignature(tx.txInputs.get(i).id.toString(), userId, signature));
////                }
////            }
////        }
////        return operations;
////    }
//    /**
//     * Cosign transaction
//     *
//     * @param tx transaction
//     * @return list of operations to apply
//     */
//    private List<ContentProviderOperation> cosignTransaction(Tx tx, long userId) {
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//        Transaction transaction = SignHelper.getTransaction(tx);
//        if (transaction != null) {
//            Multisig address = tx.getFromMultisig();
//            if (address != null) {
//                Script redeemScript = SignHelper.getRedeemScript(address, tx.cosigners);
//                for (int i = 0; i < tx.txInputs.size(); i++) {
//                    byte[] signature = cosign(redeemScript, transaction, i);
//                    operations.add(TeambrellaContentProviderClient.addSignature(tx.txInputs.get(i).id.toString(), userId, signature));
//                }
//            }
//        }
//        return operations;
//    }
//
//    private boolean cosignApprovedTransactions() throws RemoteException, OperationApplicationException {
//        List<Tx> list = mTeambrellaClient.getCosinableTx();
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
//    }
//
//
//    private boolean masterSign() throws RemoteException, OperationApplicationException {
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
//    }
//
//
//    private byte[] cosign(Script redeemScript, Transaction transaction, int inputNum) {
//        Sha256Hash hash = transaction.hashForSignature(inputNum, redeemScript, Transaction.SigHash.ALL, false);
//        return mKey.sign(hash).encodeToDER();
//    }
//
//
//    private void show(Uri uri) throws RemoteException {
//        Cursor cursor = mClient.query(uri, null, null, null, null);
//
//        if (cursor != null && cursor.moveToFirst()) {
//            do {
//                Log.e(LOG_TAG, "***");
//                for (String name : cursor.getColumnNames()) {
//                    Log.d(LOG_TAG, name + ":" + cursor.getString(cursor.getColumnIndex(name)));
//                }
//
//            } while (cursor.moveToNext());
//        }
//
//        if (cursor != null) {
//            cursor.close();
//        }
//    }
//
//
//    private boolean publishApprovedAndCosignedTxs() throws RemoteException, OperationApplicationException {
//        BlockchainNode blockchain = new BlockchainNode(true);
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//        List<Tx> txs = mTeambrellaClient.getApprovedAndCosignedTxs();
//        for (Tx tx : txs) {
//            Transaction transaction = SignHelper.getTransactionToPublish(tx);
//            String txHash = org.spongycastle.util.encoders.Hex.toHexString(transaction.bitcoinSerialize());
//            if (transaction != null && blockchain.checkTransaction(transaction.getHashAsString()) || blockchain.pushTransaction(txHash)) {
//                operations.add(TeambrellaContentProviderClient.setTxPublished(tx, txHash));
//                mClient.applyBatch(operations);
//            }
//        }
//
//        return !operations.isEmpty();
//    }
//
//    private void sync() throws CryptoException, RemoteException, OperationApplicationException, TeambrellaException {
//        Log.v(LOG_TAG, "start syncing...");
//
//        boolean hasNews = true;
//        for (int attempt = 0; attempt < 3 && hasNews; attempt++) {
//            createWallets(3_000_000);
//            verifyIfWalletIsCreated(3_000_000);
//            depositWallet();
//            autoApproveTxs();
//            cosignApprovedTransactions();
//            masterSign();
//            publishApprovedAndCosignedTxs();
//
//            hasNews = update();
//        }
//    }
//
//    private KeyStore getEthKeyStore() throws RemoteException {
//        String myPublicKey = mKey.getPublicKeyAsHex();
//        String documentsPath = getApplicationContext().getFilesDir().getPath();     //!!!
//        KeyStore ks = new KeyStore(documentsPath + "/keystore/" + myPublicKey, Geth.LightScryptN, Geth.LightScryptP);
//
//        return ks;
//    }
//
//    private Account getEthAccount(KeyStore ks) throws RemoteException {
//        try {
//            String secret = mKey.getPrivateKeyAsWiF(new MainNetParams());
//
//            Accounts aaa = ks.getAccounts();
//            Account acc;
//            if (aaa.size() > 0)
//                acc = aaa.get(0);
//            else
//                //com.teambrella.android W/System.err: go.Universe$proxyerror: invalid length, need 256 bits
//                acc = ks.importECDSAKey(mKey.getPrivKeyBytes(), secret);
//
//            return acc;
//        } catch (Exception e) {
//            Log.e(LOG_TAG, "Was unnable to read account.", e);
//            throw new RemoteException(e.getMessage());
//        }
//    }
//
//    private org.ethereum.geth.Transaction createNewWalletTx(long nonce, long gasLimit, long teamId, String[] addresses) throws RemoteException {
//        String data = createNewWalletData(teamId, addresses);
//        long gasPrice = BuildConfig.isTestNet ? 50_000_000_000L : 1_000_000_000L;  // 50 Gwei for TestNet and 0.5 Gwei for MainNet (1 Gwei = 10^9 wei)
//
//        String json = String.format("{\"nonce\":\"0x%x\",\"gasPrice\":\"0x%x\",\"gas\":\"0x%x\",\"value\":\"0x0\",\"input\":\"%s\",\"v\":\"0x29\",\"r\":\"0x29\",\"s\":\"0x29\"}",
//                nonce,
//                gasPrice,
//                gasLimit,
//                data
//        );
//        Log.v(LOG_TAG, "Constructing " + toCreationInfoString(teamId, null) + " " + json);
//
//        try {
//            org.ethereum.geth.Transaction tx = Geth.newTransactionFromJSON(json);
//            Log.v(LOG_TAG, toCreationInfoString(teamId, null) + " constructed.");
//            return tx;
//        } catch (Exception e) {
//            Log.e(LOG_TAG, "", e);
//            throw new RemoteException(e.getMessage());
//        }
//    }
//
//    private static BigInt fromME(int me) {
//        BigDecimal wei = BigDecimal.valueOf(me, -15);  // 1 milli-eth has 10^15 wei.
//        //TODO: max is 9.223372 eth
//        return new BigInt(wei.longValue());
//    }
//
//    private static BigInt fromGwei(int gwei) {
//        BigDecimal wei = BigDecimal.valueOf(gwei, -9);  // 1 Gwei has 10^9 wei.
//        //TODO: max is 9.223372 eth
//        return new BigInt(wei.longValue());
//    }
//
//    private String createNewWalletData(long teamId, String[] addresses) {
//        int n = addresses.length;
//        String contractV001 = "60606040526040516114cc3803806114cc833981016040528080518201919060200180519060200190919050505b816003908051906020019061004392919061009c565b508060018190555033600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060016000819055505b5050610169565b828054828255906000526020600020908101928215610115579160200282015b828111156101145782518260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550916020019190600101906100bc565b5b5090506101229190610126565b5090565b61016691905b8082111561016257600081816101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690555060010161012c565b5090565b90565b611354806101786000396000f30060606040523615610097576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680630b7b3eb71461009b57806322c5ec0f146100fe5780633bf2b4cd146101615780638d4754611461017657806391f34dbd1461019f578063a0175b961461022d578063d41097e3146102a7578063deff41c1146102e0578063df98ba0014610335575b5b5b005b34156100a657600080fd5b6100bc600480803590602001909190505061035e565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561010957600080fd5b61011f600480803590602001909190505061039e565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561016c57600080fd5b6101746103de565b005b341561018157600080fd5b61018961056b565b6040518082815260200191505060405180910390f35b34156101aa57600080fd5b61022b60048080359060200190919080359060200190820180359060200191909192908035906020019082018035906020019190919290806060019091908035906020019082018035906020019190919290803590602001908201803590602001919091929080359060200190820180359060200191909192905050610571565b005b341561023857600080fd5b6102a560048080359060200190919080359060200190820180359060200191909192908060600190919080359060200190820180359060200191909192908035906020019082018035906020019190919290803590602001908201803590602001919091929050506108fa565b005b34156102b257600080fd5b6102de600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610b51565b005b34156102eb57600080fd5b6102f3610c5f565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561034057600080fd5b610348610c85565b6040518082815260200191505060405180910390f35b60048181548110151561036d57fe5b906000526020600020900160005b915054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6003818154811015156103ad57fe5b906000526020600020900160005b915054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60008090505b600480549050811015610473573373ffffffffffffffffffffffffffffffffffffffff1660048281548110151561041757fe5b906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16141561046557610568565b5b80806001019150506103e4565b600090505b600380549050811015610567573373ffffffffffffffffffffffffffffffffffffffff166003828154811015156104ab57fe5b906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161415610559576004805480600101828161050891906111b4565b916000526020600020900160005b33909190916101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505b5b8080600101915050610478565b5b50565b60015481565b6000600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156105cf57600080fd5b8c60005481101515156105e157600080fd5b3073ffffffffffffffffffffffffffffffffffffffff16316106318c8c80806020026020016040519081016040528093929190818152602001838360200280828437820191505050505050610c8b565b1115151561063e57600080fd5b6001548e61067a8f8f80806020026020016040519081016040528093929190818152602001838360200280828437820191505050505050610cd4565b6106b28e8e80806020026020016040519081016040528093929190818152602001838360200280828437820191505050505050610dec565b60405180807f545200000000000000000000000000000000000000000000000000000000000081525060020185815260200184815260200183805190602001908083835b60208310151561071c57805182525b6020820191506020810190506020830392506106f6565b6001836020036101000a03801982511681845116808217855250505050505090500182805190602001908083835b60208310151561077057805182525b60208201915060208101905060208303925061074a565b6001836020036101000a0380198251168184511680821785525050505050509050019450505050506040518091039020915061086a828a600380602002604051908101604052809291908260036020028082843782019150505050508a8a8080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505089898080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505088888080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050610eee565b151561087557600080fd5b60018e016000819055506108e78d8d808060200260200160405190810160405280939291908181526020018383602002808284378201915050505050508c8c80806020026020016040519081016040528093929190818152602001838360200280828437820191505050505050611041565b5b5b505b50505050505050505050505050565b6000600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561095857600080fd5b8a600054811015151561096a57600080fd5b6001548c6109a68d8d80806020026020016040519081016040528093929190818152602001838360200280828437820191505050505050610cd4565b60405180807f4e5300000000000000000000000000000000000000000000000000000000000081525060020184815260200183815260200182805190602001908083835b602083101515610a1057805182525b6020820191506020810190506020830392506109ea565b6001836020036101000a038019825116818451168082178552505050505050905001935050505060405180910390209150610b09828a600380602002604051908101604052809291908260036020028082843782019150505050508a8a8080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505089898080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505088888080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050610eee565b1515610b1457600080fd5b60018c016000819055506000600481610b2d91906111e0565b508a8a60039190610b3f92919061120c565b505b5b505b5050505050505050505050565b600080600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141515610bb057600080fd5b600380549050915060048054905090506006821115610bd957600281111515610bd857600080fd5b5b6003821115610bf257600181111515610bf157600080fd5b5b600081111515610c0157600080fd5b8273ffffffffffffffffffffffffffffffffffffffff166108fc3073ffffffffffffffffffffffffffffffffffffffff16319081150290604051600060405180830381858888f193505050501515610c5857600080fd5b5b5b505050565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60005481565b60008060009150600090505b8251811015610cca578281815181101515610cae57fe5b90602001906020020151820191505b8080600101915050610c97565b8191505b50919050565b610cdc6112ac565b6000808351601402604051805910610cf15750595b908082528060200260200182016040525b509250600091505b8351821015610de457600090505b6014811015610dd6578060130360080260020a8483815181101515610d3957fe5b9060200190602002015173ffffffffffffffffffffffffffffffffffffffff16811515610d6257fe5b047f01000000000000000000000000000000000000000000000000000000000000000283826014850201815181101515610d9857fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a9053505b8080600101915050610d18565b5b8180600101925050610d0a565b5b5050919050565b610df46112ac565b6000808351602002604051805910610e095750595b908082528060200260200182016040525b509250600091505b8351821015610ee657600090505b6020811015610ed85780601f0360080260020a8483815181101515610e5157fe5b90602001906020020151811515610e6457fe5b047f01000000000000000000000000000000000000000000000000000000000000000283826020850201815181101515610e9a57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a9053505b8080600101915050610e30565b5b8180600101925050610e22565b5b5050919050565b60008060006003805490509150610f54888760038a6000600381101515610f1157fe5b6020020151815481101515610f2257fe5b906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff166110d2565b90506003821115610fc457808015610fc15750610fc0888660038a6001600381101515610f7d57fe5b6020020151815481101515610f8e57fe5b906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff166110d2565b5b90505b60068211156110325780801561102f575061102e888560038a6002600381101515610feb57fe5b6020020151815481101515610ffc57fe5b906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff166110d2565b5b90505b8092505b505095945050505050565b60008090505b81518110156110cc57828181518110151561105e57fe5b9060200190602002015173ffffffffffffffffffffffffffffffffffffffff166108fc838381518110151561108f57fe5b906020019060200201519081150290604051600060405180830381858888f1935050505015156110be57600080fd5b5b8080600101915050611047565b5b505050565b60008060006110e18686611134565b80925081935050506001151582151514801561112857508373ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff16145b92505b50509392505050565b60008060008060006020860151925060408601519150606086015160001a90506111608782858561116f565b945094505b5050509250929050565b60008060008060405188815287602082015286604082015285606082015260208160808360006001610bb8f1925080519150508181935093505b505094509492505050565b8154818355818115116111db578183600052602060002091820191016111da91906112c0565b5b505050565b8154818355818115116112075781836000526020600020918201910161120691906112c0565b5b505050565b82805482825590600052602060002090810192821561129b579160200282015b8281111561129a57823573ffffffffffffffffffffffffffffffffffffffff168260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055509160200191906001019061122c565b5b5090506112a891906112e5565b5090565b602060405190810160405280600081525090565b6112e291905b808211156112de5760008160009055506001016112c6565b5090565b90565b61132591905b8082111561132157600081816101000a81549073ffffffffffffffffffffffffffffffffffffffff0219169055506001016112eb565b5090565b905600a165627a7a7230582013ea6a742eda913e7d9d417017fa9957501fd861a196e9d2f2e0bb4a51ec34b10029";
//        String a0 = "0000000000000000000000000000000000000000000000000000000000000040"; // Arraay (offset where the array data starts.
//        String a1 = String.format("%064x", teamId);
//        String a2 = String.format("%064x", n);
//
//        StringBuilder hexString = new StringBuilder("0x").append(contractV001).append(a0).append(a1).append(a2);
//        for (int i = 0; i < n; i++) {
//            hexString.append("000000000000000000000000").append(addresses[i].substring(2)); // "0xABC..." to "000000000000000000000000000ABC..."
//        }
//
//        return hexString.toString();
//    }
//
//    private static byte[] toByteArray(String hexString) {
//        int len = hexString.length();
//        byte[] res = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            res[i / 2] = (byte) (
//                    Character.digit(hexString.charAt(i), 16) << 4 +
//                            Character.digit(hexString.charAt(i + 1), 16)
//            );
//        }
//        return res;
//    }
//
//    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
//
//    public static String toHexString(byte[] bytes) {
//        char[] hexChars = new char[bytes.length * 2];
//        for (int j = 0; j < bytes.length; j++) {
//            int b = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[b >>> 4];
//            hexChars[j * 2 + 1] = hexArray[b & 0x0F];
//        }
//        return new String(hexChars);
//    }
//
//    private long getMyNonce() throws RemoteException {
//
//        KeyStore ks = getEthKeyStore();
//        Account myAccount = getEthAccount(ks);
//        String myHex = myAccount.getAddress().getHex();
//
//        long myNonce = getNonce(myHex);
//        return myNonce;
//    }
//
//    private long getNonce(String addressHex) {
//        EtherNode blockchain = new EtherNode(BuildConfig.isTestNet);
//        return blockchain.checkNonce(addressHex);
//    }
//
//    private String publishCryptoTx(org.ethereum.geth.Transaction cryptoTx) throws RemoteException {
//        try {
//            byte[] rlp = cryptoTx.encodeRLP();
//            Log.v(LOG_TAG, "Publishing 'Multisig creation' tx:" + cryptoTx.getHash().getHex() + " " + cryptoTx.encodeJSON());
//            String hex = "0x" + toHexString(rlp);
//
//            EtherNode blockchain = new EtherNode(BuildConfig.isTestNet);
//            return blockchain.pushTx(hex);
//
//        } catch (Exception e) {
//            Log.e(LOG_TAG, "", e);
//            throw new RemoteException(e.getMessage());
//        }
//    }
//
//    private static String toCreationInfoString(Multisig m) {
//        if (null == m) return "null";
//
//        return toCreationInfoString(m.teamId, m.creationTx);
//    }
//
//    private static String toCreationInfoString(long teamId, String creationTx) {
//        return String.format("'Multisig creation(teamId=%s)' tx:%s", teamId, creationTx);
//    }
//}

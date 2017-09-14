package com.teambrella.android.util;

import android.annotation.SuppressLint;
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
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.blockchain.BlockchainNode;
import com.teambrella.android.blockchain.EtherNode;
import com.teambrella.android.blockchain.Scan;
import com.teambrella.android.blockchain.TxReceiptResult;
import com.teambrella.android.content.TeambrellaContentProviderClient;
import com.teambrella.android.content.TeambrellaRepository;
import com.teambrella.android.content.model.Cosigner;
import com.teambrella.android.content.model.Multisig;
import com.teambrella.android.content.model.ServerUpdates;
import com.teambrella.android.content.model.Teammate;
import com.teambrella.android.content.model.Tx;
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
import org.ethereum.geth.Hash;
import org.ethereum.geth.KeyStore;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;


/**
 * Teambrella util service
 */
public class TeambrellaUtilService extends GcmTaskService {

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


//    public TeambrellaUtilService() {
//        super("Util Service");
//    }
////!!!
    public String ethSign(String msg)
    {
        try{
            String privateKey = TeambrellaUser.get(this).getPrivateKey();

            if (privateKey != null) {

                mServer = new TeambrellaServer(this, privateKey);
                mKey = DumpedPrivateKey.fromBase58(null, privateKey).getKey();
            } else {
                //throw new RuntimeException("Missing private key");
            }
            mServer = new TeambrellaServer(this, privateKey);
            mClient = getContentResolver().acquireContentProviderClient(TeambrellaRepository.AUTHORITY);
            mTeambrellaClient = new TeambrellaContentProviderClient(mClient);

            String privKey = "L4TzGwABRFtqGBtrbKxK1ZHEByi3GczUhztEx9dtPvXkuAzGKGdo";
            KeyStore ks = getEthKeyStore();
            Account acc = getEthAccount(ks);

            Log.e(LOG_TAG, "=============================");
            Log.e(LOG_TAG, "address: " + acc.getAddress().getHex());

            byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
            String hash = Geth.newHashFromBytes(bytes).getHex();
            Log.e(LOG_TAG, "hash: " + hash);

            bytes = Geth.newHashFromBytes(bytes).getBytes();
            byte[] sig = ks.signHash(acc.getAddress(), bytes);
            Log.e(LOG_TAG, "sig: " + toHexString(sig));

            return "0x" + toHexString(sig);

        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String privateKey = TeambrellaUser.get(this).getPrivateKey();

        if (privateKey != null) {

            mServer = new TeambrellaServer(this, privateKey);
            mKey = DumpedPrivateKey.fromBase58(null, privateKey).getKey();
        } else {
            //throw new RuntimeException("Missing private key");
        }
        mServer = new TeambrellaServer(this, privateKey);
        mClient = getContentResolver().acquireContentProviderClient(TeambrellaRepository.AUTHORITY);
        mTeambrellaClient = new TeambrellaContentProviderClient(mClient);
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i1) {
        //onHandleIntent(intent);
        return super.onStartCommand(intent, i, i1);
    }

    @SuppressLint("StaticFieldLeak")
    protected void onHandleIntent(@Nullable Intent intent) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    processIntent(intent);
                } catch (RemoteException | OperationApplicationException | TeambrellaException e) {
                    Log.e(LOG_TAG, e.toString());
                }
                return null;
            }
        }.execute();
    }


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

        mTeambrellaClient.updateConnectionTime(new Date().getTime());

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

            mTeambrellaClient.setLastUpdatedTime(timestamp);

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
        cryptoTx = sign(cryptoTx, ks, myAccount, 3);
        Log.v(LOG_TAG, toCreationInfoString(m) + " signed.");

        String txHex = publishCryptoTx(cryptoTx);
        return txHex;
    }

    private org.ethereum.geth.Transaction sign(org.ethereum.geth.Transaction cryptoTx, KeyStore ks, Account ethAcc, long chainId) throws RemoteException {

        try {
            String secret = mKey.getPrivateKeyAsWiF(new MainNetParams());
            return ks.signTxPassphrase(ethAcc, secret, cryptoTx, new BigInt(chainId));
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
            throw new RemoteException(e.getMessage());
        }
    }


    private boolean verifyIfWalletIsCreated(long gasLimit) throws RemoteException, OperationApplicationException {

        EtherNode blockchain = new EtherNode(true);

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        String myPublicKey = mKey.getPublicKeyAsHex();
        List<Multisig> creationTxes = mTeambrellaClient.getMultisigsInCreation(myPublicKey);
        for (Multisig m : creationTxes) {

            Scan<TxReceiptResult> receipt = blockchain.checkTx(m.creationTx);
            if (receipt != null) {
                TxReceiptResult res = receipt.result;
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

    private void addErrorOperationIfAny(List<ContentProviderOperation> operations, Multisig m, Scan<TxReceiptResult> receipt, boolean allGasIsUsed) {
        if (allGasIsUsed) {
            Log.e(LOG_TAG, toCreationInfoString(m) + " did not create the contract and consumed all the gas. Holding the tx status for later investigation.");
            operations.add(mTeambrellaClient.setMutisigStatus(m, TeambrellaModel.USER_MULTISIG_STATUS_CREATION_FAILED));
        } else if (receipt.error != null) {
            Log.e(LOG_TAG, toCreationInfoString(m) + " denied. Resetting tx and mark for retry. Error was: " + receipt.error.toString());
            operations.add(mTeambrellaClient.setMutisigAddressTxAndNeedsServerUpdate(m, null, null, false));
        }
    }

    private boolean depositWallet() throws TeambrellaException {
        boolean result = false;

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
            Multisig address = tx.getFromAddress();
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
        BlockchainNode blockchain = new BlockchainNode(true);
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        List<Tx> txs = mTeambrellaClient.getApprovedAndCosignedTxs();
        for (Tx tx : txs) {
            Transaction transaction = SignHelper.getTransactionToPublish(tx);
            if (transaction != null && blockchain.checkTransaction(transaction.getHashAsString()) || blockchain.pushTransaction(org.spongycastle.util.encoders.Hex.toHexString(transaction.bitcoinSerialize()))) {
                operations.add(TeambrellaContentProviderClient.setTxPublished(tx));
                mClient.applyBatch(operations);
            }
        }

        return !operations.isEmpty();
    }

    private void sync() throws RemoteException, OperationApplicationException, TeambrellaException {
        Log.v(LOG_TAG, "start syncing...");
        do {
            createWallets(3_000_000);
            verifyIfWalletIsCreated(3_000_000);
            autoApproveTxs();
            cosignApprovedTransactions();
            masterSign();
            publishApprovedAndCosignedTxs();
        } while (update());
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
        long gasPrice = 500_000_000L;  // 0.5 Gwei (1 Gwei = 10^9 wei)

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
        String contract = "60606040526040516111b03803806111b0833981016040528080518201919060200180519150505b600382805161003a929160200190610066565b50600181815560028054600160a060020a03191633600160a060020a03161790556000555b50506100f9565b8280548282559060005260206000209081019282156100bd579160200282015b828111156100bd5782518254600160a060020a031916600160a060020a039190911617825560209290920191600190910190610086565b5b506100ca9291506100ce565b5090565b6100f691905b808211156100ca578054600160a060020a03191681556001016100d4565b5090565b90565b6110a8806101086000396000f300606060405236156100ac5763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416630b7b3eb781146100b057806322c5ec0f146100e257806339cdde32146101145780633bf2b4cd1461018957806377d32e941461019e5780638d4754611461021757806391f34dbd1461023c578063a0175b9614610294578063d41097e3146102df578063deff41c114610300578063df98ba001461032f575b5b5b005b34156100bb57600080fd5b6100c6600435610354565b604051600160a060020a03909116815260200160405180910390f35b34156100ed57600080fd5b6100c6600435610386565b604051600160a060020a03909116815260200160405180910390f35b341561011f57600080fd5b610175600480359060446024803590810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284375094965050509235600160a060020a031692506103b8915050565b604051901515815260200160405180910390f35b341561019457600080fd5b6100ac6103fb565b005b34156101a957600080fd5b6101f4600480359060446024803590810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284375094965061050195505050505050565b6040519115158252600160a060020a031660208201526040908101905180910390f35b341561022257600080fd5b61022a61058a565b60405190815260200160405180910390f35b341561024757600080fd5b6100ac60048035906024803580820192908101359160443580820192908101359160649160c43580830192908201359160e43580830192908201359161010435918201910135610590565b005b341561029f57600080fd5b6100ac60048035906024803580820192908101359160449160a43580830192908201359160c43580830192908201359160e4359182019101356108a5565b005b34156102ea57600080fd5b6100ac600160a060020a0360043516610a9e565b005b341561030b57600080fd5b6100c6610b45565b604051600160a060020a03909116815260200160405180910390f35b341561033a57600080fd5b61022a610b54565b60405190815260200160405180910390f35b600480548290811061036257fe5b906000526020600020900160005b915054906101000a9004600160a060020a031681565b600380548290811061036257fe5b906000526020600020900160005b915054906101000a9004600160a060020a031681565b60008060006103c78686610501565b909250905060018215151480156103ef575083600160a060020a031681600160a060020a0316145b92505b50509392505050565b60005b60045481101561045e5733600160a060020a031660048281548110151561042157fe5b906000526020600020900160005b9054906101000a9004600160a060020a0316600160a060020a03161415610455576104fd565b5b6001016103fe565b5060005b6003548110156104fd5733600160a060020a031660038281548110151561048557fe5b906000526020600020900160005b9054906101000a9004600160a060020a0316600160a060020a031614156104f45760048054600181016104c68382610f4c565b916000526020600020900160005b8154600160a060020a033381166101009390930a92830292021916179055505b5b600101610462565b5b50565b6000806000806000855160411461051e5760009450849350610580565b6020860151925060408601519150606086015160001a9050601b8160ff16101561054657601b015b8060ff16601b1415801561055e57508060ff16601c14155b1561056f5760009450849350610580565b61057b87828585610b5a565b945094505b5050509250929050565b60015481565b60025460009033600160a060020a039081169116146105ae57600080fd5b6000548d908110156105bf57600080fd5b30600160a060020a0316316106008c8c8080602002602001604051908101604052809392919081815260200183836020028082843750610b9f945050505050565b111561060b57600080fd5b6001548e6106458f8f8080602002602001604051908101604052809392919081815260200183836020028082843750610bd6945050505050565b61067b8e8e8080602002602001604051908101604052809392919081815260200183836020028082843750610cd5945050505050565b60405180807f545200000000000000000000000000000000000000000000000000000000000081525060020185815260200184815260200183805190602001908083835b602083106106df57805182525b601f1990920191602091820191016106bf565b6001836020036101000a038019825116818451161790925250505091909101905082805190602001908083835b6020831061072c57805182525b601f19909201916020918201910161070c565b6001836020036101000a0380198251168184511617909252505050919091019550604094505050505051908190039020915061081b828a600360606040519081016040529190828260608082843782019150505050508a8a8080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505089898080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505088888080601f016020809104026020016040519081016040528181529291906020840183838082843750610dcb945050505050565b151561082657600080fd5b60018e016000556108928d8d8060208082020160405190810160405280939291908181526020018383602002808284378201915050505050508c8c8080602002602001604051908101604052809392919081815260200183836020028082843750610ed4945050505050565b5b5b505b50505050505050505050505050565b60025460009033600160a060020a039081169116146108c357600080fd5b6000548b908110156108d457600080fd5b6001548c61090e8d8d8080602002602001604051908101604052809392919081815260200183836020028082843750610bd6945050505050565b60405180807f4e5300000000000000000000000000000000000000000000000000000000000081525060020184815260200183815260200182805190602001908083835b6020831061097257805182525b601f199092019160209182019101610952565b6001836020036101000a038019825116818451161790925250505091909101945060409350505050519081900390209150610a60828a600360606040519081016040529190828260608082843782019150505050508a8a8080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505089898080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505088888080601f016020809104026020016040519081016040528181529291906020840183838082843750610dcb945050505050565b1515610a6b57600080fd5b60018c016000908155610a7f600482610f4c565b5061089660038c8c610fa0565b505b5b505b5050505050505050505050565b600254600090819033600160a060020a03908116911614610abe57600080fd5b50506003546004546006821115610adc5760028111610adc57600080fd5b5b6003821115610af35760018111610af357600080fd5b5b60008111610b0157600080fd5b82600160a060020a03166108fc30600160a060020a0316319081150290604051600060405180830381858888f193505050501515610b3e57600080fd5b5b5b505050565b600254600160a060020a031681565b60005481565b60008060008060405188815287602082015286604082015285606082015260208160808360006001610bb8f1925080519150508181935093505b505094509492505050565b6000805b8251811015610bcf57828181518110610bb857fe5b90602001906020020151820191505b600101610ba3565b5b50919050565b610bde611011565b6000808351601402604051805910610bf35750595b908082528060200260200182016040525b509250600091505b8351821015610ccd575060005b6014811015610cc1578060130360080260020a848381518110610c3857fe5b90602001906020020151600160a060020a0316811515610c5457fe5b047f0100000000000000000000000000000000000000000000000000000000000000028382846014020181518110610c8857fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a9053505b600101610c19565b5b600190910190610c0c565b5b5050919050565b610cdd611011565b6000808351602002604051805910610cf25750595b908082528060200260200182016040525b509250600091505b8351821015610ccd575060005b6020811015610db75780601f0360080260020a848381518110610d3757fe5b90602001906020020151811515610d4a57fe5b047f0100000000000000000000000000000000000000000000000000000000000000028382846020020181518110610d7e57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a9053505b600101610d18565b5b600190910190610d0b565b5b5050919050565b600380546000918290610e1390899088908a855b602002015181548110610dee57fe5b906000526020600020900160005b9054906101000a9004600160a060020a03166103b8565b90506003821115610e6d57808015610e695750610e69888660038a6001610ddf565b602002015181548110610dee57fe5b906000526020600020900160005b9054906101000a9004600160a060020a03166103b8565b5b90505b6006821115610ec557808015610ec15750610ec1888560038a6002610ddf565b602002015181548110610dee57fe5b906000526020600020900160005b9054906101000a9004600160a060020a03166103b8565b5b90505b8092505b505095945050505050565b60005b8151811015610b3e57828181518110610eec57fe5b90602001906020020151600160a060020a03166108fc838381518110610f0e57fe5b906020019060200201519081150290604051600060405180830381858888f193505050501515610f3d57600080fd5b5b600101610ed7565b5b505050565b815481835581811511610b3e57600083815260209020610b3e918101908301611023565b5b505050565b815481835581811511610b3e57600083815260209020610b3e918101908301611023565b5b505050565b828054828255906000526020600020908101928215611000579160200282015b8281111561100057815473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a03843516178255602090920191600190910190610fc0565b5b5061100d929150611044565b5090565b60206040519081016040526000815290565b61104191905b8082111561100d5760008155600101611029565b5090565b90565b61104191905b8082111561100d57805473ffffffffffffffffffffffffffffffffffffffff1916815560010161104a565b5090565b905600a165627a7a723058203e8a799fb779dad3abc365c6c3dc36f07d6abac4c9307bb0599a6b8806f920740029";
        String a0 = "0000000000000000000000000000000000000000000000000000000000000040"; // Arraay (offset where the array data starts.
        String a1 = String.format("%064x", teamId);
        String a2 = String.format("%064x", n);

        StringBuilder hexString = new StringBuilder("0x").append(contract).append(a0).append(a1).append(a2);
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
        EtherNode blockchain = new EtherNode(true);
        return blockchain.checkNonce(addressHex);
    }

    private String publishCryptoTx(org.ethereum.geth.Transaction cryptoTx) throws RemoteException {
        try {
            byte[] rlp = cryptoTx.encodeRLP();
            Log.v(LOG_TAG, "Publishing 'Multisig creation' tx:" + cryptoTx.getHash().getHex() + " " + cryptoTx.encodeJSON());
            String hex = "0x" + toHexString(rlp);

            EtherNode blockchain = new EtherNode(true);
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

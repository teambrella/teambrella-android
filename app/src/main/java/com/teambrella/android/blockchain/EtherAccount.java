package com.teambrella.android.blockchain;

import android.content.Context;
import android.util.Log;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.ethereum.geth.Account;
import org.ethereum.geth.Accounts;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Geth;
import org.ethereum.geth.KeyStore;
import org.ethereum.geth.Transaction;

import java.util.Arrays;


/**
 * Local etherium keys and signing tools.
 */
public class EtherAccount {

    private static final String LOG_TAG = EtherAccount.class.getSimpleName();

    private KeyStore mKeyStore;
    private String mKeyStoreSecret;
    private Account mAccount;

    public static String toDepositAddress(String privateKey, Context context) throws CryptoException{
        return new EtherAccount(privateKey, context).getDepositAddress();
    }

    public static String toPublicKeySignature(String privateKeyAsWiF, Context context, String hex) throws CryptoException{
        return new EtherAccount(privateKeyAsWiF, context).getPublicKeySignature(hex);
    }

    public EtherAccount(String privateKeyAsWiF, Context context) throws CryptoException{
        DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(null, privateKeyAsWiF);
        ECKey key = dpk.getKey();
        String myPublicKey = key.getPublicKeyAsHex();
        String keyStorePath = context.getFilesDir().getPath() + "/keystore/" + myPublicKey;
        byte[] privateKey = key.getPrivKeyBytes();

        mKeyStoreSecret = key.getPrivateKeyAsWiF(new MainNetParams());
        mKeyStore = new KeyStore(keyStorePath , Geth.LightScryptN, Geth.LightScryptP);
        mAccount = initEthAccount(privateKey, mKeyStore, mKeyStoreSecret);
    }

    public EtherAccount(ECKey key, Context context) throws CryptoException{
        String myPublicKey = key.getPublicKeyAsHex();
        String keyStorePath = context.getFilesDir().getPath() + "/keystore/" + myPublicKey;
        byte[] privateKey = key.getPrivKeyBytes();

        mKeyStoreSecret = key.getPrivateKeyAsWiF(new MainNetParams());
        mKeyStore = new KeyStore(keyStorePath , Geth.LightScryptN, Geth.LightScryptP);
        mAccount = initEthAccount(privateKey, mKeyStore, mKeyStoreSecret);
    }

    ////TODO: remove not ETH dependencies from this class. Leave only this ctor:
    public EtherAccount(byte[] privateKey, String keyStorePath, String keyStoreSecret) throws CryptoException{

        mKeyStoreSecret = keyStoreSecret;
        mKeyStore = new KeyStore(keyStorePath, Geth.LightScryptN, Geth.LightScryptP);
        mAccount = initEthAccount(privateKey, mKeyStore, mKeyStoreSecret);
    }



    public String getDepositAddress() {
        return mAccount.getAddress().getHex();
    }

    public String getPublicKeySignature(String hex){
        byte[] signature = sign(hex);
        reverseAndCalculateV(signature);
        return "0x" + Hex.fromBytes(signature);
    }

    public Transaction newDepositTx(long nonce, long gasLimit, String toAddress, boolean isTestNet, long value) throws CryptoException {
        long gasPrice = isTestNet ? 150_000_000_000L : 1_000_000_000L;  // 50 Gwei for TestNet and 1 Gwei for MainNet (1 Gwei = 10^9 wei)

        String json = String.format("{\"nonce\":\"0x%x\",\"gasPrice\":\"0x%x\",\"gas\":\"0x%x\",\"to\":\"%s\",\"value\":\"0x%x\",\"input\":\"0x\",\"v\":\"0x29\",\"r\":\"0x29\",\"s\":\"0x29\"}",
                nonce,
                gasPrice,
                gasLimit,
                toAddress,
                value
        );

        Log.v(LOG_TAG, "Constructing deposit tx:" + json);

        try {
            Transaction tx = Geth.newTransactionFromJSON(json);
            Log.v(LOG_TAG, "deposit tx constructed.");
            return tx;
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
            throw new CryptoException(e.getMessage(), e);
        }
    }

    public Transaction newMessageTx(long nonce, long gasLimit, String contractAddress, boolean isTestNet, String methodId, Object... methodArgs) throws CryptoException {
        long gasPrice = isTestNet ? 150_000_000_000L : 1_000_000_000L;  // 50 Gwei for TestNet and 1 Gwei for MainNet (1 Gwei = 10^9 wei)

        String json = String.format("{\"nonce\":\"0x%x\",\"gasPrice\":\"0x%x\",\"gas\":\"0x%x\",\"to\":\"%s\",\"value\":\"0x0\",\"input\":\"%s\",\"v\":\"0x29\",\"r\":\"0x29\",\"s\":\"0x29\"}",
                nonce,
                gasPrice,
                gasLimit,
                contractAddress,
                "0x" + methodId + AbiArguments.encodeToHexString(methodArgs)
        );

        Log.v(LOG_TAG, "Constructing deposit tx:" + json);

        try {
            Transaction tx = Geth.newTransactionFromJSON(json);
            Log.v(LOG_TAG, "deposit tx constructed.");
            return tx;
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
            throw new CryptoException(e.getMessage(), e);
        }
    }

    public Transaction signTx(Transaction unsignedTx, boolean isTestnet) throws CryptoException{

            try {
                return mKeyStore.signTxPassphrase(mAccount, mKeyStoreSecret, unsignedTx, getChainId(isTestnet));
            }catch (Exception e){
                Log.e(LOG_TAG, "Could not sign tx; isTestnet:" + isTestnet + ". " + e.getMessage(), e);
                throw new CryptoException(e.getMessage(), e);
            }
    }

    public byte[] signHash(byte[] hash256) throws CryptoException{

        try {
            Log.v(LOG_TAG, "signing hash: " + Hex.fromBytes(hash256));
            return mKeyStore.signHashPassphrase(mAccount, mKeyStoreSecret, hash256);
        }catch (Exception e){
            Log.e(LOG_TAG, "Could not sign hash:" + Hex.fromBytes(hash256) + ". " + e.getMessage(), e);
            throw new CryptoException(e.getMessage(), e);
        }
    }

    public byte[] signHashAndCalculateV(byte[] hash256) throws CryptoException{

        byte[] sig = signHash(hash256);
        sig[sig.length - 1] += 27;
        return sig;
    }

//    private String getSecret(){
//        return mKey.getPrivateKeyAsWiF(new MainNetParams());
//    }

    private BigInt getChainId(boolean isTestNet){
        return new BigInt(isTestNet ? 3 : 1);   // 3 is for Ropsten TestNet; 1 is for MainNet
    }

    private byte[] sign(String target) {

        try{
            Log.v(LOG_TAG, "Signing last 32 bytes of a string: " + target);
//xxx:            KeyStore ks = getEthKeyStore();
//            Account acc = getEthAccount(ks);

            byte[] targetAsBytes = Hex.toBytes(target);
            int len = targetAsBytes.length;
            if (len < 32) throw new UnsupportedOperationException("Can only sign message of 32+ bytes");
            byte[] last32Bytes = Arrays.copyOfRange(targetAsBytes, len -32, len);

            byte[] sig = mKeyStore.signHashPassphrase(mAccount, mKeyStoreSecret, last32Bytes);
            return sig;

        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return null;
    }

    ////TODO: xxx: remove:
//    private KeyStore getEthKeyStore() {
//        String myPublicKey = mKey.getPublicKeyAsHex();
//        String documentsPath = mContext.getFilesDir().getPath();
//        KeyStore ks = new KeyStore(documentsPath + "/keystore/" + myPublicKey, Geth.LightScryptN, Geth.LightScryptP);
//
//        return ks;
//    }

    ////TODO: xxxx: move up before private methods.
    private static Account initEthAccount(byte[] privateKey, KeyStore ks, String ksSecret) throws CryptoException {
        try {
            Accounts aaa = ks.getAccounts();
            Account acc;
            if (aaa.size() > 0)
                acc = aaa.get(0);
            else
                //com.teambrella.android W/System.err: go.Universe$proxyerror: invalid length, need 256 bits
                acc = ks.importECDSAKey(privateKey, ksSecret);

            return acc;
        } catch (Exception e){
            Log.e("Test", "Was unnable to read account.", e);
            throw new CryptoException(e.getMessage(), e);
        }
    }

    private void reverseAndCalculateV(byte[] array){
        for (int i = 0, n = array.length, m = n/2 ; i < m; i++) {
            byte temp = array[i];
            array[i] = array[n - 1 - i];
            array[n - 1 - i] = temp;
        }

        array[0] += 27;
    }
}

package com.teambrella.android.blockchain;

import android.content.Context;
import android.os.RemoteException;
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

    private ECKey mKey;
    private KeyStore mKeyStore;
    private Account mAccount;
    private Context mContext;

    public static String toDepositAddress(String privateKey, Context context) throws CryptoException{
        return new EtherAccount(privateKey, context).getDepositAddress();
    }

    public static String toPublicKeySignature(String privateKey, Context context) throws CryptoException{
        return new EtherAccount(privateKey, context).getPublicKeySignature();
    }

    public EtherAccount(String privateKey, Context context) throws CryptoException{
        mContext = context;
        DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(null, privateKey);
        mKey = dpk.getKey();
        mKeyStore = getEthKeyStore();
        mAccount = getEthAccount(mKeyStore);
    }

    public EtherAccount(ECKey key, Context context) throws CryptoException{
        mContext = context;
        mKey = key;
        mKeyStore = getEthKeyStore();
        mAccount = getEthAccount(mKeyStore);
    }


    public String getDepositAddress() {
        return mAccount.getAddress().getHex();
    }

    public String getPublicKeySignature(){
        byte[] signature = sign(mKey.getPublicKeyAsHex());
        reverseAndCalculateV(signature);
        return "0x" + toHexString(signature);
    }

    public Transaction newDepositTx(long nonce, long gasLimit, String toAddress, boolean isTestNet, long value) throws CryptoException {
        long gasPrice = isTestNet ? 50_000_000_000L : 500_000_000L;  // 50 Gwei for TestNet and 0.5 Gwei for MainNet (1 Gwei = 10^9 wei)

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

    public Transaction signTx(Transaction unsignedTx, boolean isTestnet) throws CryptoException{

            String secret = mKey.getPrivateKeyAsWiF(new MainNetParams());
            try {
                return mKeyStore.signTxPassphrase(mAccount, secret, unsignedTx, getChainId(isTestnet));
            }catch (Exception e){
                Log.e(LOG_TAG, "Could not sign tx; isTestnet:" + isTestnet + ". " + e.getMessage(), e);
                throw new CryptoException(e.getMessage(), e);
            }
    }

    private BigInt getChainId(boolean isTestNet){
        return new BigInt(isTestNet ? 3 : 1);   // 3 is for Ropsten TestNet; 1 is for MainNet
    }

    private byte[] sign(String target)
    {
        try{
            Log.v(LOG_TAG, "Signing last 32 bytes of a string: " + target);
            KeyStore ks = getEthKeyStore();
            Account acc = getEthAccount(ks);

            byte[] targetAsBytes = toByteArray(target);
            int len = targetAsBytes.length;
            if (len < 32) throw new UnsupportedOperationException("Can only sign message of 32+ bytes");
            byte[] last32Bytes = Arrays.copyOfRange(targetAsBytes, len -32, len);

            String secret = mKey.getPrivateKeyAsWiF(new MainNetParams());
            byte[] sig = ks.signHashPassphrase(acc, secret, last32Bytes);
            return sig;

        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return null;
    }

    private KeyStore getEthKeyStore() {
        String myPublicKey = mKey.getPublicKeyAsHex();
        String documentsPath = mContext.getFilesDir().getPath();
        KeyStore ks = new KeyStore(documentsPath + "/keystore/" + myPublicKey, Geth.LightScryptN, Geth.LightScryptP);

        return ks;
    }

    private Account getEthAccount(KeyStore ks) throws CryptoException {
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

    private static byte[] toByteArray(String hexString) {
        int len = hexString.length();
        byte[] res = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            res[i / 2] = (byte) (
                    (Character.digit(hexString.charAt(i), 16) << 4) +
                    Character.digit(hexString.charAt(i+1), 16)
            );
        }
        return res;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int b = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[b >>> 4];
            hexChars[j * 2 + 1] = hexArray[b & 0x0F];
        }
        return new String(hexChars);
    }
}

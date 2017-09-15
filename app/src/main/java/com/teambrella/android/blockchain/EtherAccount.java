package com.teambrella.android.blockchain;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.ethereum.geth.Account;
import org.ethereum.geth.Accounts;
import org.ethereum.geth.Geth;
import org.ethereum.geth.KeyStore;

import java.util.Arrays;


/**
 * Local etherium keys and signing tools.
 */
public class EtherAccount {

    private static final String LOG_TAG = EtherAccount.class.getSimpleName();

    private ECKey mKey;
    private Context mContext;

    public static String toDepositAddress(String privateKey, Context context) throws CryptoException{
        return new EtherAccount(privateKey, context).getDepositAddress();
    }

    public static String toPublicKeySignature(String privateKey, Context context){
        return new EtherAccount(privateKey, context).getPublicKeySignature();
    }


    public EtherAccount(String privateKey, Context context){
        DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(null, privateKey);
        mKey = dpk.getKey();
        mContext = context;
    }

    public EtherAccount(ECKey key, Context context){
        mKey = key;
        mContext = context;
    }


    public String getDepositAddress() throws CryptoException {
        try{
            KeyStore ks = getEthKeyStore();
            Account acc = getEthAccount(ks);

            return acc.getAddress().getHex();

        }catch (RemoteException ex){
            Log.e(LOG_TAG, ex.getMessage(), ex);
            throw new CryptoException(ex.getMessage(), ex);
        }
    }

    public String getPublicKeySignature(){
        byte[] signature = sign(mKey.getPublicKeyAsHex());
        reverseAndCalculateV(signature);
        return "0x" + toHexString(signature);
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

    private KeyStore getEthKeyStore() throws RemoteException {
        String myPublicKey = mKey.getPublicKeyAsHex();
        String documentsPath = mContext.getFilesDir().getPath();
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
        } catch (Exception e){
            Log.e("Test", "Was unnable to read account.", e);
            throw new RemoteException(e.getMessage());
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

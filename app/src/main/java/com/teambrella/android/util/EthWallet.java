package com.teambrella.android.util;

import android.os.RemoteException;
import android.util.Log;

import com.teambrella.android.BuildConfig;
import com.teambrella.android.blockchain.AbiArguments;
import com.teambrella.android.blockchain.CryptoException;
import com.teambrella.android.blockchain.EtherAccount;
import com.teambrella.android.blockchain.EtherNode;
import com.teambrella.android.blockchain.Hex;
import com.teambrella.android.blockchain.Sha3;
import com.teambrella.android.content.model.Cosigner;
import com.teambrella.android.content.model.Multisig;
import com.teambrella.android.content.model.TXSignature;
import com.teambrella.android.content.model.Tx;
import com.teambrella.android.content.model.TxInput;
import com.teambrella.android.content.model.TxOutput;

import org.ethereum.geth.Transaction;

import java.util.List;
import java.util.Map;

class EthWallet {

    private static final String LOG_TAG = EthWallet.class.getSimpleName();
    private static final String METHOD_ID_TRANSFER = "91f34dbd";
    private static final String TX_PREFIX = "5452";
    private static final String NS_PREFIX = "4E53";

    private final EtherAccount mEtherAcc;
    private final boolean mIsTestNet;

    public EthWallet(byte[] privateKey, String keyStorePath, String keyStoreSecret, boolean isTestNet) throws CryptoException {
        mIsTestNet = isTestNet;
        mEtherAcc = new EtherAccount(privateKey, keyStorePath, keyStoreSecret);
    }

    public byte[] cosign(Tx tx, TxInput payFrom) throws CryptoException{

        int opNum = payFrom.previousTxIndex + 1;

        Multisig sourceMultisig = tx.getFromMultisig();
        long teamId = sourceMultisig.teamId;

        String[] payToAddresses = toAddresses(tx.txOutputs);
        String[] payToValues = toValues(tx.txOutputs);

        byte[] h = getHash(teamId, opNum, payToAddresses, payToValues);
        Log.v(LOG_TAG, "Hash created for Tx transfer(s): " + Hex.fromBytes(h));

        byte[] sig = mEtherAcc.signHashAndCalculateV(h);
        Log.v(LOG_TAG, "Hash signed.");

        return sig;
    }

    public String publish(Tx tx) throws CryptoException, RemoteException{

        List<TxInput> inputs = tx.txInputs;
        if (inputs.size() != 1){
            String msg = "Unexpected count of tx inputs of ETH tx. Expected: 1, was: " + inputs.size();
            Log.e(LOG_TAG,msg);
            throw new ArithmeticException(msg);
        }

        EtherNode blockchain = new EtherNode(BuildConfig.isTestNet);
        Multisig myMultisig = tx.getFromMultisig();
        long myNonce = blockchain.checkNonce(mEtherAcc.getDepositAddress());
        long gasLimit = 500_000L;
        long gasPrice = getGasPrice();
        String multisigAddress = myMultisig.address;
        String methodId = METHOD_ID_TRANSFER;   // TODO:

        TxInput payFrom = tx.txInputs.get(0);
        int opNum = payFrom.previousTxIndex + 1;
        String[] payToAddresses = toAddresses(tx.txOutputs);
        String[] payToValues = toValues(tx.txOutputs);

        int[] pos = new int[3];
        byte[][] sig = new byte[3][];
        sig[0] = sig[1] = sig[2] = new byte[0];
        Map<Long,TXSignature> txSignatures = payFrom.signatures;
        int index = 0, j = 0;
        for (Cosigner cos : tx.cosigners){
            if (txSignatures.containsKey(cos.teammateId)){
                TXSignature s = txSignatures.get(cos.teammateId);
                pos[j] = index;
                sig[j] = s.bSignature;

                if (++j >= 3) {
                    break;
                }
            }

            index++;
        }

        Transaction cryptoTx = mEtherAcc.newMessageTx(myNonce, gasLimit, multisigAddress, mIsTestNet, methodId, opNum, payToAddresses, payToValues, pos[0], pos[1], pos[2], sig[0], sig[1],sig[2]);
        try{
            Log.v(LOG_TAG, "tx cratated: " + cryptoTx.encodeJSON());
        }catch (Exception e){
            Log.e(LOG_TAG, "could not encode JSON to log tx: " + e.getMessage(), e);
        }

        cryptoTx = mEtherAcc.signTx(cryptoTx, mIsTestNet);
        Log.v(LOG_TAG, "tx signed.");

        return publish(cryptoTx);
    }

    public long getGasPrice(){
        return mIsTestNet ? 150_000_000_000L : 1_000_000_000L;  // 150 Gwei for TestNet and 0.5 Gwei for MainNet (1 Gwei = 10^9 wei)
    }

    private String publish(Transaction cryptoTx) throws RemoteException {
        try {
            byte[] rlp = cryptoTx.encodeRLP();
            Log.v(LOG_TAG, "Publishing 'Multisig creation' tx:" + cryptoTx.getHash().getHex() + " " + cryptoTx.encodeJSON());
            String hex = "0x" + Hex.fromBytes(rlp);

            EtherNode blockchain = new EtherNode(BuildConfig.isTestNet);
            return blockchain.pushTx(hex);

        } catch (Exception e) {
            Log.e(LOG_TAG, "" + e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        }
    }

    private String[] toAddresses(List<TxOutput> destinations){

        int n = destinations.size();
        String[] destinationAddresses = new String[n];

        for (int i=0; i<n; i++){
            destinationAddresses[i] = destinations.get(i).address;
        }

        return destinationAddresses;
    }

    private String[] toValues(List<TxOutput> destinations){

        int n = destinations.size();
        String[] destinationValues = new String[n];

        for (int i=0; i<n; i++){
            destinationValues[i] = AbiArguments.parseDecimalAmount(destinations.get(i).cryptoAmount);
        }

        return destinationValues;
    }

    private byte[] getHash(long teamId, int opNum, String[] addresses, String[] values){

        String a0 = TX_PREFIX; // Arraay (offset where the array data starts.
        String a1 = String.format("%064x", teamId);
        String a2 = String.format("%064x", opNum);
        int n = addresses.length;
        String[] a3 = new String[n];
        for (int i = 0; i < n; i++) {
            a3[i] = Hex.remove0xPrefix(addresses[i]);
        }
        String[] a4 = new String[n];
        for (int i = 0; i < n; i++) {
            a4[i] = Hex.remove0xPrefix(values[i]);
        }

        byte[] data = com.teambrella.android.blockchain.Hex.toBytes(a0, a1, a2, a3, a4);
        return Sha3.getKeccak256Hash(data);
    }

}

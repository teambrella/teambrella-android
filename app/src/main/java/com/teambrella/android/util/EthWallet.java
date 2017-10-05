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

import java.math.BigDecimal;
import java.math.MathContext;
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

    public String createOneWallet(long myNonce, Multisig m, long gasLimit) throws CryptoException, RemoteException {

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

        String contractV002 = "6060604052604051610ecd380380610ecd8339810160405280805182019190602001805191506003905082805161003a929160200190610064565b50600190815560028054600160a060020a03191633600160a060020a0316179055600055506100f2565b8280548282559060005260206000209081019282156100bb579160200282015b828111156100bb5782518254600160a060020a031916600160a060020a039190911617825560209290920191600190910190610084565b506100c79291506100cb565b5090565b6100ef91905b808211156100c7578054600160a060020a03191681556001016100d1565b90565b610dcc806101016000396000f300606060405236156100965763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416630b7b3eb7811461009857806322c5ec0f146100ca5780633bf2b4cd146100e05780638d475461146100f357806391f34dbd14610118578063a0175b961461016e578063d41097e3146101b7578063deff41c1146101d6578063df98ba00146101e9575b005b34156100a357600080fd5b6100ae6004356101fc565b604051600160a060020a03909116815260200160405180910390f35b34156100d557600080fd5b6100ae600435610224565b34156100eb57600080fd5b610096610232565b34156100fe57600080fd5b610106610312565b60405190815260200160405180910390f35b341561012357600080fd5b61009660048035906024803580820192908101359160443580820192908101359160649160c43580830192908201359160e43580830192908201359161010435918201910135610318565b341561017957600080fd5b61009660048035906024803580820192908101359160449160a43580830192908201359160c43580830192908201359160e435918201910135610628565b34156101c257600080fd5b610096600160a060020a036004351661081d565b34156101e157600080fd5b6100ae6108c0565b34156101f457600080fd5b6101066108cf565b600480548290811061020a57fe5b600091825260209091200154600160a060020a0316905081565b600380548290811061020a57fe5b60005b6004548110156102805733600160a060020a031660048281548110151561025857fe5b600091825260209091200154600160a060020a031614156102785761030f565b600101610235565b5060005b60035481101561030f5733600160a060020a03166003828154811015156102a757fe5b600091825260209091200154600160a060020a031614156103075760048054600181016102d48382610cac565b506000918252602090912001805473ffffffffffffffffffffffffffffffffffffffff191633600160a060020a03161790555b600101610284565b50565b60015481565b60025460009033600160a060020a0390811691161461033657600080fd5b6000548d9081101561034757600080fd5b30600160a060020a0316316103888c8c80806020026020016040519081016040528093929190818152602001838360200280828437506108d5945050505050565b111561039357600080fd5b6001548e6103cd8f8f808060200260200160405190810160405280939291908181526020018383602002808284375061090a945050505050565b6104038e8e8080602002602001604051908101604052809392919081815260200183836020028082843750610a05945050505050565b60405180807f545200000000000000000000000000000000000000000000000000000000000081525060020185815260200184815260200183805190602001908083835b602083106104665780518252601f199092019160209182019101610447565b6001836020036101000a038019825116818451161790925250505091909101905082805190602001908083835b602083106104b25780518252601f199092019160209182019101610493565b6001836020036101000a038019825116818451161790925250505091909101955060409450505050505190819003902091506105a1828a600360606040519081016040529190828260608082843782019150505050508a8a8080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505089898080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505088888080601f016020809104026020016040519081016040528181529291906020840183838082843750610af0945050505050565b15156105ac57600080fd5b60018e016000556106188d8d8060208082020160405190810160405280939291908181526020018383602002808284378201915050505050508c8c8080602002602001604051908101604052809392919081815260200183836020028082843750610b80945050505050565b5050505050505050505050505050565b60025460009033600160a060020a0390811691161461064657600080fd5b6000548b9081101561065757600080fd5b6001548c6106918d8d808060200260200160405190810160405280939291908181526020018383602002808284375061090a945050505050565b60405180807f4e5300000000000000000000000000000000000000000000000000000000000081525060020184815260200183815260200182805190602001908083835b602083106106f45780518252601f1990920191602091820191016106d5565b6001836020036101000a0380198251168184511617909252505050919091019450604093505050505190819003902091506107e2828a600360606040519081016040529190828260608082843782019150505050508a8a8080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505089898080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505088888080601f016020809104026020016040519081016040528181529291906020840183838082843750610af0945050505050565b15156107ed57600080fd5b60018c016000908155610801600482610cac565b5061080e60038c8c610cd0565b50505050505050505050505050565b600254600090819033600160a060020a0390811691161461083d57600080fd5b5050600354600454600682111561085b576002811161085b57600080fd5b6003821115610871576001811161087157600080fd5b6000811161087e57600080fd5b82600160a060020a03166108fc30600160a060020a0316319081150290604051600060405180830381858888f1935050505015156108bb57600080fd5b505050565b600254600160a060020a031681565b60005481565b6000805b8251811015610904578281815181106108ee57fe5b90602001906020020151909101906001016108d9565b50919050565b610912610d40565b60008083516014026040518059106109275750595b90808252806020026020018201604052509250600091505b83518210156109fe575060005b60148110156109f3578060130360080260020a84838151811061096b57fe5b90602001906020020151600160a060020a031681151561098757fe5b047f01000000000000000000000000000000000000000000000000000000000000000283828460140201815181106109bb57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a90535060010161094c565b60019091019061093f565b5050919050565b610a0d610d40565b6000808351602002604051805910610a225750595b90808252806020026020018201604052509250600091505b83518210156109fe575060005b6020811015610ae55780601f0360080260020a848381518110610a6657fe5b90602001906020020151811515610a7957fe5b047f0100000000000000000000000000000000000000000000000000000000000000028382846020020181518110610aad57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a905350600101610a47565b600190910190610a3a565b600380546000918290610b2d90899088908a855b602002015181548110610b1357fe5b600091825260209091200154600160a060020a0316610bf1565b90506003821115610b5257808015610b4f5750610b4f888660038a6001610b04565b90505b6006821115610b7557808015610b725750610b72888560038a6002610b04565b90505b979650505050505050565b60005b81518110156108bb57828181518110610b9857fe5b90602001906020020151600160a060020a03166108fc838381518110610bba57fe5b906020019060200201519081150290604051600060405180830381858888f193505050501515610be957600080fd5b600101610b83565b6000806000610c008686610c32565b90925090506001821515148015610c28575083600160a060020a031681600160a060020a0316145b9695505050505050565b60008060008060006020860151925060408601519150606086015160001a9050610c5e87828585610c6c565b945094505050509250929050565b60008060008060405188815287602082015286604082015285606082015260208160808360006001610bb8f1925080519299929850919650505050505050565b8154818355818115116108bb576000838152602090206108bb918101908301610d52565b828054828255906000526020600020908101928215610d30579160200282015b82811115610d3057815473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a03843516178255602090920191600190910190610cf0565b50610d3c929150610d6f565b5090565b60206040519081016040526000815290565b610d6c91905b80821115610d3c5760008155600101610d58565b90565b610d6c91905b80821115610d3c57805473ffffffffffffffffffffffffffffffffffffffff19168155600101610d755600a165627a7a7230582022e6d8a992945b19566381b295f214afecfe2a94d99a7c72506490dba86306200029";
        Log.v(LOG_TAG, "Constructing " + toCreationInfoString(m.teamId, null));
        Transaction cryptoTx;
        cryptoTx = mEtherAcc.newContractTx(myNonce, gasLimit, getGasPrice(), contractV002, m.teamId, cosignerAddresses);
        cryptoTx = mEtherAcc.signTx(cryptoTx, BuildConfig.isTestNet);
        Log.v(LOG_TAG, toCreationInfoString(m) + " signed.");

        String txHex = publish(cryptoTx);
        return txHex;
    }


    public boolean deposit(Multisig multisig) throws CryptoException, RemoteException{

        EtherNode blockchain = new EtherNode(mIsTestNet);
        BigDecimal gasWalletAmount = blockchain.checkBalance(mEtherAcc.getDepositAddress());

        BigDecimal txPriceLimit = eth(200_000 * getGasPrice());
        if (gasWalletAmount.compareTo(dec(30).multiply(txPriceLimit)) > 0) {
            BigDecimal minRestForGas = dec(25).multiply(txPriceLimit); // min restt in the Gas Wallet is an amount for 25 transactions.

            long myNonce = checkMyNonce();
            BigDecimal value = gasWalletAmount.subtract(minRestForGas, MathContext.UNLIMITED);
            org.ethereum.geth.Transaction depositTx;
            depositTx = mEtherAcc.newDepositTx(myNonce, 50_000L, multisig.address, getGasPrice(), value);
            depositTx = mEtherAcc.signTx(depositTx, BuildConfig.isTestNet);
            publish(depositTx);
        }

        return true;
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

        Multisig myMultisig = tx.getFromMultisig();
        long myNonce = checkMyNonce();
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

        Transaction cryptoTx = mEtherAcc.newMessageTx(myNonce, gasLimit, multisigAddress, gasPrice, methodId, opNum, payToAddresses, payToValues, pos[0], pos[1], pos[2], sig[0], sig[1],sig[2]);
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
        return mIsTestNet ? 150_000_000_000L : 2_000_000_000L;  // 150 Gwei for TestNet and 2 Gwei for MainNet (1 Gwei = 10^9 wei)
    }

    public long checkMyNonce(){
        EtherNode blockchain = new EtherNode(BuildConfig.isTestNet);
        return blockchain.checkNonce(mEtherAcc.getDepositAddress());
    }

    private static BigDecimal dec(long val){
        return new BigDecimal(val, MathContext.UNLIMITED);
    }

    private static BigDecimal eth(long weis){
        return new BigDecimal(weis, MathContext.UNLIMITED).divide(AbiArguments.WEIS_IN_ETH);
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

    private static String toCreationInfoString(Multisig m) {
        if (null == m) return "null";

        return toCreationInfoString(m.teamId, m.creationTx);
    }

    private static String toCreationInfoString(long teamId, String creationTx) {
        return String.format("'Multisig creation(teamId=%s)' tx:%s", teamId, creationTx);
    }
}
package com.teambrella.android.util;

import android.os.RemoteException;

import com.crashlytics.android.Crashlytics;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.blockchain.AbiArguments;
import com.teambrella.android.blockchain.CryptoException;
import com.teambrella.android.blockchain.EtherAccount;
import com.teambrella.android.blockchain.EtherGasStation;
import com.teambrella.android.blockchain.EtherNode;
import com.teambrella.android.blockchain.Hex;
import com.teambrella.android.blockchain.Scan;
import com.teambrella.android.blockchain.ScanResultTxReceipt;
import com.teambrella.android.blockchain.Sha3;
import com.teambrella.android.content.model.Cosigner;
import com.teambrella.android.content.model.Multisig;
import com.teambrella.android.content.model.TXSignature;
import com.teambrella.android.content.model.Tx;
import com.teambrella.android.content.model.TxInput;
import com.teambrella.android.content.model.TxOutput;
import com.teambrella.android.content.model.Unconfirmed;
import com.teambrella.android.util.log.Log;

import org.ethereum.geth.Transaction;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

class EthWallet {

    private static final String LOG_TAG = EthWallet.class.getSimpleName();
    private static final String METHOD_ID_M_TEAMID = "8d475461";
    private static final String METHOD_ID_M_COSIGNERS = "22c5ec0f";
    private static final String METHOD_ID_M_OPNUM = "df98ba00";
    private static final String METHOD_ID_TRANSFER = "91f34dbd";
    private static final String METHOD_ID_CHANGEALLCOSIGNERS = "a0175b96";
    private static final String TX_PREFIX = "5452";
    private static final String NS_PREFIX = "4E53";

    private final EtherAccount mEtherAcc;
    private final boolean mIsTestNet;

    private static final BigDecimal MIN_GAS_WALLET_BALANCE = new BigDecimal(0.0075, MathContext.UNLIMITED);
    private static final BigDecimal MAX_GAS_WALLET_BALANCE = new BigDecimal(0.01, MathContext.UNLIMITED);

    private BigDecimal mBalance = new BigDecimal(-1, MathContext.UNLIMITED);    // cashed balance to minimise Blockchain Node traffic during multiple sync loops.
    private long mNonce = -1;                                                       // cashed nonce to minimise Blockchain Node traffic during multiple sync loops.

    private long mGasPrice = -1;
    private long mClaimGasPrice = -1;
    private long mContractGasPrice = -1;
    private long mTestGasPrice = -1;
    private long mTestClaimGasPrice = -1;
    private long mTestContractGasPrice = -1;

    EthWallet(byte[] privateKey, String keyStorePath, String keyStoreSecret, boolean isTestNet) throws CryptoException {
        mIsTestNet = isTestNet;
        mEtherAcc = new EtherAccount(privateKey, keyStorePath, keyStoreSecret);
    }

    String createOneWallet(Multisig m, long gasLimit, long gasPrice) throws CryptoException, RemoteException {
        return createOneWallet(getMyNonce(), m, gasLimit, gasPrice);
    }

    String createOneWallet(long myNonce, Multisig m, long gasLimit, long gasPrice) throws CryptoException, RemoteException {

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
                Log.e(LOG_TAG, String.format("Cosigner (teammate id: %s) for multisig id:%s has no publicKeyAddress", cosigners.get(i).teammateId, m.id));
                return null;
            }
        }

        String contractV002 = "6060604052604051610ecd380380610ecd8339810160405280805182019190602001805191506003905082805161003a929160200190610064565b50600190815560028054600160a060020a03191633600160a060020a0316179055600055506100f2565b8280548282559060005260206000209081019282156100bb579160200282015b828111156100bb5782518254600160a060020a031916600160a060020a039190911617825560209290920191600190910190610084565b506100c79291506100cb565b5090565b6100ef91905b808211156100c7578054600160a060020a03191681556001016100d1565b90565b610dcc806101016000396000f300606060405236156100965763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416630b7b3eb7811461009857806322c5ec0f146100ca5780633bf2b4cd146100e05780638d475461146100f357806391f34dbd14610118578063a0175b961461016e578063d41097e3146101b7578063deff41c1146101d6578063df98ba00146101e9575b005b34156100a357600080fd5b6100ae6004356101fc565b604051600160a060020a03909116815260200160405180910390f35b34156100d557600080fd5b6100ae600435610224565b34156100eb57600080fd5b610096610232565b34156100fe57600080fd5b610106610312565b60405190815260200160405180910390f35b341561012357600080fd5b61009660048035906024803580820192908101359160443580820192908101359160649160c43580830192908201359160e43580830192908201359161010435918201910135610318565b341561017957600080fd5b61009660048035906024803580820192908101359160449160a43580830192908201359160c43580830192908201359160e435918201910135610628565b34156101c257600080fd5b610096600160a060020a036004351661081d565b34156101e157600080fd5b6100ae6108c0565b34156101f457600080fd5b6101066108cf565b600480548290811061020a57fe5b600091825260209091200154600160a060020a0316905081565b600380548290811061020a57fe5b60005b6004548110156102805733600160a060020a031660048281548110151561025857fe5b600091825260209091200154600160a060020a031614156102785761030f565b600101610235565b5060005b60035481101561030f5733600160a060020a03166003828154811015156102a757fe5b600091825260209091200154600160a060020a031614156103075760048054600181016102d48382610cac565b506000918252602090912001805473ffffffffffffffffffffffffffffffffffffffff191633600160a060020a03161790555b600101610284565b50565b60015481565b60025460009033600160a060020a0390811691161461033657600080fd5b6000548d9081101561034757600080fd5b30600160a060020a0316316103888c8c80806020026020016040519081016040528093929190818152602001838360200280828437506108d5945050505050565b111561039357600080fd5b6001548e6103cd8f8f808060200260200160405190810160405280939291908181526020018383602002808284375061090a945050505050565b6104038e8e8080602002602001604051908101604052809392919081815260200183836020028082843750610a05945050505050565b60405180807f545200000000000000000000000000000000000000000000000000000000000081525060020185815260200184815260200183805190602001908083835b602083106104665780518252601f199092019160209182019101610447565b6001836020036101000a038019825116818451161790925250505091909101905082805190602001908083835b602083106104b25780518252601f199092019160209182019101610493565b6001836020036101000a038019825116818451161790925250505091909101955060409450505050505190819003902091506105a1828a600360606040519081016040529190828260608082843782019150505050508a8a8080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505089898080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505088888080601f016020809104026020016040519081016040528181529291906020840183838082843750610af0945050505050565b15156105ac57600080fd5b60018e016000556106188d8d8060208082020160405190810160405280939291908181526020018383602002808284378201915050505050508c8c8080602002602001604051908101604052809392919081815260200183836020028082843750610b80945050505050565b5050505050505050505050505050565b60025460009033600160a060020a0390811691161461064657600080fd5b6000548b9081101561065757600080fd5b6001548c6106918d8d808060200260200160405190810160405280939291908181526020018383602002808284375061090a945050505050565b60405180807f4e5300000000000000000000000000000000000000000000000000000000000081525060020184815260200183815260200182805190602001908083835b602083106106f45780518252601f1990920191602091820191016106d5565b6001836020036101000a0380198251168184511617909252505050919091019450604093505050505190819003902091506107e2828a600360606040519081016040529190828260608082843782019150505050508a8a8080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505089898080601f016020809104026020016040519081016040528181529291906020840183838082843782019150505050505088888080601f016020809104026020016040519081016040528181529291906020840183838082843750610af0945050505050565b15156107ed57600080fd5b60018c016000908155610801600482610cac565b5061080e60038c8c610cd0565b50505050505050505050505050565b600254600090819033600160a060020a0390811691161461083d57600080fd5b5050600354600454600682111561085b576002811161085b57600080fd5b6003821115610871576001811161087157600080fd5b6000811161087e57600080fd5b82600160a060020a03166108fc30600160a060020a0316319081150290604051600060405180830381858888f1935050505015156108bb57600080fd5b505050565b600254600160a060020a031681565b60005481565b6000805b8251811015610904578281815181106108ee57fe5b90602001906020020151909101906001016108d9565b50919050565b610912610d40565b60008083516014026040518059106109275750595b90808252806020026020018201604052509250600091505b83518210156109fe575060005b60148110156109f3578060130360080260020a84838151811061096b57fe5b90602001906020020151600160a060020a031681151561098757fe5b047f01000000000000000000000000000000000000000000000000000000000000000283828460140201815181106109bb57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a90535060010161094c565b60019091019061093f565b5050919050565b610a0d610d40565b6000808351602002604051805910610a225750595b90808252806020026020018201604052509250600091505b83518210156109fe575060005b6020811015610ae55780601f0360080260020a848381518110610a6657fe5b90602001906020020151811515610a7957fe5b047f0100000000000000000000000000000000000000000000000000000000000000028382846020020181518110610aad57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a905350600101610a47565b600190910190610a3a565b600380546000918290610b2d90899088908a855b602002015181548110610b1357fe5b600091825260209091200154600160a060020a0316610bf1565b90506003821115610b5257808015610b4f5750610b4f888660038a6001610b04565b90505b6006821115610b7557808015610b725750610b72888560038a6002610b04565b90505b979650505050505050565b60005b81518110156108bb57828181518110610b9857fe5b90602001906020020151600160a060020a03166108fc838381518110610bba57fe5b906020019060200201519081150290604051600060405180830381858888f193505050501515610be957600080fd5b600101610b83565b6000806000610c008686610c32565b90925090506001821515148015610c28575083600160a060020a031681600160a060020a0316145b9695505050505050565b60008060008060006020860151925060408601519150606086015160001a9050610c5e87828585610c6c565b945094505050509250929050565b60008060008060405188815287602082015286604082015285606082015260208160808360006001610bb8f1925080519299929850919650505050505050565b8154818355818115116108bb576000838152602090206108bb918101908301610d52565b828054828255906000526020600020908101928215610d30579160200282015b82811115610d3057815473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a03843516178255602090920191600190910190610cf0565b50610d3c929150610d6f565b5090565b60206040519081016040526000815290565b610d6c91905b80821115610d3c5760008155600101610d58565b90565b610d6c91905b80821115610d3c57805473ffffffffffffffffffffffffffffffffffffffff19168155600101610d755600a165627a7a7230582022e6d8a992945b19566381b295f214afecfe2a94d99a7c72506490dba86306200029";
        Log.v(LOG_TAG, "Constructing " + toCreationInfoString(m.teamId, null));
        Transaction cryptoTx;
        cryptoTx = mEtherAcc.newContractTx(myNonce, gasLimit, gasPrice, contractV002, cosignerAddresses, m.teamId);
        cryptoTx = mEtherAcc.signTx(cryptoTx, mIsTestNet);
        Log.v(LOG_TAG, toCreationInfoString(m) + " signed.");

        return publish(cryptoTx);
    }

    /**
     * Verfifies if a given contract creation TX has been mined in the blockchain.
     *
     * @param gasLimit original gas limit, that has been set to the original creation TX. When a TX consumes all the gas up to the limit, that indicates an error.
     * @param m        the given multisig object with original TX hash to check.
     * @return The original multisig object with updated address (when verified successfully), updated error status (if any), or new unconfirmed tx if original TX is outdated.
     */
    void validateCreationTx(Multisig m, long gasLimit) throws CryptoException, RemoteException {

        EtherNode blockchain = new EtherNode(mIsTestNet);

        Scan<ScanResultTxReceipt> receipt = blockchain.checkTx(m.creationTx);
        if (receipt != null) {
            ScanResultTxReceipt res = receipt.result;
            boolean allGasIsUsed = false;
            if (res != null && res.blockNumber != null) {
                long gasUsed = Long.parseLong(res.gasUsed.substring(2), 16);
                allGasIsUsed = gasUsed == gasLimit;
                if (!allGasIsUsed) {

                    m.address = res.contractAddress;
                    return;

                }
            } else {

                recreateIfTimedOut(m, gasLimit);
                return;
            }

            setErrorStatusIfAny(m, receipt, allGasIsUsed);
        }
    }

    private void recreateIfTimedOut(Multisig m, long gasLimit) throws CryptoException, RemoteException {

        Unconfirmed unconfirmed = m.unconfirmed;
        if (unconfirmed != null) {
            Date unconfirmedDate = unconfirmed.getDateCreated();
            Date timeout = new Date(System.currentTimeMillis() - 12 * 60 * 60 * 1000);

            if (unconfirmedDate.before(timeout)) {
                long betterGasPrice = getBetterGasPriceForContractCreation(unconfirmed.cryptoFee);
                String recreatedTxHash = createOneWallet(unconfirmed.cryptoNonce, m, gasLimit, betterGasPrice);

                if (recreatedTxHash != null) {
                    Unconfirmed newUnconfirmed = new Unconfirmed();
                    newUnconfirmed.setDateCreated(new Date());
                    newUnconfirmed.cryptoFee = betterGasPrice;
                    newUnconfirmed.cryptoTx = recreatedTxHash;
                    newUnconfirmed.cryptoNonce = unconfirmed.cryptoNonce;
                    newUnconfirmed.multisigId = m.id;

                    m.unconfirmed = newUnconfirmed;
                    m.creationTx = recreatedTxHash;
                }
            }
        }
    }


    private void setErrorStatusIfAny(Multisig m, Scan<ScanResultTxReceipt> receipt, boolean allGasIsUsed) {
        if (allGasIsUsed) {
            Log.e(LOG_TAG, toCreationInfoString(m) + " did not create the contract and consumed all the gas. Holding the tx status for later investigation.");
            m.status = TeambrellaModel.USER_MULTISIG_STATUS_CREATION_FAILED;

        } else if (receipt.error != null) {
            Log.e(LOG_TAG, toCreationInfoString(m) + " denied. Resetting tx and mark for retry. Error was: " + receipt.error.toString());
            m.unconfirmed = null;
            m.creationTx = null;

        }
    }

    boolean deposit(Multisig multisig) throws CryptoException, RemoteException {

        EtherNode blockchain = new EtherNode(mIsTestNet);
        BigDecimal gasWalletAmount = getBalance();

        if (gasWalletAmount.compareTo(MAX_GAS_WALLET_BALANCE) > 0) {

            BigDecimal value = gasWalletAmount.subtract(MIN_GAS_WALLET_BALANCE, MathContext.UNLIMITED);
            org.ethereum.geth.Transaction depositTx;
            depositTx = mEtherAcc.newDepositTx(getMyNonce(), 50_000L, multisig.address, getGasPrice(), value);
            depositTx = mEtherAcc.signTx(depositTx, mIsTestNet);
            String txHash = publish(depositTx);
            if (txHash != null) {
                mBalance = mBalance.subtract(value, MathContext.UNLIMITED);    // neglect gas cost; estimate value only. Until tx is mined rough balance estimate is ok..
            }
        }

        return true;
    }


    byte[] cosign(Tx tx, TxInput payOrMoveFrom) throws CryptoException {
        switch (tx.kind) {
            case TeambrellaModel.TX_KIND_MOVE_TO_NEXT_WALLET:
                return cosignMove(tx, payOrMoveFrom);
            default:
                return cosignPay(tx, payOrMoveFrom);
        }
    }

    byte[] cosignPay(Tx tx, TxInput payFrom) throws CryptoException {

        int opNum = payFrom.previousTxIndex + 1;

        Multisig sourceMultisig = tx.getFromMultisig();
        long teamId = sourceMultisig.teamId;

        String[] payToAddresses = toAddresses(tx.txOutputs);
        String[] payToValues = toValues(tx.txOutputs);

        byte[] h = getHashForPaySignature(teamId, opNum, payToAddresses, payToValues);
        Log.v(LOG_TAG, "Hash created for Tx transfer(s): " + Hex.fromBytes(h));

        byte[] sig = mEtherAcc.signHashAndCalculateV(h);
        Log.v(LOG_TAG, "Hash signed.");

        return sig;
    }

    byte[] cosignMove(Tx tx, TxInput moveFrom) throws CryptoException {

        int opNum = moveFrom.previousTxIndex + 1;

        Multisig sourceMultisig = tx.getFromMultisig();
        long teamId = sourceMultisig.teamId;

        String[] nextCosignerAddresses = toCosignerAddresses(tx.getToMultisig());
        byte[] h = getHashForMoveSignature(teamId, opNum, nextCosignerAddresses);
        Log.v(LOG_TAG, "Hash created for Tx transfer(s): " + Hex.fromBytes(h));

        byte[] sig = mEtherAcc.signHashAndCalculateV(h);
        Log.v(LOG_TAG, "Hash signed.");

        return sig;
    }

    public String publish(Tx tx) throws CryptoException, RemoteException {
        switch (tx.kind) {
            case TeambrellaModel.TX_KIND_MOVE_TO_NEXT_WALLET:
                return publishMove(tx);
            default:
                return publishPay(tx);
        }
    }

    private String publishPay(Tx tx) throws CryptoException, RemoteException {

        List<TxInput> inputs = tx.txInputs;
        if (inputs.size() != 1) {
            String msg = "Unexpected count of tx inputs of ETH tx. Expected: 1, was: " + inputs.size() + ". (tx ID: " + tx.id + ")";
            Log.e(LOG_TAG, msg);
            if (!BuildConfig.DEBUG) {
                Crashlytics.log(msg);
            }

            return null;
        }

        Multisig myMultisig = tx.getFromMultisig();
        long myNonce = getMyNonce();
        long gasLimit = 100_000L + 30_000L * tx.txOutputs.size();
        long gasPrice = getGasPriceForClaim();
        String multisigAddress = myMultisig.address;
        String methodId = METHOD_ID_TRANSFER;

        TxInput payFrom = tx.txInputs.get(0);
        int opNum = payFrom.previousTxIndex + 1;
        String[] payToAddresses = toAddresses(tx.txOutputs);
        String[] payToValues = toValues(tx.txOutputs);

        int[] pos = new int[3];
        byte[][] sig = new byte[3][];
        sig[0] = sig[1] = sig[2] = new byte[0];
        Map<Long, TXSignature> txSignatures = payFrom.signatures;
        int index = 0, j = 0;
        for (Cosigner cos : tx.cosigners) {
            if (txSignatures.containsKey(cos.teammateId)) {
                TXSignature s = txSignatures.get(cos.teammateId);
                pos[j] = index;
                sig[j] = s.bSignature;

                if (++j >= 3) {
                    break;
                }
            }

            index++;
        }
        if (j < txSignatures.size() && j < 3) {
            Log.reportNonFatal(LOG_TAG, new EtherWalletException("tx was skipped. One or more signatures are not from a valid cosigner. Total signatures: " + txSignatures.size() + ". Valid signatures: " + j +
                    ". pos[0]: " + pos[0] + "" + ". pos[1]: " + pos[1] + ". pos[2]: " + pos[2] + ". Tx.id: " + tx.id));
            return null;
        }

        Transaction cryptoTx = mEtherAcc.newMessageTx(myNonce, gasLimit, multisigAddress, gasPrice, methodId, opNum, payToAddresses, payToValues, pos[0], pos[1], pos[2], sig[0], sig[1], sig[2]);
        if (cryptoTx == null) {
            Log.w(LOG_TAG, "tx was skipped. Seek details in the log above. Tx.id: " + tx.id);
            return null;
        }

        try {
            Log.v(LOG_TAG, "tx created: " + cryptoTx.encodeJSON());
        } catch (Exception e) {
            Log.e(LOG_TAG, "could not encode JSON to log tx: " + e.getMessage(), e);
        }

        cryptoTx = mEtherAcc.signTx(cryptoTx, mIsTestNet);
        Log.v(LOG_TAG, "tx signed.");

        return publish(cryptoTx);
    }

    private String publishMove(Tx tx) throws CryptoException, RemoteException {

        List<TxInput> inputs = tx.txInputs;
        if (inputs.size() != 1) {
            String msg = "Unexpected count of move tx inputs of ETH tx. Expected: 1, was: " + inputs.size() + ". (tx ID: " + tx.id + ")";
            Log.e(LOG_TAG, msg);
            if (!BuildConfig.DEBUG) {
                Crashlytics.log(msg);
            }

            return null;
        }

        Multisig myMultisig = tx.getFromMultisig();
        long myNonce = getMyNonce();
        long gasLimit = 200_000L;
        long gasPrice = getGasPrice();
        String multisigAddress = myMultisig.address;
        String methodId = METHOD_ID_CHANGEALLCOSIGNERS;

        TxInput moveFrom = tx.txInputs.get(0);
        int opNum = moveFrom.previousTxIndex + 1;
        String[] nextCosignerAddresses = toCosignerAddresses(tx.getToMultisig());

        int[] pos = new int[3];
        byte[][] sig = new byte[3][];
        sig[0] = sig[1] = sig[2] = new byte[0];
        Map<Long, TXSignature> txSignatures = moveFrom.signatures;
        int index = 0, j = 0;
        for (Cosigner cos : tx.cosigners) {
            if (txSignatures.containsKey(cos.teammateId)) {
                TXSignature s = txSignatures.get(cos.teammateId);
                pos[j] = index;
                sig[j] = s.bSignature;

                if (++j >= 3) {
                    break;
                }
            }

            index++;
        }
        if (j < txSignatures.size() && j < 3) {
            Log.reportNonFatal(LOG_TAG, new EtherWalletException("tx was skipped. One or more signatures are not from a valid cosigner. Total signatures: " + txSignatures.size() + ". Valid signatures: " + j +
                    ". pos[0]: " + pos[0] + "" + ". pos[1]: " + pos[1] + ". pos[2]: " + pos[2] + ". Tx.id: " + tx.id));
            return null;
        }

        Transaction cryptoTx = mEtherAcc.newMessageTx(myNonce, gasLimit, multisigAddress, gasPrice, methodId, opNum, nextCosignerAddresses, pos[0], pos[1], pos[2], sig[0], sig[1], sig[2]);
        if (cryptoTx == null) {
            Log.w(LOG_TAG, "move tx was skipped. Seek details in the log above. Tx.id: " + tx.id);
            return null;
        }

        try {
            Log.v(LOG_TAG, "move tx created: " + cryptoTx.encodeJSON());
        } catch (Exception e) {
            Log.e(LOG_TAG, "could not encode JSON to log move tx: " + e.getMessage(), e);
        }

        cryptoTx = mEtherAcc.signTx(cryptoTx, mIsTestNet);
        Log.v(LOG_TAG, "move tx signed.");

        return publish(cryptoTx);
    }

    long refreshGasPrice() {
        EtherGasStation gasStation = new EtherGasStation(mIsTestNet);
        long price = gasStation.checkGasPrice();
        if (price < 0) {
            Log.reportNonFatal(LOG_TAG, new EtherWalletException("Failed to get the gas price from a server. A default gas price will be used."));
            return 100_000_001L;  // 0.1 Gwei is enough since October 16, 2017 (1 Gwei = 10^9 wei)
        } else if (price > 50_000_000_001L) {
            Log.reportNonFatal(LOG_TAG, new EtherWalletException("The server is kidding with us about the gas price: " + price));
            // The server is kidding with us
            return 50_000_000_001L;
        }

        if (mIsTestNet) {
            return mTestGasPrice = price;
        }

        return mGasPrice = price;
    }

    long refreshClaimGasPrice() {
        EtherGasStation gasStation = new EtherGasStation(mIsTestNet);
        long price = gasStation.checkGasPrice();
        if (price < 0) {
            Log.reportNonFatal(LOG_TAG, new EtherWalletException("Failed to get the gas price from a server. A default gas price will be used."));
            return 1_000_000_001L;  // 1 Gwei or more is required since Dec, 2017 (1 Gwei = 10^9 wei)
        } else if (price > 4_000_000_001L) {
            Log.reportNonFatal(LOG_TAG, new EtherWalletException("With the current version gas price for a clime is limited. This high price will be supported later (when off-chain payments are implemented) : " + price));
            // The server is kidding with us
            return 4_000_000_001L;
        }

        if (mIsTestNet) {
            return mTestClaimGasPrice = price;
        }

        return mClaimGasPrice = price;
    }

    private long refreshContractCreateGasPrice() {
        EtherGasStation gasStation = new EtherGasStation(mIsTestNet);
        long price = gasStation.checkContractCreationGasPrice();
        if (price < 0) {
            Log.reportNonFatal(LOG_TAG, new EtherWalletException("Failed to get the contract gas price from a server. A default contract gas price will be used."));
            return 100_000_001L;
        } else if (price > 8_000_000_002L) {
            Log.reportNonFatal(LOG_TAG, new EtherWalletException("The server is kidding with us about the contract gas price: " + price));
            // The server is kidding with us
            return 8_000_000_002L;
        }


        if (mIsTestNet) {
            return mTestContractGasPrice = price;
        }

        return mContractGasPrice = price;
    }

    private long getGasPrice() {
        if (mIsTestNet) {
            return mTestGasPrice < 0 ? refreshGasPrice() : mTestGasPrice;
        }
        return mGasPrice < 0 ? refreshGasPrice() : mGasPrice;
    }

    private long getGasPriceForClaim() {
        if (mIsTestNet) {
            return mTestClaimGasPrice < 0 ? refreshClaimGasPrice() : mTestClaimGasPrice;
        }
        return mClaimGasPrice < 0 ? refreshClaimGasPrice() : mClaimGasPrice;
    }

    long getGasPriceForContractCreation() {
        if (mIsTestNet) {
            return mTestContractGasPrice < 0 ? refreshContractCreateGasPrice() : mTestContractGasPrice;
        }
        return mContractGasPrice < 0 ? refreshContractCreateGasPrice() : mContractGasPrice;
    }


    long refreshMyNonce() {
        EtherNode blockchain = new EtherNode(mIsTestNet);
        return mNonce = blockchain.checkNonce(mEtherAcc.getDepositAddress());
    }

    long getMyNonce() {
        return mNonce < 0 ? refreshMyNonce() : mNonce;
    }

    BigDecimal refreshBalance() {
        EtherNode blockchain = new EtherNode(mIsTestNet);
        return mBalance = blockchain.checkBalance(mEtherAcc.getDepositAddress());
    }

    BigDecimal getBalance() {
        return (mBalance.compareTo(BigDecimal.ZERO) < 0) ? refreshBalance() : mBalance;
    }

    private long getBetterGasPriceForContractCreation(long oldPrice) {

        long betterPrice = oldPrice + 1;
        long recommendedPrice = refreshContractCreateGasPrice();

        if (recommendedPrice > betterPrice) {
            betterPrice = recommendedPrice;
        }

        return betterPrice;
    }

    private static BigDecimal dec(long val) {
        return new BigDecimal(val, MathContext.UNLIMITED);
    }

    private static BigDecimal eth(long weis) {
        return new BigDecimal(weis, MathContext.UNLIMITED).divide(AbiArguments.WEIS_IN_ETH);
    }

    private String publish(Transaction cryptoTx) throws RemoteException {
        try {
            byte[] rlp = cryptoTx.encodeRLP();
            Log.v(LOG_TAG, "Publishing 'Multisig creation' tx:" + cryptoTx.getHash().getHex() + " " + cryptoTx.encodeJSON());

            String hex = "0x" + Hex.fromBytes(rlp);

            EtherNode blockchain = new EtherNode(mIsTestNet);
            String txHash = blockchain.pushTx(hex);
            if (txHash != null) {
                mNonce++;
            }
            return txHash;


        } catch (Exception e) {
            Log.e(LOG_TAG, "" + e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        }
    }

    private String[] toAddresses(List<TxOutput> destinations) {

        int n = destinations.size();
        String[] destinationAddresses = new String[n];

        for (int i = 0; i < n; i++) {
            destinationAddresses[i] = destinations.get(i).address;
            sanityAddressCheck(destinationAddresses[i]);
        }

        return destinationAddresses;
    }

    private String[] toCosignerAddresses(Multisig nextMultisig) {

        if (nextMultisig == null) throw new IllegalArgumentException("nextMultisig is null");

        List<Cosigner> cosigners = nextMultisig.cosigners;
        int n = cosigners.size();
        String[] cosignerAddresses = new String[n];

        for (int i = 0; i < n; i++) {
            cosignerAddresses[i] = cosigners.get(i).publicKeyAddress;
            sanityAddressCheck(cosignerAddresses[i]);
        }

        return cosignerAddresses;
    }

    private String[] toValues(List<TxOutput> destinations) {

        int n = destinations.size();
        String[] destinationValues = new String[n];

        for (int i = 0; i < n; i++) {
            destinationValues[i] = AbiArguments.parseDecimalAmount(destinations.get(i).cryptoAmount);
        }

        return destinationValues;
    }

    private byte[] getHashForPaySignature(long teamId, int opNum, String[] addresses, String[] values) {

        String a0 = TX_PREFIX; // prefix, that is used in the contract to indicate a signature for transfer tx
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

    private byte[] getHashForMoveSignature(long teamId, int opNum, String[] addresses) {

        String a0 = NS_PREFIX; // prefix, that is used in the contract to indicate a signature for move tx.
        String a1 = String.format("%064x", teamId);
        String a2 = String.format("%064x", opNum);
        int n = addresses.length;
        String[] a3 = new String[n];
        for (int i = 0; i < n; i++) {
            a3[i] = Hex.remove0xPrefix(addresses[i]);
        }

        byte[] data = com.teambrella.android.blockchain.Hex.toBytes(a0, a1, a2, a3);
        return Sha3.getKeccak256Hash(data);
    }

    private static void sanityAddressCheck(String a) {
        if (null == a) throw new IllegalArgumentException("address is null");

        if (!Pattern.matches("^0x[a-fA-F0-9]{40}$", a))
            throw new IllegalArgumentException("expected ETH address of 40 hex chars, but was: " + a);
    }

    private static String toCreationInfoString(Multisig m) {
        if (null == m) return "null";

        return toCreationInfoString(m.teamId, m.creationTx);
    }

    private static String toCreationInfoString(long teamId, String creationTx) {
        return String.format("'Multisig creation(teamId=%s)' tx:%s", teamId, creationTx);
    }


    public static class EtherWalletException extends Exception {
        public EtherWalletException(String message) {
            super(message);
        }
    }

}

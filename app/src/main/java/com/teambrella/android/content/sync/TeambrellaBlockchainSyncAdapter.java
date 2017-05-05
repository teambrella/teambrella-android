package com.teambrella.android.content.sync;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import com.subgraph.orchid.encoders.Hex;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaContentProviderClient;
import com.teambrella.android.content.TeambrellaRepository;
import com.teambrella.android.content.model.BTCAddress;
import com.teambrella.android.content.model.Cosigner;
import com.teambrella.android.content.model.Teammate;
import com.teambrella.android.content.model.Tx;
import com.teambrella.android.content.model.TxInput;
import com.teambrella.android.content.model.TxOutput;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Blockchain sync adapter
 */
class TeambrellaBlockchainSyncAdapter {

    private static final String LOG_TAG = TeambrellaBlockchainSyncAdapter.class.getSimpleName();

    private SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    {
        mSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    void onPerformSync(Context context, ContentProviderClient provider) {
        try {
            cosignApprovedTransactions(provider);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void cosignApprovedTransactions(ContentProviderClient client) throws RemoteException, OperationApplicationException {
        TeambrellaContentProviderClient tbClient = new TeambrellaContentProviderClient(client);
        List<Tx> list = getCosinableTx(tbClient);
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        if (list != null) {
            for (Tx tx : list) {
                Transaction transaction = getTransaction(tx);
                if (transaction != null) {
                    BTCAddress address = tx.getFromAddress();
                    if (address != null) {
                        List<Cosigner> cosigners = getCosigners(tbClient, address);
                        Script redeemScript = getRedeemScript(address, cosigners);
                        Collections.sort(tx.txInputs, new Comparator<TxInput>() {
                            @Override
                            public int compare(TxInput o1, TxInput o2) {
                                return o1.id.compareTo(o2.id);
                            }
                        });
                        for (int i = 0; i < tx.txInputs.size(); i++) {
                            byte[] signature = cosign(redeemScript, transaction, i);
                            operations.add(addSignature(tx.txInputs.get(i).id.toString(), tx.teammateId, signature));
                        }
                    }
                }
                operations.add(setTxSigned(tx));
            }
        }
        client.applyBatch(operations);
    }


    private void piblishApprovedAndCosignedTxs(ContentProviderClient client) throws RemoteException, OperationApplicationException {
        TeambrellaContentProviderClient tbClient = new TeambrellaContentProviderClient(client);
        List<Tx> txs = getApprovedAndCosignedTxs(tbClient);
        for (Tx tx : txs) {
            Transaction transaction = getTransaction(tx);
            BTCAddress fromAddress = tx.getFromAddress();
            List<Cosigner> cosigners = getCosigners(tbClient, fromAddress);
            Collections.sort(cosigners, new Comparator<Cosigner>() {
                @Override
                public int compare(Cosigner o1, Cosigner o2) {
                    return Integer.valueOf(o1.keyOrder).compareTo(o2.keyOrder);
                }
            });
            Script script = getRedeemScript(tx.getFromAddress(), cosigners);
            ArrayList<ScriptBuilder> ops = new ArrayList<>();

            for (int i = 0; i < tx.txInputs.size(); i++) {
                ScriptBuilder builder = new ScriptBuilder();
                builder.op(ScriptOpCodes.OP_0);
                ops.add(builder);
            }
        }
    }


    private Transaction getTransaction(final Tx tx) {
        NetworkParameters params = new TestNet3Params();
        final float normalFeeBTC = 0.0001f;
        float totalBTCAmount = 0f;
        Transaction transaction = null;
        if (tx.txInputs != null) {
            Collections.sort(tx.txInputs, new Comparator<TxInput>() {
                @Override
                public int compare(TxInput o1, TxInput o2) {
                    return o1.id.compareTo(o2.id);
                }
            });

            transaction = new Transaction(params);

            for (TxInput txInput : tx.txInputs) {
                totalBTCAmount += txInput.btcAmount;
                TransactionOutPoint outpoint = new TransactionOutPoint(params, txInput.previousTxIndex,
                        Sha256Hash.wrap(txInput.previousTxId));
                transaction.addInput(new TransactionInput(params, transaction, new byte[0], outpoint))
                ;
            }

            //totalBTCAmount -= tx.FeeBtc ?? NormalFeeBTC;

            if (totalBTCAmount < tx.btcAmount) {
                return null;
            }

            if (tx.kind == TeambrellaModel.TX_KIND_PAYOUT || tx.kind == TeambrellaModel.TX_KIND_WITHDRAW) {
                Collections.sort(tx.txOutputs, new Comparator<TxOutput>() {
                    @Override
                    public int compare(TxOutput o1, TxOutput o2) {
                        return o1.id.compareTo(o2.id);
                    }
                });

                float outputSum = 0f;

                for (TxOutput txOutput : tx.txOutputs) {
                    Address address = Address.fromBase58(params, txOutput.address);
                    transaction.addOutput(new TransactionOutput(params, null, Coin.parseCoin(Float.toString(txOutput.btcAmount)), address));
                    outputSum += txOutput.btcAmount;
                }

                float changeAmount = totalBTCAmount - outputSum;

                if (changeAmount > normalFeeBTC) {
                    BTCAddress current = tx.teammate.getCurrentAddress();
                    if (current != null) {
                        transaction.addOutput(new TransactionOutput(params, null, Coin.parseCoin(Float.toString(changeAmount)),
                                Address.fromBase58(params, current.address)));
                    }
                } else if (tx.kind == TeambrellaModel.TX_KIND_MOVE_TO_NEXT_WALLET) {
                    BTCAddress next = tx.teammate.getNextAddress();
                    if (next != null) {
                        transaction.addOutput(new TransactionOutput(params, null, Coin.parseCoin(Float.toString(totalBTCAmount)),
                                Address.fromBase58(params, next.address)));
                    }
                } else if (tx.kind == TeambrellaModel.TX_KIND_SAVE_FROM_PREV_WALLLET) {
                    BTCAddress current = tx.teammate.getCurrentAddress();
                    if (current != null) {
                        transaction.addOutput(new TransactionOutput(params, null, Coin.parseCoin(Float.toString(totalBTCAmount)),
                                Address.fromBase58(params, current.address)));
                    }
                }
            }
        }

        return transaction;
    }


    private static List<Tx> getCosinableTx(TeambrellaContentProviderClient client) throws RemoteException {
        List<Tx> list = client.queryList(TeambrellaRepository.Tx.CONTENT_URI, TeambrellaRepository.Tx.RESOLUTION + "=? AND "
                + TeambrellaRepository.Tx.STATE + "=?", new String[]{Integer.toString(TeambrellaModel.TX_CLIENT_RESOLUTION_APPROVED),
                Integer.toString(TeambrellaModel.TX_STATE_SELECTED_FOR_COSIGNING)}, Tx.class);
        Iterator<Tx> iterator = list != null ? list.iterator() : null;
        if (iterator != null) {
            while (iterator.hasNext()) {
                Tx tx = iterator.next();
                tx.txInputs = client.queryList(TeambrellaRepository.TXInput.CONTENT_URI, TeambrellaRepository.TXInput.TX_ID + "=?", new String[]{tx.id.toString()}, TxInput.class);
                if (tx.txInputs == null || tx.txInputs.isEmpty()) {
                    iterator.remove();
                } else {
                    tx.txOutputs = client.queryList(TeambrellaRepository.TXOutput.CONTENT_URI,
                            TeambrellaRepository.TXOutput.TX_ID + "=?", new String[]{tx.id.toString()}, TxOutput.class);
                    tx.teammate = client.queryOne(TeambrellaRepository.Teammate.CONTENT_URI,
                            TeambrellaRepository.TEAMMATE_TABLE + "." + TeambrellaRepository.Teammate.ID + "=?", new String[]{Long.toString(tx.teammateId)}, Teammate.class);
                    if (tx.teammate != null) {
                        tx.teammate.addresses = client.queryList(TeambrellaRepository.BTCAddress.CONTENT_URI, TeambrellaRepository.BTCAddress.TEAMMATE_ID + "=?"
                                , new String[]{Long.toString(tx.teammate.id)}, BTCAddress.class);
                    }
                }
            }
        }
        return list;
    }

    private static List<Tx> getApprovedAndCosignedTxs(TeambrellaContentProviderClient client) throws RemoteException {
        List<Tx> list = client.queryList(TeambrellaRepository.Tx.CONTENT_URI, TeambrellaRepository.Tx.RESOLUTION + "=? AND "
                + TeambrellaRepository.Tx.STATE + "= ?", new String[]{Integer.toString(TeambrellaModel.TX_CLIENT_RESOLUTION_APPROVED),
                Integer.toString(TeambrellaModel.TX_STATE_COSIGNED)}, Tx.class);
        Iterator<Tx> iterator = list != null ? list.iterator() : null;
        if (iterator != null) {
            while (iterator.hasNext()) {
                Tx tx = iterator.next();
                tx.txInputs = client.queryList(TeambrellaRepository.TXInput.CONTENT_URI, TeambrellaRepository.TXInput.TX_ID + "=?", new String[]{tx.id.toString()}, TxInput.class);
                if (tx.txInputs == null || tx.txInputs.isEmpty()) {
                    iterator.remove();
                } else {
                    tx.txOutputs = client.queryList(TeambrellaRepository.TXOutput.CONTENT_URI,
                            TeambrellaRepository.TXOutput.TX_ID + "=?", new String[]{tx.id.toString()}, TxOutput.class);
                    tx.teammate = client.queryOne(TeambrellaRepository.Teammate.CONTENT_URI,
                            TeambrellaRepository.Teammate.ID + "=?", new String[]{Long.toString(tx.teammateId)}, Teammate.class);
                    if (tx.teammate != null) {
                        tx.teammate.addresses = client.queryList(TeambrellaRepository.BTCAddress.CONTENT_URI, TeambrellaRepository.BTCAddress.TEAMMATE_ID + "=?"
                                , new String[]{Long.toString(tx.teammate.id)}, BTCAddress.class);
                    }
                }
            }
        }
        return list;
    }


    private static List<Cosigner> getCosigners(TeambrellaContentProviderClient client, BTCAddress btcAddress) throws RemoteException {
        return client.queryList(TeambrellaRepository.Cosigner.CONTENT_URI, TeambrellaRepository.Cosigner.ADDRESS_ID + "=?",
                new String[]{btcAddress.address}, Cosigner.class);
    }


    private static Script getRedeemScript(BTCAddress btcAddress, List<Cosigner> cosigners) {
        ScriptBuilder builder = new ScriptBuilder();
        builder.data(Hex.decode(btcAddress.teammatePublicKey)).op(ScriptOpCodes.OP_CHECKMULTISIGVERIFY);
        int size = cosigners.size();
        if (size > 6) {
            builder.op(ScriptOpCodes.OP_3);
        } else if (size > 3) {
            builder.op(ScriptOpCodes.OP_2);
        } else if (size > 0) {
            builder.op(ScriptOpCodes.OP_1);
        } else {
            builder.op(ScriptOpCodes.OP_0);
        }
        for (Cosigner cosigner : cosigners) {
            builder.data(Hex.decode(cosigner.publicKey));
        }
        builder.op(ScriptOpCodes.OP_RESERVED + size);
        builder.op(ScriptOpCodes.OP_CHECKMULTISIG);
        builder.data(new BigInteger(btcAddress.teamId).toByteArray());
        builder.op(ScriptOpCodes.OP_DROP);
        return builder.build();
    }

    private static byte[] cosign(Script redeemScript, Transaction transaction, int inputNum) {
        DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(null, "cNqQ7aZWitJCk1o9dNhr1o9k3UKdeW92CDYrvDHHLuwFuEnfcBXo");
        ECKey key = dpk.getKey();
        Sha256Hash hash = transaction.hashForSignature(inputNum, redeemScript, Transaction.SigHash.ALL, false);
        return key.sign(hash).encodeToDER();
    }

    private static ContentProviderOperation addSignature(String txInputId, long teammateId, byte[] signature) {
        return ContentProviderOperation.newInsert(TeambrellaRepository.TXSignature.CONTENT_URI)
                .withValue(TeambrellaRepository.TXSignature.ID, UUID.randomUUID().toString())
                .withValue(TeambrellaRepository.TXSignature.TX_INPUT_ID, txInputId)
                .withValue(TeambrellaRepository.TXSignature.TEAMMATE_ID, teammateId)
                .withValue(TeambrellaRepository.TXSignature.SIGNATURE, signature)
                .withValue(TeambrellaRepository.TXSignature.NEED_UPDATE_SERVER, true)
                .build();
    }

    private ContentProviderOperation setTxSigned(Tx tx) {
        return ContentProviderOperation.newUpdate(TeambrellaRepository.Tx.CONTENT_URI)
                .withValue(TeambrellaRepository.Tx.RESOLUTION, TeambrellaModel.TX_CLIENT_RESOLUTION_SIGNED)
                .withValue(TeambrellaRepository.Tx.NEED_UPDATE_SERVER, true)
                .withValue(TeambrellaRepository.Tx.CLIENT_RESOLUTION_TIME, mSDF.format(new Date()))
                .withSelection(TeambrellaRepository.Tx.ID + "=?", new String[]{tx.id.toString()})
                .build();
    }
}

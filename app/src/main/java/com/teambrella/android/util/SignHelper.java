package com.teambrella.android.util;

import com.subgraph.orchid.encoders.Hex;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.model.Multisig;
import com.teambrella.android.content.model.Cosigner;
import com.teambrella.android.content.model.TXSignature;
import com.teambrella.android.content.model.Tx;
import com.teambrella.android.content.model.TxInput;
import com.teambrella.android.content.model.TxOutput;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.List;

/**
 * Sign Helper
 */
class SignHelper {

    private static final String LOG_TAG = SignHelper.class.getSimpleName();


    static Script getRedeemScript(Multisig multisig, List<Cosigner> cosigners) {
        ScriptBuilder builder = new ScriptBuilder();
        builder.data(Hex.decode(multisig.teammatePublicKey)).op(ScriptOpCodes.OP_CHECKSIGVERIFY);
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
        builder.number(multisig.teamId);
        builder.op(ScriptOpCodes.OP_DROP);
        return builder.build();
    }

    static Transaction getTransaction(final Tx tx) {
        NetworkParameters params = new TestNet3Params();
        BigDecimal totalBTCAmount = new BigDecimal(0, MathContext.UNLIMITED);
        Transaction transaction = null;
        if (tx.txInputs != null) {
            Collections.sort(tx.txInputs);

            transaction = new Transaction(params);

            for (TxInput txInput : tx.txInputs) {
                totalBTCAmount = totalBTCAmount.add(new BigDecimal(txInput.cryptoAmount, MathContext.UNLIMITED));
                TransactionOutPoint outpoint = new TransactionOutPoint(params, txInput.previousTxIndex,
                        Sha256Hash.wrap(txInput.previousTxId));
                transaction.addInput(new TransactionInput(params, transaction, new byte[0], outpoint))
                ;
            }

            totalBTCAmount = totalBTCAmount.subtract(new BigDecimal("0.0001", MathContext.UNLIMITED));

            if (totalBTCAmount.compareTo(new BigDecimal(tx.cryptoAmount)) == -1) {
                return null;
            }

            if (tx.kind == TeambrellaModel.TX_KIND_PAYOUT || tx.kind == TeambrellaModel.TX_KIND_WITHDRAW) {
                Collections.sort(tx.txOutputs);

                BigDecimal outputSum = new BigDecimal(0f, MathContext.UNLIMITED);

                for (TxOutput txOutput : tx.txOutputs) {
                    Address address = Address.fromBase58(params, txOutput.address);
                    transaction.addOutput(new TransactionOutput(params, null, Coin.parseCoin(new BigDecimal(txOutput.cryptoAmount).toString()), address));
                    outputSum = outputSum.add(new BigDecimal(txOutput.cryptoAmount));
                }

                BigDecimal changeAmount = totalBTCAmount.subtract(outputSum);

                if (changeAmount.compareTo(new BigDecimal("0.0001", MathContext.UNLIMITED)) == 1) {
                    Multisig current = tx.teammate.getCurrentAddress();
                    if (current != null) {
                        transaction.addOutput(new TransactionOutput(params, null, Coin.parseCoin(changeAmount.toString()),
                                Address.fromBase58(params, current.address)));
                    }
                } else if (tx.kind == TeambrellaModel.TX_KIND_MOVE_TO_NEXT_WALLET) {
                    Multisig next = tx.teammate.getNextAddress();
                    if (next != null) {
                        transaction.addOutput(new TransactionOutput(params, null, Coin.parseCoin(totalBTCAmount.toString()),
                                Address.fromBase58(params, next.address)));
                    }
                } else if (tx.kind == TeambrellaModel.TX_KIND_SAVE_FROM_PREV_WALLLET) {
                    Multisig current = tx.teammate.getCurrentAddress();
                    if (current != null) {
                        transaction.addOutput(new TransactionOutput(params, null, Coin.parseCoin(totalBTCAmount.toString()),
                                Address.fromBase58(params, current.address)));
                    }
                }
            }
        }

        return transaction;
    }


    static Transaction getTransactionToPublish(Tx tx) {

        Transaction transaction = getTransaction(tx);

        if (transaction == null) {
            return null;
        }

        Script script = getRedeemScript(tx.getFromMultisig(), tx.cosigners);
        ScriptBuilder[] ops = new ScriptBuilder[tx.txInputs.size()];

        for (Cosigner cosigner : tx.cosigners) {
            for (int i = 0; i < tx.txInputs.size(); i++) {
                TxInput txInput = tx.txInputs.get(i);
                TXSignature signature = txInput.signatures.get(cosigner.teammateId);
                if (signature == null) {
                    break;
                }

                if (ops[i] == null) {
                    ScriptBuilder builder = new ScriptBuilder();
                    builder.addChunk(new ScriptChunk(ScriptOpCodes.OP_0, null));
                    ops[i] = builder;
                }


                byte[] data = new byte[signature.bSignature.length + 1];
                System.arraycopy(signature.bSignature, 0, data, 0, signature.bSignature.length);
                data[signature.bSignature.length] = Transaction.SigHash.ALL.byteValue();
                ops[i].data(data);
            }
        }

        for (int i = 0; i < tx.txInputs.size(); i++) {
            TXSignature txSignature = tx.txInputs.get(i).signatures.get(tx.teammateId);
            byte[] vSignature = new byte[txSignature.bSignature.length + 1];
            System.arraycopy(txSignature.bSignature, 0, vSignature, 0, txSignature.bSignature.length);
            vSignature[txSignature.bSignature.length] = Transaction.SigHash.ALL.byteValue();
            ops[i].data(vSignature);
            ops[i].data(script.getProgram());
            transaction.getInput(i).setScriptSig(ops[i].build());
        }

        return transaction;
    }


}

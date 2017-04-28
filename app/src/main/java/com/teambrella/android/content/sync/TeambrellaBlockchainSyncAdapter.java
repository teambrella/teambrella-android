package com.teambrella.android.content.sync;

import android.content.ContentProviderClient;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaContentProviderClient;
import com.teambrella.android.content.TeambrellaRepository;
import com.teambrella.android.content.model.BTCAddress;
import com.teambrella.android.content.model.Teammate;
import com.teambrella.android.content.model.Tx;
import com.teambrella.android.content.model.TxInput;
import com.teambrella.android.content.model.TxOutput;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.params.TestNet3Params;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Blockchain sync adapter
 */
class TeambrellaBlockchainSyncAdapter {


    void onPerformSync(Context context, ContentProviderClient provider) {
        try {
            cosignApprovedTransactions(provider);
        } catch (Exception e) {
            Log.e("sdsdsdds", e.toString());
        }
    }


    private void cosignApprovedTransactions(ContentProviderClient client) throws RemoteException, OperationApplicationException {
        TeambrellaContentProviderClient tbClient = new TeambrellaContentProviderClient(client);
        List<Tx> list = getCosinableTx(tbClient);
        if (list != null) {
            for (Tx tx : list) {
                getTransaction(tx);
            }
        }
    }


    private Transaction getTransaction(final Tx tx) {
        NetworkParameters params = new TestNet3Params();
        float totalBTCAmount = 0f;
        if (tx.txInputs != null) {
            Collections.sort(tx.txInputs, new Comparator<TxInput>() {
                @Override
                public int compare(TxInput o1, TxInput o2) {
                    return o1.id.compareTo(o2.id);
                }
            });
            Transaction transaction = new Transaction(params);
            for (TxInput txInput : tx.txInputs) {
                totalBTCAmount += txInput.btcAmount;
                TransactionOutPoint outpoint = new TransactionOutPoint(params, txInput.previousTxIndex,
                        Sha256Hash.wrap(txInput.previousTxId));
                transaction.addInput(new TransactionInput(params, transaction, null, outpoint));
            }

            //totalBTCAmount -= tx.FeeBtc ?? NormalFeeBTC;

            if (totalBTCAmount < tx.btcAmount) {
                return null;
            }

            if (tx.kind == TeambrellaModel.TX_KIND_PAYOUT || tx.kind == TeambrellaModel.TX_KIND_WITHDRAW) {
                Collections.sort(tx.txOutOut, new Comparator<TxOutput>() {
                    @Override
                    public int compare(TxOutput o1, TxOutput o2) {
                        return o1.id.compareTo(o2.id);
                    }
                });

                float outputAmount = 0f;


            }


        }

        return null;
    }


    private static List<Tx> getCosinableTx(TeambrellaContentProviderClient client) throws RemoteException {
        List<Tx> list = client.queryList(TeambrellaRepository.Tx.CONTENT_URI, TeambrellaRepository.Tx.RESOLUTION + "=? AND "
                + TeambrellaRepository.Tx.STATE + "= ?", new String[]{Integer.toString(TeambrellaModel.TX_CLIENT_RESOLUTON_APPROVED),
                Integer.toString(TeambrellaModel.TX_STATE_SELECTED_FOR_COSIGNING)}, Tx.class);
        Iterator<Tx> iterator = list != null ? list.iterator() : null;
        if (iterator != null) {
            while (iterator.hasNext()) {
                Tx tx = iterator.next();
                tx.txInputs = client.queryList(TeambrellaRepository.TXInput.CONTENT_URI, TeambrellaRepository.TXInput.TX_ID + "=?", new String[]{tx.id.toString()}, TxInput.class);
                if (tx.txInputs == null || tx.txInputs.isEmpty()) {
                    iterator.remove();
                } else {
                    tx.txOutOut = client.queryList(TeambrellaRepository.TXOutput.CONTENT_URI,
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
}

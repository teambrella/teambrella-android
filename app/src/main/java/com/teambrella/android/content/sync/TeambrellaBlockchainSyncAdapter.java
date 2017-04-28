package com.teambrella.android.content.sync;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;

import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import java.util.ArrayList;

/**
 * Blockchain sync adapter
 */
class TeambrellaBlockchainSyncAdapter {


    void onPerformSync(Context context, ContentProviderClient provider) {

    }


    private void cosignApprovedTransactions(ContentProviderClient client) throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        String txSelection = TeambrellaRepository.Tx.RESOLUTION + "=? AND "
                + TeambrellaRepository.Tx.STATE + "= ?";
        String[] txSelectionArgs = new String[]{Integer.toString(TeambrellaModel.TX_CLIENT_RESOLUTON_APPROVED),
                Integer.toString(TeambrellaModel.TX_STATE_SELECTED_FOR_COSIGNING)};
        Cursor txCursor = client.query(TeambrellaRepository.Tx.CONTENT_URI, null, txSelection, txSelectionArgs, null);
        if (txCursor != null && txCursor.moveToFirst()) {
            do {
                String txInputSelection = TeambrellaRepository.TXInput.TX_ID + "=?";
                String[] txInputSelectionArgs = new String[]{txCursor.getString(txCursor.getColumnIndex(TeambrellaRepository.Tx.ID))};
                Cursor txInputCursor = client.query(TeambrellaRepository.TXInput.CONTENT_URI, null, txInputSelection, txInputSelectionArgs, null);
                if (txInputCursor != null && txInputCursor.moveToFirst()) {
                    do {

                    } while (txInputCursor.moveToNext());
                }
            } while (txCursor.moveToNext());

        }

        if (txCursor != null) {
            txCursor.close();
        }

        if (operations.size() > 0) {
            client.applyBatch(operations);
        }
    }

}

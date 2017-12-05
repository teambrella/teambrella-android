package com.teambrella.android.ui.withdraw;

import android.content.Context;
import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.data.base.TeambrellaDataPagerLoader;

/**
 * Withdrawals Data Pager Loader
 */
class WithdrawalsDataPagerLoader extends TeambrellaDataPagerLoader {


    private static final JsonObject QUEUED_HEADER_ITEM = new JsonObject();
    private static final JsonObject IN_PROCESS_HEADER_ITEM = new JsonObject();
    private static final JsonObject HISTORY_HEADER_ITEM = new JsonObject();

    static {
        QUEUED_HEADER_ITEM.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.WithdrawlsItemType.ITEM_QUEUED_HEADER);
        IN_PROCESS_HEADER_ITEM.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.WithdrawlsItemType.ITEM_IN_PROCESS_HEADER);
        HISTORY_HEADER_ITEM.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.WithdrawlsItemType.ITEM_HISTORY_HEADER);
    }

    private JsonArray mQueuedWithdrawals = new JsonArray();
    private JsonArray mInProcessWithdrawals = new JsonArray();
    private JsonArray mHistoryWithdrawals = new JsonArray();


    WithdrawalsDataPagerLoader(Context context, Uri uri, String property) {
        super(context, uri, property);
    }

    @Override
    protected void onAddNewData(JsonArray newData) {

        if (mArray.size() == 0) {
            if (mQueuedWithdrawals.size() == 0) {
                mQueuedWithdrawals.add(QUEUED_HEADER_ITEM);
            }

            if (mInProcessWithdrawals.size() == 0) {
                mInProcessWithdrawals.add(IN_PROCESS_HEADER_ITEM);
            }

            if (mHistoryWithdrawals.size() == 0) {
                mHistoryWithdrawals.add(HISTORY_HEADER_ITEM);
            }
        }

        for (JsonElement element : newData) {
            JsonObject item = element.getAsJsonObject();
            int state = item.get(TeambrellaModel.ATTR_DATA_SERVER_TX_STATE).getAsInt();
            if (state < 0) {
                item.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.WithdrawlsItemType.ITEM_QUEDUED);
                mQueuedWithdrawals.add(item);
            } else if (state < 10) {
                item.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.WithdrawlsItemType.ITEM_IN_PROCESS);
                mInProcessWithdrawals.add(item);
            } else {
                item.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.WithdrawlsItemType.ITEM_HISTORY);
                mHistoryWithdrawals.add(item);
            }
        }

        if (mQueuedWithdrawals.size() > 1) {
            mArray.addAll(mQueuedWithdrawals);
            mQueuedWithdrawals = new JsonArray();
        }

        if (mInProcessWithdrawals.size() > 1) {
            mArray.addAll(mInProcessWithdrawals);
            mInProcessWithdrawals = new JsonArray();
        }

        if (mHistoryWithdrawals.size() > 1) {
            mArray.addAll(mHistoryWithdrawals);
            mHistoryWithdrawals = new JsonArray();
        }
    }
}

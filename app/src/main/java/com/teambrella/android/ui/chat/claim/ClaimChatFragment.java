package com.teambrella.android.ui.chat.claim;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import io.reactivex.Notification;


/**
 * Claim Chat Fragment
 */
public class ClaimChatFragment extends ADataPagerProgressFragment<IDataHost> {


    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new ClaimChatAdapter(mDataHost.getPager(mTag));
    }

    @Override
    protected void onDataUpdated(Notification<JsonArray> notification) {
        super.onDataUpdated(notification);
        if (notification.isOnNext()) {
            JsonArray array = notification.getValue();
            if (array != null && array.size() > 0) {
                //mList.getLayoutManager().smoothScrollToPosition(mList, null, mAdapter.getItemCount() - 1);
            }
        }
    }
}

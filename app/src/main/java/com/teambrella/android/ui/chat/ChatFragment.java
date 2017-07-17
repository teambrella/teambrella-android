package com.teambrella.android.ui.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Pair;
import android.view.View;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import io.reactivex.Notification;


/**
 * Claim Chat Fragment
 */
public class ChatFragment extends ADataPagerProgressFragment<IDataHost> {


    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new ChatAdapter(mDataHost.getPager(mTag));
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((LinearLayoutManager) mList.getLayoutManager()).setStackFromEnd(true);
    }

    @Override
    protected void onDataUpdated(Notification<Pair<Integer, JsonArray>> notification) {
        super.onDataUpdated(notification);
        if (notification.isOnNext()) {
            JsonArray array = notification.getValue().second;
            if (array != null && array.size() > 0) {
                //mList.getLayoutManager().smoothScrollToPosition(mList, null, mAdapter.getItemCount() - 1);
            }
        }
    }
}

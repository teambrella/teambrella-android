package com.teambrella.android.ui.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
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
        setRefreshable(false);
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        super.onDataUpdated(notification);
        if (notification.isOnNext()) {
            JsonWrapper metadata = new JsonWrapper(notification.getValue()).getObject(TeambrellaModel.ATTR_METADATA_);
            if (metadata != null && metadata.getBoolean(TeambrellaModel.ATTR_METADATA_FORCE, false)
                    && metadata.getInt(TeambrellaModel.ATTR_METADATA_SIZE) > 0) {
                mList.post(() -> mList.getLayoutManager().scrollToPosition(mAdapter.getItemCount() - 1));
            }
        }
    }
}
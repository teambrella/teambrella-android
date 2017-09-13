package com.teambrella.android.ui.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

import io.reactivex.Notification;


/**
 * Claim Chat Fragment
 */
public class ChatFragment extends ADataPagerProgressFragment<IChatActivity> {


    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        int mode = ChatAdapter.MODE_DISCUSSION;
        switch (TeambrellaUris.sUriMatcher.match(mDataHost.getChatUri())) {
            case TeambrellaUris.CLAIMS_CHAT:
                mode = ChatAdapter.MODE_CLAIM;
                break;
            case TeambrellaUris.TEAMMATE_CHAT:
                mode = ChatAdapter.MODE_APPLICATION;
                break;
            case TeambrellaUris.CONVERSATION_CHAT:
                mode = ChatAdapter.MODE_CONVERSATION;
                break;
        }
        return new ChatAdapter(mDataHost.getPager(mTag), mDataHost.getTeamId(), mode, TeambrellaUser.get(getContext()).getUserId());
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
            if (metadata != null && (metadata.getBoolean(TeambrellaModel.ATTR_METADATA_FORCE, false)
                    || metadata.getBoolean(TeambrellaModel.ATTR_METADATA_RELOAD, false)) && metadata.getInt(TeambrellaModel.ATTR_METADATA_SIZE) > 0) {
                mList.getLayoutManager().scrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
    }
}